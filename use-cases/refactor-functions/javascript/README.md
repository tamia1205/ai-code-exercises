# User Validator

A JavaScript utility for validating user registration and profile data.

## Overview

This project contains a function for validating user input data with support for:
- User registration validation
- Profile update validation
- Email format validation
- Password strength requirements
- Date of birth validation
- Address validation
- Phone number validation
- Custom field validations

## Installation

1. Clone this repository
2. Install dependencies:

```bash
npm install
```

## Running Tests

The project uses Jest for testing. To run the tests:

```bash
npm test
```

To run tests in watch mode (tests automatically re-run when files change):

```bash
npm run test:watch
```

## Project Structure

- `user_validator.js` - Main validation function
- `user_store.js` - Mock class for checking existing usernames and emails
- `user_validator.test.js` - Unit tests for the validator function

## Usage Example

```javascript
import validateUserData from './user_validator.js';
import UserStore from './user_store.js';

const userStore = new UserStore();

// Validate registration data
const registrationData = {
  username: 'newuser',
  email: 'user@example.com',
  password: 'Password123!',
  confirmPassword: 'Password123!'
};

const registrationErrors = validateUserData(registrationData, { 
  isRegistration: true,
  checkExisting: userStore
});

// Validate profile update
const profileData = {
  firstName: 'John',
  lastName: 'Doe',
  dateOfBirth: '1990-01-01',
  address: {
    street: '123 Main St',
    city: 'Anytown',
    zip: '12345',
    country: 'US'
  },
  phone: '+1 (555) 123-4567'
};

const profileErrors = validateUserData(profileData);
```