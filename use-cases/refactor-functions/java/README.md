# Customer Relationship Management (CRM) Data Processing System

A robust Java-based CRM system that processes, validates, and manages customer data from multiple sources with built-in deduplication and customizable validation rules.

This CRM system provides a flexible and reliable way to handle customer data ingestion from various sources like CSV files, APIs, and manual entry. It features comprehensive data validation, deduplication based on email and phone numbers, and customizable processing options. The system is designed to handle large datasets efficiently while maintaining data integrity and providing detailed processing results.

Key features include:
- Multi-source data processing (CSV, API, manual entry)
- Configurable deduplication rules for emails and phone numbers
- Customizable validation rules for customer data
- Detailed error tracking and reporting
- Flexible processing options for handling duplicates and validation failures
- Logging support for monitoring and debugging

## Repository Structure
```
.
├── build.gradle.kts              # Gradle build configuration with dependencies
├── gradle/wrapper/              # Gradle wrapper for consistent builds
├── src/
    ├── main/java/com/example/crm/
    │   ├── Crm.java             # Core CRM processing logic
    │   ├── Customer.java        # Customer data model
    │   ├── CustomerProcessingOptions.java  # Processing configuration
    │   ├── CustomerRepository.java         # Data persistence layer
    │   └── DefaultValidator.java           # Data validation implementation
    └── test/
        └── java/com/example/crm/
            └── CrmTest.java     # Unit tests for CRM functionality
```

## Usage Instructions
### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Gradle build tool (included via wrapper)
- Log4j2 for logging functionality

### Installation

1. Clone the repository:
```bash
git clone [repository-url]
cd [repository-name]
```

2. Build the project using Gradle wrapper:
```bash
# For Unix-like systems
./gradlew build

# For Windows
gradlew.bat build
```

### Quick Start
1. Create a CRM instance:
```java
Crm crm = new Crm();
CustomerProcessingOptions options = new CustomerProcessingOptions();
```

2. Process customer data:
```java
List<Map<String, Object>> customerData = new ArrayList<>();
Map<String, Object> customer = new HashMap<>();
customer.put("email", "john.doe@example.com");
customer.put("firstName", "John");
customer.put("lastName", "Doe");
customer.put("phone", "123-456-7890");
customerData.add(customer);

Map<String, Object> result = crm.processCustomerData(customerData, "manual", options);
```

### More Detailed Examples
1. Processing with deduplication:
```java
CustomerProcessingOptions options = new CustomerProcessingOptions();
options.setPerformDeduplication(true);
options.setDuplicatesAreErrors(false);

Map<String, Object> result = crm.processCustomerData(customerData, "csv", options);
```

2. Custom validation rules:
```java
DefaultValidator validator = new DefaultValidator();
options.setValidator(validator);
```

## Running Tests

### Basic Test Execution
To run all unit tests:
```bash
# For Unix-like systems
./gradlew test

# For Windows
gradlew.bat test
```

### Running Specific Tests
To run a specific test class:
```bash
./gradlew test --tests "com.example.crm.CrmTest"
```

To run a specific test method:
```bash
./gradlew test --tests "com.example.crm.CrmTest.testProcessValidCustomerData"
```

### Test Output
Test results are available in:
- HTML report: `build/reports/tests/test/index.html`
- XML report: `build/test-results/test`
- Console output during test execution

### Troubleshooting Test Failures
1. Check the detailed test report in `build/reports/tests/test/index.html`
2. Enable debug logging for tests by updating `src/test/resources/log4j2-test.xml`:
```xml
<Root level="debug">
    <AppenderRef ref="Console"/>
</Root>
```
3. Run tests with increased logging:
```bash
./gradlew test --info
```

### Troubleshooting
1. Common Issues:
- **Duplicate Records**: Check the `duplicateRecords` in the result map
- **Validation Failures**: Examine the `invalidRecords` in the result map
- **Processing Errors**: Review the log files in the logs directory

2. Debug Mode:
```xml
<!-- Update log4j2.xml -->
<Root level="debug">
    <AppenderRef ref="Console"/>
</Root>
```

## Data Flow
The CRM system processes customer data through a pipeline of validation, deduplication, and transformation steps.

```ascii
Input Data -> Validation -> Deduplication -> Transformation -> Storage
     ↓            ↓             ↓                ↓              ↓
  Multiple     Required     Email/Phone     Field Format    Repository
  Sources      Fields       Matching        Standardization  Storage
```

Key component interactions:
- Input data is received from various sources (CSV, API, manual)
- Validator checks required fields and format
- Deduplication engine checks for existing records
- Transformer standardizes data format
- Repository handles persistent storage
- Error handling occurs at each stage with detailed reporting