/**
 * Validates user input data for user registration and profile updates.
 * Returns an array of validation errors if any are found.
 */
function validateUserData(userData, options = {}) {
    const errors = [];
    const isRegistration = options.isRegistration || false;
    const requiredForRegistration = ['username', 'email', 'password', 'confirmPassword'];
    const requiredForProfile = ['firstName', 'lastName', 'dateOfBirth', 'address'];

    // Check required fields based on operation type
    if (isRegistration) {
        for (const field of requiredForRegistration) {
            if (!userData[field] || userData[field].trim() === '') {
                errors.push(`${field} is required for registration`);
            }
        }

        // Additional username validation
        if (userData.username) {
            if (userData.username.length < 3) {
                errors.push('Username must be at least 3 characters long');
            } else if (userData.username.length > 20) {
                errors.push('Username must be at most 20 characters long');
            } else if (!/^[a-zA-Z0-9_]+$/.test(userData.username)) {
                errors.push('Username can only contain letters, numbers, and underscores');
            } else {
                // Check if username is taken
                if (options.checkExisting && options.checkExisting.usernameExists(userData.username)) {
                    errors.push('Username is already taken');
                }
            }
        }

        // Password validation
        if (userData.password) {
            if (userData.password.length < 8) {
                errors.push('Password must be at least 8 characters long');
            } else if (!/[A-Z]/.test(userData.password)) {
                errors.push('Password must contain at least one uppercase letter');
            } else if (!/[a-z]/.test(userData.password)) {
                errors.push('Password must contain at least one lowercase letter');
            } else if (!/[0-9]/.test(userData.password)) {
                errors.push('Password must contain at least one number');
            } else if (!/[^A-Za-z0-9]/.test(userData.password)) {
                errors.push('Password must contain at least one special character');
            }

            // Check password confirmation
            if (userData.confirmPassword !== userData.password) {
                errors.push('Password and confirmation do not match');
            }
        }
    } else {
        // Profile update validation
        for (const field of requiredForProfile) {
            if (userData[field] !== undefined && userData[field] === '') {
                errors.push(`${field} cannot be empty if provided`);
            }
        }
    }

    // Email validation (for both registration and profile update)
    if (userData.email !== undefined) {
        if (userData.email.trim() === '') {
            if (isRegistration) {
                errors.push('Email is required');
            }
        } else {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(userData.email)) {
                errors.push('Email format is invalid');
            } else {
                // Check if email is taken
                if (options.checkExisting && options.checkExisting.emailExists(userData.email)) {
                    errors.push('Email is already registered');
                }
            }
        }
    }

    // Date of birth validation
    if (userData.dateOfBirth !== undefined && userData.dateOfBirth !== '') {
        const dobDate = new Date(userData.dateOfBirth);

        if (isNaN(dobDate.getTime())) {
            errors.push('Date of birth is not a valid date');
        } else {
            const now = new Date();
            const minAgeDate = new Date(now.getFullYear() - 13, now.getMonth(), now.getDate());
            const maxAgeDate = new Date(now.getFullYear() - 120, now.getMonth(), now.getDate());

            if (dobDate > now) {
                errors.push('Date of birth cannot be in the future');
            } else if (dobDate > minAgeDate) {
                errors.push('You must be at least 13 years old');
            } else if (dobDate < maxAgeDate) {
                errors.push('Invalid date of birth (age > 120 years)');
            }
        }
    }

    // Address validation
    if (userData.address !== undefined && userData.address !== '') {
        if (typeof userData.address === 'object') {
            const requiredAddressFields = ['street', 'city', 'zip', 'country'];

            for (const field of requiredAddressFields) {
                if (!userData.address[field] || userData.address[field].trim() === '') {
                    errors.push(`Address ${field} is required`);
                }
            }

            // Zip code validation
            if (userData.address.zip && userData.address.country) {
                if (userData.address.country === 'US' && !/^\d{5}(-\d{4})?$/.test(userData.address.zip)) {
                    errors.push('Invalid US ZIP code format');
                } else if (userData.address.country === 'CA' && !/^[A-Za-z]\d[A-Za-z] \d[A-Za-z]\d$/.test(userData.address.zip)) {
                    errors.push('Invalid Canadian postal code format');
                } else if (userData.address.country === 'UK' && !/^[A-Z]{1,2}\d[A-Z\d]? \d[A-Z]{2}$/.test(userData.address.zip)) {
                    errors.push('Invalid UK postal code format');
                }
            }
        } else {
            errors.push('Address must be an object with required fields');
        }
    }

    // Phone validation
    if (userData.phone !== undefined && userData.phone !== '') {
        // Basic phone number validation - could be more sophisticated based on country
        if (!/^\+?[\d\s\-()]{10,15}$/.test(userData.phone)) {
            errors.push('Phone number format is invalid');
        }
    }

    // Custom field validation if provided
    if (options.customValidations) {
        for (const validation of options.customValidations) {
            const field = validation.field;

            if (userData[field] !== undefined) {
                const valid = validation.validator(userData[field], userData);

                if (!valid) {
                    errors.push(validation.message || `Invalid value for ${field}`);
                }
            }
        }
    }

    return errors;
}

// Export the function for testing
// export default validateUserData;
// Export functions for testing
module.exports = { validateUserData };
