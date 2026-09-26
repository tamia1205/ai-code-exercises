// tests/task_list_merge.test.js
const { Task, TaskStatus, TaskPriority } = require('../models');
const { mergeTaskLists, resolveTaskConflict, arraysEqual } = require('../task_list_merge');

describe('Task List Merge', () => {
  describe('mergeTaskLists', () => {
    test('should handle tasks that exist only locally', () => {
      const localTask = new Task('Local Task');
      const localTasks = { [localTask.id]: localTask };
      const remoteTasks = {};

      const result = mergeTaskLists(localTasks, remoteTasks);

      expect(result.mergedTasks[localTask.id]).toEqual(localTask);
      expect(result.toCreateRemote[localTask.id]).toEqual(localTask);
      expect(Object.keys(result.toUpdateRemote).length).toBe(0);
      expect(Object.keys(result.toCreateLocal).length).toBe(0);
      expect(Object.keys(result.toUpdateLocal).length).toBe(0);
    });

    test('should handle tasks that exist only remotely', () => {
      const remoteTask = new Task('Remote Task');
      const localTasks = {};
      const remoteTasks = { [remoteTask.id]: remoteTask };

      const result = mergeTaskLists(localTasks, remoteTasks);

      expect(result.mergedTasks[remoteTask.id]).toEqual(remoteTask);
      expect(result.toCreateLocal[remoteTask.id]).toEqual(remoteTask);
      expect(Object.keys(result.toCreateRemote).length).toBe(0);
      expect(Object.keys(result.toUpdateRemote).length).toBe(0);
      expect(Object.keys(result.toUpdateLocal).length).toBe(0);
    });

    test('should handle tasks that exist in both sources', () => {
      const taskId = 'task-123';
      const localTask = new Task('Local Version');
      localTask.id = taskId;
      localTask.updatedAt = new Date('2023-01-01');
      
      const remoteTask = new Task('Remote Version');
      remoteTask.id = taskId;
      remoteTask.updatedAt = new Date('2023-01-02'); // Newer
      
      const localTasks = { [taskId]: localTask };
      const remoteTasks = { [taskId]: remoteTask };

      const result = mergeTaskLists(localTasks, remoteTasks);

      expect(result.mergedTasks[taskId].title).toBe('Remote Version');
      expect(Object.keys(result.toCreateRemote).length).toBe(0);
      expect(Object.keys(result.toCreateLocal).length).toBe(0);
      expect(Object.keys(result.toUpdateLocal).length).toBe(1);
      expect(Object.keys(result.toUpdateRemote).length).toBe(0);
    });
  });

  describe('resolveTaskConflict', () => {
    test('should use remote task when it is newer', () => {
      const localTask = new Task('Local Version');
      localTask.updatedAt = new Date('2023-01-01');
      
      const remoteTask = new Task('Remote Version');
      remoteTask.updatedAt = new Date('2023-01-02'); // Newer
      remoteTask.priority = TaskPriority.HIGH;
      
      const [mergedTask, shouldUpdateLocal, shouldUpdateRemote] = 
        resolveTaskConflict(localTask, remoteTask);

      expect(mergedTask.title).toBe('Remote Version');
      expect(mergedTask.priority).toBe(TaskPriority.HIGH);
      expect(shouldUpdateLocal).toBe(true);
      expect(shouldUpdateRemote).toBe(false);
    });

    test('should use local task when it is newer', () => {
      const localTask = new Task('Local Version');
      localTask.updatedAt = new Date('2023-01-02'); // Newer
      localTask.priority = TaskPriority.HIGH;
      
      const remoteTask = new Task('Remote Version');
      remoteTask.updatedAt = new Date('2023-01-01');
      
      const [mergedTask, shouldUpdateLocal, shouldUpdateRemote] = 
        resolveTaskConflict(localTask, remoteTask);

      expect(mergedTask.title).toBe('Local Version');
      expect(mergedTask.priority).toBe(TaskPriority.HIGH);
      expect(shouldUpdateLocal).toBe(false);
      expect(shouldUpdateRemote).toBe(true);
    });

    test('should prioritize completed status regardless of timestamp', () => {
      const localTask = new Task('Local Version');
      localTask.updatedAt = new Date('2023-01-02'); // Newer
      
      const remoteTask = new Task('Remote Version');
      remoteTask.updatedAt = new Date('2023-01-01');
      remoteTask.status = TaskStatus.DONE;
      remoteTask.completedAt = new Date('2023-01-01');
      
      const [mergedTask, shouldUpdateLocal, shouldUpdateRemote] = 
        resolveTaskConflict(localTask, remoteTask);

      expect(mergedTask.status).toBe(TaskStatus.DONE);
      expect(mergedTask.completedAt).toEqual(remoteTask.completedAt);
      expect(shouldUpdateLocal).toBe(true);
      expect(shouldUpdateRemote).toBe(true); // Local is newer, so we update remote with other fields
    });

    test('should merge tags from both sources', () => {
      const localTask = new Task('Task');
      localTask.tags = ['tag1', 'tag2'];
      
      const remoteTask = new Task('Task');
      remoteTask.tags = ['tag2', 'tag3'];
      
      const [mergedTask, shouldUpdateLocal, shouldUpdateRemote] = 
        resolveTaskConflict(localTask, remoteTask);

      expect(mergedTask.tags).toContain('tag1');
      expect(mergedTask.tags).toContain('tag2');
      expect(mergedTask.tags).toContain('tag3');
      expect(mergedTask.tags.length).toBe(3);
      expect(shouldUpdateLocal).toBe(true);
      expect(shouldUpdateRemote).toBe(true);
    });
  });

  describe('arraysEqual', () => {
    test('should return true for identical arrays', () => {
      expect(arraysEqual([1, 2, 3], [1, 2, 3])).toBe(true);
    });

    test('should return true for arrays with same elements in different order', () => {
      expect(arraysEqual([1, 2, 3], [3, 1, 2])).toBe(true);
    });

    test('should return false for arrays with different lengths', () => {
      expect(arraysEqual([1, 2, 3], [1, 2])).toBe(false);
    });

    test('should return false for arrays with different elements', () => {
      expect(arraysEqual([1, 2, 3], [1, 2, 4])).toBe(false);
    });
  });
});