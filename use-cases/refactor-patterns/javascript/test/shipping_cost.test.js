const { calculateShippingCost } = require('../src/shipping_cost');

// Test cases for shipping cost calculator
describe('calculateShippingCost', () => {
  // Test standard shipping calculations
  test('standard shipping to USA', () => {
    const package = { weight: 5, length: 10, width: 10, height: 10 };
    expect(calculateShippingCost(package, 'USA', 'standard')).toBe('12.50');
  });

  test('standard shipping to Canada', () => {
    const package = { weight: 4, length: 8, width: 8, height: 8 };
    expect(calculateShippingCost(package, 'Canada', 'standard')).toBe('14.00');
  });

  test('standard shipping with dimensional weight surcharge', () => {
    const package = { weight: 1, length: 20, width: 20, height: 20 };
    // Base: 1 * 2.5 = 2.5
    // Surcharge: 5.0 (because weight < 2 and volume 8000 > 1000)
    // Total: 7.5
    expect(calculateShippingCost(package, 'USA', 'standard')).toBe('7.50');
  });

  // Test express shipping calculations
  test('express shipping to USA', () => {
    const package = { weight: 3, length: 10, width: 10, height: 10 };
    expect(calculateShippingCost(package, 'USA', 'express')).toBe('13.50');
  });

  test('express shipping with large package surcharge', () => {
    const package = { weight: 2, length: 30, width: 30, height: 30 };
    // Base: 2 * 4.5 = 9
    // Surcharge: 15 (because volume 27000 > 5000)
    // Total: 24
    expect(calculateShippingCost(package, 'USA', 'express')).toBe('24.00');
  });

  // Test overnight shipping calculations
  test('overnight shipping to USA', () => {
    const package = { weight: 2, length: 5, width: 5, height: 5 };
    expect(calculateShippingCost(package, 'USA', 'overnight')).toBe('19.00');
  });

  test('overnight shipping to Canada', () => {
    const package = { weight: 1, length: 5, width: 5, height: 5 };
    expect(calculateShippingCost(package, 'Canada', 'overnight')).toBe('12.50');
  });

  test('overnight shipping to unsupported country', () => {
    const package = { weight: 1, length: 5, width: 5, height: 5 };
    expect(calculateShippingCost(package, 'Mexico', 'overnight')).toBe(
      'Overnight shipping not available for this destination'
    );
  });
});