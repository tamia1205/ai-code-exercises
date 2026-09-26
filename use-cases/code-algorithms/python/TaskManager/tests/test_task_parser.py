import unittest
from datetime import datetime, timedelta
from unittest.mock import patch

from models import TaskPriority
from task_parser import parse_task_from_text, get_next_weekday


class TaskParserTest(unittest.TestCase):
    def test_parse_basic_task(self):
        """Test parsing a basic task with no special markers."""
        task = parse_task_from_text("Buy milk")
        
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.priority, TaskPriority.MEDIUM)  # Default priority
        self.assertIsNone(task.due_date)
        self.assertEqual(task.tags, [])

    def test_parse_task_with_priority_number(self):
        """Test parsing a task with numeric priority markers."""
        # Test low priority
        task = parse_task_from_text("Buy milk !1")
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.priority, TaskPriority.LOW)
        
        # Test medium priority
        task = parse_task_from_text("Buy milk !2")
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.priority, TaskPriority.MEDIUM)
        
        # Test high priority
        task = parse_task_from_text("Buy milk !3")
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.priority, TaskPriority.HIGH)
        
        # Test urgent priority
        task = parse_task_from_text("Buy milk !4")
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.priority, TaskPriority.URGENT)

    def test_parse_task_with_priority_name(self):
        """Test parsing a task with named priority markers."""
        # Test low priority
        task = parse_task_from_text("Buy milk !low")
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.priority, TaskPriority.LOW)
        
        # Test medium priority
        task = parse_task_from_text("Buy milk !medium")
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.priority, TaskPriority.MEDIUM)
        
        # Test high priority
        task = parse_task_from_text("Buy milk !high")
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.priority, TaskPriority.HIGH)
        
        # Test urgent priority
        task = parse_task_from_text("Buy milk !urgent")
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.priority, TaskPriority.URGENT)
        
        # Test case insensitivity
        task = parse_task_from_text("Buy milk !HIGH")
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.priority, TaskPriority.HIGH)

    def test_parse_task_with_tags(self):
        """Test parsing a task with tags."""
        task = parse_task_from_text("Buy milk @shopping @groceries")
        
        self.assertEqual(task.title, "Buy milk")
        self.assertIn("shopping", task.tags)
        self.assertIn("groceries", task.tags)
        self.assertEqual(len(task.tags), 2)

    @patch('task_parser.datetime')
    def test_parse_task_with_date_keywords(self, mock_datetime):
        """Test parsing a task with date keywords."""
        # Mock the current date to be 2023-06-15 (a Thursday)
        mock_now = datetime(2023, 6, 15, 10, 0, 0)
        mock_datetime.now.return_value = mock_now
        
        # Test "today"
        task = parse_task_from_text("Buy milk #today")
        expected_date = datetime(2023, 6, 15, 0, 0, 0)
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.due_date, expected_date)
        
        # Test "tomorrow"
        task = parse_task_from_text("Buy milk #tomorrow")
        expected_date = datetime(2023, 6, 16, 0, 0, 0)
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.due_date, expected_date)
        
        # Test "next_week"
        task = parse_task_from_text("Buy milk #next_week")
        expected_date = datetime(2023, 6, 22, 0, 0, 0)
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.due_date, expected_date)

    @patch('task_parser.datetime')
    def test_parse_task_with_weekday_names(self, mock_datetime):
        """Test parsing a task with weekday names."""
        # Mock the current date to be 2023-06-15 (a Thursday)
        mock_now = datetime(2023, 6, 15, 10, 0, 0)
        mock_datetime.now.return_value = mock_now
        
        # Test Monday (should be next Monday)
        task = parse_task_from_text("Buy milk #monday")
        expected_date = datetime(2023, 6, 19, 0, 0, 0)  # Next Monday
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.due_date, expected_date)
        
        # Test Friday (should be next day)
        task = parse_task_from_text("Buy milk #friday")
        expected_date = datetime(2023, 6, 16, 0, 0, 0)  # Next Friday
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.due_date, expected_date)
        
        # Test abbreviated weekday name
        task = parse_task_from_text("Buy milk #wed")
        expected_date = datetime(2023, 6, 21, 0, 0, 0)  # Next Wednesday
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.due_date, expected_date)

    def test_parse_complex_task(self):
        """Test parsing a complex task with multiple markers."""
        task = parse_task_from_text("Buy milk @shopping @groceries !urgent #tomorrow")
        
        self.assertEqual(task.title, "Buy milk")
        self.assertEqual(task.priority, TaskPriority.URGENT)
        self.assertIn("shopping", task.tags)
        self.assertIn("groceries", task.tags)
        self.assertEqual(len(task.tags), 2)
        self.assertIsNotNone(task.due_date)

    def test_parse_task_with_embedded_markers(self):
        """Test parsing a task with markers embedded in the middle of the text."""
        task = parse_task_from_text("Buy milk @shopping and eggs !high for breakfast #tomorrow")
        
        self.assertEqual(task.title, "Buy milk and eggs for breakfast")
        self.assertEqual(task.priority, TaskPriority.HIGH)
        self.assertIn("shopping", task.tags)
        self.assertEqual(len(task.tags), 1)
        self.assertIsNotNone(task.due_date)

    def test_get_next_weekday(self):
        """Test the get_next_weekday function."""
        # Test when target day is after current day
        current_date = datetime(2023, 6, 15)  # Thursday
        next_monday = get_next_weekday(current_date, 0)  # 0 = Monday
        self.assertEqual(next_monday, datetime(2023, 6, 19))  # Next Monday
        
        # Test when target day is before current day
        current_date = datetime(2023, 6, 15)  # Thursday
        next_wednesday = get_next_weekday(current_date, 2)  # 2 = Wednesday
        self.assertEqual(next_wednesday, datetime(2023, 6, 21))  # Next Wednesday
        
        # Test when target day is same as current day
        current_date = datetime(2023, 6, 15)  # Thursday
        next_thursday = get_next_weekday(current_date, 3)  # 3 = Thursday
        self.assertEqual(next_thursday, datetime(2023, 6, 22))  # Next Thursday (not today)


if __name__ == '__main__':
    unittest.main()