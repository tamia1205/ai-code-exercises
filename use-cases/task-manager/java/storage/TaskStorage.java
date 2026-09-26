// src/main/java/taskmanager/storage/TaskStorage.java
package taskmanager.storage;

import com.google.gson.*;
import taskmanager.model.Task;
import taskmanager.model.TaskPriority;
import taskmanager.model.TaskStatus;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskStorage {
    private final String storagePath;
    private final Map<String, Task> tasks;
    private final Gson gson;

    public TaskStorage(String storagePath) {
        this.storagePath = storagePath;
        this.tasks = new HashMap<>();

        // Configure Gson with custom adapters for LocalDateTime
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .setPrettyPrinting()
                .create();

        load();
    }

    public void load() {
        File file = new File(storagePath);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Task[] loadedTasks = gson.fromJson(reader, Task[].class);
                if (loadedTasks != null) {
                    for (Task task : loadedTasks) {
                        tasks.put(task.getId(), task);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading tasks: " + e.getMessage());
            }
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(storagePath)) {
            gson.toJson(tasks.values(), writer);
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
        }
    }

    public String addTask(Task task) {
        tasks.put(task.getId(), task);
        save();
        return task.getId();
    }

    public Task getTask(String taskId) {
        return tasks.get(taskId);
    }

    public boolean updateTask(String taskId, Task updates) {
        Task task = getTask(taskId);
        if (task != null) {
            task.update(updates);
            save();
            return true;
        }
        return false;
    }

    public boolean deleteTask(String taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            save();
            return true;
        }
        return false;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        return tasks.values().stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByPriority(TaskPriority priority) {
        return tasks.values().stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    public List<Task> getOverdueTasks() {
        return tasks.values().stream()
                .filter(Task::isOverdue)
                .collect(Collectors.toList());
    }

    // Custom serializer for LocalDateTime
    private static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(src));
        }
    }

    // Custom deserializer for LocalDateTime
    private static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
