const {TaskPriority, TaskStatus} = require("./models");

function calculateTaskScore(task) {
  // Base priority weights
  const priorityWeights = {
    [TaskPriority.LOW]: 1,
    [TaskPriority.MEDIUM]: 2,
    [TaskPriority.HIGH]: 3,
    [TaskPriority.URGENT]: 4
  };

  // Calculate base score from priority
  let score = (priorityWeights[task.priority] || 0) * 10;

  // Add due date factor (higher score for tasks due sooner)
  if (task.dueDate) {
    const now = new Date();
    const dueDate =task.dueDate;
    const daysUntilDue = Math.ceil((dueDate - now) / (1000 * 60 * 60 * 24));

    if (daysUntilDue < 0) {  // Overdue tasks
      score += 30;
    } else if (daysUntilDue === 0) {  // Due today
      score += 20;
    } else if (daysUntilDue <= 2) {  // Due in next 2 days
      score += 15;
    } else if (daysUntilDue <= 7) {  // Due in next week
      score += 10;
    }
  }

  // Reduce score for tasks that are completed or in review
  if (task.status === TaskStatus.DONE) {
    score -= 50;
  } else if (task.status === TaskStatus.REVIEW) {
    score -= 15;
  }

  // Boost score for tasks with certain tags
  if (task.tags.some(tag => ["blocker", "critical", "urgent"].includes(tag))) {
    score += 8;
  }

  // Boost score for recently updated tasks
  const now = new Date();
  const updatedAt = new Date(task.updatedAt);
  const daysSinceUpdate = Math.floor((now - updatedAt) / (1000 * 60 * 60 * 24));
  if (daysSinceUpdate < 1) {
    score += 5;
  }

  return score;
}

function sortTasksByImportance(tasks) {
  // Create a copy of the tasks array to avoid modifying the original
  return [...tasks].sort((a, b) => {
    return calculateTaskScore(b) - calculateTaskScore(a);
  });
}

function getTopPriorityTasks(tasks, limit = 5) {
  const sortedTasks = sortTasksByImportance(tasks);
  return sortedTasks.slice(0, limit);
}

// Export functions for testing
module.exports = { calculateTaskScore, sortTasksByImportance, getTopPriorityTasks };
