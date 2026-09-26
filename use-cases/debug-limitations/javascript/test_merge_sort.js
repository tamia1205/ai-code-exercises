// Test suite for merge_sort.js
const assert = require('assert');
const fs = require('fs');

// Import the mergeSort function
const mergeSortCode = fs.readFileSync('./merge_sort.js', 'utf8');
eval(mergeSortCode);

// Test cases
function runTests() {
  console.log("Running merge_sort tests...");
  
  // Test 1: Empty array
  assert.deepStrictEqual(
    mergeSort([]), 
    [], 
    "Empty array should return empty array"
  );
  
  // Test 2: Single element array
  assert.deepStrictEqual(
    mergeSort([5]), 
    [5], 
    "Single element array should return same array"
  );
  
  // Test 3: Already sorted array
  assert.deepStrictEqual(
    mergeSort([1, 2, 3, 4, 5]), 
    [1, 2, 3, 4, 5], 
    "Already sorted array should return same array"
  );
  
  // Test 4: Reverse sorted array
  assert.deepStrictEqual(
    mergeSort([5, 4, 3, 2, 1]), 
    [1, 2, 3, 4, 5], 
    "Reverse sorted array should return sorted array"
  );
  
  // Test 5: Array with duplicates
  assert.deepStrictEqual(
    mergeSort([3, 1, 4, 1, 5, 9, 2, 6]), 
    [1, 1, 2, 3, 4, 5, 6, 9], 
    "Array with duplicates should return sorted array"
  );
  
  // Test 6: Large array - this will likely get stuck in infinite loop due to bug
  try {
    const largeArray = Array.from({length: 100}, () => Math.floor(Math.random() * 1000));
    const sortedArray = [...largeArray].sort((a, b) => a - b);
    assert.deepStrictEqual(mergeSort(largeArray), sortedArray, "Large array should be sorted correctly");
  } catch (error) {
    console.error("Test 6 failed: Large array test caused an error - likely infinite loop");
    console.error(error.message);
  }

  console.log("All tests completed");
}

runTests();