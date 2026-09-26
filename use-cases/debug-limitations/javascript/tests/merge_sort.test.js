// Test suite for merge_sort.js using Jest
const fs = require('fs');
const { mergeSort } = require('../merge_sort');


describe('Merge Sort Tests', () => {
  // Test 1: Empty array
  test('Empty array should return empty array', () => {
    expect(mergeSort([])).toEqual([]);
  });
  
  // Test 2: Single element array
  test('Single element array should return same array', () => {
    expect(mergeSort([5])).toEqual([5]);
  });
  
  // Test 3: Already sorted array
  test('Already sorted array should return same array', () => {
    expect(mergeSort([1, 2, 3, 4, 5])).toEqual([1, 2, 3, 4, 5]);
  });
  
  // Test 4: Reverse sorted array
  test('Reverse sorted array should return sorted array', () => {
    expect(mergeSort([5, 4, 3, 2, 1])).toEqual([1, 2, 3, 4, 5]);
  });
  
  // Test 5: Array with duplicates
  test('Array with duplicates should return sorted array', () => {
    expect(mergeSort([3, 1, 4, 1, 5, 9, 2, 6])).toEqual([1, 1, 2, 3, 4, 5, 6, 9]);
  });
  
  // Test 6: Large array - this will likely time out due to the bug
  test('Large array should be sorted correctly', () => {
    jest.setTimeout(5000); // 5 second timeout
    
    try {
      const largeArray = Array.from({length: 100}, () => Math.floor(Math.random() * 1000));
      const sortedArray = [...largeArray].sort((a, b) => a - b);
      expect(mergeSort(largeArray)).toEqual(sortedArray);
    } catch (error) {
      // This test is expected to fail or timeout due to the bug
      throw error;
    }
  });
});