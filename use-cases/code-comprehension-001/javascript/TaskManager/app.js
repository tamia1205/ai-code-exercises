// app.js
const { Task, TaskPriority, TaskStatus } = require('./models');
const { TaskStorage } = require('./storage');

class TaskManager {
  constructor(storagePath = 'tasks.json') {
    this.storage = new TaskStorage(storagePath);
  }

  createTask(title, description = "", priorityValue = 2, dueDateStr = null, tags = []) {
    const priority = priorityValue;
    let dueDate = null;

    if (dueDateStr) {
      try {
        dueDate = new Date(dueDateStr);
        if (isNaN(dueDate.getTime())) {
          throw new Error("Invalid date");
        }
      } catch (error) {
        console.error("Invalid date format. Use YYYY-MM-DD");
        return null;
      }
    }

    const task = new Task(title, description, priority, dueDate, tags);
    const taskId = this.storage.addTask(task);
    return taskId;
  }

  listTasks(statusFilter = null, priorityFilter = null, showOverdue = false) {
    if (showOverdue) {
      return this.storage.getOverdueTasks();
    }

    if (statusFilter) {
      return this.storage.getTasksByStatus(statusFilter);
    }

    if (priorityFilter) {
      return this.storage.getTasksByPriority(parseInt(priorityFilter));
    }

    return this.storage.getAllTasks();
  }

  updateTaskStatus(taskId, newStatusValue) {
    if (newStatusValue === TaskStatus.DONE) {
      const task = this.storage.getTask(taskId);
      if (task) {
        task.markAsDone();
        this.storage.save();
        return true;
      }
      return false;
    } else {
      return this.storage.updateTask(taskId, { status: newStatusValue });
    }
  }

  updateTaskPriority(taskId, newPriorityValue) {
    return this.storage.updateTask(taskId, { priority: parseInt(newPriorityValue) });
  }

  updateTaskDueDate(taskId, dueDateStr) {
    try {
      const dueDate = new Date(dueDateStr);
      if (isNaN(dueDate.getTime())) {
        throw new Error("Invalid date");
      }
      return this.storage.updateTask(taskId, { dueDate });
    } catch (error) {
      console.error("Invalid date format. Use YYYY-MM-DD");
      return false;
    }
  }

  deleteTask(taskId) {
    return this.storage.deleteTask(taskId);
  }

  getTaskDetails(taskId) {
    return this.storage.getTask(taskId);
  }

  addTagToTask(taskId, tag) {
    const task = this.storage.getTask(taskId);
    if (task) {
      if (!task.tags.includes(tag)) {
        task.tags.push(tag);
        this.storage.save();
      }
      return true;
    }
    return false;
  }

  removeTagFromTask(taskId, tag) {
    const task = this.storage.getTask(taskId);
    if (task && task.tags.includes(tag)) {
      task.tags = task.tags.filter(t => t !== tag);
      this.storage.save();
      return true;
    }
    return false;
  }

  getStatistics() {
    const tasks = this.storage.getAllTasks();
    const total = tasks.length;

    // Count by status
    const statusCounts = Object.values(TaskStatus).reduce((acc, status) => {
      acc[status] = 0;
      return acc;
    }, {});

    tasks.forEach(task => {
      statusCounts[task.status]++;
    });

    // Count by priority
    const priorityCounts = Object.values(TaskPriority).reduce((acc, priority) => {
      acc[priority] = 0;
      return acc;
    }, {});

    tasks.forEach(task => {
      priorityCounts[task.priority]++;
    });

    // Count overdue
    const overdueTasks = tasks.filter(task => task.isOverdue());

    // Count completed in last 7 days
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

    const completedRecently = tasks.filter(task =>
      task.completedAt && task.completedAt >= sevenDaysAgo
    );

    return {
      total,
      byStatus: statusCounts,
      byPriority: priorityCounts,
      overdue: overdueTasks.length,
      completedLastWeek: completedRecently.length
    };
  }
}

module.exports = { TaskManager };
