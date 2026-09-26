import unittest
from datetime import datetime, timedelta
from unittest.mock import Mock

from models import Task, TaskStatus, TaskPriority
from task_list_merge import merge_task_lists, resolve_task_conflict


class TaskListMergeTest(unittest.TestCase):
    def setUp(self):
        """Set up test fixtures before each test method."""
        # Create some sample tasks for testing
        self.task1 = Task("Task 1", "Description 1", TaskPriority.MEDIUM)
        self.task2 = Task("Task 2", "Description 2", TaskPriority.HIGH)
        self.task3 = Task("Task 3", "Description 3", TaskPriority.LOW)
        
        # Set specific IDs for easier testing
        self.task1.id = "task1"
        self.task2.id = "task2"
        self.task3.id = "task3"
        
        # Set specific timestamps for predictable testing
        self.now = datetime.now()
        self.task1.created_at = self.now - timedelta(days=2)
        self.task1.updated_at = self.now - timedelta(days=1)
        self.task2.created_at = self.now - timedelta(days=3)
        self.task2.updated_at = self.now - timedelta(hours=12)
        self.task3.created_at = self.now - timedelta(days=1)
        self.task3.updated_at = self.now - timedelta(hours=6)

    def test_merge_task_lists_only_local(self):
        """Test merging when tasks exist only in local source."""
        local_tasks = {"task1": self.task1, "task2": self.task2}
        remote_tasks = {}
        
        merged, to_create_remote, to_update_remote, to_create_local, to_update_local = merge_task_lists(
            local_tasks, remote_tasks
        )
        
        # Check merged tasks
        self.assertEqual(len(merged), 2)
        self.assertIn("task1", merged)
        self.assertIn("task2", merged)
        
        # Check tasks to create in remote
        self.assertEqual(len(to_create_remote), 2)
        self.assertIn("task1", to_create_remote)
        self.assertIn("task2", to_create_remote)
        
        # Check other lists are empty
        self.assertEqual(len(to_update_remote), 0)
        self.assertEqual(len(to_create_local), 0)
        self.assertEqual(len(to_update_local), 0)

    def test_merge_task_lists_only_remote(self):
        """Test merging when tasks exist only in remote source."""
        local_tasks = {}
        remote_tasks = {"task2": self.task2, "task3": self.task3}
        
        merged, to_create_remote, to_update_remote, to_create_local, to_update_local = merge_task_lists(
            local_tasks, remote_tasks
        )
        
        # Check merged tasks
        self.assertEqual(len(merged), 2)
        self.assertIn("task2", merged)
        self.assertIn("task3", merged)
        
        # Check tasks to create in local
        self.assertEqual(len(to_create_local), 2)
        self.assertIn("task2", to_create_local)
        self.assertIn("task3", to_create_local)
        
        # Check other lists are empty
        self.assertEqual(len(to_create_remote), 0)
        self.assertEqual(len(to_update_remote), 0)
        self.assertEqual(len(to_update_local), 0)

    def test_merge_task_lists_both_sources(self):
        """Test merging when tasks exist in both sources with conflicts."""
        # Create local and remote versions of the same task with different updates
        local_task = Task("Local Task", "Local Description", TaskPriority.MEDIUM)
        local_task.id = "task1"
        local_task.updated_at = self.now - timedelta(days=1)
        local_task.tags = ["local"]
        
        remote_task = Task("Remote Task", "Remote Description", TaskPriority.HIGH)
        remote_task.id = "task1"
        remote_task.updated_at = self.now  # More recent
        remote_task.tags = ["remote"]
        
        local_tasks = {"task1": local_task}
        remote_tasks = {"task1": remote_task}
        
        merged, to_create_remote, to_update_remote, to_create_local, to_update_local = merge_task_lists(
            local_tasks, remote_tasks
        )
        
        # Check merged tasks
        self.assertEqual(len(merged), 1)
        merged_task = merged["task1"]
        
        # Remote is newer, so its fields should be used
        self.assertEqual(merged_task.title, "Remote Task")
        self.assertEqual(merged_task.description, "Remote Description")
        self.assertEqual(merged_task.priority, TaskPriority.HIGH)
        
        # Tags should be merged from both
        self.assertIn("local", merged_task.tags)
        self.assertIn("remote", merged_task.tags)
        
        # Check update lists
        self.assertEqual(len(to_create_remote), 0)
        self.assertEqual(len(to_create_local), 0)
        self.assertEqual(len(to_update_remote), 1)  # Remote needs tag update
        self.assertEqual(len(to_update_local), 1)   # Local needs field updates

    def test_resolve_task_conflict_remote_newer(self):
        """Test resolving conflicts when remote task is newer."""
        local_task = Task("Local Task", "Local Description", TaskPriority.MEDIUM)
        local_task.updated_at = self.now - timedelta(days=1)
        
        remote_task = Task("Remote Task", "Remote Description", TaskPriority.HIGH)
        remote_task.updated_at = self.now  # More recent
        
        merged_task, update_local, update_remote = resolve_task_conflict(local_task, remote_task)
        
        # Remote is newer, so its fields should be used
        self.assertEqual(merged_task.title, "Remote Task")
        self.assertEqual(merged_task.description, "Remote Description")
        self.assertEqual(merged_task.priority, TaskPriority.HIGH)
        
        # Local should be updated, remote should not
        self.assertTrue(update_local)
        self.assertFalse(update_remote)

    def test_resolve_task_conflict_local_newer(self):
        """Test resolving conflicts when local task is newer."""
        local_task = Task("Local Task", "Local Description", TaskPriority.MEDIUM)
        local_task.updated_at = self.now  # More recent
        
        remote_task = Task("Remote Task", "Remote Description", TaskPriority.HIGH)
        remote_task.updated_at = self.now - timedelta(days=1)
        
        merged_task, update_local, update_remote = resolve_task_conflict(local_task, remote_task)
        
        # Local is newer, so its fields should be used
        self.assertEqual(merged_task.title, "Local Task")
        self.assertEqual(merged_task.description, "Local Description")
        self.assertEqual(merged_task.priority, TaskPriority.MEDIUM)
        
        # Remote should be updated, local should not
        self.assertFalse(update_local)
        self.assertTrue(update_remote)

    def test_resolve_task_conflict_completed_status(self):
        """Test resolving conflicts with completed status."""
        local_task = Task("Task", "Description", TaskPriority.MEDIUM)
        local_task.updated_at = self.now
        local_task.status = TaskStatus.TODO
        
        remote_task = Task("Task", "Description", TaskPriority.MEDIUM)
        remote_task.updated_at = self.now - timedelta(days=1)  # Older
        remote_task.status = TaskStatus.DONE
        remote_task.completed_at = self.now - timedelta(days=1)
        
        merged_task, update_local, update_remote = resolve_task_conflict(local_task, remote_task)
        
        # Completed status should win even if the task is older
        self.assertEqual(merged_task.status, TaskStatus.DONE)
        self.assertEqual(merged_task.completed_at, remote_task.completed_at)
        
        # Local should be updated with completed status
        self.assertTrue(update_local)
        self.assertTrue(update_remote)  # Remote should be updated with newer timestamp

    def test_resolve_task_conflict_tag_merging(self):
        """Test that tags are properly merged from both sources."""
        local_task = Task("Task", "Description", TaskPriority.MEDIUM)
        local_task.tags = ["tag1", "tag2"]
        
        remote_task = Task("Task", "Description", TaskPriority.MEDIUM)
        remote_task.tags = ["tag2", "tag3"]
        
        merged_task, update_local, update_remote = resolve_task_conflict(local_task, remote_task)
        
        # Tags should be merged from both sources
        self.assertIn("tag1", merged_task.tags)
        self.assertIn("tag2", merged_task.tags)
        self.assertIn("tag3", merged_task.tags)
        self.assertEqual(len(merged_task.tags), 3)
        
        # Both sources should be updated with the merged tags
        self.assertTrue(update_local)
        self.assertTrue(update_remote)

    def test_resolve_task_conflict_different_non_completed_status(self):
        """Test resolving conflicts with different non-completed statuses."""
        local_task = Task("Task", "Description", TaskPriority.MEDIUM)
        local_task.updated_at = self.now  # More recent
        local_task.status = TaskStatus.IN_PROGRESS
        
        remote_task = Task("Task", "Description", TaskPriority.MEDIUM)
        remote_task.updated_at = self.now - timedelta(days=1)
        remote_task.status = TaskStatus.REVIEW
        
        merged_task, update_local, update_remote = resolve_task_conflict(local_task, remote_task)
        
        # Local is newer, so its status should be used
        self.assertEqual(merged_task.status, TaskStatus.IN_PROGRESS)
        
        # Remote should be updated with local status
        self.assertFalse(update_local)
        self.assertTrue(update_remote)


if __name__ == '__main__':
    unittest.main()