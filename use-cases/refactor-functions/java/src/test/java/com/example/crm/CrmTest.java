package com.example.crm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CrmTest {
    
    private Crm crm;
    
    @BeforeEach
    void setUp() {
        crm = new Crm();
    }
    
    @Test
    @DisplayName("Test processing valid customer data")
    void testProcessValidCustomerData() {
        // Arrange
        List<Map<String, Object>> rawData = new ArrayList<>();
        Map<String, Object> customer = new HashMap<>();
        customer.put("firstName", "John");
        customer.put("lastName", "Doe");
        customer.put("email", "john.doe@example.com");
        customer.put("phone", "1234567890");
        
        Map<String, Object> address = new HashMap<>();
        address.put("street", "123 Main St");
        address.put("city", "New York");
        address.put("state", "NY");
        address.put("zip", "10001");
        address.put("country", "US");
        customer.put("address", address);
        
        rawData.add(customer);
        
        CustomerProcessingOptions options = new CustomerProcessingOptions(
            true,  // performDeduplication
            3,     // maxErrorCount
            false, // duplicatesAreErrors
            new DefaultValidator(), // customValidator
            true,  // saveToDatabase
            true,  // includeRecordsInResponse
            true,  // includeValidRecords
            true,  // includeInvalidRecords
            true   // includeDuplicateRecords
        );
        
        // Act
        Map<String, Object> result = crm.processCustomerData(rawData, "manual", options);
        
        // Assert
        assertEquals("success", result.get("status"));
        assertEquals(1, result.get("totalRecords"));
        assertEquals(1, result.get("successCount"));
        assertEquals(0, result.get("errorCount"));
        
        // Check that valid records are included in the response
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> validRecords = (List<Map<String, Object>>) result.get("validRecords");
        assertNotNull(validRecords);
        assertEquals(1, validRecords.size());
        
        Map<String, Object> processedCustomer = validRecords.get(0);
        assertEquals("John", processedCustomer.get("firstName"));
        assertEquals("Doe", processedCustomer.get("lastName"));
        assertEquals("john.doe@example.com", processedCustomer.get("email"));
    }
    
    @Test
    @DisplayName("Test processing invalid customer data")
    void testProcessInvalidCustomerData() {
        // Arrange
        List<Map<String, Object>> rawData = new ArrayList<>();
        Map<String, Object> customer = new HashMap<>();
        customer.put("firstName", "Jane");
        // Missing lastName
        customer.put("email", "invalid-email"); // Invalid email format
        
        rawData.add(customer);
        
        CustomerProcessingOptions options = new CustomerProcessingOptions(
            true,  // performDeduplication
            3,     // maxErrorCount
            false, // duplicatesAreErrors
            new DefaultValidator(), // customValidator
            true,  // saveToDatabase
            true,  // includeRecordsInResponse
            true,  // includeValidRecords
            true,  // includeInvalidRecords
            true   // includeDuplicateRecords
        );
        
        // Act
        Map<String, Object> result = crm.processCustomerData(rawData, "manual", options);
        
        // Assert
        assertEquals("success", result.get("status"));
        assertEquals(1, result.get("totalRecords"));
        assertEquals(0, result.get("successCount"));
        assertEquals(1, result.get("errorCount"));
        
        // Check that invalid records are included in the response
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> invalidRecords = (List<Map<String, Object>>) result.get("invalidRecords");
        assertNotNull(invalidRecords);
        assertEquals(1, invalidRecords.size());
        
        Map<String, Object> invalidCustomer = invalidRecords.get(0);
        assertEquals("Jane", invalidCustomer.get("firstName"));
        
        // Check that errors are recorded
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) invalidCustomer.get("errors");
        assertNotNull(errors);
        assertTrue(errors.size() >= 2); // At least 2 errors: missing lastName and invalid email
        assertTrue(errors.stream().anyMatch(error -> error.contains("Missing required field: lastName")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("Invalid email format")));
    }
    
    @Test
    @DisplayName("Test deduplication functionality")
    void testDeduplication() {
        // Arrange
        List<Map<String, Object>> rawData = new ArrayList<>();
        
        // First customer
        Map<String, Object> customer1 = new HashMap<>();
        customer1.put("firstName", "John");
        customer1.put("lastName", "Doe");
        customer1.put("email", "john.doe@example.com");
        customer1.put("phone", "1234567890");
        rawData.add(customer1);
        
        // Second customer with same email (duplicate)
        Map<String, Object> customer2 = new HashMap<>();
        customer2.put("firstName", "Johnny");
        customer2.put("lastName", "Doe");
        customer2.put("email", "john.doe@example.com"); // Same email
        customer2.put("phone", "9876543210");
        rawData.add(customer2);
        
        CustomerProcessingOptions options = new CustomerProcessingOptions(
            true,  // performDeduplication
            3,     // maxErrorCount
            false, // duplicatesAreErrors (false means duplicates are skipped, not errors)
            new DefaultValidator(), // customValidator
            true,  // saveToDatabase
            true,  // includeRecordsInResponse
            true,  // includeValidRecords
            true,  // includeInvalidRecords
            true   // includeDuplicateRecords
        );
        
        // Act
        Map<String, Object> result = crm.processCustomerData(rawData, "manual", options);
        
        // Assert
        assertEquals("success", result.get("status"));
        assertEquals(2, result.get("totalRecords"));
        assertEquals(1, result.get("successCount")); // Only one should succeed
        
        // Check duplicate count
        assertEquals(1, result.get("duplicateCount"));
        
        // Check that duplicate records are included in the response
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> duplicateRecords = (List<Map<String, Object>>) result.get("duplicateRecords");
        assertNotNull(duplicateRecords);
        assertEquals(1, duplicateRecords.size());
        
        Map<String, Object> duplicateCustomer = duplicateRecords.get(0);
        assertEquals("Johnny", duplicateCustomer.get("firstName"));
        assertEquals("john.doe@example.com", duplicateCustomer.get("email"));
    }
    
    @Test
    @DisplayName("Test processing empty data")
    void testProcessEmptyData() {
        // Arrange
        List<Map<String, Object>> rawData = new ArrayList<>();
        CustomerProcessingOptions options = new CustomerProcessingOptions();
        
        // Act
        Map<String, Object> result = crm.processCustomerData(rawData, "manual", options);
        
        // Assert
        assertEquals("error", result.get("status"));
        assertEquals("No data provided for processing", result.get("message"));
    }
    
    @Test
    @DisplayName("Test processing null data")
    void testProcessNullData() {
        // Arrange
        CustomerProcessingOptions options = new CustomerProcessingOptions();
        
        // Act
        Map<String, Object> result = crm.processCustomerData(null, "manual", options);
        
        // Assert
        assertEquals("error", result.get("status"));
        assertEquals("No data provided for processing", result.get("message"));
    }
    
    @Test
    @DisplayName("Test different data sources")
    void testDifferentDataSources() {
        // Arrange
        List<Map<String, Object>> rawData = new ArrayList<>();
        Map<String, Object> customer = new HashMap<>();
        customer.put("firstName", "John");
        customer.put("lastName", "Doe");
        customer.put("email", "john.doe@example.com");
        rawData.add(customer);
        
        CustomerProcessingOptions options = new CustomerProcessingOptions();
        
        // Act - Test CSV source
        Map<String, Object> csvResult = crm.processCustomerData(rawData, "csv", options);
        
        // Act - Test API source
        Map<String, Object> apiResult = crm.processCustomerData(rawData, "api", options);
        
        // Assert
        assertEquals("success", csvResult.get("status"));
        assertEquals("csv", csvResult.get("source"));
        
        assertEquals("success", apiResult.get("status"));
        assertEquals("api", apiResult.get("source"));
    }
}