// src/main/java/taskmanager/model/Task.java
package za.co.wethinkcode.taskmanager.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task {
    private String id;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private List<String> tags;

    public Task(String title, String description, TaskPriority priority, LocalDateTime dueDate, List<String> tags) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = TaskStatus.TODO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.dueDate = dueDate;
        this.completedAt = null;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public Task(String title) {
        this(title, "", TaskPriority.MEDIUM, null, null);
    }

    public Task(String title, String description) {
        this(title, description, TaskPriority.MEDIUM, null, null);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    public void setTags(List<String> tags) {
        this.tags = new ArrayList<>(tags);
    }

    // Business methods
    public void update(Task updates) {
        if (updates.getTitle() != null) {
            this.title = updates.getTitle();
        }
        if (updates.getDescription() != null) {
            this.description = updates.getDescription();
        }
        if (updates.getPriority() != null) {
            this.priority = updates.getPriority();
        }
        if (updates.getStatus() != null) {
            this.status = updates.getStatus();
        }
        if (updates.getDueDate() != null) {
            this.dueDate = updates.getDueDate();
        }
        if (updates.getTags() != null && !updates.getTags().isEmpty()) {
            this.tags = new ArrayList<>(updates.getTags());
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsDone() {
        this.status = TaskStatus.DONE;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = this.completedAt;
    }

    public boolean isOverdue() {
        if (this.dueDate == null) {
            return false;
        }
        return this.dueDate.isBefore(LocalDateTime.now()) && this.status != TaskStatus.DONE;
    }

    public void addTag(String tag) {
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    public boolean removeTag(String tag) {
        return this.tags.remove(tag);
    }
}
