package za.co.wethinkcode.taskmanager.util;

import za.co.wethinkcode.taskmanager.model.Task;
import za.co.wethinkcode.taskmanager.model.TaskPriority;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskTextParser {

    /**
     * Parse free-form text to extract task properties.
     *
     * Examples of format it can parse:
     * "Buy milk @shopping !2 #tomorrow"
     * "Finish report for client XYZ !urgent #friday #work @project"
     *
     * Where:
     * - Basic text is the task title
     * - @tag adds a tag
     * - !N sets priority (1=low, 2=medium, 3=high, 4=urgent)
     * - !urgent/!high/!medium/!low sets priority by name
     * - #date sets a due date
     */
    public static Task parseTaskFromText(String text) {
        // Default task properties
        String title = text.trim();
        TaskPriority priority = TaskPriority.MEDIUM;
        LocalDateTime dueDate = null;
        List<String> tags = new ArrayList<>();

        // Extract priority markers (!N or !name)
        Pattern priorityPattern = Pattern.compile(
            "\\s!([1-4]|urgent|high|medium|low)\\b",
            Pattern.CASE_INSENSITIVE
        );
        Matcher priorityMatcher = priorityPattern.matcher(title);

        if (priorityMatcher.find()) {
            String priorityText = priorityMatcher.group(1).toLowerCase();
            // Remove from title
            title = title.replaceAll(
                "\\s!([1-4]|urgent|high|medium|low)\\b",
                ""
            );

            // Convert to TaskPriority
            switch (priorityText) {
                case "1":
                case "low":
                    priority = TaskPriority.LOW;
                    break;
                case "2":
                case "medium":
                    priority = TaskPriority.MEDIUM;
                    break;
                case "3":
                case "high":
                    priority = TaskPriority.HIGH;
                    break;
                case "4":
                case "urgent":
                    priority = TaskPriority.URGENT;
                    break;
            }
        }

        // Extract tags (@tag)
        Pattern tagPattern = Pattern.compile("\\s@(\\w+)");
        Matcher tagMatcher = tagPattern.matcher(text);

        while (tagMatcher.find()) {
            tags.add(tagMatcher.group(1));
        }

        // Remove all tags from the title
        title = title.replaceAll("\\s@\\w+", "");

        // Extract date markers (#date)
        Pattern datePattern = Pattern.compile("\\s#(\\w+)");
        Matcher dateMatcher = datePattern.matcher(text);
        List<String> dates = new ArrayList<>();

        while (dateMatcher.find()) {
            dates.add(dateMatcher.group(1));
        }

        // Remove all date markers from the title
        title = title.replaceAll("\\s#(\\w+)", "");

        // Try to parse date references
        if (!dates.isEmpty()) {
            LocalDateTime today = LocalDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

            for (String dateStr : dates) {
                String lowerDateStr = dateStr.toLowerCase();

                if (
                    lowerDateStr.equals("today") || lowerDateStr.equals("now")
                ) {
                    dueDate = today;
                    break;
                } else if (lowerDateStr.equals("tomorrow")) {
                    dueDate = today.plusDays(1);
                    break;
                } else if (
                    lowerDateStr.equals("next_week") ||
                    lowerDateStr.equals("nextweek")
                ) {
                    dueDate = today.plusDays(7);
                    break;
                } else if (
                    Arrays.asList(
                        "monday",
                        "mon",
                        "tuesday",
                        "tue",
                        "wednesday",
                        "wed",
                        "thursday",
                        "thu",
                        "friday",
                        "fri",
                        "saturday",
                        "sat",
                        "sunday",
                        "sun"
                    ).contains(lowerDateStr)
                ) {
                    Map<String, Integer> dayMap = Map.ofEntries(
                            Map.entry("monday", DayOfWeek.MONDAY.getValue()),
                            Map.entry("mon", DayOfWeek.MONDAY.getValue()),
                            Map.entry("tuesday", DayOfWeek.TUESDAY.getValue()),
                            Map.entry("tue", DayOfWeek.TUESDAY.getValue()),
                            Map.entry("wednesday", DayOfWeek.WEDNESDAY.getValue()),
                            Map.entry("wed", DayOfWeek.WEDNESDAY.getValue()),
                            Map.entry("thursday", DayOfWeek.THURSDAY.getValue()),
                            Map.entry("thu", DayOfWeek.THURSDAY.getValue()),
                            Map.entry("friday", DayOfWeek.FRIDAY.getValue()),
                            Map.entry("fri", DayOfWeek.FRIDAY.getValue()),
                            Map.entry("saturday", DayOfWeek.SATURDAY.getValue()),
                            Map.entry("sat", DayOfWeek.SATURDAY.getValue()),
                            Map.entry("sunday", DayOfWeek.SUNDAY.getValue()),
                            Map.entry("sun", DayOfWeek.SUNDAY.getValue())
                    );
                    dueDate = getNextWeekday(today, dayMap.get(lowerDateStr));
                    break;
                }

                // Try to parse as YYYY-MM-DD
                try {
                    dueDate = LocalDate.parse(lowerDateStr).atStartOfDay();
                    break;
                } catch (DateTimeParseException e) {
                    // Not a valid date format, continue to next date marker
                }
            }
        }

        // Trim excess whitespace from title
        title = title.replaceAll("\\s+", " ").trim();

        // Create a new task with the extracted properties
        Task task = new Task(title);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setTags(tags);

        return task;
    }

    private static LocalDateTime getNextWeekday(
        LocalDateTime date,
        int targetDayOfWeek
    ) {
        // Get the next occurrence of a specific weekday
        int currentDayOfWeek = date.getDayOfWeek().getValue();
        int daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7;

        // If we landed on today and it's the target day, move to next week
        if (daysToAdd == 0) {
            daysToAdd = 7;
        }

        return date.plusDays(daysToAdd);
    }
}
