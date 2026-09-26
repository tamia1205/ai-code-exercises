// tests/taskManagerIntegration.test.js
const fs = require('fs');
const path = require('path');
const { TaskManager } = require('../app');
const { TaskStatus, TaskPriority } = require('../models');

// This is an integration test that tests TaskManager with the real TaskStorage
describe('TaskManager Integration', () => {
  const TEST_STORAGE_PATH = 'test-integration-tasks.json';
  let taskManager;
  
  beforeEach(() => {
    // Create a fresh TaskManager instance with a test storage file
    taskManager = new TaskManager(TEST_STORAGE_PATH);
    
    // Make sure we start with a clean state
    if (fs.existsSync(TEST_STORAGE_PATH)) {
      fs.unlinkSync(TEST_STORAGE_PATH);
    }
  });
  
  afterEach(() => {
    // Clean up the test file after each test
    if (fs.existsSync(TEST_STORAGE_PATH)) {
      fs.unlinkSync(TEST_STORAGE_PATH);
    }
  });
  
  test('should create and retrieve a task', () => {
    // Create a task
    const title = 'Integration Test Task';
    const description = 'Testing TaskManager with real storage';
    const priority = TaskPriority.HIGH;
    const dueDate = '2023-12-31';
    const tags = ['integration', 'test'];
    
    const taskId = taskManager.createTask(title, description, priority, dueDate, tags);
    expect(taskId).toBeDefined();
    
    // Retrieve the task
    const task = taskManager.getTaskDetails(taskId);
    expect(task).toBeDefined();
    expect(task.title).toBe(title);
    expect(task.description).toBe(description);
    expect(task.priority).toBe(priority);
    expect(task.dueDate).toBeInstanceOf(Date);
    expect(task.tags).toEqual(tags);
    
    // Verify the file was created
    expect(fs.existsSync(TEST_STORAGE_PATH)).toBe(true);
  });
  
  test('should update task status', () => {
    // Create a task
    const taskId = taskManager.createTask('Status Test Task');
    
    // Update status to IN_PROGRESS
    const updateResult = taskManager.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS);
    expect(updateResult).toBe(true);
    
    // Verify the status was updated
    const task = taskManager.getTaskDetails(taskId);
    expect(task.status).toBe(TaskStatus.IN_PROGRESS);
  });
  
  test('should update task priority', () => {
    // Create a task
    const taskId = taskManager.createTask('Priority Test Task');
    
    // Update priority to HIGH
    const updateResult = taskManager.updateTaskPriority(taskId, TaskPriority.HIGH);
    expect(updateResult).toBe(true);
    
    // Verify the priority was updated
    const task = taskManager.getTaskDetails(taskId);
    expect(task.priority).toBe(TaskPriority.HIGH);
  });
  
  test('should update task due date', () => {
    // Create a task
    const taskId = taskManager.createTask('Due Date Test Task');
    
    // Update due date
    const dueDate = '2023-12-31';
    const updateResult = taskManager.updateTaskDueDate(taskId, dueDate);
    expect(updateResult).toBe(true);
    
    // Verify the due date was updated
    const task = taskManager.getTaskDetails(taskId);
    expect(task.dueDate).toBeInstanceOf(Date);
    expect(task.dueDate.toISOString().split('T')[0]).toBe(dueDate);
  });
  
  test('should add and remove tags', () => {
    // Create a task
    const taskId = taskManager.createTask('Tag Test Task');
    
    // Add a tag
    const addResult = taskManager.addTagToTask(taskId, 'test-tag');
    expect(addResult).toBe(true);
    
    // Verify the tag was added
    let task = taskManager.getTaskDetails(taskId);
    expect(task.tags).toContain('test-tag');
    
    // Remove the tag
    const removeResult = taskManager.removeTagFromTask(taskId, 'test-tag');
    expect(removeResult).toBe(true);
    
    // Verify the tag was removed
    task = taskManager.getTaskDetails(taskId);
    expect(task.tags).not.toContain('test-tag');
  });
  
  test('should delete a task', () => {
    // Create a task
    const taskId = taskManager.createTask('Delete Test Task');
    
    // Verify the task exists
    expect(taskManager.getTaskDetails(taskId)).toBeDefined();
    
    // Delete the task
    const deleteResult = taskManager.deleteTask(taskId);
    expect(deleteResult).toBe(true);
    
    // Verify the task was deleted
    expect(taskManager.getTaskDetails(taskId)).toBeUndefined();
  });
  
  test('should list tasks with filters', () => {
    // Create tasks with different statuses and priorities
    const todoTaskId = taskManager.createTask('Todo Task', '', TaskPriority.LOW);
    const inProgressTaskId = taskManager.createTask('In Progress Task', '', TaskPriority.MEDIUM);
    const reviewTaskId = taskManager.createTask('Review Task', '', TaskPriority.HIGH);
    
    // Update statuses
    taskManager.updateTaskStatus(inProgressTaskId, TaskStatus.IN_PROGRESS);
    taskManager.updateTaskStatus(reviewTaskId, TaskStatus.REVIEW);
    
    // List all tasks
    const allTasks = taskManager.listTasks();
    expect(allTasks.length).toBe(3);
    
    // List tasks by status
    const todoTasks = taskManager.listTasks(TaskStatus.TODO);
    expect(todoTasks.length).toBe(1);
    expect(todoTasks[0].id).toBe(todoTaskId);
    
    // List tasks by priority
    const highPriorityTasks = taskManager.listTasks(null, TaskPriority.HIGH);
    expect(highPriorityTasks.length).toBe(1);
    expect(highPriorityTasks[0].id).toBe(reviewTaskId);
  });
  
  test('should get statistics', () => {
    // Create tasks with different statuses and priorities
    taskManager.createTask('Todo Task', '', TaskPriority.LOW);
    
    const inProgressTaskId = taskManager.createTask('In Progress Task', '', TaskPriority.MEDIUM);
    taskManager.updateTaskStatus(inProgressTaskId, TaskStatus.IN_PROGRESS);
    
    const doneTaskId = taskManager.createTask('Done Task', '', TaskPriority.HIGH);
    taskManager.updateTaskStatus(doneTaskId, TaskStatus.DONE);
    
    // Get statistics
    const stats = taskManager.getStatistics();
    
    // Verify statistics
    expect(stats.total).toBe(3);
    expect(stats.byStatus[TaskStatus.TODO]).toBe(1);
    expect(stats.byStatus[TaskStatus.IN_PROGRESS]).toBe(1);
    expect(stats.byStatus[TaskStatus.DONE]).toBe(1);
    expect(stats.byPriority[TaskPriority.LOW]).toBe(1);
    expect(stats.byPriority[TaskPriority.MEDIUM]).toBe(1);
    expect(stats.byPriority[TaskPriority.HIGH]).toBe(1);
  });
});