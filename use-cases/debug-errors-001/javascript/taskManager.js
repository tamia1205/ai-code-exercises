// taskManager.js
// Global variable to store tasks
let tasks = [];

// Function to initialize the application
function initApp() {
  console.log("Task Manager initialized");
  tasks = [
    { id: 1, name: "Complete project proposal", completed: false },
    { id: 2, name: "Meeting with team", completed: true }
  ];
  displayTasks();
  return tasks; // Return for testing
}

// Function to add a new task
function addTask(taskName) {
  let tasks = { id: Date.now(), name: taskName, completed: false };  // Notice the 'let tasks' here!
  console.log("Task added:", tasks);
  displayTasks();
  return tasks; // Return for testing
}

// Function to display all tasks
function displayTasks() {
  // Check if we're in a browser environment
  if (typeof document !== 'undefined') {
    const taskListElement = document.getElementById('task-list');
    if (taskListElement) {
      taskListElement.innerHTML = "";

      tasks.forEach(task => {
        const taskElement = document.createElement('div');
        taskElement.innerHTML = `
          <div class="task-item ${task.completed ? 'completed' : ''}">
            <span>${task.name}</span>
            <button onclick="toggleTaskStatus(${task.id})">Toggle</button>
            <button onclick="deleteTask(${task.id})">Delete</button>
          </div>
        `;
        taskListElement.appendChild(taskElement);
      });
    }
  } else {
    tasks.forEach(task => {
      console.log(task.name);
    });
  }
  return tasks; // Return for testing
}

// Toggle task status
function toggleTaskStatus(taskId) {
  tasks = tasks.map(task => {
    if (task.id === taskId) {
      return { ...task, completed: !task.completed };
    }
    return task;
  });
  displayTasks();
  return tasks; // Return for testing
}

// Delete a task
function deleteTask(taskId) {
  tasks = tasks.filter(task => task.id !== taskId);
  displayTasks();
  return tasks; // Return for testing
}

// Function to get all tasks (for testing)
function getAllTasks() {
  return tasks;
}

// Function to reset tasks (for testing)
function resetTasks() {
  tasks = [];
  return tasks;
}

// Initialize the app when page loads (only in browser environment)
if (typeof window !== 'undefined') {
  window.onload = initApp;
}

// Export functions for testing
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    initApp,
    addTask,
    displayTasks,
    toggleTaskStatus,
    deleteTask,
    getAllTasks,
    resetTasks
  };
}
