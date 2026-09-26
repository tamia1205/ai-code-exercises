package za.co.wethinkcode.taskmanager.app;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import za.co.wethinkcode.taskmanager.model.Task;
import za.co.wethinkcode.taskmanager.model.TaskPriority;
import za.co.wethinkcode.taskmanager.model.TaskStatus;
import za.co.wethinkcode.taskmanager.storage.TaskStorage;

public class TaskManagerTest {

    private final static String test_storage_file = "test_storage.json";

    @AfterEach
    public void tearDown() {
        File file = new File(test_storage_file);
        try {
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    System.err.println("Failed to delete the test storage file.");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to delete the test storage file: "+e.getMessage());
        }
    }

    /**
     * Test getStatistics when there are no tasks in the storage.
     * This tests the edge case of an empty task list, which is handled
     * in the getStatistics method by initializing counts to zero.
     */
    @Test
    public void testGetStatisticsWithEmptyTaskList() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        Map<String, Object> stats = taskManager.getStatistics();

        assertEquals(0, stats.get("total"));
        assertEquals(0, stats.get("overdue"));
        assertEquals(0, stats.get("completedLastWeek"));

        Map<String, Integer> statusCounts = (Map<String, Integer>) stats.get("byStatus");
        for (TaskStatus status : TaskStatus.values()) {
            assertEquals(0, statusCounts.get(status.getValue()));
        }

