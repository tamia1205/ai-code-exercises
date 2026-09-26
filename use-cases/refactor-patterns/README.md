# Design Pattern Implementation Challenge

## Overview

In this exercise, you'll practice identifying opportunities to apply design patterns in code and implement refactored solutions. The exercise will help you understand when and how to apply common design patterns to improve code maintainability, flexibility, and extensibility.

## Available Pattern Exercises

This directory contains four pattern implementation exercises in different languages:

1. **Strategy Pattern (JavaScript)**: A shipping cost calculator with conditional logic that could benefit from the Strategy pattern
2. **Factory Pattern (Python)**: A database connection system with complex initialization that could use the Factory pattern
3. **Observer Pattern (Java)**: A weather monitoring system where multiple displays need to be updated when data changes
4. **Adapter Pattern (TypeScript)**: A payment processing system that integrates with incompatible external APIs

## Exercise Instructions

For each pattern exercise:

1. Review the existing code to understand its functionality
2. Identify how the chosen design pattern could improve the code
3. Refactor the code to implement the design pattern
4. Run the tests to ensure your refactored code preserves the original behavior
5. Document the benefits gained from implementing the pattern

## Prerequisites

Each implementation has its own specific requirements:

- **JavaScript**: Node.js with Jest for testing
- **Python**: Python 3 with unittest
- **Java**: JDK 11+ with JUnit 5
- **TypeScript**: TypeScript with Jest for testing

## Design Patterns Overview

### Strategy Pattern
Use when you have multiple algorithms that can be interchanged at runtime.
- **Problem**: Code with many conditional statements selecting between different behaviors
- **Solution**: Extract behaviors into separate strategy classes with a common interface

### Factory Pattern
Use when you need to create objects without exposing creation logic.
- **Problem**: Complex object creation with many steps or dependencies
- **Solution**: Create a factory class that handles the creation details

### Observer Pattern
Use when changes to one object require updates in other objects.
- **Problem**: One object needs to notify multiple other objects about changes
- **Solution**: Define a one-to-many relationship where observers are notified of state changes

### Adapter Pattern
Use when interfaces are incompatible but need to work together.
- **Problem**: Classes with incompatible interfaces need to collaborate
- **Solution**: Create an adapter that converts one interface to another