package za.co.wethinkcode.taskmanager.util;

import za.co.wethinkcode.taskmanager.model.Task;
import za.co.wethinkcode.taskmanager.model.TaskStatus;

import java.util.*;

public class TaskMergeService {

    /**
     * Merge two task lists with conflict resolution.
     *
     * @param localTasks Map of tasks from local source {task_id: task}
     * @param remoteTasks Map of tasks from remote source {task_id: task}
     * @return MergeResult containing all merged and categorized tasks
     */
    public MergeResult mergeTaskLists(Map<String, Task> localTasks, Map<String, Task> remoteTasks) {
        Map<String, Task> mergedTasks = new HashMap<>();
        Map<String, Task> toCreateRemote = new HashMap<>();
        Map<String, Task> toUpdateRemote = new HashMap<>();
        Map<String, Task> toCreateLocal = new HashMap<>();
        Map<String, Task> toUpdateLocal = new HashMap<>();

        // Step 1: Identify all unique task IDs across both sources
        Set<String> allTaskIds = new HashSet<>();
        allTaskIds.addAll(localTasks.keySet());
        allTaskIds.addAll(remoteTasks.keySet());

        for (String taskId : allTaskIds) {
            Task localTask = localTasks.get(taskId);
            Task remoteTask = remoteTasks.get(taskId);

            // Case 1: Task exists only locally - add to remote
            if (localTask != null && remoteTask == null) {
                mergedTasks.put(taskId, localTask);
                toCreateRemote.put(taskId, localTask);
            }
            // Case 2: Task exists only in remote - add to local
            else if (localTask == null && remoteTask != null) {
                mergedTasks.put(taskId, remoteTask);
                toCreateLocal.put(taskId, remoteTask);
            }
            // Case 3: Task exists in both - resolve conflicts
            else {
                ConflictResolution resolution = resolveTaskConflict(localTask, remoteTask);
                Task mergedTask = resolution.getMergedTask();

                mergedTasks.put(taskId, mergedTask);

                if (resolution.isShouldUpdateLocal()) {
                    toUpdateLocal.put(taskId, mergedTask);
                }

                if (resolution.isShouldUpdateRemote()) {
                    toUpdateRemote.put(taskId, mergedTask);
                }
            }
        }

        return new MergeResult(
                mergedTasks,
                toCreateRemote,
                toUpdateRemote,
                toCreateLocal,
                toUpdateLocal
        );
    }

    /**
     * Resolve conflicts between two versions of the same task.
     */
    private ConflictResolution resolveTaskConflict(Task localTask, Task remoteTask) {
        // Make a copy of the local task to use as our base
        Task mergedTask = copyTask(localTask);

        // Track if we need to update either source
        boolean shouldUpdateLocal = false;
        boolean shouldUpdateRemote = false;

        // Most recent update wins for most fields
        if (remoteTask.getUpdatedAt().isAfter(localTask.getUpdatedAt())) {
            // Remote task is newer, update local fields
            mergedTask.setTitle(remoteTask.getTitle());
            mergedTask.setDescription(remoteTask.getDescription());
            mergedTask.setPriority(remoteTask.getPriority());
            mergedTask.setDueDate(remoteTask.getDueDate());
            shouldUpdateLocal = true;
        } else {
            // Local task is newer or same age, update remote fields
            shouldUpdateRemote = true;
        }

        // Special handling for completed status - completed wins over not completed
        if (remoteTask.getStatus() == TaskStatus.DONE && localTask.getStatus() != TaskStatus.DONE) {
            mergedTask.setStatus(TaskStatus.DONE);
            mergedTask.setCompletedAt(remoteTask.getCompletedAt());
            shouldUpdateLocal = true;
        } else if (localTask.getStatus() == TaskStatus.DONE && remoteTask.getStatus() != TaskStatus.DONE) {
            // Keep local status (already in mergedTask)
            shouldUpdateRemote = true;
        } else if (remoteTask.getStatus() != localTask.getStatus()) {
            // Different non-completed status - most recent wins
            if (remoteTask.getUpdatedAt().isAfter(localTask.getUpdatedAt())) {
                mergedTask.setStatus(remoteTask.getStatus());
                shouldUpdateLocal = true;
            } else {
                // Keep local status (already in mergedTask)
                shouldUpdateRemote = true;
            }
        }

        // Merge tags from both sources (union)
        Set<String> allTags = new HashSet<>(localTask.getTags());
        allTags.addAll(remoteTask.getTags());
        mergedTask.setTags(new ArrayList<>(allTags));

        // If tags changed in either source, update both
        if (!new HashSet<>(mergedTask.getTags()).equals(new HashSet<>(localTask.getTags()))) {
            shouldUpdateLocal = true;
        }
        if (!new HashSet<>(mergedTask.getTags()).equals(new HashSet<>(remoteTask.getTags()))) {
            shouldUpdateRemote = true;
        }

        // Update the timestamp to latest
        mergedTask.setUpdatedAt(
                localTask.getUpdatedAt().isAfter(remoteTask.getUpdatedAt()) ?
                        localTask.getUpdatedAt() : remoteTask.getUpdatedAt()
        );

        return new ConflictResolution(mergedTask, shouldUpdateLocal, shouldUpdateRemote);
    }

