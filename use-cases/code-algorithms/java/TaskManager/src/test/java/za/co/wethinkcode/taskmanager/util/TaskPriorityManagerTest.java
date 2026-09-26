package za.co.wethinkcode.taskmanager.util;

import za.co.wethinkcode.taskmanager.model.Task;
import za.co.wethinkcode.taskmanager.model.TaskPriority;
import za.co.wethinkcode.taskmanager.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskPriorityManagerTest {

    @Test
    void calculateTaskScore_shouldScoreHighPriorityTasksHigher() {
        // Arrange
        Task lowPriorityTask = new Task("Low priority task");
        lowPriorityTask.setPriority(TaskPriority.LOW);
        
        Task highPriorityTask = new Task("High priority task");
        highPriorityTask.setPriority(TaskPriority.HIGH);

        // Act
        int lowPriorityScore = TaskPriorityManager.calculateTaskScore(lowPriorityTask);
        int highPriorityScore = TaskPriorityManager.calculateTaskScore(highPriorityTask);

        // Assert
        assertTrue(highPriorityScore > lowPriorityScore);
    }

    @Test
    void calculateTaskScore_shouldScoreOverdueTasksHigher() {
        // Arrange
        Task overdueTask = new Task("Overdue task");
        overdueTask.setDueDate(LocalDateTime.now().minusDays(1));
        
        Task upcomingTask = new Task("Upcoming task");
        upcomingTask.setDueDate(LocalDateTime.now().plusDays(10));

        // Act
        int overdueTaskScore = TaskPriorityManager.calculateTaskScore(overdueTask);
        int upcomingTaskScore = TaskPriorityManager.calculateTaskScore(upcomingTask);

        // Assert
        assertTrue(overdueTaskScore > upcomingTaskScore);
    }

    @Test
    void calculateTaskScore_shouldGiveLowerScoreToDoneTasks() {
        // Arrange
        Task todoTask = new Task("Todo task");
        todoTask.setStatus(TaskStatus.TODO);
        
        Task doneTask = new Task("Done task");
        doneTask.setStatus(TaskStatus.DONE);

        // Act
        int todoTaskScore = TaskPriorityManager.calculateTaskScore(todoTask);
        int doneTaskScore = TaskPriorityManager.calculateTaskScore(doneTask);

        // Assert
        assertTrue(todoTaskScore > doneTaskScore);
    }

    @Test
    void calculateTaskScore_shouldBoostScoreForBlockerTag() {
        // Arrange
        Task regularTask = new Task("Regular task");
        
        Task blockerTask = new Task("Blocker task");
        blockerTask.addTag("blocker");

        // Act
        int regularTaskScore = TaskPriorityManager.calculateTaskScore(regularTask);
        int blockerTaskScore = TaskPriorityManager.calculateTaskScore(blockerTask);

        // Assert
        assertTrue(blockerTaskScore > regularTaskScore);
    }

    @Test
    void sortTasksByImportance_shouldSortTasksCorrectly() {
        // Arrange
        Task lowPriorityTask = new Task("Low priority task");
        lowPriorityTask.setPriority(TaskPriority.LOW);
        
        Task highPriorityTask = new Task("High priority task");
        highPriorityTask.setPriority(TaskPriority.HIGH);
        
        Task urgentOverdueTask = new Task("Urgent overdue task");
        urgentOverdueTask.setPriority(TaskPriority.URGENT);
        urgentOverdueTask.setDueDate(LocalDateTime.now().minusDays(1));
        
        Task completedTask = new Task("Completed task");
        completedTask.setPriority(TaskPriority.HIGH);
        completedTask.setStatus(TaskStatus.DONE);
        
        List<Task> tasks = Arrays.asList(
            lowPriorityTask, completedTask, urgentOverdueTask, highPriorityTask
        );

        // Act
        List<Task> sortedTasks = TaskPriorityManager.sortTasksByImportance(tasks);

        // Assert
        assertEquals(4, sortedTasks.size());
        assertEquals(urgentOverdueTask, sortedTasks.get(0));
        assertEquals(highPriorityTask, sortedTasks.get(1));
        assertEquals(lowPriorityTask, sortedTasks.get(2));
        assertEquals(completedTask, sortedTasks.get(3));
    }

    @Test
    void getTopPriorityTasks_shouldReturnTopNTasks() {
        // Arrange
        Task task1 = new Task("Task 1");
        task1.setPriority(TaskPriority.LOW);
        
        Task task2 = new Task("Task 2");
        task2.setPriority(TaskPriority.MEDIUM);
        
        Task task3 = new Task("Task 3");
        task3.setPriority(TaskPriority.HIGH);
        
        Task task4 = new Task("Task 4");
        task4.setPriority(TaskPriority.URGENT);
        
        List<Task> tasks = Arrays.asList(task1, task2, task3, task4);

        // Act
        List<Task> topTasks = TaskPriorityManager.getTopPriorityTasks(tasks, 2);

        // Assert
        assertEquals(2, topTasks.size());
        assertEquals(task4, topTasks.get(0));
        assertEquals(task3, topTasks.get(1));
    }
}