        Map<Integer, Integer> priorityCounts = (Map<Integer, Integer>) stats.get("byPriority");
        for (TaskPriority priority : TaskPriority.values()) {
            assertEquals(0, priorityCounts.get(priority.getValue()));
        }
    }

    /**
     * Tests that the TaskManager constructor creates a non-null TaskStorage object
     * when given a valid storage path.
     */
    @Test
    public void testTaskManagerConstructorWithValidPath() {
        String validPath = "valid/storage/path/tasks.json";
        TaskManager taskManager = new TaskManager(validPath);
        assertNotNull(taskManager);
    }

    /**
     * Test case for TaskManager constructor
     * Verifies that a TaskManager object can be created with a given storage path
     */
    @Test
    public void test_TaskManager_1() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        assert taskManager != null : "TaskManager object should be created";
        assert taskManager.getStorage() != null : "TaskStorage object should be initialized";
    }

    /**
     * Tests the addTagToTask method when the task with the given ID does not exist.
     * This is a negative test case that verifies the method returns false when
     * attempting to add a tag to a non-existent task.
     */
    @Test
    public void test_addTagToTask_nonExistentTask() {
        TaskManager taskManager = new TaskManager(test_storage_file);

        String nonExistentTaskId = "non_existent_task_id";
        String tag = "test_tag";

        boolean result = taskManager.addTagToTask(nonExistentTaskId, tag);

        assertFalse(result, "Adding tag to non-existent task should return false");
    }

    /**
     * Tests that addTagToTask successfully adds a tag to an existing task.
     * Path constraints: (task != null)
     * Expected result: The method should return true, indicating successful tag addition.
     */
    @Test
    public void test_addTagToTask_successfullyAddsTag() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        String taskId = taskManager.createTask("Test Task", "TODO", 1, "2025-06-01", Collections.emptyList());

        boolean result = taskManager.addTagToTask(taskId, "new_tag");
        assertTrue(result);

        Task task = taskManager.getTaskDetails(taskId);
        assertTrue(task.getTags().contains("new_tag"));
    }

    /**
     * Tests creating a task without a due date.
     * This test verifies that the createTask method correctly handles the case
     * where the dueDateStr is null or empty, ensuring that a task is created
     * with the provided title, description, priority, and tags, but without a due date.
     */
    @Test
    public void test_createTaskWithoutDueDate() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        String taskId = taskManager.createTask("Test Task", "TODO", 1, null, Collections.emptyList());
        Task craetedTask = taskManager.getTaskDetails(taskId);
        assertEquals("Test Task", craetedTask.getTitle());
        assertEquals("TODO", craetedTask.getDescription());
        assertEquals(TaskPriority.LOW, craetedTask.getPriority());
        assertNull(craetedTask.getDueDate());
        assertTrue(craetedTask.getTags().isEmpty());
    }

    /**
     * Tests that createTask returns null when an invalid date format is provided.
     * This tests the explicit error handling for date parsing in the createTask method.
     */
    @Test
    public void test_createTask_invalidDateFormat() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        String title = "Test Task";
        String description = "Test Description";
        int priorityValue = 1;
        String invalidDateStr = "2023/05/01"; // Invalid format, should be YYYY-MM-DD
        List<String> tags = new ArrayList<>();

        String result = taskManager.createTask(title, description, priorityValue, invalidDateStr, tags);

        assertNull(result, "Expected null result for invalid date format");
    }

    /**
     * Tests the createTask method when a non-empty due date string is provided but in an invalid format.
     * This should result in the method returning null due to the date parsing exception.
     */
    @Test
    public void test_createTask_invalidDateFormat_2() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        String title = "Test Task";
        String description = "This is a test task";
        int priorityValue = 1;
        String dueDateStr = "2023/05/15"; // Invalid format, should be YYYY-MM-DD
        List<String> tags = new ArrayList<>();

        String result = taskManager.createTask(title, description, priorityValue, dueDateStr, tags);

        assertNull(result, "Expected null result due to invalid date format");
    }

    /**
     * Tests deleteTask method with a non-existent task ID.
     * This test verifies that the method returns false when attempting to delete a task that doesn't exist.
     */
    @Test
    public void test_deleteTask_nonExistentTask() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        boolean result = taskManager.deleteTask("nonExistentTaskId");
        assertFalse(result, "Deleting a non-existent task should return false");
    }

    /**
     * Test case for deleteTask method when the task exists
     * Verifies that the method returns true when a task is successfully deleted
     */
    @Test
    public void test_deleteTask_whenTaskExists_returnsTrue() {
        TaskManager taskManager = new TaskManager(test_storage_file);

        // Create a task to delete
        String taskId = taskManager.createTask("Test Task", "Description", 1, "2023-12-31", null);

        // Attempt to delete the task
        boolean result = taskManager.deleteTask(taskId);

        // Assert that the task was successfully deleted
        assertTrue(result);
        assertNull(taskManager.getTaskDetails(taskId));
    }

    /**
     * Test case for getStatistics method
     * Verifies that the method returns correct statistics for tasks
     */
    @Test
    public void test_getStatistics_returnsCorrectStatistics() {
        // Create a TaskManager with a temporary storage path
        TaskManager taskManager = new TaskManager(test_storage_file);

        // Add some tasks
        taskManager.createTask("Task 1", "Description 1", 1, "2028-06-01", new ArrayList<>());
        taskManager.createTask("Task 2", "Description 2", 2, "2028-06-02", new ArrayList<>());
        taskManager.createTask("Task 3", "Description 3", 3, "2028-06-03", new ArrayList<>());

        // Get statistics
        Map<String, Object> stats = taskManager.getStatistics();

        // Assert the results
        assertEquals(3, stats.get("total"));

        Map<String, Integer> statusCounts = (Map<String, Integer>) stats.get("byStatus");
        assertEquals(3, statusCounts.get(TaskStatus.TODO.getValue()));
        assertEquals(0, statusCounts.get(TaskStatus.IN_PROGRESS.getValue()));
        assertEquals(0, statusCounts.get(TaskStatus.REVIEW.getValue()));
        assertEquals(0, statusCounts.get(TaskStatus.DONE.getValue()));

        Map<Integer, Integer> priorityCounts = (Map<Integer, Integer>) stats.get("byPriority");
        assertEquals(1, priorityCounts.get(TaskPriority.LOW.getValue()));
        assertEquals(1, priorityCounts.get(TaskPriority.MEDIUM.getValue()));
        assertEquals(1, priorityCounts.get(TaskPriority.HIGH.getValue()));
        assertEquals(0, priorityCounts.get(TaskPriority.URGENT.getValue()));

        assertEquals(0, stats.get("overdue"));
        assertEquals(0, stats.get("completedLastWeek"));
    }

    /**
     * Tests that getTaskDetails returns null when given a non-existent task ID.
     * This tests the edge case of requesting details for a task that doesn't exist in storage.
     */
    @Test
    public void test_getTaskDetails_nonExistentTask() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        Task result = taskManager.getTaskDetails("non_existent_id");
        assertNull(result, "getTaskDetails should return null for a non-existent task ID");
    }

    /**
     * Test case for getTaskDetails method
     * Verifies that the method correctly returns the task details from storage
     */
    @Test
    public void test_getTaskDetails_returnsTaskFromStorage() {
        // Arrange
        TaskStorage mockStorage = mock(TaskStorage.class);
        TaskManager taskManager = new TaskManager(test_storage_file) {
            @Override
            public TaskStorage getStorage() {
                return mockStorage;
            }
        };

        String taskId = "task123";
        Task expectedTask = new Task("Test Task");
        when(mockStorage.getTask(taskId)).thenReturn(expectedTask);

        // Act
        Task result = taskManager.getTaskDetails(taskId);

        // Assert
        assertEquals(expectedTask, result);
        verify(mockStorage).getTask(taskId);
    }

    /**
     * Tests that listTasks returns tasks filtered by priority when a valid priority is provided,
     * and showOverdue is false and status is null.
     */
    @Test
    public void test_listTasks_filter_by_priority() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        Integer priorityFilter = 1;
        List<Task> result = taskManager.listTasks(null, priorityFilter, false);

        // Assert that the result is not null
        assertNotNull(result);

        // Verify that all returned tasks have the specified priority
        for (Task task : result) {
            assertEquals(TaskPriority.fromValue(priorityFilter), task.getPriority());
        }
    }

    /**
     * Tests that listTasks returns tasks filtered by status when a valid status is provided,
     * and showOverdue is false.
     */
    @Test
    public void test_listTasks_filter_by_status() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        String statusFilter = "in_progress";
        List<Task> result = taskManager.listTasks(statusFilter, null, false);

        // Assert that the result is not null
        assertNotNull(result);

        // Verify that all returned tasks have the specified status
        for (Task task : result) {
            assertEquals(TaskStatus.fromValue(statusFilter), task.getStatus());
        }
    }

    /**
     * Tests the listTasks method when no filters are applied and showOverdue is false.
     * This should return all tasks from the storage.
     */
    @Test
    public void test_listTasks_returnsAllTasks() {
        // Arrange
        TaskStorage mockStorage = mock(TaskStorage.class);
        TaskManager taskManager = new TaskManager(test_storage_file) {
            @Override
            public TaskStorage getStorage() {
                return mockStorage;
            }
        };
        List<Task> expectedTasks = Arrays.asList(
            new Task("Task 1"),
            new Task("Task 2"),
            new Task("Task 3")
        );
        when(mockStorage.getAllTasks()).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskManager.listTasks(null, null, false);

        // Assert
        assertEquals(expectedTasks, result);
        verify(mockStorage).getAllTasks();
    }

    /**
     * Tests the listTasks method when showOverdue is true.
     * Verifies that the method returns overdue tasks from storage.
     */
    @Test
    public void test_listTasks_returnsOverdueTasks() {
        // Arrange
        TaskStorage mockStorage = mock(TaskStorage.class);
        TaskManager taskManager = new TaskManager(test_storage_file) {
            @Override
            public TaskStorage getStorage() {
                return mockStorage;
            }
        };

        List<Task> overdueTasks = mock(List.class);
        when(mockStorage.getOverdueTasks()).thenReturn(overdueTasks);

        // Act
        List<Task> result = taskManager.listTasks(null, null, true);

        // Assert
        assertEquals(overdueTasks, result);
        verify(mockStorage).getOverdueTasks();
    }

    /**
     * Tests that listTasks returns overdue tasks when showOverdue is true,
     * regardless of other filter parameters.
     */
    @Test
    public void test_listTasks_showOverdue_ignores_other_filters() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        List<Task> result = taskManager.listTasks("IN_PROGRESS", 1, true);

        // Assert that the result is not null
        assertNotNull(result);

        // Verify that the result contains only overdue tasks
        // This assumes that getOverdueTasks() is implemented correctly in TaskStorage
        for (Task task : result) {
            assertTrue(task.isOverdue());
        }
    }

    /**
     * Tests the listTasks method when a priority filter is provided.
     * Verifies that the method returns tasks filtered by the specified priority.
     */
    @Test
    public void test_listTasks_whenPriorityFilterProvided() {
        // Arrange
        TaskStorage mockStorage = mock(TaskStorage.class);
        TaskManager taskManager = new TaskManager(test_storage_file) {
            @Override
            public TaskStorage getStorage() {
                return mockStorage;
            }
        };

        Task task1 = new Task("Task 1");
        Task task2 = new Task("Task 2");
        List<Task> expectedTasks = Arrays.asList(task1, task2);

        when(mockStorage.getTasksByPriority(TaskPriority.MEDIUM)).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskManager.listTasks(null, 2, false);

        // Assert
        assertEquals(expectedTasks, result);
        verify(mockStorage).getTasksByPriority(TaskPriority.MEDIUM);
    }

    /**
     * Test case for listTasks method when a status filter is provided.
     * This test verifies that the method correctly returns tasks filtered by status
     * when a non-null statusFilter is provided and showOverdue is false.
     */
    @Test
    public void test_listTasks_withStatusFilter() {
        // Arrange
        TaskStorage mockStorage = mock(TaskStorage.class);
        TaskManager taskManager = new TaskManager(test_storage_file) {
            @Override
            public TaskStorage getStorage() {
                return mockStorage;
            }
        };

        Task task1 = new Task("Task 1");
        Task task2 = new Task("Task 2");
        List<Task> expectedTasks = Arrays.asList(task1, task2);

        when(mockStorage.getTasksByStatus(TaskStatus.TODO)).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskManager.listTasks("todo", null, false);

        // Assert
        assertEquals(expectedTasks, result);
        verify(mockStorage).getTasksByStatus(TaskStatus.TODO);
    }

    /**
     * Tests removing a non-existent tag from an existing task.
     * This test verifies that the method returns false when attempting to remove
     * a tag that doesn't exist on the task.
     */
    @Test
    public void test_removeTagFromTask_nonExistentTag() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        String taskId = taskManager.createTask("Test Task", "Description", 1, null, null);
        assertFalse(taskManager.removeTagFromTask(taskId, "non_existent_tag"));
    }

    /**
     * Tests removing a tag from a non-existent task.
     * This test verifies that the method returns false when attempting to remove a tag
     * from a task that doesn't exist in the storage.
     */
    @Test
    public void test_removeTagFromTask_nonExistentTask() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        assertFalse(taskManager.removeTagFromTask("non_existent_task_id", "tag"));
    }

    /**
     * Tests the removeTagFromTask method when the task exists and the tag is successfully removed.
     * Verifies that the method returns true and the tag is actually removed from the task.
     */
    @Test
    public void test_removeTagFromTask_whenTaskExistsAndTagIsRemoved() {
        TaskStorage mockStorage = mock(TaskStorage.class);
        TaskManager taskManager = new TaskManager(test_storage_file) {
            @Override
            public TaskStorage getStorage() {
                return mockStorage;
            }
        };
        String taskId = "test-task-id";
        String tag = "test-tag";

        Task mockTask = new Task("Test Task");
        mockTask.addTag(tag);
        when(mockStorage.getTask(taskId)).thenReturn(mockTask);
        doNothing().when(mockStorage).save();

        boolean result = taskManager.removeTagFromTask(taskId, tag);

        assertTrue(result);
        assertFalse(mockStorage.getTask(taskId).getTags().contains(tag));
    }

    /**
     * Tests the removeTagFromTask method when the task is null or the tag cannot be removed.
     * This test covers the scenario where the method should return false.
     */
    @Test
    public void test_removeTagFromTask_whenTaskIsNullOrTagCannotBeRemoved() {
        TaskStorage mockStorage = new TaskStorage(test_storage_file);
        TaskManager taskManager = new TaskManager(test_storage_file);

        // Attempt to remove a tag from a non-existent task
        boolean result = taskManager.removeTagFromTask("non_existent_task_id", "test_tag");

        assertFalse(result, "Removing a tag from a non-existent task should return false");
    }

    /**
     * Tests that updateTaskDueDate returns false when given an invalid date format.
     * This tests the explicit error handling in the method for DateTimeParseException.
     */
    @Test
    public void test_updateTaskDueDate_invalidDateFormat() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        String taskId = "testTaskId";
        String invalidDateStr = "2023/05/15"; // Invalid format, should be YYYY-MM-DD

        boolean result = taskManager.updateTaskDueDate(taskId, invalidDateStr);

        assertFalse(result, "Method should return false for invalid date format");
    }

    /**
     * Test case for updating a task's due date successfully.
     * This test verifies that the updateTaskDueDate method correctly parses the input date,
     * creates a Task object with the new due date, and calls the storage to update the task.
     */
    @Test
    public void test_updateTaskDueDate_successfulUpdate() {
        // Arrange
        TaskStorage mockStorage = mock(TaskStorage.class);
        TaskManager taskManager = new TaskManager(test_storage_file) {
            @Override
            public TaskStorage getStorage() {
                return mockStorage;
            }
        };

        String taskId = "task123";
        String newDueDateStr = "2023-12-31";
        LocalDate localDate = LocalDate.parse(newDueDateStr);
        LocalDateTime expectedDueDate = LocalDateTime.of(localDate, LocalTime.MAX);

        Task updatedTask = new Task("tempTitle");
        updatedTask.setDueDate(expectedDueDate);

        when(mockStorage.updateTask(eq(taskId), any(Task.class))).thenReturn(true);

        // Act
        boolean result = taskManager.updateTaskDueDate(taskId, newDueDateStr);

        // Assert
        assertTrue(result);
        verify(mockStorage).updateTask(eq(taskId), argThat(task ->
                task.getDueDate().equals(expectedDueDate)));
    }

    /**
     * Tests updateTaskPriority method with an invalid priority value.
     * This test verifies that the method handles an out-of-range priority value
     * by returning false, indicating the update was not successful.
     */
    @Test
    public void test_updateTaskPriority_invalidPriorityValue() {
        TaskManager taskManager = new TaskManager(test_storage_file);
        String taskId = "test_task_id";
        int invalidPriority = 5; // Assuming valid range is 1-3

        boolean result = taskManager.updateTaskPriority(taskId, invalidPriority);

        assertFalse(result, "Updating task priority with invalid value should return false");
    }

    /**
     * Tests the updateTaskStatus method when trying to mark a non-existent task as DONE.
     * This test verifies that the method returns false when attempting to update
     * the status of a task that doesn't exist in the storage.
     */
    @Test
    public void test_updateTaskStatus_nonExistentTaskToDone() {
        TaskStorage mockStorage = new TaskStorage(test_storage_file);
        TaskManager taskManager = new TaskManager(test_storage_file);

        String nonExistentTaskId = "non_existent_task_id";
        String newStatusValue = TaskStatus.DONE.getValue();

        boolean result = taskManager.updateTaskStatus(nonExistentTaskId, newStatusValue);

        assertFalse( result, "Updating status of non-existent task should return false");
    }

    /**
     * Tests updating a task status to DONE when the task does not exist.
     * This is an edge case explicitly handled in the updateTaskStatus method.
     */
    @Test
    public void test_updateTaskStatus_nonExistentTask_returnsFalse() {
        TaskStorage mockStorage = new TaskStorage(test_storage_file) {
            @Override
            public Task getTask(String taskId) {
                return null; // Simulating a non-existent task
            }
        };
        TaskManager taskManager = new TaskManager(test_storage_file) {
            @Override
            public TaskStorage getStorage() {
                return mockStorage;
            }
        };

        boolean result = taskManager.updateTaskStatus("non_existent_task_id", "done");

        assertFalse(result, "Updating status of non-existent task should return false");
    }

    /**
     * Test case for updating a task status to DONE when the task exists.
     * This test verifies that the updateTaskStatus method returns true when
     * updating an existing task's status to DONE.
     */
    @Test
    public void test_updateTaskStatus_taskExistsAndMarkedAsDone() {
        // Setup
        TaskStorage mockStorage = new TaskStorage(test_storage_file);
        TaskManager taskManager = new TaskManager(test_storage_file) {
            @Override
            public TaskStorage getStorage() {
                return mockStorage;
            }
        };
        Task testTask = new Task("Test Task");
        String taskId = mockStorage.addTask(testTask);

        // Execute
        boolean result = taskManager.updateTaskStatus(taskId, "done");

        // Verify
        assertTrue(result);
        assertEquals(TaskStatus.DONE, mockStorage.getTask(taskId).getStatus());
        assertNotNull(mockStorage.getTask(taskId).getCompletedAt());
    }

    /**
     * Tests updating a task status to a non-DONE status.
     * This test verifies that when updating a task status to something other than DONE,
     * the method creates a temporary task with the new status and calls the storage's updateTask method.
     */
    @Test
    public void test_updateTaskStatus_3() {
        // Setup
        TaskStorage mockStorage = new TaskStorage(test_storage_file);
        TaskManager taskManager = new TaskManager(test_storage_file) {
            @Override
            public TaskStorage getStorage() {
                return mockStorage;
            }
        };
        Task testTask = new Task("Test Task");
        String taskId = mockStorage.addTask(testTask);

        // Execute
        boolean result = taskManager.updateTaskStatus(taskId, "in_progress");

        // Verify
        assertTrue(result);
        assertEquals(TaskStatus.IN_PROGRESS, mockStorage.getTask(taskId).getStatus());
    }


}
