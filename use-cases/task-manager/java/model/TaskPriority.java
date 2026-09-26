// src/main/java/taskmanager/model/TaskPriority.java
package taskmanager.model;

public enum TaskPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    URGENT(4);

    private final int value;

    TaskPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TaskPriority fromValue(int value) {
        for (TaskPriority priority : TaskPriority.values()) {
            if (priority.getValue() == value) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid priority value: " + value);
    }
}
