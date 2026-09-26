// tests/task_parser.test.js
const { Task, TaskPriority } = require('../models');
const { parseTaskFromText, getNextWeekday } = require('../task_parser');

describe('Task Parser', () => {
  describe('parseTaskFromText', () => {
    test('should parse basic task title', () => {
      const text = 'Buy milk';
      const task = parseTaskFromText(text);
      
      expect(task).toBeInstanceOf(Task);
      expect(task.title).toBe('Buy milk');
      expect(task.priority).toBe(TaskPriority.MEDIUM); // Default priority
      expect(task.dueDate).toBeNull();
      expect(task.tags).toEqual([]);
    });

    test('should parse task with priority', () => {
      const tests = [
        { text: 'Buy milk !1', expected: TaskPriority.LOW },
        { text: 'Buy milk !low', expected: TaskPriority.LOW },
        { text: 'Buy milk !2', expected: TaskPriority.MEDIUM },
        { text: 'Buy milk !medium', expected: TaskPriority.MEDIUM },
        { text: 'Buy milk !3', expected: TaskPriority.HIGH },
        { text: 'Buy milk !high', expected: TaskPriority.HIGH },
        { text: 'Buy milk !4', expected: TaskPriority.URGENT },
        { text: 'Buy milk !urgent', expected: TaskPriority.URGENT }
      ];

      for (const { text, expected } of tests) {
        const task = parseTaskFromText(text);
        expect(task.title).toBe('Buy milk');
        expect(task.priority).toBe(expected);
      }
    });

    test('should parse task with tags', () => {
      const text = 'Buy milk @shopping @groceries';
      const task = parseTaskFromText(text);
      
      expect(task.title).toBe('Buy milk');
      expect(task.tags).toContain('shopping');
      expect(task.tags).toContain('groceries');
      expect(task.tags.length).toBe(2);
    });

    test('should parse task with date markers', () => {
      // Mock the current date to ensure consistent test results
      const mockDate = new Date('2023-06-15');
      jest.spyOn(global, 'Date').mockImplementation(() => mockDate);
      
      const tests = [
        { text: 'Buy milk #today', expected: new Date('2023-06-15') },
        { text: 'Buy milk #tomorrow', expected: new Date('2023-06-16') },
        { text: 'Buy milk #next_week', expected: new Date('2023-06-22') },
        { text: 'Buy milk #monday', expected: getNextWeekday(mockDate, 1) },
        { text: 'Buy milk #fri', expected: getNextWeekday(mockDate, 5) }
      ];

      for (const { text, expected } of tests) {
        const task = parseTaskFromText(text);
        expect(task.title).toBe('Buy milk');
        expect(task.dueDate).toEqual(expected);
      }
      
      // Restore the original Date implementation
      jest.restoreAllMocks();
    });

    test('should parse task with multiple properties', () => {
      const text = 'Finish report for client XYZ !urgent #friday #work @project @client';
      const task = parseTaskFromText(text);
      
      expect(task.title).toBe('Finish report for client XYZ');
      expect(task.priority).toBe(TaskPriority.URGENT);
      expect(task.tags).toContain('project');
      expect(task.tags).toContain('client');
      expect(task.tags.length).toBe(2);
      expect(task.dueDate).not.toBeNull(); // Exact date depends on when test is run
    });

    test('should handle whitespace correctly', () => {
      const text = '  Buy   milk   @shopping   !high  ';
      const task = parseTaskFromText(text);
      
      expect(task.title).toBe('Buy milk');
      expect(task.priority).toBe(TaskPriority.HIGH);
      expect(task.tags).toContain('shopping');
    });
  });

  describe('getNextWeekday', () => {
    test('should return the next occurrence of a weekday', () => {
      // Wednesday
      const currentDate = new Date('2023-06-14');
      
      // Test getting next Monday (should be 5 days later)
      const nextMonday = getNextWeekday(currentDate, 1);
      expect(nextMonday.getDay()).toBe(1); // Monday
      expect(nextMonday.getDate()).toBe(19); // 14 + 5 = 19
      
      // Test getting next Friday (should be 2 days later)
      const nextFriday = getNextWeekday(currentDate, 5);
      expect(nextFriday.getDay()).toBe(5); // Friday
      expect(nextFriday.getDate()).toBe(16); // 14 + 2 = 16
      
      // Test getting next Wednesday (should be 7 days later, not today)
      const nextWednesday = getNextWeekday(currentDate, 3);
      expect(nextWednesday.getDay()).toBe(3); // Wednesday
      expect(nextWednesday.getDate()).toBe(21); // 14 + 7 = 21
    });
  });
});