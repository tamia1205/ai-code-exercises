// userList.js
function renderUserList(users) {
  const userListElement = document.getElementById('user-list');
  userListElement.innerHTML = '';

  // Loop through users, up to a maximum of 5
  for (let i = 0; i < 5; i++) {
    const user = users[i];

    // Now safe because we only process existing users
    const userName = user.name;
    const userEmail = user.email;

    const userElement = document.createElement('div');
    userElement.innerHTML = `
      <div class="user-card">
        <h3>${userName}</h3>
        <p>${userEmail}</p>
      </div>
    `;

    userListElement.appendChild(userElement);
  }
}

// dashboard.js
function loadDashboard() {
  renderUserList(sampleResponse.users);

}

// Sample data from API
const sampleResponse = {
  users: [
    { name: "John Doe", email: "john@example.com" },
    { name: "Jane Smith", email: "jane@example.com" },
    { name: "Bob Johnson", email: "bob@example.com" }
  ]
};

// Export the loadDashboard function for testing
module.exports = {
  loadDashboard
};
