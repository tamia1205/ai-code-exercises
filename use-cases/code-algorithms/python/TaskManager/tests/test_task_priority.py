import unittest
from datetime import datetime, timedelta
from unittest.mock import patch

from models import Task, TaskStatus, TaskPriority
from task_priority import calculate_task_score, sort_tasks_by_importance, get_top_priority_tasks


class TaskPriorityTest(unittest.TestCase):
    def setUp(self):
        """Set up test fixtures before each test method."""
        self.now = datetime.now()
        
        # Create sample tasks with different priorities
        self.low_task = Task("Low Priority Task", priority=TaskPriority.LOW)
        self.medium_task = Task("Medium Priority Task", priority=TaskPriority.MEDIUM)
        self.high_task = Task("High Priority Task", priority=TaskPriority.HIGH)
        self.urgent_task = Task("Urgent Priority Task", priority=TaskPriority.URGENT)
        
        # Set consistent timestamps for testing
        for task in [self.low_task, self.medium_task, self.high_task, self.urgent_task]:
            task.created_at = self.now
            task.updated_at = self.now

    @patch('task_priority.datetime')
    def test_calculate_task_score_priority_weights(self, mock_datetime):
        """Test that calculate_task_score correctly applies priority weights."""
        mock_datetime.now.return_value = self.now
        
        # Test each priority level
        low_score = calculate_task_score(self.low_task)
        medium_score = calculate_task_score(self.medium_task)
        high_score = calculate_task_score(self.high_task)
        urgent_score = calculate_task_score(self.urgent_task)
        
        # Verify that higher priority tasks have higher scores
        self.assertLess(low_score, medium_score)
        self.assertLess(medium_score, high_score)
        self.assertLess(high_score, urgent_score)

    @patch('task_priority.datetime')
    def test_calculate_task_score_due_date_factor(self, mock_datetime):
        """Test that calculate_task_score correctly factors in due dates."""
        mock_datetime.now.return_value = self.now
        
        # Create tasks with different due dates
        overdue_task = Task("Overdue Task", priority=TaskPriority.MEDIUM)
        overdue_task.due_date = self.now - timedelta(days=1)
        
        due_today_task = Task("Due Today Task", priority=TaskPriority.MEDIUM)
        due_today_task.due_date = self.now
        
        due_soon_task = Task("Due Soon Task", priority=TaskPriority.MEDIUM)
        due_soon_task.due_date = self.now + timedelta(days=2)
        
        due_later_task = Task("Due Later Task", priority=TaskPriority.MEDIUM)
        due_later_task.due_date = self.now + timedelta(days=10)
        
        no_due_date_task = Task("No Due Date Task", priority=TaskPriority.MEDIUM)
        
        # Calculate scores
        overdue_score = calculate_task_score(overdue_task)
        due_today_score = calculate_task_score(due_today_task)
        due_soon_score = calculate_task_score(due_soon_task)
        due_later_score = calculate_task_score(due_later_task)
        no_due_date_score = calculate_task_score(no_due_date_task)
        
        # Verify that tasks due sooner have higher scores
        self.assertGreater(overdue_score, due_today_score)
        self.assertGreater(due_today_score, due_soon_score)
        self.assertGreater(due_soon_score, due_later_score)

    @patch('task_priority.datetime')
    def test_calculate_task_score_status_factor(self, mock_datetime):
        """Test that calculate_task_score correctly factors in task status."""
        mock_datetime.now.return_value = self.now
        
        # Create tasks with different statuses
        todo_task = Task("Todo Task", priority=TaskPriority.MEDIUM)
        todo_task.status = TaskStatus.TODO
        
        in_progress_task = Task("In Progress Task", priority=TaskPriority.MEDIUM)
        in_progress_task.status = TaskStatus.IN_PROGRESS
        
        review_task = Task("Review Task", priority=TaskPriority.MEDIUM)
        review_task.status = TaskStatus.REVIEW
        
        done_task = Task("Done Task", priority=TaskPriority.MEDIUM)
        done_task.status = TaskStatus.DONE
        
        # Calculate scores
        todo_score = calculate_task_score(todo_task)
        in_progress_score = calculate_task_score(in_progress_task)
        review_score = calculate_task_score(review_task)
        done_score = calculate_task_score(done_task)
        
        # Verify that completed tasks have lower scores
        self.assertGreater(todo_score, review_score)
        self.assertGreater(review_score, done_score)
        self.assertEqual(todo_score, in_progress_score)  # These should be equal as no specific modifier for IN_PROGRESS

    @patch('task_priority.datetime')
    def test_calculate_task_score_tag_factor(self, mock_datetime):
        """Test that calculate_task_score correctly factors in task tags."""
        mock_datetime.now.return_value = self.now
        
        # Create tasks with different tags
        no_tags_task = Task("No Tags Task", priority=TaskPriority.MEDIUM)
        
        normal_tags_task = Task("Normal Tags Task", priority=TaskPriority.MEDIUM)
        normal_tags_task.tags = ["work", "project"]
        
        critical_tag_task = Task("Critical Tag Task", priority=TaskPriority.MEDIUM)
        critical_tag_task.tags = ["work", "critical"]
        
        urgent_tag_task = Task("Urgent Tag Task", priority=TaskPriority.MEDIUM)
        urgent_tag_task.tags = ["urgent", "project"]
        
        blocker_tag_task = Task("Blocker Tag Task", priority=TaskPriority.MEDIUM)
        blocker_tag_task.tags = ["blocker"]
        
        # Calculate scores
        no_tags_score = calculate_task_score(no_tags_task)
        normal_tags_score = calculate_task_score(normal_tags_task)
        critical_tag_score = calculate_task_score(critical_tag_task)
        urgent_tag_score = calculate_task_score(urgent_tag_task)
        blocker_tag_score = calculate_task_score(blocker_tag_task)
        
        # Verify that tasks with critical/urgent/blocker tags have higher scores
        self.assertEqual(no_tags_score, normal_tags_score)  # Regular tags don't affect score
        self.assertGreater(critical_tag_score, normal_tags_score)
        self.assertGreater(urgent_tag_score, normal_tags_score)
        self.assertGreater(blocker_tag_score, normal_tags_score)

    @patch('task_priority.datetime')
    def test_calculate_task_score_recency_factor(self, mock_datetime):
        """Test that calculate_task_score correctly factors in task update recency."""
        mock_datetime.now.return_value = self.now
        
        # Create tasks with different update times
        recent_task = Task("Recent Task", priority=TaskPriority.MEDIUM)
        recent_task.updated_at = self.now - timedelta(hours=1)
        
        older_task = Task("Older Task", priority=TaskPriority.MEDIUM)
        older_task.updated_at = self.now - timedelta(days=2)
        
        # Calculate scores
        recent_score = calculate_task_score(recent_task)
        older_score = calculate_task_score(older_task)
        
        # Verify that recently updated tasks have higher scores
        self.assertGreater(recent_score, older_score)

    def test_sort_tasks_by_importance(self):
        """Test that sort_tasks_by_importance correctly sorts tasks by their calculated scores."""
        # Create tasks with different characteristics that will affect their scores
        task1 = Task("High Priority", priority=TaskPriority.HIGH)
        
        task2 = Task("Medium Priority Due Soon", priority=TaskPriority.MEDIUM)
        task2.due_date = datetime.now() + timedelta(days=1)
        
        task3 = Task("Low Priority Overdue", priority=TaskPriority.LOW)
        task3.due_date = datetime.now() - timedelta(days=1)
        
        task4 = Task("Medium Priority Done", priority=TaskPriority.MEDIUM)
        task4.status = TaskStatus.DONE
        
        task5 = Task("Urgent Priority", priority=TaskPriority.URGENT)
        
        # Sort the tasks
        tasks = [task1, task2, task3, task4, task5]
        sorted_tasks = sort_tasks_by_importance(tasks)
        
        # Verify the order (highest score first)
        # Urgent priority should be first
        self.assertEqual("Urgent Priority", sorted_tasks[0].title)
        
        # Low priority but overdue should be high in the list
        self.assertEqual("Low Priority Overdue", sorted_tasks[1].title)
        
        # High priority with no due date should be next
        self.assertEqual("High Priority", sorted_tasks[2].title)
        
        # Medium priority due soon should be next
        self.assertEqual("Medium Priority Due Soon", sorted_tasks[3].title)
        
        # Done task should be last regardless of priority
        self.assertEqual("Medium Priority Done", sorted_tasks[4].title)

    def test_get_top_priority_tasks(self):
        """Test that get_top_priority_tasks returns the correct number of highest priority tasks."""
        # Create a list of tasks
        tasks = [
            Task("Task 1", priority=TaskPriority.MEDIUM),
            Task("Task 2", priority=TaskPriority.HIGH),
            Task("Task 3", priority=TaskPriority.LOW),
            Task("Task 4", priority=TaskPriority.URGENT),
            Task("Task 5", priority=TaskPriority.MEDIUM),
            Task("Task 6", priority=TaskPriority.HIGH),
            Task("Task 7", priority=TaskPriority.LOW),
        ]
        
        # Get top 3 tasks
        top_tasks = get_top_priority_tasks(tasks, limit=3)
        
        # Verify we get exactly 3 tasks
        self.assertEqual(len(top_tasks), 3)
        
        # Verify the urgent task is first
        self.assertEqual(top_tasks[0].title, "Task 4")
        
        # Get top 5 tasks
        top_tasks = get_top_priority_tasks(tasks, limit=5)
        
        # Verify we get exactly 5 tasks
        self.assertEqual(len(top_tasks), 5)
        
        # Test with empty list
        empty_top_tasks = get_top_priority_tasks([], limit=3)
        self.assertEqual(len(empty_top_tasks), 0)
        
        # Test with default limit
        default_top_tasks = get_top_priority_tasks(tasks)
        self.assertEqual(len(default_top_tasks), 5)  # Default limit is 5


if __name__ == '__main__':
    unittest.main()