// Buggy sorting function
function mergeSort(arr) {
    if (arr.length <= 1) return arr;

    const mid = Math.floor(arr.length / 2);
    const left = mergeSort(arr.slice(0, mid));
    const right = mergeSort(arr.slice(mid));

    return merge(left, right);
}

function merge(left, right) {
    let result = [];
    let i = 0;
    let j = 0;

    while (i < left.length && j < right.length) {
        if (left[i] < right[j]) {
            result.push(left[i]);
            i++;
        } else {
            result.push(right[j]);
            j++;
        }
    }

    // Bug: Only one of these loops will execute
    while (i < left.length) {
        result.push(left[i]);
        j++; // Bug: incrementing j instead of i
    }

    while (j < right.length) {
        result.push(right[j]);
        j++;
    }

    return result;
}

// Export functions for testing
module.exports = { mergeSort };
