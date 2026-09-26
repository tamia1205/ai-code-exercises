package com.example.crm;

import java.util.concurrent.locks.StampedLock;

public class CustomerProcessingOptions {
    private final boolean performDeduplication;
    private final int maxErrorCount;
    private final boolean duplicatesAreErrors;
    private final DefaultValidator customValidator;
    private final boolean saveToDatabase;
    private final boolean includeRecordsInResponse;
    private final boolean includeValidRecords;
    private final boolean includeInvalidRecords;
    private final boolean includeDuplicateRecords;


    public CustomerProcessingOptions() {
        this(true, 3, false, new DefaultValidator(), true, true, true, false, false);
    }
    public CustomerProcessingOptions(boolean performDeduplication,
                                     int maxErrorCount,
                                     boolean duplicatesAreErrors,
                                     DefaultValidator customValidator,
                                     boolean saveToDatabase,
                                     boolean includeRecordsInResponse,
                                     boolean includeValidRecords,
                                     boolean includeInvalidRecords,
                                     boolean includeDuplicateRecords) {
        this.performDeduplication = performDeduplication;
        this.maxErrorCount = maxErrorCount;
        this.duplicatesAreErrors = duplicatesAreErrors;
        this.customValidator = customValidator;
        this.saveToDatabase = saveToDatabase;
        this.includeRecordsInResponse = includeRecordsInResponse;
        this.includeValidRecords = includeValidRecords;
        this.includeInvalidRecords = includeInvalidRecords;
        this.includeDuplicateRecords = includeDuplicateRecords;
    }

    public boolean isPerformDeduplication() {
        return performDeduplication;
    }

    public int getMaxErrorCount() {
        return maxErrorCount;
    }

    public boolean isDuplicatesAreErrors() {
        return duplicatesAreErrors;
    }


    public DefaultValidator getCustomValidator() {
        return customValidator;
    }

    public boolean isSaveToDatabase() {
        return saveToDatabase;
    }

    public boolean isIncludeRecordsInResponse() {
        return includeRecordsInResponse;
    }

    public boolean isIncludeValidRecords() {
        return includeValidRecords;
    }

    public boolean isIncludeInvalidRecords() {
        return includeInvalidRecords;
    }

    public boolean isIncludeDuplicateRecords() {
        return includeDuplicateRecords;
    }
}
