const userAuth = require('../src/user_auth');

// Mock localStorage
const localStorageMock = (() => {
  let store = {};
  return {
    getItem: jest.fn(key => store[key]),
    setItem: jest.fn((key, value) => {
      store[key] = value;
    }),
    clear: () => {
      store = {};
    }
  };
})();

// Set up global localStorage mock
global.localStorage = localStorageMock;

// Mock Date.now for predictable testing
const NOW = 1625097600000; // July 1, 2021
const originalDateNow = Date.now;

describe('User Authentication Utility', () => {
  let mockUserDb;
  
  beforeEach(() => {
    // Set up a fresh version of userAuth for each test
    userAuth.sessions = {};
    userAuth.config = {
      tokenExpiration: 24 * 60 * 60 * 1000, // 24 hours
      maxLoginAttempts: 3,
      lockoutDuration: 15 * 60 * 1000, // 15 minutes
      requireStrongPassword: true,
      passwordMinLength: 8
    };
    
    // Mock users database
    mockUserDb = [
      {
        id: '1',
        username: 'testuser',
        password: 'Password123!',
        role: 'user',
        loginAttempts: 0
      },
      {
        id: '2',
        username: 'admin',
        password: 'Admin456!',
        role: 'admin',
        loginAttempts: 0
      },
      {
        id: '3',
        username: 'lockeduser',
        password: 'Locked789!',
        role: 'user',
        loginAttempts: 3,
        lastFailedLogin: NOW - 5 * 60 * 1000 // 5 minutes ago
      }
    ];
    
    // Mock Date.now to return consistent values
    Date.now = jest.fn(() => NOW);
    
    // Clear localStorage mock
    localStorage.clear();
    jest.clearAllMocks();
  });
  
  afterAll(() => {
    // Restore original Date.now
    Date.now = originalDateNow;
  });
  
  describe('init', () => {
    test('should initialize with default configuration', () => {
      const result = userAuth.init();
      expect(result).toBe(userAuth); // Should return itself for chaining
      expect(userAuth.config.maxLoginAttempts).toBe(3);
    });
    
    test('should override defaults with custom configuration', () => {
      userAuth.init({
        maxLoginAttempts: 5,
        passwordMinLength: 10
      });
      expect(userAuth.config.maxLoginAttempts).toBe(5);
      expect(userAuth.config.passwordMinLength).toBe(10);
    });
    
    test('should load and clean sessions from localStorage', () => {
      // Set up mock data in localStorage
      const sessions = {
        'valid_token': {
          userId: '1',
          username: 'testuser',
          role: 'user',
          expires: NOW + 60000 // Still valid
        },
        'expired_token': {
          userId: '2',
          username: 'admin',
          role: 'admin',
          expires: NOW - 60000 // Expired
        }
      };
      
      localStorage.getItem.mockReturnValueOnce(JSON.stringify(sessions));
      
      userAuth.init();
      
      // Should keep valid session and remove expired one
      expect(userAuth.sessions).toEqual({
        'valid_token': sessions.valid_token
      });
    });
    
    test('should handle localStorage exceptions', () => {
      // Mock localStorage.getItem to throw an error
      localStorage.getItem.mockImplementationOnce(() => {
        throw new Error('Storage error');
      });
      
      // Should not throw and should set empty sessions
      expect(() => userAuth.init()).not.toThrow();
      expect(userAuth.sessions).toEqual({});
    });
  });
  
  describe('login', () => {
    test('should login user successfully with valid credentials', () => {
      const result = userAuth.login('testuser', 'Password123!', mockUserDb);
      
      expect(result.success).toBe(true);
      expect(result.token).toBeDefined();
      expect(result.user).toEqual({
        id: '1', 
        username: 'testuser',
        role: 'user'
      });
      
      // Should create session
      expect(Object.keys(userAuth.sessions)).toHaveLength(1);
      
      // Should update localStorage
      expect(localStorage.setItem).toHaveBeenCalled();
    });
    
    test('should fail login with invalid username', () => {
      const result = userAuth.login('nonexistent', 'Password123!', mockUserDb);
      
      expect(result.success).toBe(false);
      expect(result.message).toBe('Invalid username or password');
    });
    
    test('should fail login with invalid password', () => {
      const result = userAuth.login('testuser', 'WrongPassword', mockUserDb);
      
      expect(result.success).toBe(false);
      expect(result.message).toBe('Invalid username or password');
      
      // Should increment login attempts
      expect(mockUserDb[0].loginAttempts).toBe(1);
      expect(mockUserDb[0].lastFailedLogin).toBe(NOW);
    });
    
    test('should prevent login for locked account', () => {
      const result = userAuth.login('lockeduser', 'Locked789!', mockUserDb);
      
      expect(result.success).toBe(false);
      expect(result.message).toContain('Account locked');
    });
    
    test('should reset login attempts after successful login', () => {
      // Set up a user with previous failed attempts
      mockUserDb[0].loginAttempts = 2;
      
      const result = userAuth.login('testuser', 'Password123!', mockUserDb);
      
      expect(result.success).toBe(true);
      expect(mockUserDb[0].loginAttempts).toBe(0);
    });
  });
  
  describe('validateToken', () => {
    beforeEach(() => {
      // Set up a session
      userAuth.sessions = {
        'valid_token': {
          userId: '1',
          username: 'testuser',
          role: 'user',
          expires: NOW + 3600000 // 1 hour from now
        },
        'expired_token': {
          userId: '2',
          username: 'admin',
          role: 'admin',
          expires: NOW - 3600000 // 1 hour ago
        }
      };
    });
    
    test('should validate a valid token', () => {
      const result = userAuth.validateToken('valid_token');
      
      expect(result.valid).toBe(true);
      expect(result.user).toEqual({
        userId: '1',
        username: 'testuser',
        role: 'user'
      });
    });
    
    test('should reject an invalid token', () => {
      const result = userAuth.validateToken('nonexistent_token');
      
      expect(result.valid).toBe(false);
      expect(result.message).toBe('Invalid token');
    });
    
    test('should reject an expired token', () => {
      const result = userAuth.validateToken('expired_token');
      
      expect(result.valid).toBe(false);
      expect(result.message).toBe('Token expired');
      
      // Should remove expired token from sessions
      expect(userAuth.sessions).not.toHaveProperty('expired_token');
    });
  });
  
  describe('logout', () => {
    beforeEach(() => {
      userAuth.sessions = {
        'test_token': {
          userId: '1',
          username: 'testuser',
          role: 'user',
          expires: NOW + 3600000
        }
      };
    });
    
    test('should logout user with valid token', () => {
      const result = userAuth.logout('test_token');
      
      expect(result.success).toBe(true);
      expect(userAuth.sessions).not.toHaveProperty('test_token');
      expect(localStorage.setItem).toHaveBeenCalled();
    });
    
    test('should fail logout with invalid token', () => {
      const result = userAuth.logout('invalid_token');
      
      expect(result.success).toBe(false);
      expect(result.message).toBe('Invalid token');
    });
    
    test('should handle localStorage exceptions during logout', () => {
      localStorage.setItem.mockImplementationOnce(() => {
        throw new Error('Storage error');
      });
      
      // Should not throw
      expect(() => userAuth.logout('test_token')).not.toThrow();
      
      // Should still remove the session
      expect(userAuth.sessions).not.toHaveProperty('test_token');
    });
  });
  
  describe('checkPasswordStrength', () => {
    test('should reject empty password', () => {
      const result = userAuth.checkPasswordStrength('');
      
      expect(result.strong).toBe(false);
      expect(result.message).toBe('Password is required');
    });
    
    test('should reject password that is too short', () => {
      const result = userAuth.checkPasswordStrength('Short1!');
      
      expect(result.strong).toBe(false);
      expect(result.message).toBe('Password must be at least 8 characters');
    });
    
    test('should reject password missing uppercase', () => {
      const result = userAuth.checkPasswordStrength('password123!');
      
      expect(result.strong).toBe(false);
      expect(result.message).toBe('Password must contain at least one uppercase letter');
    });
    
    test('should reject password missing number', () => {
      const result = userAuth.checkPasswordStrength('Password!');
      
      expect(result.strong).toBe(false);
      expect(result.message).toBe('Password must contain at least one number');
    });
    
    test('should reject password missing special character', () => {
      const result = userAuth.checkPasswordStrength('Password123');
      
      expect(result.strong).toBe(false);
      expect(result.message).toBe('Password must contain at least one special character');
    });
    
    test('should accept strong password', () => {
      const result = userAuth.checkPasswordStrength('Password123!');
      
      expect(result.strong).toBe(true);
    });
    
    test('should respect disabling strong password requirement', () => {
      userAuth.config.requireStrongPassword = false;
      
      const result = userAuth.checkPasswordStrength('password123');
      
      expect(result.strong).toBe(true);
    });
  });
});