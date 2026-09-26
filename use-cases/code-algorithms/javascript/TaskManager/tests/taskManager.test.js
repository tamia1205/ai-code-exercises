// tests/taskManager.test.js
const { TaskManager } = require('../app');
const { TaskStorage } = require('../storage');
const { Task, TaskPriority, TaskStatus } = require('../models');

// Mock the TaskStorage class
jest.mock('../storage', () => {
  return {
    TaskStorage: jest.fn().mockImplementation(() => {
      return {
        addTask: jest.fn().mockImplementation(task => task.id),
        getTask: jest.fn(),
        updateTask: jest.fn().mockReturnValue(true),
        deleteTask: jest.fn().mockReturnValue(true),
        getAllTasks: jest.fn().mockReturnValue([]),
        getTasksByStatus: jest.fn().mockReturnValue([]),
        getTasksByPriority: jest.fn().mockReturnValue([]),
        getOverdueTasks: jest.fn().mockReturnValue([]),
        save: jest.fn()
      };
    })
  };
});

describe('TaskManager', () => {
  let taskManager;
  
  beforeEach(() => {
    // Clear all mocks before each test
    jest.clearAllMocks();
    taskManager = new TaskManager('test-tasks.json');
  });

  describe('constructor', () => {
    test('should initialize with default storage path', () => {
      const defaultTaskManager = new TaskManager();
      expect(TaskStorage).toHaveBeenCalledWith('tasks.json');
    });

    test('should initialize with custom storage path', () => {
      expect(TaskStorage).toHaveBeenCalledWith('test-tasks.json');
    });
  });

  describe('createTask', () => {
    test('should create a task with minimal information', () => {
      const taskId = taskManager.createTask('Test Task');
      
      expect(taskManager.storage.addTask).toHaveBeenCalled();
      expect(taskId).toBeDefined();
    });

    test('should create a task with all information', () => {
      const title = 'Test Task';
      const description = 'Test Description';
      const priority = TaskPriority.HIGH;
      const dueDate = '2023-12-31';
      const tags = ['test', 'important'];
      
      const taskId = taskManager.createTask(title, description, priority, dueDate, tags);
      
      expect(taskManager.storage.addTask).toHaveBeenCalled();
      expect(taskId).toBeDefined();
    });

    test('should return null for invalid date', () => {
      const taskId = taskManager.createTask('Test Task', '', 2, 'invalid-date');
      
      expect(taskManager.storage.addTask).not.toHaveBeenCalled();
      expect(taskId).toBeNull();
    });
  });

  describe('listTasks', () => {
    test('should return all tasks when no filters are provided', () => {
      taskManager.listTasks();
      
      expect(taskManager.storage.getAllTasks).toHaveBeenCalled();
    });

    test('should return overdue tasks when showOverdue is true', () => {
      taskManager.listTasks(null, null, true);
      
      expect(taskManager.storage.getOverdueTasks).toHaveBeenCalled();
    });

    test('should return tasks filtered by status', () => {
      taskManager.listTasks(TaskStatus.TODO);
      
      expect(taskManager.storage.getTasksByStatus).toHaveBeenCalledWith(TaskStatus.TODO);
    });

    test('should return tasks filtered by priority', () => {
      taskManager.listTasks(null, TaskPriority.HIGH);
      
      expect(taskManager.storage.getTasksByPriority).toHaveBeenCalledWith(TaskPriority.HIGH);
    });
  });

  describe('updateTaskStatus', () => {
    test('should update task status to non-done status', () => {
      const result = taskManager.updateTaskStatus('task-id', TaskStatus.IN_PROGRESS);
      
      expect(taskManager.storage.updateTask).toHaveBeenCalledWith('task-id', { status: TaskStatus.IN_PROGRESS });
      expect(result).toBe(true);
    });

    test('should mark task as done when status is DONE', () => {
      const mockTask = {
        markAsDone: jest.fn()
      };
      
      taskManager.storage.getTask.mockReturnValueOnce(mockTask);
      
      const result = taskManager.updateTaskStatus('task-id', TaskStatus.DONE);
      
      expect(mockTask.markAsDone).toHaveBeenCalled();
      expect(taskManager.storage.save).toHaveBeenCalled();
      expect(result).toBe(true);
    });

    test('should return false when task is not found and status is DONE', () => {
      taskManager.storage.getTask.mockReturnValueOnce(null);
      
      const result = taskManager.updateTaskStatus('non-existent-task', TaskStatus.DONE);
      
      expect(result).toBe(false);
    });
  });

  describe('updateTaskPriority', () => {
    test('should update task priority', () => {
      const result = taskManager.updateTaskPriority('task-id', TaskPriority.HIGH);
      
      expect(taskManager.storage.updateTask).toHaveBeenCalledWith('task-id', { priority: TaskPriority.HIGH });
      expect(result).toBe(true);
    });
  });

  describe('updateTaskDueDate', () => {
    test('should update task due date with valid date', () => {
      const result = taskManager.updateTaskDueDate('task-id', '2023-12-31');
      
      expect(taskManager.storage.updateTask).toHaveBeenCalled();
      expect(result).toBe(true);
    });

    test('should return false for invalid date', () => {
      const result = taskManager.updateTaskDueDate('task-id', 'invalid-date');
      
      expect(taskManager.storage.updateTask).not.toHaveBeenCalled();
      expect(result).toBe(false);
    });
  });

  describe('deleteTask', () => {
    test('should delete a task', () => {
      const result = taskManager.deleteTask('task-id');
      
      expect(taskManager.storage.deleteTask).toHaveBeenCalledWith('task-id');
      expect(result).toBe(true);
    });
  });

  describe('getTaskDetails', () => {
    test('should return task details', () => {
      const mockTask = { id: 'task-id', title: 'Test Task' };
      taskManager.storage.getTask.mockReturnValueOnce(mockTask);
      
      const result = taskManager.getTaskDetails('task-id');
      
      expect(taskManager.storage.getTask).toHaveBeenCalledWith('task-id');
      expect(result).toEqual(mockTask);
    });
  });

  describe('addTagToTask', () => {
    test('should add a tag to a task', () => {
      const mockTask = {
        tags: ['existing-tag']
      };
      
      taskManager.storage.getTask.mockReturnValueOnce(mockTask);
      
      const result = taskManager.addTagToTask('task-id', 'new-tag');
      
      expect(mockTask.tags).toContain('new-tag');
      expect(taskManager.storage.save).toHaveBeenCalled();
      expect(result).toBe(true);
    });

    test('should not add duplicate tag', () => {
      const mockTask = {
        tags: ['existing-tag']
      };
      
      taskManager.storage.getTask.mockReturnValueOnce(mockTask);
      
      const result = taskManager.addTagToTask('task-id', 'existing-tag');
      
      expect(mockTask.tags).toEqual(['existing-tag']);
      expect(taskManager.storage.save).not.toHaveBeenCalled();
      expect(result).toBe(true);
    });

    test('should return false when task is not found', () => {
      taskManager.storage.getTask.mockReturnValueOnce(null);
      
      const result = taskManager.addTagToTask('non-existent-task', 'tag');
      
      expect(result).toBe(false);
    });
  });

  describe('removeTagFromTask', () => {
    test('should remove a tag from a task', () => {
      const mockTask = {
        tags: ['tag-to-remove', 'other-tag']
      };
      
      taskManager.storage.getTask.mockReturnValueOnce(mockTask);
      
      const result = taskManager.removeTagFromTask('task-id', 'tag-to-remove');
      
      expect(mockTask.tags).not.toContain('tag-to-remove');
      expect(mockTask.tags).toContain('other-tag');
      expect(taskManager.storage.save).toHaveBeenCalled();
      expect(result).toBe(true);
    });

    test('should return false when task is not found', () => {
      taskManager.storage.getTask.mockReturnValueOnce(null);
      
      const result = taskManager.removeTagFromTask('non-existent-task', 'tag');
      
      expect(result).toBe(false);
    });

    test('should return false when tag is not found', () => {
      const mockTask = {
        tags: ['existing-tag']
      };
      
      taskManager.storage.getTask.mockReturnValueOnce(mockTask);
      
      const result = taskManager.removeTagFromTask('task-id', 'non-existent-tag');
      
      expect(result).toBe(false);
    });
  });

  describe('getStatistics', () => {
    test('should return task statistics', () => {
      const mockTasks = [
        {
          status: TaskStatus.TODO,
          priority: TaskPriority.LOW,
          isOverdue: jest.fn().mockReturnValue(false),
          completedAt: null
        },
        {
          status: TaskStatus.IN_PROGRESS,
          priority: TaskPriority.MEDIUM,
          isOverdue: jest.fn().mockReturnValue(true),
          completedAt: null
        },
        {
          status: TaskStatus.DONE,
          priority: TaskPriority.HIGH,
          isOverdue: jest.fn().mockReturnValue(false),
          completedAt: new Date()
        }
      ];
      
      taskManager.storage.getAllTasks.mockReturnValueOnce(mockTasks);
      
      const stats = taskManager.getStatistics();
      
      expect(stats).toHaveProperty('total', 3);
      expect(stats).toHaveProperty('byStatus');
      expect(stats).toHaveProperty('byPriority');
      expect(stats).toHaveProperty('overdue');
      expect(stats).toHaveProperty('completedLastWeek');
    });
  });
});