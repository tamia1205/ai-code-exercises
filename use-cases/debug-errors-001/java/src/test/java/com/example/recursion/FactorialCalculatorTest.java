package com.example.recursion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the FactorialCalculator.
 */
class FactorialCalculatorTest {

    /**
     * Test that factorial of 0 is 1.
     * Note: This test will fail with the buggy implementation.
     */
    @Test
    void factorialOfZeroShouldBeOne() {
        try {
            assertEquals(1, FactorialCalculator.calculateFactorial(0));
        } catch (StackOverflowError e) {
            fail("Factorial of 0 caused stack overflow - missing base case?");
        }
    }

    /**
     * Test that factorial of 1 is 1.
     * Note: This test will fail with the buggy implementation.
     */
    @Test
    void factorialOfOneShouldBeOne() {
        try {
            assertEquals(1, FactorialCalculator.calculateFactorial(1));
        } catch (StackOverflowError e) {
            fail("Factorial of 1 caused stack overflow - missing base case?");
        }
    }

    /**
     * Test factorial of 5 is 120.
     * Note: This test will fail with the buggy implementation.
     */
    @Test
    void factorialOfFiveShouldBe120() {
        try {
            assertEquals(120, FactorialCalculator.calculateFactorial(5));
        } catch (StackOverflowError e) {
            fail("Factorial of 5 caused stack overflow - missing base case?");
        }
    }

    /**
     * Test factorial of 10 is 3628800.
     * Note: This test will fail with the buggy implementation.
     */
    @Test
    void factorialOfTenShouldBe3628800() {
        try {
            assertEquals(3628800, FactorialCalculator.calculateFactorial(10));
        } catch (StackOverflowError e) {
            fail("Factorial of 10 caused stack overflow - missing base case?");
        }
    }

    /**
     * Test that negative input throws IllegalArgumentException.
     * Note: This test will fail with the buggy implementation.
     */
    @Test
    void factorialOfNegativeNumberShouldThrowException() {
        try {
            assertThrows(IllegalArgumentException.class, () -> {
                FactorialCalculator.calculateFactorial(-1);
            });
        } catch (StackOverflowError e) {
            fail("Factorial of -1 caused stack overflow - missing input validation?");
        }
    }
}