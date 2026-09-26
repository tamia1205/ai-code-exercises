// cli.js
const { program } = require('commander');
const { TaskManager } = require('./app');
const { TaskStatus, TaskPriority } = require('./models');

const formatTask = (task) => {
  const statusSymbol = {
    [TaskStatus.TODO]: '[ ]',
    [TaskStatus.IN_PROGRESS]: '[>]',
    [TaskStatus.REVIEW]: '[?]',
    [TaskStatus.DONE]: '[✓]'
  };

  const prioritySymbol = {
    [TaskPriority.LOW]: '!',
    [TaskPriority.MEDIUM]: '!!',
    [TaskPriority.HIGH]: '!!!',
    [TaskPriority.URGENT]: '!!!!'
  };

  const dueStr = task.dueDate
    ? `Due: ${task.dueDate.toISOString().split('T')[0]}`
    : 'No due date';

  const tagsStr = task.tags.length
    ? `Tags: ${task.tags.join(', ')}`
    : 'No tags';

  return (
    `${statusSymbol[task.status]} ${task.id.substr(0, 8)} - ${prioritySymbol[task.priority]} ${task.title}\n` +
    `  ${task.description}\n` +
    `  ${dueStr} | ${tagsStr}\n` +
    `  Created: ${task.createdAt.toISOString().split('T')[0]} ${task.createdAt.toTimeString().split(' ')[0]}`
  );
};

const taskManager = new TaskManager();

program
  .version('1.0.0')
  .description('Task Manager CLI');

program
  .command('create <title>')
  .description('Create a new task')
  .option('-d, --description <description>', 'Task description', '')
  .option('-p, --priority <priority>', 'Task priority (1-4)', 2)
  .option('-u, --due <due_date>', 'Due date (YYYY-MM-DD)')
  .option('-t, --tags <tags>', 'Comma-separated tags', '')
  .action((title, options) => {
    const tags = options.tags ? options.tags.split(',').map(tag => tag.trim()) : [];
    const taskId = taskManager.createTask(
      title,
      options.description,
      options.priority,
      options.due,
      tags
    );

    if (taskId) {
      console.log(`Created task with ID: ${taskId}`);
    }
  });

program
  .command('list')
  .description('List all tasks')
  .option('-s, --status <status>', 'Filter by status')
  .option('-p, --priority <priority>', 'Filter by priority')
  .option('-o, --overdue', 'Show only overdue tasks')
  .action((options) => {
    const tasks = taskManager.listTasks(options.status, options.priority, options.overdue);
    if (tasks.length > 0) {
      tasks.forEach(task => {
        console.log(formatTask(task));
        console.log('-'.repeat(50));
      });
    } else {
      console.log('No tasks found matching the criteria.');
    }
  });

program
  .command('status <task_id> <status>')
  .description('Update task status')
  .action((taskId, status) => {
    if (taskManager.updateTaskStatus(taskId, status)) {
      console.log(`Updated task status to ${status}`);
    } else {
      console.log('Failed to update task status. Task not found.');
    }
  });

program
  .command('priority <task_id> <priority>')
  .description('Update task priority')
  .action((taskId, priority) => {
    if (taskManager.updateTaskPriority(taskId, priority)) {
      console.log(`Updated task priority to ${priority}`);
    } else {
      console.log('Failed to update task priority. Task not found.');
    }
  });

program
  .command('due <task_id> <due_date>')
  .description('Update task due date')
  .action((taskId, dueDate) => {
    if (taskManager.updateTaskDueDate(taskId, dueDate)) {
      console.log(`Updated task due date to ${dueDate}`);
    } else {
      console.log('Failed to update task due date. Task not found or invalid date.');
    }
  });

program
  .command('tag <task_id> <tag>')
  .description('Add tag to task')
  .action((taskId, tag) => {
    if (taskManager.addTagToTask(taskId, tag)) {
      console.log(`Added tag '${tag}' to task`);
    } else {
      console.log('Failed to add tag. Task not found.');
    }
  });

program
  .command('untag <task_id> <tag>')
  .description('Remove tag from task')
  .action((taskId, tag) => {
    if (taskManager.removeTagFromTask(taskId, tag)) {
      console.log(`Removed tag '${tag}' from task`);
    } else {
      console.log('Failed to remove tag. Task or tag not found.');
    }
  });

program
  .command('show <task_id>')
  .description('Show task details')
  .action((taskId) => {
    const task = taskManager.getTaskDetails(taskId);
    if (task) {
      console.log(formatTask(task));
    } else {
      console.log('Task not found.');
    }
  });

program
  .command('delete <task_id>')
  .description('Delete a task')
  .action((taskId) => {
    if (taskManager.deleteTask(taskId)) {
      console.log(`Deleted task ${taskId}`);
    } else {
      console.log('Failed to delete task. Task not found.');
    }
  });

program
  .command('stats')
  .description('Show task statistics')
  .action(() => {
    const stats = taskManager.getStatistics();
    console.log(`Total tasks: ${stats.total}`);
    console.log('By status:');
    Object.entries(stats.byStatus).forEach(([status, count]) => {
      console.log(`  ${status}: ${count}`);
    });
    console.log('By priority:');
    Object.entries(stats.byPriority).forEach(([priority, count]) => {
      console.log(`  ${priority}: ${count}`);
    });
    console.log(`Overdue tasks: ${stats.overdue}`);
    console.log(`Completed in last 7 days: ${stats.completedLastWeek}`);
  });

program.parse(process.argv);

// If no arguments, show help
if (!process.argv.slice(2).length) {
  program.outputHelp();
}