    // Helper method to copy a task
    private Task copyTask(Task original) {
        Task copy = new Task(original.getTitle(), original.getDescription());
        copy.setId(original.getId());
        copy.setPriority(original.getPriority());
        copy.setStatus(original.getStatus());
        copy.setCreatedAt(original.getCreatedAt());
        copy.setUpdatedAt(original.getUpdatedAt());
        copy.setDueDate(original.getDueDate());
        copy.setCompletedAt(original.getCompletedAt());
        copy.setTags(new ArrayList<>(original.getTags()));
        return copy;
    }

    // Helper class to return conflict resolution results
    private static class ConflictResolution {
        private final Task mergedTask;
        private final boolean shouldUpdateLocal;
        private final boolean shouldUpdateRemote;

        public ConflictResolution(Task mergedTask, boolean shouldUpdateLocal, boolean shouldUpdateRemote) {
            this.mergedTask = mergedTask;
            this.shouldUpdateLocal = shouldUpdateLocal;
            this.shouldUpdateRemote = shouldUpdateRemote;
        }

        public Task getMergedTask() {
            return mergedTask;
        }

        public boolean isShouldUpdateLocal() {
            return shouldUpdateLocal;
        }

        public boolean isShouldUpdateRemote() {
            return shouldUpdateRemote;
        }
    }

    // Result class to return multiple outputs from merge operation
    public static class MergeResult {
        private final Map<String, Task> mergedTasks;
        private final Map<String, Task> toCreateRemote;
        private final Map<String, Task> toUpdateRemote;
        private final Map<String, Task> toCreateLocal;
        private final Map<String, Task> toUpdateLocal;

        public MergeResult(
                Map<String, Task> mergedTasks,
                Map<String, Task> toCreateRemote,
                Map<String, Task> toUpdateRemote,
                Map<String, Task> toCreateLocal,
                Map<String, Task> toUpdateLocal
        ) {
            this.mergedTasks = mergedTasks;
            this.toCreateRemote = toCreateRemote;
            this.toUpdateRemote = toUpdateRemote;
            this.toCreateLocal = toCreateLocal;
            this.toUpdateLocal = toUpdateLocal;
        }

        // Getters
        public Map<String, Task> getMergedTasks() {
            return mergedTasks;
        }

        public Map<String, Task> getToCreateRemote() {
            return toCreateRemote;
        }

        public Map<String, Task> getToUpdateRemote() {
            return toUpdateRemote;
        }

        public Map<String, Task> getToCreateLocal() {
            return toCreateLocal;
        }

        public Map<String, Task> getToUpdateLocal() {
            return toUpdateLocal;
        }
    }
}