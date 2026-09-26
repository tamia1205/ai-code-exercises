/**
 * Mock user store for checking existing usernames and emails
 */
class UserStore {
  constructor() {
    this.users = [
      { username: "existinguser", email: "existing@example.com" },
      { username: "johndoe", email: "john.doe@example.com" }
    ];
  }

  /**
   * Check if a username already exists
   * @param {string} username
   * @returns {boolean}
   */
  usernameExists(username) {
    return this.users.some(user => user.username === username);
  }

  /**
   * Check if an email already exists
   * @param {string} email
   * @returns {boolean}
   */
  emailExists(email) {
    return this.users.some(user => user.email === email);
  }
}

// export default UserStore;
// Export functions for testing
module.exports = { UserStore };
