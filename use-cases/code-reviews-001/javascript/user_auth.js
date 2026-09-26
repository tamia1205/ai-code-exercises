// User authentication utility
const userAuth = {
  // Store for active user sessions
  sessions: {},

  // Configuration with default values
  config: {
    tokenExpiration: 24 * 60 * 60 * 1000, // 24 hours in milliseconds
    maxLoginAttempts: 5,
    lockoutDuration: 15 * 60 * 1000, // 15 minutes in milliseconds
    requireStrongPassword: true,
    passwordMinLength: 8
  },

  // Initialize with custom configuration
  init: function(customConfig) {
    if (customConfig) {
      this.config = {...this.config, ...customConfig};
    }
    // Load active sessions from localStorage if available
    if (typeof localStorage !== 'undefined') {
      try {
        const savedSessions = localStorage.getItem('userAuth_sessions');
        if (savedSessions) {
          this.sessions = JSON.parse(savedSessions);

          // Clean expired sessions
          for (let token in this.sessions) {
            if (this.sessions[token].expires < Date.now()) {
              delete this.sessions[token];
            }
          }
        }
      } catch (e) {
        console.error('Failed to load sessions from localStorage:', e);
      }
    }
    return this;
  },

  // Login function
  login: function(username, password, userDb) {
    // Check if user exists
    const user = userDb.find(u => u.username === username);
    if (!user) {
      return {
        success: false,
        message: 'Invalid username or password'
      };
    }

    // Check if account is locked
    if (user.loginAttempts >= this.config.maxLoginAttempts) {
      const lockoutTime = user.lastFailedLogin + this.config.lockoutDuration;
      if (Date.now() < lockoutTime) {
        const waitMinutes = Math.ceil((lockoutTime - Date.now()) / (60 * 1000));
        return {
          success: false,
          message: `Account locked. Try again in ${waitMinutes} minutes.`
        };
      }
    }

    // Verify password - In real app, use proper password hashing!
    if (user.password !== password) {
      // Update failed login attempts - Note: this should update the actual userDb
      user.loginAttempts = (user.loginAttempts || 0) + 1;
      user.lastFailedLogin = Date.now();

      return {
        success: false,
        message: 'Invalid username or password'
      };
    }

    // Generate session token
    const token = this._generateToken();

    // Create session
    this.sessions[token] = {
      userId: user.id,
      username: user.username,
      role: user.role,
      expires: Date.now() + this.config.tokenExpiration
    };

    // Reset login attempts
    user.loginAttempts = 0;

    // Save sessions to localStorage if available
    if (typeof localStorage !== 'undefined') {
      try {
        localStorage.setItem('userAuth_sessions', JSON.stringify(this.sessions));
      } catch (e) {
        console.error('Failed to save sessions to localStorage:', e);
      }
    }

    return {
      success: true,
      token: token,
      expires: this.sessions[token].expires,
      user: {
        id: user.id,
        username: user.username,
        role: user.role
      }
    };
  },

  // Validate session token
  validateToken: function(token) {
    if (!token || !this.sessions[token]) {
      return {
        valid: false,
        message: 'Invalid token'
      };
    }

    const session = this.sessions[token];

    // Check if token is expired
    if (session.expires < Date.now()) {
      delete this.sessions[token];
      return {
        valid: false,
        message: 'Token expired'
      };
    }

    return {
      valid: true,
      user: {
        userId: session.userId,
        username: session.username,
        role: session.role
      }
    };
  },

  // Logout function
  logout: function(token) {
    if (token && this.sessions[token]) {
      delete this.sessions[token];

      // Update localStorage if available
      if (typeof localStorage !== 'undefined') {
        try {
          localStorage.setItem('userAuth_sessions', JSON.stringify(this.sessions));
        } catch (e) {
          console.error('Failed to save sessions to localStorage:', e);
        }
      }

      return {
        success: true,
        message: 'Logged out successfully'
      };
    }

    return {
      success: false,
      message: 'Invalid token'
    };
  },

  // Check password strength
  checkPasswordStrength: function(password) {
    if (!password) return { strong: false, message: 'Password is required' };

    if (password.length < this.config.passwordMinLength) {
      return {
        strong: false,
        message: `Password must be at least ${this.config.passwordMinLength} characters`
      };
    }

    // Only check if strong passwords are required
    if (this.config.requireStrongPassword) {
      // Check for uppercase letters
      if (!/[A-Z]/.test(password)) {
        return {
          strong: false,
          message: 'Password must contain at least one uppercase letter'
        };
      }

      // Check for numbers
      if (!/[0-9]/.test(password)) {
        return {
          strong: false,
          message: 'Password must contain at least one number'
        };
      }

      // Check for special characters
      if (!/[^A-Za-z0-9]/.test(password)) {
        return {
          strong: false,
          message: 'Password must contain at least one special character'
        };
      }
    }

    return {
      strong: true,
      message: 'Password meets strength requirements'
    };
  },

  // Generate random token - In a real app, use a more secure method
  _generateToken: function() {
    return 'token_' + Math.random().toString(36).substr(2) + Date.now().toString(36);
  }
};