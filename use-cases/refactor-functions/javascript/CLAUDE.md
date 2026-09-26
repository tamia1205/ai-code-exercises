# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

This repository contains exercises for refactoring functions. The JavaScript implementation features a complex user validation function that handles different types of validations for user registration and profile updates.

## Code Structure

The main file is `user_validator.js`, which contains a single function `validateUserData()` that:
- Validates user input for registration or profile updates
- Performs various validations based on operation type:
  - For registration: username, email, password requirements
  - For profile updates: profile fields validation
- Handles email, date of birth, address, and phone validations
- Supports custom validation through options

## Refactoring Guidelines

When refactoring this code:
1. Split the large validation function into smaller, focused functions
2. Maintain the same validation rules and behavior
3. Improve readability and maintainability
4. Ensure the function signatures remain compatible with existing usage
5. Consider organizing related validations into logical groups

## Testing

The repository doesn't include explicit test files yet. When refactoring, manual verification can be done by checking that the refactored code returns the same errors for the same inputs as the original function.