// src/main/java/taskmanager/cli/TaskManagerCli.java
package za.co.wethinkcode.taskmanager.cli;

import org.apache.commons.cli.*;
import za.co.wethinkcode.taskmanager.app.TaskManager;
import za.co.wethinkcode.taskmanager.model.Task;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskManagerCli {
    private static final TaskManager taskManager = new TaskManager("tasks.json");

    public static void main(String[] args) {
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        // Global options
        options.addOption(Option.builder("h").longOpt("help").desc("Show help").build());

        try {
            CommandLine cmd = parser.parse(options, args, true);

            if (cmd.hasOption("help") || args.length == 0) {
                showHelp(formatter, options);
                return;
            }

            String command = args[0];
            String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

            executeCommand(command, commandArgs);

        } catch (ParseException e) {
            System.err.println("Error parsing command: " + e.getMessage());
            showHelp(formatter, options);
        }
    }

    private static void executeCommand(String command, String[] args) {
        switch (command) {
            case "create":
                handleCreateCommand(args);
                break;
            case "list":
                handleListCommand(args);
                break;
            case "status":
                handleStatusCommand(args);
                break;
            case "priority":
                handlePriorityCommand(args);
                break;
            case "due":
                handleDueCommand(args);
                break;
            case "tag":
                handleTagCommand(args);
                break;
            case "untag":
                handleUntagCommand(args);
                break;
            case "show":
                handleShowCommand(args);
                break;
            case "delete":
                handleDeleteCommand(args);
                break;
            case "stats":
                handleStatsCommand();
                break;
            default:
                System.err.println("Unknown command: " + command);
                System.err.println("Available commands: create, list, status, priority, due, tag, untag, show, delete, stats");
        }
    }

    private static void handleCreateCommand(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: create <title> [description] [priority] [due_date] [tags]");
            return;
        }

        String title = args[0];
        String description = args.length > 1 ? args[1] : "";
        int priority = args.length > 2 ? Integer.parseInt(args[2]) : 2;
        String dueDate = args.length > 3 ? args[3] : null;
        List<String> tags = args.length > 4 ?
                Arrays.asList(args[4].split(",")).stream().map(String::trim).collect(Collectors.toList()) :
                null;

        String taskId = taskManager.createTask(title, description, priority, dueDate, tags);
        if (taskId != null) {
            System.out.println("Created task with ID: " + taskId);
        }
    }

    private static void handleListCommand(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("s").longOpt("status").hasArg().desc("Filter by status").build());
        options.addOption(Option.builder("p").longOpt("priority").hasArg().desc("Filter by priority").build());
        options.addOption(Option.builder("o").longOpt("overdue").desc("Show only overdue tasks").build());

        try {
            CommandLine cmd = new DefaultParser().parse(options, args);

            String status = cmd.getOptionValue("status");
            Integer priority = cmd.hasOption("priority") ? Integer.valueOf(cmd.getOptionValue("priority")) : null;
            boolean showOverdue = cmd.hasOption("overdue");

            List<Task> tasks = taskManager.listTasks(status, priority, showOverdue);

            if (tasks.isEmpty()) {
                System.out.println("No tasks found matching the criteria.");
                return;
            }

            for (Task task : tasks) {
                System.out.println(formatTask(task));
                System.out.println("-".repeat(50));
            }

        } catch (ParseException e) {
            System.err.println("Error parsing list options: " + e.getMessage());
        }
    }

    private static void handleStatusCommand(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: status <task_id> <new_status>");
            return;
        }

        String taskId = args[0];
        String newStatus = args[1];

        if (taskManager.updateTaskStatus(taskId, newStatus)) {
            System.out.println("Updated task status to " + newStatus);
        } else {
            System.out.println("Failed to update task status. Task not found.");
        }
    }

    private static void handlePriorityCommand(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: priority <task_id> <new_priority>");
            return;
        }

        String taskId = args[0];
        int newPriority = Integer.parseInt(args[1]);

        if (taskManager.updateTaskPriority(taskId, newPriority)) {
            System.out.println("Updated task priority to " + newPriority);
        } else {
            System.out.println("Failed to update task priority. Task not found.");
        }
    }

    private static void handleDueCommand(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: due <task_id> <new_due_date>");
            return;
        }

        String taskId = args[0];
        String newDueDate = args[1];

        if (taskManager.updateTaskDueDate(taskId, newDueDate)) {
            System.out.println("Updated task due date to " + newDueDate);
        } else {
            System.out.println("Failed to update task due date. Task not found or invalid date.");
        }
    }

    private static void handleTagCommand(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: tag <task_id> <tag>");
            return;
        }

        String taskId = args[0];
        String tag = args[1];

        if (taskManager.addTagToTask(taskId, tag)) {
            System.out.println("Added tag '" + tag + "' to task");
        } else {
            System.out.println("Failed to add tag. Task not found.");
        }
    }

    private static void handleUntagCommand(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: untag <task_id> <tag>");
            return;
        }

        String taskId = args[0];
        String tag = args[1];

        if (taskManager.removeTagFromTask(taskId, tag)) {
            System.out.println("Removed tag '" + tag + "' from task");
        } else {
            System.out.println("Failed to remove tag. Task or tag not found.");
        }
    }

    private static void handleShowCommand(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: show <task_id>");
            return;
        }

        String taskId = args[0];
        Task task = taskManager.getTaskDetails(taskId);

        if (task != null) {
            System.out.println(formatTask(task));
        } else {
            System.out.println("Task not found.");
        }
    }

    private static void handleDeleteCommand(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: delete <task_id>");
            return;
        }

        String taskId = args[0];

        if (taskManager.deleteTask(taskId)) {
            System.out.println("Deleted task " + taskId);
        } else {
            System.out.println("Failed to delete task. Task not found.");
        }
    }

    private static void handleStatsCommand() {
        Map<String, Object> stats = taskManager.getStatistics();

        System.out.println("Total tasks: " + stats.get("total"));

        System.out.println("By status:");
        Map<String, Integer> statusCounts = (Map<String, Integer>) stats.get("byStatus");
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("By priority:");
        Map<Integer, Integer> priorityCounts = (Map<Integer, Integer>) stats.get("byPriority");
        for (Map.Entry<Integer, Integer> entry : priorityCounts.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("Overdue tasks: " + stats.get("overdue"));
        System.out.println("Completed in last 7 days: " + stats.get("completedLastWeek"));
    }

    private static void showHelp(HelpFormatter formatter, Options options) {
        System.out.println("Task Manager CLI");
        System.out.println("Available commands:");
        System.out.println("  create <title> [description] [priority] [due_date] [tags] - Create a new task");
        System.out.println("  list [-s <status>] [-p <priority>] [-o] - List tasks");
        System.out.println("  status <task_id> <new_status> - Update task status");
        System.out.println("  priority <task_id> <new_priority> - Update task priority");
        System.out.println("  due <task_id> <new_due_date> - Update task due date");
        System.out.println("  tag <task_id> <tag> - Add tag to task");
        System.out.println("  untag <task_id> <tag> - Remove tag from task");
        System.out.println("  show <task_id> - Show task details");
        System.out.println("  delete <task_id> - Delete a task");
        System.out.println("  stats - Show task statistics");
    }

    private static String formatTask(Task task) {
        String statusSymbol;
        switch (task.getStatus()) {
            case TODO:
                statusSymbol = "[ ]";
                break;
            case IN_PROGRESS:
                statusSymbol = "[>]";
                break;
            case REVIEW:
                statusSymbol = "[?]";
                break;
            case DONE:
                statusSymbol = "[✓]";
                break;
            default:
                statusSymbol = "[-]";
        }

        String prioritySymbol;
        switch (task.getPriority()) {
            case LOW:
                prioritySymbol = "!";
                break;
            case MEDIUM:
                prioritySymbol = "!!";
                break;
            case HIGH:
                prioritySymbol = "!!!";
                break;
            case URGENT:
                prioritySymbol = "!!!!";
                break;
            default:
                prioritySymbol = "";
        }

        String dueStr = task.getDueDate() != null ?
                "Due: " + task.getDueDate().format(DateTimeFormatter.ISO_DATE) :
                "No due date";

        String tagsStr = !task.getTags().isEmpty() ?
                "Tags: " + String.join(", ", task.getTags()) :
                "No tags";

        return statusSymbol + " " + task.getId().substring(0, 8) + " - " + prioritySymbol + " " + task.getTitle() + "\n" +
                "  " + task.getDescription() + "\n" +
                "  " + dueStr + " | " + tagsStr + "\n" +
                "  Created: " + task.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}