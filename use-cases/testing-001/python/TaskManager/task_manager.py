import argparse
from datetime import datetime, timedelta

from models import TaskPriority, Task, TaskStatus
from storage import TaskStorage


class TaskManager:
    def __init__(self, storage_path="tasks.json"):
        self.storage = TaskStorage(storage_path)

    def create_task(self, title, description="", priority_value=2,
                   due_date_str=None, tags=None):
        priority = TaskPriority(priority_value)
        due_date = None
        if due_date_str:
            try:
                due_date = datetime.strptime(due_date_str, "%Y-%m-%d")
            except ValueError:
                print("Invalid date format. Use YYYY-MM-DD")
                return None

        task = Task(title, description, priority, due_date, tags)
        task_id = self.storage.add_task(task)
        return task_id

    def list_tasks(self, status_filter=None, priority_filter=None, show_overdue=False):
        if show_overdue:
            return self.storage.get_overdue_tasks()

        if status_filter:
            status = TaskStatus(status_filter)
            return self.storage.get_tasks_by_status(status)

        if priority_filter:
            priority = TaskPriority(priority_filter)
            return self.storage.get_tasks_by_priority(priority)

        return self.storage.get_all_tasks()

    def update_task_status(self, task_id, new_status_value):
        new_status = TaskStatus(new_status_value)
        if new_status == TaskStatus.DONE:
            task = self.storage.get_task(task_id)
            if task:
                task.mark_as_done()
                self.storage.save()
                return True
        else:
            return self.storage.update_task(task_id, status=new_status)

    def update_task_priority(self, task_id, new_priority_value):
        new_priority = TaskPriority(new_priority_value)
        return self.storage.update_task(task_id, priority=new_priority)

    def update_task_due_date(self, task_id, due_date_str):
        try:
            due_date = datetime.strptime(due_date_str, "%Y-%m-%d")
            return self.storage.update_task(task_id, due_date=due_date)
        except ValueError:
            print("Invalid date format. Use YYYY-MM-DD")
            return False

    def delete_task(self, task_id):
        return self.storage.delete_task(task_id)

    def get_task_details(self, task_id):
        return self.storage.get_task(task_id)

    def add_tag_to_task(self, task_id, tag):
        task = self.storage.get_task(task_id)
        if task:
            if tag not in task.tags:
                task.tags.append(tag)
                self.storage.save()
            return True
        return False

    def remove_tag_from_task(self, task_id, tag):
        task = self.storage.get_task(task_id)
        if task and tag in task.tags:
            task.tags.remove(tag)
            self.storage.save()
            return True
        return False

    def get_statistics(self):
        tasks = self.storage.get_all_tasks()
        total = len(tasks)

        # Count by status
        status_counts = {status.value: 0 for status in TaskStatus}
        for task in tasks:
            status_counts[task.status.value] += 1

        # Count by priority
        priority_counts = {priority.name: 0 for priority in TaskPriority}
        for task in tasks:
            priority_counts[task.priority.name] += 1

        # Count overdue
        overdue_count = len([task for task in tasks if task.is_overdue()])

        # Count completed in last 7 days
        seven_days_ago = datetime.now() - timedelta(days=7)
        completed_recently = len([
            task for task in tasks
            if task.completed_at and task.completed_at >= seven_days_ago
        ])

        return {
            "total": total,
            "by_status": status_counts,
            "by_priority": priority_counts,
            "overdue": overdue_count,
            "completed_last_week": completed_recently
        }

