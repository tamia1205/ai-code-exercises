package com.example.crm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Crm {
    private static final Logger logger = LogManager.getLogger(Crm.class);
    private final CustomerRepository customerRepository = new CustomerRepository();

    /**
     * Processes raw customer data, validates it, transforms it, and loads it into the database.
     * Handles different data sources, performs deduplication, and tracks processing errors.
     */
    public Map<String, Object> processCustomerData(List<Map<String, Object>> rawData,
                                                   String source,
                                                   CustomerProcessingOptions options) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> validRecords = new ArrayList<>();
        List<Map<String, Object>> invalidRecords = new ArrayList<>();
        List<Map<String, Object>> duplicateRecords = new ArrayList<>();
        List<Map<String, Object>> processedRecords = new ArrayList<>();
        Set<String> existingEmails = new HashSet<>();
        Set<String> existingPhones = new HashSet<>();
        Map<String, Integer> errorCounts = new HashMap<>();
        int totalProcessed = 0;
        int totalSuccess = 0;
        int totalSkipped = 0;
        int totalErrors = 0;

        if (rawData == null || rawData.isEmpty()) {
            result.put("status", "error");
            result.put("message", "No data provided for processing");
            return result;
        }

        logger.info("Starting to process {} customer records from source: {}",
                rawData.size(), source);

        // Load existing records for deduplication if needed
        if (options.isPerformDeduplication()) {
            try {
                // Query existing customers for deduplication
                List<Customer> existingCustomers = customerRepository.findAll();
                for (Customer customer : existingCustomers) {
                    if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                        existingEmails.add(customer.getEmail().toLowerCase());
                    }
                    if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().isEmpty()) {
                        existingPhones.add(normalizePhoneNumber(customer.getPhoneNumber()));
                    }
                }
                logger.info("Loaded {} existing customers for deduplication", existingCustomers.size());
            } catch (Exception e) {
                logger.error("Error loading existing customers for deduplication: {}", e.getMessage());
                result.put("status", "error");
                result.put("message", "Failed to load existing customers for deduplication");
                result.put("error", e.getMessage());
                return result;
            }
        }

        // Process each record
        for (Map<String, Object> record : rawData) {
            totalProcessed++;
            Map<String, Object> processedRecord = new HashMap<>(record);
            List<String> recordErrors = new ArrayList<>();
            boolean isValid = true;

            // Skip processing if max error threshold has been reached
            if (options.getMaxErrorCount() > 0 && totalErrors >= options.getMaxErrorCount()) {
                logger.warn("Maximum error threshold reached ({}). Skipping remaining records.",
                        options.getMaxErrorCount());
                totalSkipped = rawData.size() - totalProcessed + 1;
                break;
            }

            try {
                // Source-specific preprocessing
                if (source.equals("csv")) {
                    // Handle CSV-specific formatting
                    processedRecord = preprocessCsvRecord(processedRecord);
                } else if (source.equals("api")) {
                    // Handle API-specific formatting
                    processedRecord = preprocessApiRecord(processedRecord);
                } else if (source.equals("manual")) {
                    // Handle manually entered data formatting
                    processedRecord = preprocessManualRecord(processedRecord);
                }

                // Validate required fields
                if (!processedRecord.containsKey("email") ||
                        processedRecord.get("email") == null ||
                        processedRecord.get("email").toString().trim().isEmpty()) {
                    recordErrors.add("Missing required field: email");
                    isValid = false;
                } else {
                    String email = processedRecord.get("email").toString().trim().toLowerCase();
                    processedRecord.put("email", email);

                    // Validate email format
                    if (!isValidEmail(email)) {
                        recordErrors.add("Invalid email format: " + email);
                        isValid = false;
                    }

                    // Check for duplicates in current batch
                    if (options.isPerformDeduplication()) {
                        boolean isDuplicate = false;
                        for (Map<String, Object> validRecord : validRecords) {
                            if (email.equals(validRecord.get("email").toString().toLowerCase())) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        // Check against existing records in database
                        if (!isDuplicate && existingEmails.contains(email)) {
                            isDuplicate = true;
                        }

                        if (isDuplicate) {
                            recordErrors.add("Duplicate email: " + email);
                            if (options.isDuplicatesAreErrors()) {
                                isValid = false;
                            } else {
                                duplicateRecords.add(processedRecord);
                                totalSkipped++;
                                continue;
                            }
                        }
                    }
                }

                // Validate and process name fields
                if (processedRecord.containsKey("firstName") && processedRecord.get("firstName") != null) {
                    String firstName = processedRecord.get("firstName").toString().trim();
                    // Capitalize first letter
                    if (!firstName.isEmpty()) {
                        firstName = firstName.substring(0, 1).toUpperCase() +
                                (firstName.length() > 1 ? firstName.substring(1) : "");
                    }
                    processedRecord.put("firstName", firstName);
                } else {
                    // First name is required
                    recordErrors.add("Missing required field: firstName");
                    isValid = false;
                }

                if (processedRecord.containsKey("lastName") && processedRecord.get("lastName") != null) {
                    String lastName = processedRecord.get("lastName").toString().trim();
                    // Capitalize first letter
                    if (!lastName.isEmpty()) {
                        lastName = lastName.substring(0, 1).toUpperCase() +
                                (lastName.length() > 1 ? lastName.substring(1) : "");
                    }
                    processedRecord.put("lastName", lastName);
                } else {
                    // Last name is required
                    recordErrors.add("Missing required field: lastName");
                    isValid = false;
                }

                // Process and validate phone number if present
                if (processedRecord.containsKey("phone") &&
                        processedRecord.get("phone") != null &&
                        !processedRecord.get("phone").toString().trim().isEmpty()) {

                    String phone = processedRecord.get("phone").toString().trim();
                    String normalizedPhone = normalizePhoneNumber(phone);

                    if (!isValidPhoneNumber(normalizedPhone)) {
                        recordErrors.add("Invalid phone number format: " + phone);
                        isValid = false;
                    } else {
                        processedRecord.put("phone", formatPhoneNumber(normalizedPhone));

                        // Check for duplicate phone numbers if deduplication is enabled
                        if (options.isPerformDeduplication()) {
                            boolean isDuplicate = false;
                            for (Map<String, Object> validRecord : validRecords) {
                                if (validRecord.containsKey("phone") &&
                                        normalizedPhone.equals(
                                                normalizePhoneNumber(validRecord.get("phone").toString()))) {
                                    isDuplicate = true;
                                    break;
                                }
                            }

                            // Check against existing records in database
                            if (!isDuplicate && existingPhones.contains(normalizedPhone)) {
                                isDuplicate = true;
                            }

                            if (isDuplicate) {
                                recordErrors.add("Duplicate phone number: " + phone);
                                if (options.isDuplicatesAreErrors()) {
                                    isValid = false;
                                }
                            }
                        }
                    }
                }

                // Process address fields if present
                if (processedRecord.containsKey("address") &&
                        processedRecord.get("address") != null) {

                    Map<String, Object> addressData;

                    // Handle different address formats
                    if (processedRecord.get("address") instanceof Map) {
                        addressData = (Map<String, Object>) processedRecord.get("address");
                    } else if (processedRecord.get("address") instanceof String) {
                        // Parse address string into components
                        addressData = parseAddressString(processedRecord.get("address").toString());
                    } else {
                        recordErrors.add("Invalid address format");
                        isValid = false;
                        addressData = new HashMap<>();
                    }

                    // Validate and normalize address components
                    Map<String, Object> normalizedAddress = new HashMap<>();

                    if (addressData.containsKey("street") && addressData.get("street") != null) {
                        normalizedAddress.put("street", addressData.get("street").toString().trim());
                    }

                    if (addressData.containsKey("city") && addressData.get("city") != null) {
                        String city = addressData.get("city").toString().trim();
                        // Capitalize each word in city name
                        normalizedAddress.put("city", capitalizeWords(city));
                    }

                    if (addressData.containsKey("state") && addressData.get("state") != null) {
                        String state = addressData.get("state").toString().trim().toUpperCase();

                        // Validate state/province code for US and Canada
                        if (addressData.containsKey("country") &&
                                (addressData.get("country").toString().equals("US") ||
                                        addressData.get("country").toString().equals("CA"))) {

                            if (!isValidStateOrProvince(state, addressData.get("country").toString())) {
                                recordErrors.add("Invalid state/province: " + state);
                                isValid = false;
                            }
                        }

                        normalizedAddress.put("state", state);
                    }

                    if (addressData.containsKey("zip") && addressData.get("zip") != null) {
                        String zip = addressData.get("zip").toString().trim();
                        normalizedAddress.put("zip", zip);

                        // Validate postal code format if country is provided
                        if (addressData.containsKey("country")) {
                            String country = addressData.get("country").toString();
                            if (!isValidPostalCode(zip, country)) {
                                recordErrors.add("Invalid postal code format for " + country + ": " + zip);
                                isValid = false;
                            }
                        }
                    }

                    if (addressData.containsKey("country") && addressData.get("country") != null) {
                        String country = addressData.get("country").toString().trim();

                        // Convert country codes to full names if needed
                        if (country.length() <= 3) {
                            String fullCountryName = getCountryNameFromCode(country);
                            if (fullCountryName != null) {
                                country = fullCountryName;
                            } else {
                                recordErrors.add("Invalid country code: " + country);
                                isValid = false;
                            }
                        }

                        normalizedAddress.put("country", country);
                    }

                    processedRecord.put("address", normalizedAddress);
                }

                // Process date fields
                if (processedRecord.containsKey("dateOfBirth") &&
                        processedRecord.get("dateOfBirth") != null &&
                        !processedRecord.get("dateOfBirth").toString().trim().isEmpty()) {

                    String dobString = processedRecord.get("dateOfBirth").toString().trim();
                    try {
                        Date dob = parseDate(dobString);
                        // Validate age range
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dob);
                        int birthYear = cal.get(Calendar.YEAR);
                        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

                        if (currentYear - birthYear > 120 || currentYear - birthYear < 18) {
                            recordErrors.add("Invalid date of birth (age must be between 18 and 120): " + dobString);
                            isValid = false;
                        } else {
                            // Format date consistently
                            processedRecord.put("dateOfBirth", formatDate(dob));
                        }
                    } catch (ParseException e) {
                        recordErrors.add("Invalid date format for date of birth: " + dobString);
                        isValid = false;
                    }
                }

                // Custom validation if provided
                if (options.getCustomValidator() != null) {
                    List<String> customErrors = options.getCustomValidator().validate(processedRecord);
                    if (customErrors != null && !customErrors.isEmpty()) {
                        recordErrors.addAll(customErrors);
                        isValid = false;
                    }
                }

                // Final decision on record validity
                if (isValid) {
                    validRecords.add(processedRecord);
                    totalSuccess++;
                } else {
                    // Add errors to record
                    processedRecord.put("errors", recordErrors);
                    invalidRecords.add(processedRecord);
                    totalErrors++;

                    // Track error types
                    for (String error : recordErrors) {
                        String errorType = error.split(":")[0].trim();
                        errorCounts.put(errorType, errorCounts.getOrDefault(errorType, 0) + 1);
                    }
                }

            } catch (Exception e) {
                logger.error("Unexpected error processing record {}: {}", totalProcessed, e.getMessage());
                e.printStackTrace();

                // Add to invalid records with error info
                processedRecord.put("errors", Collections.singletonList("Processing error: " + e.getMessage()));
                invalidRecords.add(processedRecord);
                totalErrors++;

                // Track error
                String errorType = "Processing error";
                errorCounts.put(errorType, errorCounts.getOrDefault(errorType, 0) + 1);
            }
        }

        // Save valid records to database if requested
        if (options.isSaveToDatabase() && !validRecords.isEmpty()) {
            try {
                List<Customer> customers = new ArrayList<>();
                for (Map<String, Object> record : validRecords) {
                    Customer customer = mapToCustomerEntity(record);
                    customer.setDataSource(source);
                    customer.setCreatedAt(new Date());
                    customers.add(customer);
                }

                customerRepository.saveAll(customers);
                logger.info("Successfully saved {} customer records to database", customers.size());

                // Mark records as processed
                processedRecords.addAll(validRecords);

            } catch (Exception e) {
                logger.error("Error saving records to database: {}", e.getMessage());
                result.put("status", "error");
                result.put("message", "Failed to save valid records to database");
                result.put("error", e.getMessage());
                return result;
            }
        } else {
            // Just mark as processed without saving
            processedRecords.addAll(validRecords);
        }

        // Generate processing report
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        result.put("status", "success");
        result.put("source", source);
        result.put("totalRecords", rawData.size());
        result.put("processedCount", totalProcessed);
        result.put("successCount", totalSuccess);
        result.put("errorCount", totalErrors);
        result.put("skippedCount", totalSkipped);
        result.put("duplicateCount", duplicateRecords.size());
        result.put("processingTimeMs", processingTime);
        result.put("errorsByType", errorCounts);

        if (options.isIncludeRecordsInResponse()) {
            if (options.isIncludeValidRecords()) {
                result.put("validRecords", validRecords);
            }
            if (options.isIncludeInvalidRecords()) {
                result.put("invalidRecords", invalidRecords);
            }
            if (options.isIncludeDuplicateRecords()) {
                result.put("duplicateRecords", duplicateRecords);
            }
        }

        // Log completion
        logger.info("Customer data processing completed. Total: {}, Success: {}, Error: {}, Skipped: {}, Time: {} ms",
                rawData.size(), totalSuccess, totalErrors, totalSkipped, processingTime);

        return result;
    }

    // Helper methods (not fully implemented)
    private Map<String, Object> preprocessCsvRecord(Map<String, Object> record) {
// Handle CSV-specific preprocessing
        return record;
    }

    private Map<String, Object> preprocessApiRecord(Map<String, Object> record) {
// Handle API-specific preprocessing
        return record;
    }

    private Map<String, Object> preprocessManualRecord(Map<String, Object> record) {
// Handle manually entered data formatting
        return record;
    }

    private boolean isValidEmail(String email) {
// Validate email format
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    private String normalizePhoneNumber(String phone) {
// Remove non-digit characters
        return phone.replaceAll("[^0-9+]", "");
    }

    private boolean isValidPhoneNumber(String phone) {
// Basic phone validation
        return phone.matches("^\\+?[0-9]{10,15}$");
    }

    private String formatPhoneNumber(String phone) {
// Format phone number consistently
        return phone;
    }

    private Map<String, Object> parseAddressString(String addressString) {
// Parse address string into components
        Map<String, Object> addressComponents = new HashMap<>();
// Implementation omitted
        return addressComponents;
    }

    private String capitalizeWords(String text) {
// Capitalize first letter of each word
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        String[] words = text.split("\\s");

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    private boolean isValidStateOrProvince(String code, String country) {
        return true; // Implementation omitted
    }

    private boolean isValidPostalCode(String postalCode, String country) {
        return true; // Implementation omitted
    }

    private String getCountryNameFromCode(String countryCode) {
        return countryCode; // Implementation omitted
    }

    private Date parseDate(String dateString) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private Customer mapToCustomerEntity(Map<String, Object> record) {
// Map record to Customer entity
        Customer customer = new Customer("Test", "Test@test.com", "Test");
// Implementation omitted
        return customer;
    }
}
