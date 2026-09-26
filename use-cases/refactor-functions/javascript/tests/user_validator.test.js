const { validateUserData } = require('../user_validator');
const { UserStore } = require('../user_store');

describe('validateUserData', () => {
  let userStore;

  beforeEach(() => {
    userStore = new UserStore();
  });

  describe('Registration validation', () => {
    test('should validate required fields for registration', () => {
      const userData = {
        username: '',
        email: '',
        password: '',
        confirmPassword: ''
      };

      const errors = validateUserData(userData, { isRegistration: true });
      
      expect(errors).toContain('username is required for registration');
      expect(errors).toContain('email is required for registration');
      expect(errors).toContain('password is required for registration');
      expect(errors).toContain('confirmPassword is required for registration');
    });

    test('should validate username format', () => {
      const userData = {
        username: 'a@', // Too short and invalid characters
        email: 'test@example.com',
        password: 'Password123!',
        confirmPassword: 'Password123!'
      };

      const errors = validateUserData(userData, { isRegistration: true });
      
      expect(errors).toContain('Username must be at least 3 characters long');
      
      userData.username = 'thisusernameiswaytoolongforthesystem';
      const lengthErrors = validateUserData(userData, { isRegistration: true });
      expect(lengthErrors).toContain('Username must be at most 20 characters long');
      
      userData.username = 'invalid@username';
      const formatErrors = validateUserData(userData, { isRegistration: true });
      expect(formatErrors).toContain('Username can only contain letters, numbers, and underscores');
    });

    test('should detect existing username', () => {
      const userData = {
        username: 'existinguser',
        email: 'new@example.com',
        password: 'Password123!',
        confirmPassword: 'Password123!'
      };

      const errors = validateUserData(userData, { 
        isRegistration: true, 
        checkExisting: userStore 
      });
      
      expect(errors).toContain('Username is already taken');
    });

    test('should validate password requirements', () => {
      const baseUserData = {
        username: 'newuser',
        email: 'new@example.com',
        confirmPassword: 'Password123!'
      };
      
      // Test password length
      const shortPassword = { ...baseUserData, password: 'Short1!' };
      const lengthErrors = validateUserData(shortPassword, { isRegistration: true });
      expect(lengthErrors).toContain('Password must be at least 8 characters long');
      
      // Test uppercase requirement
      const noUppercase = { ...baseUserData, password: 'password123!' };
      const uppercaseErrors = validateUserData(noUppercase, { isRegistration: true });
      expect(uppercaseErrors).toContain('Password must contain at least one uppercase letter');
      
      // Test lowercase requirement
      const noLowercase = { ...baseUserData, password: 'PASSWORD123!' };
      const lowercaseErrors = validateUserData(noLowercase, { isRegistration: true });
      expect(lowercaseErrors).toContain('Password must contain at least one lowercase letter');
      
      // Test number requirement
      const noNumber = { ...baseUserData, password: 'PasswordABC!' };
      const numberErrors = validateUserData(noNumber, { isRegistration: true });
      expect(numberErrors).toContain('Password must contain at least one number');
      
      // Test special character requirement
      const noSpecial = { ...baseUserData, password: 'Password123' };
      const specialErrors = validateUserData(noSpecial, { isRegistration: true });
      expect(specialErrors).toContain('Password must contain at least one special character');
    });

    test('should validate password confirmation', () => {
      const userData = {
        username: 'newuser',
        email: 'new@example.com',
        password: 'Password123!',
        confirmPassword: 'DifferentPassword123!'
      };

      const errors = validateUserData(userData, { isRegistration: true });
      
      expect(errors).toContain('Password and confirmation do not match');
    });
  });

  describe('Email validation', () => {
    test('should validate email format', () => {
      // Invalid email in registration
      const userData = {
        username: 'newuser',
        email: 'invalid-email',
        password: 'Password123!',
        confirmPassword: 'Password123!'
      };

      const errors = validateUserData(userData, { isRegistration: true });
      expect(errors).toContain('Email format is invalid');
      
      // Invalid email in profile
      const profileData = {
        email: 'invalid-email'
      };
      
      const profileErrors = validateUserData(profileData);
      expect(profileErrors).toContain('Email format is invalid');
    });

    test('should detect existing email', () => {
      const userData = {
        username: 'newuser',
        email: 'existing@example.com',
        password: 'Password123!',
        confirmPassword: 'Password123!'
      };

      const errors = validateUserData(userData, { 
        isRegistration: true, 
        checkExisting: userStore 
      });
      
      expect(errors).toContain('Email is already registered');
    });
  });

  describe('Profile update validation', () => {
    test('should validate empty fields in profile update', () => {
      const userData = {
        firstName: '',
        lastName: '',
        dateOfBirth: '',
        address: ''
      };

      const errors = validateUserData(userData);
      
      expect(errors).toContain('firstName cannot be empty if provided');
      expect(errors).toContain('lastName cannot be empty if provided');
      expect(errors).toContain('dateOfBirth cannot be empty if provided');
      expect(errors).toContain('address cannot be empty if provided');
    });
  });

  describe('Date of birth validation', () => {
    test('should validate date format', () => {
      const userData = {
        dateOfBirth: 'not-a-date'
      };

      const errors = validateUserData(userData);
      
      expect(errors).toContain('Date of birth is not a valid date');
    });

    test('should validate date constraints', () => {
      const now = new Date();
      
      // Future date
      const futureDate = new Date(now);
      futureDate.setFullYear(futureDate.getFullYear() + 1);
      
      const futureDateData = {
        dateOfBirth: futureDate.toISOString()
      };
      
      const futureErrors = validateUserData(futureDateData);
      expect(futureErrors).toContain('Date of birth cannot be in the future');
      
      // Too young
      const tooYoungDate = new Date(now);
      tooYoungDate.setFullYear(tooYoungDate.getFullYear() - 10);
      
      const tooYoungData = {
        dateOfBirth: tooYoungDate.toISOString()
      };
      
      const youngErrors = validateUserData(tooYoungData);
      expect(youngErrors).toContain('You must be at least 13 years old');
      
      // Too old
      const tooOldDate = new Date(now);
      tooOldDate.setFullYear(tooOldDate.getFullYear() - 150);
      
      const tooOldData = {
        dateOfBirth: tooOldDate.toISOString()
      };
      
      const oldErrors = validateUserData(tooOldData);
      expect(oldErrors).toContain('Invalid date of birth (age > 120 years)');
    });
  });

  describe('Custom validations', () => {
    test('should run custom validations', () => {
      const userData = {
        nickname: 'test'
      };

      const options = {
        customValidations: [
          {
            field: 'nickname',
            validator: (value) => value.length > 5,
            message: 'Nickname must be longer than 5 characters'
          }
        ]
      };

      const errors = validateUserData(userData, options);
      
      expect(errors).toContain('Nickname must be longer than 5 characters');
    });
  });

  test('should return empty array for valid registration data', () => {
    const userData = {
      username: 'validuser',
      email: 'valid@example.com',
      password: 'Password123!',
      confirmPassword: 'Password123!'
    };

    const errors = validateUserData(userData, { isRegistration: true });
    
    expect(errors).toEqual([]);
  });

  test('should return empty array for valid profile data', () => {
    const userData = {
      firstName: 'John',
      lastName: 'Doe',
      dateOfBirth: '1990-01-01',
      address: {
        street: '123 Main St',
        city: 'Anytown',
        zip: '12345',
        country: 'US'
      },
      phone: '(555) 123-4567'
    };

    const errors = validateUserData(userData);
    
    expect(errors).toEqual([]);
  });
});