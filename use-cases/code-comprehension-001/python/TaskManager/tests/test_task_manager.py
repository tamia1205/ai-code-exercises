from datetime import datetime, timedelta
from unittest.mock import MagicMock
from unittest.mock import Mock
from unittest.mock import Mock, patch
import unittest

from task_manager import TaskManager
from models import TaskPriority, Task, TaskStatus
from storage import TaskStorage


class TaskManagerTest(unittest.TestCase):

    def test_add_tag_to_nonexistent_task(self):
        """
        Test adding a tag to a nonexistent task.
        This test verifies that the method returns False when trying to add a tag to a task that doesn't exist.
        """
        storage_mock = Mock()
        storage_mock.get_task.return_value = None
        task_manager = TaskManager()
        task_manager.storage = storage_mock

        result = task_manager.add_tag_to_task(1, "new_tag")

        self.assertFalse(result)
        storage_mock.get_task.assert_called_once_with(1)
        storage_mock.save.assert_not_called()

    def test_add_tag_to_task_1(self):
        """
        Test adding a new tag to a task when the tag doesn't exist.

        This test verifies that:
        1. The add_tag_to_task method returns True when adding a new tag.
        2. The new tag is actually added to the task's tags list.
        3. The storage.save() method is called to persist the changes.
        """
        # Setup
        task_manager = TaskManager()
        task_id = "task1"
        new_tag = "new_tag"

        # Create a mock task
        mock_task = Mock(spec=Task)
        mock_task.tags = []

        # Mock the storage
        task_manager.storage.get_task = Mock(return_value=mock_task)
        task_manager.storage.save = Mock()

        # Execute
        result = task_manager.add_tag_to_task(task_id, new_tag)

        # Assert
        self.assertTrue(result)
        self.assertIn(new_tag, mock_task.tags)
        task_manager.storage.save.assert_called_once()

    def test_add_tag_to_task_2(self):
        """
        Test adding a tag to a task when the tag already exists.

        This test verifies that the add_tag_to_task method returns True
        when trying to add a tag that already exists in the task's tags.
        It ensures that duplicate tags are not added and the method
        behaves correctly in this scenario.
        """
        # Create a TaskManager instance
        task_manager = TaskManager()

        # Create a task with an existing tag
        task = Task("Test Task", "Description", TaskPriority.MEDIUM, datetime.now(), ["existing_tag"])
        task_id = task_manager.storage.add_task(task)

        # Attempt to add the existing tag
        result = task_manager.add_tag_to_task(task_id, "existing_tag")

        # Assert that the method returns True
        self.assertTrue(result)

        # Verify that the tag was not added again
        updated_task = task_manager.get_task_details(task_id)
        self.assertEqual(len(updated_task.tags), 1)
        self.assertIn("existing_tag", updated_task.tags)

    def test_create_task_2(self):
        """
        Test creating a task without a due date.

        This test verifies that the create_task method correctly handles
        the case when no due_date_str is provided. It checks that the task
        is created with the given title, description, and priority, and
        that the due_date is set to None.
        """
        storage_mock = Mock()
        storage_mock.add_task.return_value = "task_id_1"

        task_manager = TaskManager()
        task_manager.storage = storage_mock

        task_id = task_manager.create_task(
            title="Test Task",
            description="Test Description",
            priority_value=1
        )

        assert task_id == "task_id_1"
        storage_mock.add_task.assert_called_once()
        created_task = storage_mock.add_task.call_args[0][0]
        assert isinstance(created_task, Task)
        assert created_task.title == "Test Task"
        assert created_task.description == "Test Description"
        assert created_task.priority == TaskPriority(1)
        assert created_task.due_date is None

    def test_create_task_invalid_date_format(self):
        """
        Test that create_task handles an invalid date format correctly.
        """
        task_manager = TaskManager()
        result = task_manager.create_task("Test Task", due_date_str="2023/05/01")
        self.assertIsNone(result)

    def test_create_task_with_invalid_date_format(self):
        """
        Test creating a task with an invalid date format.
        Ensures that the method returns None when an invalid date format is provided.
        """
        task_manager = TaskManager()
        task_id = task_manager.create_task("Test Task", "Description", 2, "2023/05/15")
        self.assertIsNone(task_id)

    def test_delete_nonexistent_task(self):
        """
        Test deleting a task with a non-existent task_id.
        This test verifies that the delete_task method returns False when attempting to delete a task that doesn't exist.
        """
        task_manager = TaskManager()
        non_existent_task_id = "nonexistent_id"
        result = task_manager.delete_task(non_existent_task_id)
        self.assertFalse(result)

    def test_delete_task_1(self):
        """
        Test that delete_task method calls storage.delete_task with the correct task_id
        and returns the result of storage.delete_task.
        """
        # Setup
        task_manager = TaskManager()
        task_manager.storage = Mock()
        task_manager.storage.delete_task.return_value = True

        # Execute
        result = task_manager.delete_task(1)

        # Assert
        task_manager.storage.delete_task.assert_called_once_with(1)
        self.assertTrue(result)

    def test_get_statistics_1(self):
        """
        Test the get_statistics method of TaskManager class.

        This test verifies that the get_statistics method correctly calculates
        and returns statistics for tasks, including total count, counts by status
        and priority, overdue tasks, and tasks completed in the last week.
        """
        # Mock the TaskStorage
        mock_storage = Mock()

        # Create sample tasks
        task1 = Task("Task 1", "Description 1", TaskPriority.MEDIUM, datetime.now() + timedelta(days=1))
        task2 = Task("Task 2", "Description 2", TaskPriority.HIGH, datetime.now() - timedelta(days=1))
        task2.status = TaskStatus.DONE
        task2.completed_at = datetime.now() - timedelta(days=2)
        task3 = Task("Task 3", "Description 3", TaskPriority.LOW, datetime.now() + timedelta(days=2))
        task3.status = TaskStatus.IN_PROGRESS

        # Set up the mock to return the sample tasks
        mock_storage.get_all_tasks.return_value = [task1, task2, task3]

        # Create TaskManager instance with mocked storage
        task_manager = TaskManager()
        task_manager.storage = mock_storage

        # Call the method under test
        result = task_manager.get_statistics()

        # Assert the expected results
        self.assertEqual(result["total"], 3)
        self.assertEqual(result["by_status"], {
            "todo": 1,
            "in_progress": 1,
            "done": 1,
            "review": 0
        })
        self.assertEqual(result["by_priority"], {
            "LOW": 1,
            "MEDIUM": 1,
            "HIGH": 1,
            "URGENT": 0
        })
        self.assertEqual(0, result["overdue"])
        self.assertEqual(1, result["completed_last_week"])

    def test_get_statistics_with_empty_task_list(self):
        """
        Test get_statistics method when the task list is empty.
        This is an edge case where the method should handle having no tasks gracefully.
        """
        task_manager = TaskManager()
        task_manager.storage = Mock()
        task_manager.storage.get_all_tasks.return_value = []

        stats = task_manager.get_statistics()

        self.assertEqual(stats['total'], 0)
        self.assertEqual(stats['by_status'], {status.value: 0 for status in TaskStatus})
        self.assertEqual(stats['by_priority'], {priority.name: 0 for priority in TaskPriority})
        self.assertEqual(stats['overdue'], 0)
        self.assertEqual(stats['completed_last_week'], 0)

    def test_get_task_details_1(self):
        """
        Test that get_task_details correctly retrieves task details from storage.
        """
        mock_storage = Mock()
        task_manager = TaskManager(storage_path="test_tasks.json")
        task_manager.storage = mock_storage

        test_task = Task("Test Task", "Description", TaskPriority.MEDIUM, datetime.now(), ["tag1", "tag2"])
        mock_storage.get_task.return_value = test_task

        result = task_manager.get_task_details("task_id_1")

        mock_storage.get_task.assert_called_once_with("task_id_1")
        self.assertEqual(result, test_task)

    def test_get_task_details_nonexistent_task(self):
        """
        Test the get_task_details method with a non-existent task ID.
        This test verifies that the method returns None when trying to retrieve details for a task that does not exist.
        """
        task_manager = TaskManager()
        non_existent_task_id = "non_existent_id"
        result = task_manager.get_task_details(non_existent_task_id)
        self.assertIsNone(result)

    def test_list_tasks_1(self):
        """
        Test that list_tasks returns overdue tasks when show_overdue is True.
        """
        # Create a mock storage
        mock_storage = Mock()
        mock_storage.get_overdue_tasks.return_value = [Mock(), Mock()]  # Simulating two overdue tasks

        # Create TaskManager instance with mock storage
        task_manager = TaskManager()
        task_manager.storage = mock_storage

        # Call the method under test
        result = task_manager.list_tasks(show_overdue=True)

        # Assert that the correct method was called on the storage
        mock_storage.get_overdue_tasks.assert_called_once()

        # Assert that the result is what we expect
        self.assertEqual(result, mock_storage.get_overdue_tasks.return_value)
        self.assertEqual(len(result), 2)

    def test_list_tasks_3(self):
        """
        Test that list_tasks returns tasks filtered by priority when priority_filter is provided.
        """
        # Setup
        mock_storage = Mock()
        task_manager = TaskManager()
        task_manager.storage = mock_storage

        # Test
        priority_filter = 2
        task_manager.list_tasks(priority_filter=priority_filter)

        # Assert
        mock_storage.get_tasks_by_priority.assert_called_once_with(TaskPriority(priority_filter))

    def test_list_tasks_4(self):
        """
        Test that list_tasks returns all tasks when no filters are applied.

        This test verifies that when calling list_tasks without any filters
        (status_filter=None, priority_filter=None, show_overdue=False),
        the method returns the result of self.storage.get_all_tasks().
        """
        # Create a mock storage
        mock_storage = Mock()
        mock_storage.get_all_tasks.return_value = ["task1", "task2", "task3"]

        # Create TaskManager instance with mock storage
        task_manager = TaskManager()
        task_manager.storage = mock_storage

        # Call list_tasks without any filters
        result = task_manager.list_tasks()

        # Assert that get_all_tasks was called and its result was returned
        mock_storage.get_all_tasks.assert_called_once()
        self.assertEqual(result, ["task1", "task2", "task3"])

    def test_list_tasks_filtered_by_status(self):
        """
        Test that list_tasks correctly filters tasks by status when status_filter is provided.

        This test verifies that:
        1. The method calls get_tasks_by_status on the storage object
        2. The correct TaskStatus is passed to get_tasks_by_status
        3. The method returns the result from get_tasks_by_status
        """
        # Setup
        mock_storage = Mock()
        task_manager = TaskManager()
        task_manager.storage = mock_storage

        # Define test data
        status_filter = "in_progress"
        expected_status = TaskStatus.IN_PROGRESS
        expected_tasks = [Mock(), Mock()]
        mock_storage.get_tasks_by_status.return_value = expected_tasks

        # Execute
        result = task_manager.list_tasks(status_filter=status_filter)

        # Assert
        mock_storage.get_tasks_by_status.assert_called_once_with(expected_status)
        self.assertEqual(result, expected_tasks)

    def test_list_tasks_invalid_priority_filter(self):
        """
        Test list_tasks method with an invalid priority filter.
        This test verifies that the method raises a ValueError when an invalid priority is provided.
        """
        task_manager = TaskManager()
        with self.assertRaises(ValueError):
            task_manager.list_tasks(priority_filter=10)

    def test_list_tasks_invalid_status_filter(self):
        """
        Test list_tasks method with an invalid status filter.
        This test verifies that the method raises a ValueError when an invalid status is provided.
        """
        task_manager = TaskManager()
        with self.assertRaises(ValueError):
            task_manager.list_tasks(status_filter="INVALID_STATUS")

    def test_remove_nonexistent_tag_from_task(self):
        """
        Test removing a tag that doesn't exist from a task.
        This tests the edge case where the specified tag is not present in the task's tags.
        """
        task_manager = TaskManager()
        task_id = task_manager.create_task("Test Task", tags=["existing_tag"])
        result = task_manager.remove_tag_from_task(task_id, "nonexistent_tag")
        assert result == False

    def test_remove_tag_from_nonexistent_task(self):
        """
        Test removing a tag from a task that doesn't exist.
        This tests the edge case where the task_id doesn't correspond to any existing task.
        """
        task_manager = TaskManager()
        result = task_manager.remove_tag_from_task("nonexistent_id", "tag")
        assert result == False

    def test_remove_tag_from_task_1(self):
        """
        Test removing an existing tag from a task.

        This test verifies that the remove_tag_from_task method correctly
        removes a tag from a task when both the task and the tag exist.
        It checks if the method returns True and if the tag is actually
        removed from the task's tags list.
        """
        task_manager = TaskManager()
        task_id = "1"
        tag_to_remove = "important"

        mock_task = Task("Test Task", "Description", TaskPriority.MEDIUM, None, ["important", "work"])
        task_manager.storage.get_task = MagicMock(return_value=mock_task)
        task_manager.storage.save = MagicMock()

        result = task_manager.remove_tag_from_task(task_id, tag_to_remove)

        assert result == True
        assert tag_to_remove not in mock_task.tags
        task_manager.storage.save.assert_called_once()

    def test_remove_tag_from_task_2(self):
        """
        Test removing a tag from a task when the task doesn't exist or the tag is not in the task's tags.

        This test verifies that the remove_tag_from_task method returns False when:
        1. The task does not exist in the storage
        2. The task exists but the specified tag is not in the task's tags
        """
        # Create a mock storage
        mock_storage = MagicMock()

        # Create a TaskManager instance with the mock storage
        task_manager = TaskManager()
        task_manager.storage = mock_storage

        # Case 1: Task does not exist
        mock_storage.get_task.return_value = None
        result = task_manager.remove_tag_from_task(1, "test_tag")
        assert result == False

        # Case 2: Task exists but tag is not in task's tags
        mock_task = Task("Test Task", priority=TaskPriority.MEDIUM)
        mock_storage.get_task.return_value = mock_task
        result = task_manager.remove_tag_from_task(1, "non_existent_tag")
        assert result == False

        # Verify that save method was not called
        mock_storage.save.assert_not_called()

    def test_update_task_due_date_1(self):
        """
        Test updating a task's due date with a valid date string.

        This test verifies that the update_task_due_date method correctly
        parses a valid date string, converts it to a datetime object,
        and calls the storage's update_task method with the correct parameters.
        """
        task_manager = TaskManager()
        task_manager.storage = Mock()
        task_manager.storage.update_task.return_value = True

        task_id = 1
        due_date_str = "2023-12-31"
        expected_due_date = datetime(2023, 12, 31)

        result = task_manager.update_task_due_date(task_id, due_date_str)

        self.assertTrue(result)
        task_manager.storage.update_task.assert_called_once_with(task_id, due_date=expected_due_date)

    def test_update_task_due_date_with_invalid_format(self):
        """
        Test updating a task's due date with an invalid date format.
        This should trigger the ValueError exception handling in the method.
        """
        task_manager = TaskManager()
        task_id = 1
        invalid_date = "2023/05/01"  # Invalid format, should be YYYY-MM-DD

        result = task_manager.update_task_due_date(task_id, invalid_date)

        assert result == False, "Method should return False for invalid date format"

    def test_update_task_priority_1(self):
        """
        Test that update_task_priority correctly updates the priority of a task.

        This test verifies that the update_task_priority method of TaskManager
        successfully updates the priority of a given task and returns the result
        from the storage.update_task method.
        """
        task_manager = TaskManager()
        task_id = "test_task_id"
        new_priority_value = 3

        result = task_manager.update_task_priority(task_id, new_priority_value)

        assert result == task_manager.storage.update_task(task_id, priority=TaskPriority(new_priority_value))

    def test_update_task_priority_invalid_priority(self):
        """
        Test updating a task's priority with an invalid priority value.
        This should raise a ValueError as TaskPriority enum will reject invalid values.
        """
        task_manager = TaskManager()
        task_id = 1  # Assuming a task with ID 1 exists
        invalid_priority = 5  # Assuming valid priorities are 1, 2, 3

        with self.assertRaises(ValueError):
            task_manager.update_task_priority(task_id, invalid_priority)

    def test_update_task_status_1(self):
        """
        Test updating task status to DONE when the task exists.

        This test verifies that when a task is updated to DONE status:
        1. The task is retrieved from storage
        2. The task is marked as done
        3. The changes are saved to storage
        4. The method returns True
        """
        # Create a mock storage
        mock_storage = Mock()

        # Create a mock task
        mock_task = Mock(spec=Task)

        # Configure the mock storage to return the mock task
        mock_storage.get_task.return_value = mock_task

        # Create a TaskManager instance with the mock storage
        task_manager = TaskManager()
        task_manager.storage = mock_storage

        # Call the method under test
        result = task_manager.update_task_status("task_id", TaskStatus.DONE.value)

        # Verify the expected behavior
        mock_storage.get_task.assert_called_once_with("task_id")
        mock_task.mark_as_done.assert_called_once()
        mock_storage.save.assert_called_once()
        self.assertTrue(result)

    def test_update_task_status_2(self):
        """
        Test updating a task status to a non-DONE status.

        This test verifies that when updating a task status to a value other than DONE,
        the method calls the storage's update_task method with the correct parameters
        and returns its result.
        """
        # Setup
        task_manager = TaskManager()
        task_manager.storage = MagicMock()
        task_manager.storage.update_task.return_value = True

        # Execute
        result = task_manager.update_task_status(1, TaskStatus.IN_PROGRESS.value)

        # Assert
        task_manager.storage.update_task.assert_called_once_with(1, status=TaskStatus.IN_PROGRESS)
        self.assertTrue(result)

    def test_update_task_status_non_done_status(self):
        """
        Test updating a task status to a non-DONE status.
        This test verifies that the method correctly handles updating to statuses other than DONE.
        """
        storage_mock = Mock()
        storage_mock.update_task.return_value = True
        task_manager = TaskManager()
        task_manager.storage = storage_mock

        result = task_manager.update_task_status("task_id", TaskStatus.IN_PROGRESS.value)

        self.assertTrue(result)
        storage_mock.update_task.assert_called_once_with("task_id", status=TaskStatus.IN_PROGRESS)
        storage_mock.get_task.assert_not_called()
        storage_mock.save.assert_not_called()

    def test_update_task_status_nonexistent_task(self):
        """
        Test updating the status of a non-existent task.
        This test verifies that the method handles the case where the task_id doesn't exist.
        """
        storage_mock = Mock()
        storage_mock.get_task.return_value = None
        task_manager = TaskManager()
        task_manager.storage = storage_mock

        result = task_manager.update_task_status("nonexistent_id", TaskStatus.DONE.value)

        self.assertFalse(result)
        storage_mock.get_task.assert_called_once_with("nonexistent_id")
        storage_mock.save.assert_not_called()

    def test_update_task_status_when_new_status_is_done_and_task_not_found(self):
        """
        Test updating a task status to DONE when the task is not found.

        This test verifies that when attempting to update a task's status to DONE,
        and the task is not found in the storage, the method returns False.
        """
        # Arrange
        task_manager = TaskManager()
        task_manager.storage = MagicMock()
        task_manager.storage.get_task.return_value = None

        # Act
        result = task_manager.update_task_status("non_existent_task_id", TaskStatus.DONE.value)

        # Assert
        self.assertFalse(result)
        task_manager.storage.get_task.assert_called_once_with("non_existent_task_id")
        task_manager.storage.save.assert_not_called()
