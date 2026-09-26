// src/main/java/taskmanager/model/TaskStatus.java
package taskmanager.model;

public enum TaskStatus {
    TODO("todo"),
    IN_PROGRESS("in_progress"),
    REVIEW("review"),
    DONE("done");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TaskStatus fromValue(String value) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status value: " + value);
    }
}
