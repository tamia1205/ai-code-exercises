// storage.js
const fs = require('fs');
const path = require('path');
const { Task, TaskPriority, TaskStatus } = require('./models');

class TaskStorage {
  constructor(storagePath = 'tasks.json') {
    this.storagePath = storagePath;
    this.tasks = {};
    this.load();
  }

  load() {
    if (fs.existsSync(this.storagePath)) {
      try {
        const rawData = fs.readFileSync(this.storagePath, 'utf8');
        const tasksData = JSON.parse(rawData);

        tasksData.forEach(taskData => {
          const task = new Task(taskData.title, taskData.description);
          // Restore all properties from saved data
          task.id = taskData.id;
          task.priority = taskData.priority;
          task.status = taskData.status;
          task.createdAt = new Date(taskData.createdAt);
          task.updatedAt = new Date(taskData.updatedAt);

          if (taskData.dueDate) {
            task.dueDate = new Date(taskData.dueDate);
          }

          if (taskData.completedAt) {
            task.completedAt = new Date(taskData.completedAt);
          }

          task.tags = taskData.tags || [];

          this.tasks[task.id] = task;
        });
      } catch (error) {
        console.error(`Error loading tasks: ${error.message}`);
      }
    }
  }

  save() {
    try {
      const tasksArray = Object.values(this.tasks);
      fs.writeFileSync(this.storagePath, JSON.stringify(tasksArray, null, 2));
    } catch (error) {
      console.error(`Error saving tasks: ${error.message}`);
    }
  }

  addTask(task) {
    this.tasks[task.id] = task;
    this.save();
    return task.id;
  }

  getTask(taskId) {
    return this.tasks[taskId];
  }

  updateTask(taskId, updates) {
    const task = this.getTask(taskId);
    if (task) {
      task.update(updates);
      this.save();
      return true;
    }
    return false;
  }

  deleteTask(taskId) {
    if (this.tasks[taskId]) {
      delete this.tasks[taskId];
      this.save();
      return true;
    }
    return false;
  }

  getAllTasks() {
    return Object.values(this.tasks);
  }

  getTasksByStatus(status) {
    return Object.values(this.tasks).filter(task => task.status === status);
  }

  getTasksByPriority(priority) {
    return Object.values(this.tasks).filter(task => task.priority === priority);
  }

  getOverdueTasks() {
    return Object.values(this.tasks).filter(task => task.isOverdue());
  }
}

module.exports = { TaskStorage };

