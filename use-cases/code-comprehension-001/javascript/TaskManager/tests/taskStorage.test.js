// tests/taskStorage.test.js
const fs = require('fs');
const { TaskStorage } = require('../storage');
const { Task, TaskPriority, TaskStatus } = require('../models');

// Mock fs module
jest.mock('fs', () => {
  const originalFs = jest.requireActual('fs');
  return {
    ...originalFs,
    existsSync: jest.fn(),
    readFileSync: jest.fn(),
    writeFileSync: jest.fn()
  };
});

describe('TaskStorage', () => {
  const TEST_STORAGE_PATH = 'test-storage.json';
  let taskStorage;
  
  beforeEach(() => {
    // Clear all mocks before each test
    jest.clearAllMocks();
    
    // Reset the mock implementation for existsSync
    fs.existsSync.mockImplementation(() => false);
    
    taskStorage = new TaskStorage(TEST_STORAGE_PATH);
  });
  
  describe('constructor and load', () => {
    test('should initialize with empty tasks if storage file does not exist', () => {
      expect(fs.existsSync).toHaveBeenCalledWith(TEST_STORAGE_PATH);
      expect(fs.readFileSync).not.toHaveBeenCalled();
      expect(taskStorage.tasks).toEqual({});
    });
    
    test('should load tasks from storage file if it exists', () => {
      // Mock file existence and content
      fs.existsSync.mockReturnValueOnce(true);
      
      const mockTask = {
        id: 'test-id',
        title: 'Test Task',
        description: 'Test Description',
        priority: TaskPriority.MEDIUM,
        status: TaskStatus.TODO,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        dueDate: null,
        completedAt: null,
        tags: ['test']
      };
      
      fs.readFileSync.mockReturnValueOnce(JSON.stringify([mockTask]));
      
      // Create a new storage instance to trigger load
      const loadedStorage = new TaskStorage(TEST_STORAGE_PATH);
      
      expect(fs.existsSync).toHaveBeenCalledWith(TEST_STORAGE_PATH);
      expect(fs.readFileSync).toHaveBeenCalledWith(TEST_STORAGE_PATH, 'utf8');
      expect(loadedStorage.tasks).toHaveProperty(mockTask.id);
      expect(loadedStorage.tasks[mockTask.id]).toBeInstanceOf(Task);
      expect(loadedStorage.tasks[mockTask.id].title).toBe(mockTask.title);
    });
    
    test('should handle errors when loading tasks', () => {
      // Mock file existence but throw error on read
      fs.existsSync.mockReturnValueOnce(true);
      fs.readFileSync.mockImplementationOnce(() => {
        throw new Error('Test error');
      });
      
      // Spy on console.error
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      
      // Create a new storage instance to trigger load
      const loadedStorage = new TaskStorage(TEST_STORAGE_PATH);
      
      expect(consoleSpy).toHaveBeenCalled();
      expect(loadedStorage.tasks).toEqual({});
      
      // Restore console.error
      consoleSpy.mockRestore();
    });
  });
  
  describe('save', () => {
    test('should save tasks to storage file', () => {
      // Add a task to storage
      const task = new Task('Test Task');
      taskStorage.tasks[task.id] = task;
      
      // Call save
      taskStorage.save();
      
      expect(fs.writeFileSync).toHaveBeenCalledWith(
        TEST_STORAGE_PATH,
        expect.any(String)
      );
      
      // Verify the saved data
      const savedData = JSON.parse(fs.writeFileSync.mock.calls[0][1]);
      expect(savedData).toBeInstanceOf(Array);
      expect(savedData.length).toBe(1);
      expect(savedData[0].title).toBe('Test Task');
    });
    
    test('should handle errors when saving tasks', () => {
      // Mock writeFileSync to throw error
      fs.writeFileSync.mockImplementationOnce(() => {
        throw new Error('Test error');
      });
      
      // Spy on console.error
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      
      // Call save
      taskStorage.save();
      
      expect(consoleSpy).toHaveBeenCalled();
      
      // Restore console.error
      consoleSpy.mockRestore();
    });
  });
  
  describe('addTask', () => {
    test('should add a task and return its id', () => {
      // Spy on save method
      const saveSpy = jest.spyOn(taskStorage, 'save');
      
      const task = new Task('Test Task');
      const taskId = taskStorage.addTask(task);
      
      expect(taskId).toBe(task.id);
      expect(taskStorage.tasks[task.id]).toBe(task);
      expect(saveSpy).toHaveBeenCalled();
    });
  });
  
  describe('getTask', () => {
    test('should return a task by id', () => {
      const task = new Task('Test Task');
      taskStorage.tasks[task.id] = task;
      
      const retrievedTask = taskStorage.getTask(task.id);
      
      expect(retrievedTask).toBe(task);
    });
    
    test('should return undefined for non-existent task', () => {
      const retrievedTask = taskStorage.getTask('non-existent-id');
      
      expect(retrievedTask).toBeUndefined();
    });
  });
  
  describe('updateTask', () => {
    test('should update a task and return true', () => {
      // Spy on save method
      const saveSpy = jest.spyOn(taskStorage, 'save');
      
      const task = new Task('Original Title');
      taskStorage.tasks[task.id] = task;
      
      const result = taskStorage.updateTask(task.id, { title: 'Updated Title' });
      
      expect(result).toBe(true);
      expect(task.title).toBe('Updated Title');
      expect(saveSpy).toHaveBeenCalled();
    });
    
    test('should return false for non-existent task', () => {
      const result = taskStorage.updateTask('non-existent-id', { title: 'Updated Title' });
      
      expect(result).toBe(false);
    });
  });
  
  describe('deleteTask', () => {
    test('should delete a task and return true', () => {
      // Spy on save method
      const saveSpy = jest.spyOn(taskStorage, 'save');
      
      const task = new Task('Test Task');
      taskStorage.tasks[task.id] = task;
      
      const result = taskStorage.deleteTask(task.id);
      
      expect(result).toBe(true);
      expect(taskStorage.tasks[task.id]).toBeUndefined();
      expect(saveSpy).toHaveBeenCalled();
    });
    
    test('should return false for non-existent task', () => {
      const result = taskStorage.deleteTask('non-existent-id');
      
      expect(result).toBe(false);
    });
  });
  
  describe('getAllTasks', () => {
    test('should return all tasks', () => {
      const task1 = new Task('Task 1');
      const task2 = new Task('Task 2');
      
      taskStorage.tasks[task1.id] = task1;
      taskStorage.tasks[task2.id] = task2;
      
      const allTasks = taskStorage.getAllTasks();
      
      expect(allTasks).toBeInstanceOf(Array);
      expect(allTasks.length).toBe(2);
      expect(allTasks).toContain(task1);
      expect(allTasks).toContain(task2);
    });
  });
  
  describe('getTasksByStatus', () => {
    test('should return tasks filtered by status', () => {
      const todoTask = new Task('Todo Task');
      const inProgressTask = new Task('In Progress Task');
      inProgressTask.status = TaskStatus.IN_PROGRESS;
      
      taskStorage.tasks[todoTask.id] = todoTask;
      taskStorage.tasks[inProgressTask.id] = inProgressTask;
      
      const todoTasks = taskStorage.getTasksByStatus(TaskStatus.TODO);
      
      expect(todoTasks).toBeInstanceOf(Array);
      expect(todoTasks.length).toBe(1);
      expect(todoTasks[0]).toBe(todoTask);
    });
  });
  
  describe('getTasksByPriority', () => {
    test('should return tasks filtered by priority', () => {
      const lowPriorityTask = new Task('Low Priority Task', '', TaskPriority.LOW);
      const highPriorityTask = new Task('High Priority Task', '', TaskPriority.HIGH);
      
      taskStorage.tasks[lowPriorityTask.id] = lowPriorityTask;
      taskStorage.tasks[highPriorityTask.id] = highPriorityTask;
      
      const highPriorityTasks = taskStorage.getTasksByPriority(TaskPriority.HIGH);
      
      expect(highPriorityTasks).toBeInstanceOf(Array);
      expect(highPriorityTasks.length).toBe(1);
      expect(highPriorityTasks[0]).toBe(highPriorityTask);
    });
  });
  
  describe('getOverdueTasks', () => {
    test('should return overdue tasks', () => {
      const notOverdueTask = new Task('Not Overdue Task');
      
      const overdueTask = new Task('Overdue Task');
      // Mock isOverdue to return true
      overdueTask.isOverdue = jest.fn().mockReturnValue(true);
      
      taskStorage.tasks[notOverdueTask.id] = notOverdueTask;
      taskStorage.tasks[overdueTask.id] = overdueTask;
      
      const overdueTasks = taskStorage.getOverdueTasks();
      
      expect(overdueTasks).toBeInstanceOf(Array);
      expect(overdueTasks.length).toBe(1);
      expect(overdueTasks[0]).toBe(overdueTask);
    });
  });
});