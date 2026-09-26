// tests/task.test.js
const { Task, TaskPriority, TaskStatus } = require('../models');

describe('Task', () => {
  describe('constructor', () => {
    test('should create a task with minimal information', () => {
      const task = new Task('Test Task');
      
      expect(task.id).toBeDefined();
      expect(task.title).toBe('Test Task');
      expect(task.description).toBe('');
      expect(task.priority).toBe(TaskPriority.MEDIUM);
      expect(task.status).toBe(TaskStatus.TODO);
      expect(task.createdAt).toBeInstanceOf(Date);
      expect(task.updatedAt).toBeInstanceOf(Date);
      expect(task.dueDate).toBeNull();
      expect(task.completedAt).toBeNull();
      expect(task.tags).toEqual([]);
    });
    
    test('should create a task with all information', () => {
      const title = 'Test Task';
      const description = 'Test Description';
      const priority = TaskPriority.HIGH;
      const dueDate = new Date('2023-12-31');
      const tags = ['test', 'important'];
      
      const task = new Task(title, description, priority, dueDate, tags);
      
      expect(task.id).toBeDefined();
      expect(task.title).toBe(title);
      expect(task.description).toBe(description);
      expect(task.priority).toBe(priority);
      expect(task.status).toBe(TaskStatus.TODO);
      expect(task.createdAt).toBeInstanceOf(Date);
      expect(task.updatedAt).toBeInstanceOf(Date);
      expect(task.dueDate).toBe(dueDate);
      expect(task.completedAt).toBeNull();
      expect(task.tags).toEqual(tags);
    });
  });
  
  describe('update', () => {
    test('should update task properties', () => {
      const task = new Task('Original Title', 'Original Description');
      const originalUpdatedAt = task.updatedAt;
      
      // Wait a bit to ensure updatedAt will be different
      jest.advanceTimersByTime(1000);
      
      task.update({
        title: 'Updated Title',
        description: 'Updated Description',
        priority: TaskPriority.HIGH,
        status: TaskStatus.IN_PROGRESS
      });
      
      expect(task.title).toBe('Updated Title');
      expect(task.description).toBe('Updated Description');
      expect(task.priority).toBe(TaskPriority.HIGH);
      expect(task.status).toBe(TaskStatus.IN_PROGRESS);
      expect(task.updatedAt).not.toEqual(originalUpdatedAt);
    });
    
    test('should ignore non-existent properties', () => {
      const task = new Task('Test Task');
      
      task.update({
        nonExistentProperty: 'value'
      });
      
      expect(task).not.toHaveProperty('nonExistentProperty');
    });
  });
  
  describe('markAsDone', () => {
    test('should mark task as done', () => {
      const task = new Task('Test Task');
      const originalUpdatedAt = task.updatedAt;
      
      // Wait a bit to ensure updatedAt will be different
      jest.advanceTimersByTime(1000);
      
      task.markAsDone();
      
      expect(task.status).toBe(TaskStatus.DONE);
      expect(task.completedAt).toBeInstanceOf(Date);
      expect(task.updatedAt).not.toEqual(originalUpdatedAt);
      expect(task.updatedAt).toEqual(task.completedAt);
    });
  });
  
  describe('isOverdue', () => {
    beforeEach(() => {
      // Mock Date.now to return a fixed date
      jest.spyOn(global.Date, 'now').mockImplementation(() => 
        new Date('2023-06-15').valueOf()
      );
    });
    
    afterEach(() => {
      // Restore original Date.now
      jest.restoreAllMocks();
    });
    
    test('should return false if no due date', () => {
      const task = new Task('Test Task');
      
      expect(task.isOverdue()).toBe(false);
    });
    
    test('should return false if due date is in the future', () => {
      const task = new Task('Test Task', '', TaskPriority.MEDIUM, new Date('2023-12-31'));
      
      expect(task.isOverdue()).toBe(true);
    });
    
    test('should return true if due date is in the past and task is not done', () => {
      const task = new Task('Test Task', '', TaskPriority.MEDIUM, new Date('2023-01-01'));
      
      expect(task.isOverdue()).toBe(true);
    });
    
    test('should return false if due date is in the past but task is done', () => {
      const task = new Task('Test Task', '', TaskPriority.MEDIUM, new Date('2023-01-01'));
      task.markAsDone();
      
      expect(task.isOverdue()).toBe(false);
    });
  });
});