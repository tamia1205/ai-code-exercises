package za.co.wethinkcode.taskmanager.util;

import za.co.wethinkcode.taskmanager.model.Task;
import za.co.wethinkcode.taskmanager.model.TaskPriority;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTextParserTest {

    @Test
    void parseTaskFromText_basicTitle() {
        // Arrange
        String taskText = "Buy milk";
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertNull(task.getDueDate());
        assertTrue(task.getTags().isEmpty());
    }
    
    @Test
    void parseTaskFromText_withNumericPriority() {
        // Arrange
        String taskText = "Buy milk !1";
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertEquals(TaskPriority.LOW, task.getPriority());
    }
    
    @Test
    void parseTaskFromText_withNamedPriority() {
        // Arrange
        String taskText = "Buy milk !urgent";
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertEquals(TaskPriority.URGENT, task.getPriority());
    }
    
    @Test
    void parseTaskFromText_withTags() {
        // Arrange
        String taskText = "Buy milk @shopping @groceries";
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        List<String> tags = task.getTags();
        assertEquals(2, tags.size());
        assertTrue(tags.contains("shopping"));
        assertTrue(tags.contains("groceries"));
    }
    
    @Test
    void parseTaskFromText_withToday() {
        // Arrange
        String taskText = "Buy milk #today";
        LocalDateTime today = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertNotNull(task.getDueDate());
        assertEquals(today.toLocalDate(), task.getDueDate().toLocalDate());
    }
    
    @Test
    void parseTaskFromText_withTomorrow() {
        // Arrange
        String taskText = "Buy milk #tomorrow";
        LocalDateTime tomorrow = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .plusDays(1);
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertNotNull(task.getDueDate());
        assertEquals(tomorrow.toLocalDate(), task.getDueDate().toLocalDate());
    }
    
    @Test
    void parseTaskFromText_withNextWeek() {
        // Arrange
        String taskText = "Buy milk #next_week";
        LocalDateTime nextWeek = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .plusDays(7);
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertNotNull(task.getDueDate());
        assertEquals(nextWeek.toLocalDate(), task.getDueDate().toLocalDate());
    }
    
    @Test
    void parseTaskFromText_withNextWeekAlternative() {
        // Arrange
        String taskText = "Buy milk #nextweek";
        LocalDateTime nextWeek = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .plusDays(7);
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertNotNull(task.getDueDate());
        assertEquals(nextWeek.toLocalDate(), task.getDueDate().toLocalDate());
    }
    
    @Test
    void parseTaskFromText_withDayName() {
        // Arrange
        String taskText = "Buy milk #monday";
        LocalDateTime today = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        LocalDateTime nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertNotNull(task.getDueDate());
        assertEquals(nextMonday.toLocalDate(), task.getDueDate().toLocalDate());
    }
    
    @Test
    void parseTaskFromText_withDayAbbreviation() {
        // Arrange
        String taskText = "Buy milk #fri";
        LocalDateTime today = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        LocalDateTime nextFriday = today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertNotNull(task.getDueDate());
        assertEquals(nextFriday.toLocalDate(), task.getDueDate().toLocalDate());
    }
    
    @Test
    void parseTaskFromText_withMultipleFeatures() {
        // Arrange
        String taskText = "Buy milk @shopping !high #tomorrow";
        LocalDateTime tomorrow = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .plusDays(1);
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertNotNull(task.getDueDate());
        assertEquals(tomorrow.toLocalDate(), task.getDueDate().toLocalDate());
        List<String> tags = task.getTags();
        assertEquals(1, tags.size());
        assertTrue(tags.contains("shopping"));
    }
    
    @Test
    void parseTaskFromText_withComplexTitle() {
        // Arrange
        String taskText = "Finish report for client XYZ !urgent #friday #work @project";
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Finish report for client XYZ", task.getTitle());
        assertEquals(TaskPriority.URGENT, task.getPriority());
        assertNotNull(task.getDueDate());
        List<String> tags = task.getTags();
        assertEquals(1, tags.size());
        assertTrue(tags.contains("project"));
    }
    
    @Test
    void parseTaskFromText_withMultipleDateMarkers_shouldUseFirstValid() {
        // Arrange
        String taskText = "Buy milk #tomorrow #next_week";
        LocalDateTime tomorrow = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .plusDays(1);
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertNotNull(task.getDueDate());
        assertEquals(tomorrow.toLocalDate(), task.getDueDate().toLocalDate());
    }
    
    @Test
    void parseTaskFromText_withExtraWhitespace() {
        // Arrange
        String taskText = "  Buy   milk   @shopping   !high   #tomorrow  ";
        
        // Act
        Task task = TaskTextParser.parseTaskFromText(taskText);
        
        // Assert
        assertEquals("Buy milk", task.getTitle());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        List<String> tags = task.getTags();
        assertEquals(1, tags.size());
        assertTrue(tags.contains("shopping"));
    }
}