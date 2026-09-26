package za.co.wethinkcode.taskmanager.app;

import za.co.wethinkcode.taskmanager.model.Task;
import za.co.wethinkcode.taskmanager.model.TaskPriority;
import za.co.wethinkcode.taskmanager.model.TaskStatus;
import za.co.wethinkcode.taskmanager.storage.TaskStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private final TaskStorage storage;

    public TaskManager(String storagePath) {
        this.storage = new TaskStorage(storagePath);
    }

    TaskStorage getStorage() {
        return storage;
    }

    public String createTask(String title, String description, int priorityValue,
                             String dueDateStr, List<String> tags) {
        TaskPriority priority = TaskPriority.fromValue(priorityValue);
        LocalDateTime dueDate = null;

        if (dueDateStr != null && !dueDateStr.isEmpty()) {
            try {
                LocalDate localDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ISO_DATE);
                dueDate = LocalDateTime.of(localDate, LocalTime.MAX);
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date format. Use YYYY-MM-DD");
                return null;
            }
        }

        Task task = new Task(title, description, priority, dueDate, tags);
        return getStorage().addTask(task);
    }

    public List<Task> listTasks(String statusFilter, Integer priorityFilter, boolean showOverdue) {
        if (showOverdue) {
            return getStorage().getOverdueTasks();
        }

        if (statusFilter != null) {
            TaskStatus status = TaskStatus.fromValue(statusFilter);
            return getStorage().getTasksByStatus(status);
        }

        if (priorityFilter != null) {
            TaskPriority priority = TaskPriority.fromValue(priorityFilter);
            return getStorage().getTasksByPriority(priority);
        }

        return getStorage().getAllTasks();
    }

    public boolean updateTaskStatus(String taskId, String newStatusValue) {
        TaskStatus newStatus = TaskStatus.fromValue(newStatusValue);
        Task task = getStorage().getTask(taskId);
        if (task != null) {
            task.setStatus(newStatus);
            if (newStatus == TaskStatus.DONE) {
                task.markAsDone();
            }
            getStorage().save();
            return true;
        }
        return false;
    }

    public boolean updateTaskPriority(String taskId, int newPriorityValue) {

        TaskPriority newPriority = null;
        try {
            newPriority = TaskPriority.fromValue(newPriorityValue);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return false;
        }

        Task updates = new Task("tempTitle");
        updates.setPriority(newPriority);
        return getStorage().updateTask(taskId, updates);
    }

    public boolean updateTaskDueDate(String taskId, String dueDateStr) {
        try {
            LocalDate localDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ISO_DATE);
            LocalDateTime dueDate = LocalDateTime.of(localDate, LocalTime.MAX);

            Task updates = new Task("tempTitle");
            updates.setDueDate(dueDate);
            return getStorage().updateTask(taskId, updates);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format. Use YYYY-MM-DD");
            return false;
        }
    }

    public boolean deleteTask(String taskId) {
        return getStorage().deleteTask(taskId);
    }

    public Task getTaskDetails(String taskId) {
        return getStorage().getTask(taskId);
    }

    public boolean addTagToTask(String taskId, String tag) {
        Task task = getStorage().getTask(taskId);
        if (task != null) {
            task.addTag(tag);
            getStorage().save();
            return true;
        }
        return false;
    }

    public boolean removeTagFromTask(String taskId, String tag) {
        Task task = getStorage().getTask(taskId);
        if (task != null && task.removeTag(tag)) {
            getStorage().save();
            return true;
        }
        return false;
    }

    public Map<String, Object> getStatistics() {
        List<Task> tasks = getStorage().getAllTasks();
        int total = tasks.size();

        // Count by status
        Map<String, Integer> statusCounts = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            statusCounts.put(status.getValue(), 0);
        }

        for (Task task : tasks) {
            String statusValue = task.getStatus().getValue();
            statusCounts.put(statusValue, statusCounts.get(statusValue) + 1);
        }

        // Count by priority
        Map<Integer, Integer> priorityCounts = new HashMap<>();
        for (TaskPriority priority : TaskPriority.values()) {
            priorityCounts.put(priority.getValue(), 0);
        }

        for (Task task : tasks) {
            int priorityValue = task.getPriority().getValue();
            priorityCounts.put(priorityValue, priorityCounts.get(priorityValue) + 1);
        }

        // Count overdue
        int overdueCount = (int) tasks.stream().filter(Task::isOverdue).count();

        // Count completed in last 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        int completedRecently = (int) tasks.stream()
                .filter(task -> task.getCompletedAt() != null && task.getCompletedAt().isAfter(sevenDaysAgo))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("byStatus", statusCounts);
        stats.put("byPriority", priorityCounts);
        stats.put("overdue", overdueCount);
        stats.put("completedLastWeek", completedRecently);

        return stats;
    }
}
