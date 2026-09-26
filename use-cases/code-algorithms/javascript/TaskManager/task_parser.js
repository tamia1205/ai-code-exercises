const {TaskPriority, Task} = require("./models");

function parseTaskFromText(text) {
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

  // Default task properties
  let title = text.trim();
  let priority = TaskPriority.MEDIUM;
  let dueDate = null;
  let tags = [];

  // Extract priority markers (!N or !name)
  const priorityMatches = title.match(/\s!([1-4]|urgent|high|medium|low)\b/i);
  if (priorityMatches) {
    const priorityText = priorityMatches[1].toLowerCase();
    // Remove from title
    title = title.replace(/\s!([1-4]|urgent|high|medium|low)\b/i, '');

    // Convert to TaskPriority
    if (priorityText === '1' || priorityText === 'low') {
      priority = TaskPriority.LOW;
    } else if (priorityText === '2' || priorityText === 'medium') {
      priority = TaskPriority.MEDIUM;
    } else if (priorityText === '3' || priorityText === 'high') {
      priority = TaskPriority.HIGH;
    } else if (priorityText === '4' || priorityText === 'urgent') {
      priority = TaskPriority.URGENT;
    }
  }

  // Extract tags (@tag)
  const tagRegex = /\s@(\w+)/g;
  let tagMatch;
  while ((tagMatch = tagRegex.exec(text)) !== null) {
    tags.push(tagMatch[1]);
    // Remove from title (we'll handle this after all tags are collected)
  }

  // Remove all tags from the title
  title = title.replace(/\s@\w+/g, '');

  // Extract date markers (#date)
  const dateRegex = /\s#(\w+)/g;
  const dates = [];
  let dateMatch;
  while ((dateMatch = dateRegex.exec(text)) !== null) {
    dates.push(dateMatch[1]);
    // Remove from title (we'll handle this after all dates are collected)
  }

  // Remove all date markers from the title
  title = title.replace(/\s#\w+/g, '');

  // Try to parse date references
  if (dates.length > 0) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    for (const dateStr of dates) {
      const lowerDateStr = dateStr.toLowerCase();

      if (lowerDateStr === 'today' || lowerDateStr === 'now') {
        dueDate = today;
        break;
      } else if (lowerDateStr === 'tomorrow') {
        const tomorrow = new Date(today);
        tomorrow.setDate(today.getDate() + 1);
        dueDate = tomorrow;
        break;
      } else if (lowerDateStr === 'next_week' || lowerDateStr === 'nextweek') {
        const nextWeek = new Date(today);
        nextWeek.setDate(today.getDate() + 7);
        dueDate = nextWeek;
        break;
      } else if (['monday', 'mon', 'tuesday', 'tue', 'wednesday', 'wed',
                  'thursday', 'thu', 'friday', 'fri', 'saturday', 'sat',
                  'sunday', 'sun'].includes(lowerDateStr)) {
        const dayMap = {
          'monday': 1, 'mon': 1,
          'tuesday': 2, 'tue': 2,
          'wednesday': 3, 'wed': 3,
          'thursday': 4, 'thu': 4,
          'friday': 5, 'fri': 5,
          'saturday': 6, 'sat': 6,
          'sunday': 0, 'sun': 0
        };

        dueDate = getNextWeekday(today, dayMap[lowerDateStr]);
        break;
      }

      // Try to parse as YYYY-MM-DD
      const dateComponents = lowerDateStr.split('-');
      if (dateComponents.length === 3) {
        const parsedDate = new Date(
          parseInt(dateComponents[0]),
          parseInt(dateComponents[1]) - 1,
          parseInt(dateComponents[2])
        );

        if (!isNaN(parsedDate.getTime())) {
          dueDate = parsedDate;
          break;
        }
      }
    }
  }

  // Trim excess whitespace from title
  title = title.replace(/\s+/g, ' ').trim();

  // Create a new task with the extracted properties
  const task = new Task(title);
  task.priority = priority;
  task.dueDate = dueDate;
  task.tags = tags;

  return task;
}

function getNextWeekday(currentDate, targetDay) {
  // Get the next occurrence of a specific weekday
  const result = new Date(currentDate);
  result.setDate(currentDate.getDate() + (targetDay + 7 - currentDate.getDay()) % 7);

  // If we landed on today and it's the target day, move to next week
  if (result.getTime() === currentDate.getTime()) {
    result.setDate(result.getDate() + 7);
  }

  return result;
}

// Export functions for testing
module.exports = { parseTaskFromText, getNextWeekday };
