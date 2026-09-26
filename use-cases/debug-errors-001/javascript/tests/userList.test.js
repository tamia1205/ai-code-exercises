// userList.test.js
const { loadDashboard } = require('../userList');
describe('Dashboard', () => {
  beforeEach(() => {
    // Setup a mock DOM environment
    document.body.innerHTML = '<div id="user-list"></div>';
  });

  test('loadDashboard should render user list correctly', () => {
    // Call the function
    loadDashboard();

    // Verify the results
    const userCards = document.querySelectorAll('.user-card');
    expect(userCards.length).toBe(5); // Expecting 3 users from the sample data

    // Check if the first user is rendered correctly
    expect(userCards[0].querySelector('h3').textContent).toBe('John Doe');
    expect(userCards[0].querySelector('p').textContent).toBe('john@example.com');
  });
});
