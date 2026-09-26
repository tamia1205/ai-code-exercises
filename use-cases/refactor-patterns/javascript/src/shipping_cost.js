// Shipping cost calculator with complex pricing rules
function calculateShippingCost(packageDetails, destinationCountry, shippingMethod) {
  const { weight, length, width, height } = packageDetails;
  let cost = 0;

  // Calculate base cost by shipping method
  if (shippingMethod === 'standard') {
    // Standard shipping calculations
    if (destinationCountry === 'USA') {
      cost = weight * 2.5;
    } else if (destinationCountry === 'Canada') {
      cost = weight * 3.5;
    } else if (destinationCountry === 'Mexico') {
      cost = weight * 4.0;
    } else {
      // International shipping
      cost = weight * 4.5;
    }

    // Add dimensional weight adjustment for standard shipping
    if (weight < 2 && (length * width * height) > 1000) {
      cost += 5.0; // Dimensional weight surcharge
    }
  } else if (shippingMethod === 'express') {
    // Express shipping calculations
    if (destinationCountry === 'USA') {
      cost = weight * 4.5;
    } else if (destinationCountry === 'Canada') {
      cost = weight * 5.5;
    } else if (destinationCountry === 'Mexico') {
      cost = weight * 6.0;
    } else {
      // International shipping
      cost = weight * 7.5;
    }

    // Express has different dimensional weight rules
    if ((length * width * height) > 5000) {
      cost += 15.0; // Large package surcharge
    }
  } else if (shippingMethod === 'overnight') {
    // Overnight shipping calculations
    if (destinationCountry === 'USA') {
      cost = weight * 9.5;
    } else if (destinationCountry === 'Canada') {
      cost = weight * 12.5;
    } else {
      // Overnight not available for other countries
      return "Overnight shipping not available for this destination";
    }
  }

  return cost.toFixed(2);
}

// Example usage
const package = { weight: 5, length: 10, width: 10, height: 10 };
console.log(calculateShippingCost(package, 'USA', 'standard'));  // "12.50"
console.log(calculateShippingCost(package, 'Canada', 'express')); // "27.50"

module.exports = { calculateShippingCost };