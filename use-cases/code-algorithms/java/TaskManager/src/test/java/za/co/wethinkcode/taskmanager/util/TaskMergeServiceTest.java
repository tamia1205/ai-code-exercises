package za.co.wethinkcode.taskmanager.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.taskmanager.model.Task;
import za.co.wethinkcode.taskmanager.model.TaskPriority;
import za.co.wethinkcode.taskmanager.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskMergeServiceTest {

    private TaskMergeService mergeService;
    private Map<String, Task> localTasks;
    private Map<String, Task> remoteTasks;
    private String taskId1, taskId2, taskId3, taskId4;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        mergeService = new TaskMergeService();
        localTasks = new HashMap<>();
        remoteTasks = new HashMap<>();
        now = LocalDateTime.now();
        
        // Create task IDs for consistent testing
        taskId1 = "task-1";
        taskId2 = "task-2";
        taskId3 = "task-3";
        taskId4 = "task-4";
    }

    @Test
    void mergeTaskLists_shouldHandleEmptyLists() {
        // Act
        TaskMergeService.MergeResult result = mergeService.mergeTaskLists(localTasks, remoteTasks);
        
        // Assert
        assertTrue(result.getMergedTasks().isEmpty());
        assertTrue(result.getToCreateLocal().isEmpty());
        assertTrue(result.getToCreateRemote().isEmpty());
        assertTrue(result.getToUpdateLocal().isEmpty());
        assertTrue(result.getToUpdateRemote().isEmpty());
    }

    @Test
    void mergeTaskLists_shouldAddLocalOnlyTasksToRemote() {
        // Arrange
        Task localTask = createTask(taskId1, "Local Task", "Description", TaskPriority.HIGH, TaskStatus.TODO, now);
        localTasks.put(taskId1, localTask);
        
        // Act
        TaskMergeService.MergeResult result = mergeService.mergeTaskLists(localTasks, remoteTasks);
        
        // Assert
        assertEquals(1, result.getMergedTasks().size());
        assertEquals(1, result.getToCreateRemote().size());
        assertTrue(result.getToCreateLocal().isEmpty());
        assertTrue(result.getToUpdateLocal().isEmpty());
        assertTrue(result.getToUpdateRemote().isEmpty());
        
        assertEquals(localTask, result.getMergedTasks().get(taskId1));
        assertEquals(localTask, result.getToCreateRemote().get(taskId1));
    }

    @Test
    void mergeTaskLists_shouldAddRemoteOnlyTasksToLocal() {
        // Arrange
        Task remoteTask = createTask(taskId1, "Remote Task", "Description", TaskPriority.MEDIUM, TaskStatus.TODO, now);
        remoteTasks.put(taskId1, remoteTask);
        
        // Act
        TaskMergeService.MergeResult result = mergeService.mergeTaskLists(localTasks, remoteTasks);
        
        // Assert
        assertEquals(1, result.getMergedTasks().size());
        assertTrue(result.getToCreateRemote().isEmpty());
        assertEquals(1, result.getToCreateLocal().size());
        assertTrue(result.getToUpdateLocal().isEmpty());
        assertTrue(result.getToUpdateRemote().isEmpty());
        
        assertEquals(remoteTask, result.getMergedTasks().get(taskId1));
        assertEquals(remoteTask, result.getToCreateLocal().get(taskId1));
    }

    @Test
    void mergeTaskLists_shouldPreferNewerTaskWhenBothExist() {
        // Arrange
        LocalDateTime olderTime = now.minusHours(1);
        LocalDateTime newerTime = now;
        
        Task localTask = createTask(taskId1, "Local Task", "Local Description", TaskPriority.HIGH, TaskStatus.TODO, olderTime);
        Task remoteTask = createTask(taskId1, "Remote Task", "Remote Description", TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, newerTime);
        
        localTasks.put(taskId1, localTask);
        remoteTasks.put(taskId1, remoteTask);
        
        // Act
        TaskMergeService.MergeResult result = mergeService.mergeTaskLists(localTasks, remoteTasks);
        
        // Assert
        assertEquals(1, result.getMergedTasks().size());
        assertTrue(result.getToCreateRemote().isEmpty());
        assertTrue(result.getToCreateLocal().isEmpty());
        assertEquals(1, result.getToUpdateLocal().size());
        assertTrue(result.getToUpdateRemote().isEmpty());
        
        Task mergedTask = result.getMergedTasks().get(taskId1);
        assertEquals("Remote Task", mergedTask.getTitle());
        assertEquals("Remote Description", mergedTask.getDescription());
        assertEquals(TaskPriority.MEDIUM, mergedTask.getPriority());
        assertEquals(TaskStatus.IN_PROGRESS, mergedTask.getStatus());
    }

    @Test
    void mergeTaskLists_shouldPreferLocalTaskWhenNewer() {
        // Arrange
        LocalDateTime olderTime = now.minusHours(1);
        LocalDateTime newerTime = now;
        
        Task localTask = createTask(taskId1, "Local Task", "Local Description", TaskPriority.HIGH, TaskStatus.TODO, newerTime);
        Task remoteTask = createTask(taskId1, "Remote Task", "Remote Description", TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, olderTime);
        
        localTasks.put(taskId1, localTask);
        remoteTasks.put(taskId1, remoteTask);
        
        // Act
        TaskMergeService.MergeResult result = mergeService.mergeTaskLists(localTasks, remoteTasks);
        
        // Assert
        assertEquals(1, result.getMergedTasks().size());
        assertTrue(result.getToCreateRemote().isEmpty());
        assertTrue(result.getToCreateLocal().isEmpty());
        assertTrue(result.getToUpdateLocal().isEmpty());
        assertEquals(1, result.getToUpdateRemote().size());
        
        Task mergedTask = result.getMergedTasks().get(taskId1);
        assertEquals("Local Task", mergedTask.getTitle());
        assertEquals("Local Description", mergedTask.getDescription());
        assertEquals(TaskPriority.HIGH, mergedTask.getPriority());
        assertEquals(TaskStatus.TODO, mergedTask.getStatus());
    }

    @Test
    void mergeTaskLists_shouldPreferCompletedStatusRegardlessOfTimestamp() {
        // Arrange
        LocalDateTime olderTime = now.minusHours(1);
        LocalDateTime newerTime = now;
        
        // Local task is newer but not completed
        Task localTask = createTask(taskId1, "Local Task", "Local Description", TaskPriority.HIGH, TaskStatus.IN_PROGRESS, newerTime);
        // Remote task is older but completed
        Task remoteTask = createTask(taskId1, "Remote Task", "Remote Description", TaskPriority.MEDIUM, TaskStatus.DONE, olderTime);
        remoteTask.setCompletedAt(olderTime);
        
        localTasks.put(taskId1, localTask);
        remoteTasks.put(taskId1, remoteTask);
        
        // Act
        TaskMergeService.MergeResult result = mergeService.mergeTaskLists(localTasks, remoteTasks);
        
        // Assert
        assertEquals(1, result.getMergedTasks().size());
        assertTrue(result.getToCreateRemote().isEmpty());
        assertTrue(result.getToCreateLocal().isEmpty());
        assertEquals(1, result.getToUpdateLocal().size());

        Task mergedTask = result.getMergedTasks().get(taskId1);
        // Should keep local task's title and description since it's newer
        assertEquals("Local Task", mergedTask.getTitle());
        assertEquals("Local Description", mergedTask.getDescription());
        // But should take the DONE status from remote
        assertEquals(TaskStatus.DONE, mergedTask.getStatus());
        assertNotNull(mergedTask.getCompletedAt());
    }

    @Test
    void mergeTaskLists_shouldMergeTagsFromBothSources() {
        // Arrange
        Task localTask = createTask(taskId1, "Task", "Description", TaskPriority.MEDIUM, TaskStatus.TODO, now);
        localTask.setTags(Arrays.asList("tag1", "tag2"));
        
        Task remoteTask = createTask(taskId1, "Task", "Description", TaskPriority.MEDIUM, TaskStatus.TODO, now);
        remoteTask.setTags(Arrays.asList("tag2", "tag3"));
        
        localTasks.put(taskId1, localTask);
        remoteTasks.put(taskId1, remoteTask);
        
        // Act
        TaskMergeService.MergeResult result = mergeService.mergeTaskLists(localTasks, remoteTasks);
        
        // Assert
        Task mergedTask = result.getMergedTasks().get(taskId1);
        List<String> mergedTags = mergedTask.getTags();
        
        assertEquals(3, mergedTags.size());
        assertTrue(mergedTags.contains("tag1"));
        assertTrue(mergedTags.contains("tag2"));
        assertTrue(mergedTags.contains("tag3"));
        
        // Both sides should be updated with the merged tags
        assertEquals(1, result.getToUpdateLocal().size());
        assertEquals(1, result.getToUpdateRemote().size());
    }

    @Test
    void mergeTaskLists_shouldHandleMultipleTasks() {
        // Arrange
        // Task 1: Only in local
        Task localOnly = createTask(taskId1, "Local Only", "Description", TaskPriority.LOW, TaskStatus.TODO, now);
        localTasks.put(taskId1, localOnly);
        
        // Task 2: Only in remote
        Task remoteOnly = createTask(taskId2, "Remote Only", "Description", TaskPriority.HIGH, TaskStatus.IN_PROGRESS, now);
        remoteTasks.put(taskId2, remoteOnly);
        
        // Task 3: In both, local is newer
        LocalDateTime olderTime = now.minusHours(1);
        Task localNewer = createTask(taskId3, "Local Newer", "Description", TaskPriority.MEDIUM, TaskStatus.TODO, now);
        Task remoteOlder = createTask(taskId3, "Remote Older", "Description", TaskPriority.MEDIUM, TaskStatus.TODO, olderTime);
        localTasks.put(taskId3, localNewer);
        remoteTasks.put(taskId3, remoteOlder);
        
        // Task 4: In both, remote is completed
        Task localIncomplete = createTask(taskId4, "Local Incomplete", "Description", TaskPriority.URGENT, TaskStatus.IN_PROGRESS, now);
        Task remoteComplete = createTask(taskId4, "Remote Complete", "Description", TaskPriority.URGENT, TaskStatus.DONE, olderTime);
        remoteComplete.setCompletedAt(olderTime);
        localTasks.put(taskId4, localIncomplete);
        remoteTasks.put(taskId4, remoteComplete);
        
        // Act
        TaskMergeService.MergeResult result = mergeService.mergeTaskLists(localTasks, remoteTasks);
        
        // Assert
        assertEquals(4, result.getMergedTasks().size());
        
        // Task 1: Should be added to remote
        assertEquals(1, result.getToCreateRemote().size());
        assertTrue(result.getToCreateRemote().containsKey(taskId1));
        
        // Task 2: Should be added to local
        assertEquals(1, result.getToCreateLocal().size());
        assertTrue(result.getToCreateLocal().containsKey(taskId2));
        
        // Task 3: Remote should be updated with local version
        assertTrue(result.getToUpdateRemote().containsKey(taskId3));
        assertEquals("Local Newer", result.getMergedTasks().get(taskId3).getTitle());
        
        // Task 4: Local should be updated with completed status
        assertTrue(result.getToUpdateLocal().containsKey(taskId4));
        assertEquals(TaskStatus.DONE, result.getMergedTasks().get(taskId4).getStatus());
    }

    // Helper method to create tasks with specific properties
    private Task createTask(String id, String title, String description, TaskPriority priority, 
                           TaskStatus status, LocalDateTime timestamp) {
        Task task = new Task(title, description);
        task.setId(id);
        task.setPriority(priority);
        task.setStatus(status);
        task.setCreatedAt(timestamp);
        task.setUpdatedAt(timestamp);
        return task;
    }
}