# task_manager/storage.py
import json
import os
from datetime import datetime
from models import Task, TaskPriority, TaskStatus

class TaskEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, Task):
            task_dict = obj.__dict__.copy()
            task_dict['priority'] = obj.priority.value
            task_dict['status'] = obj.status.value
            # Convert datetime objects to ISO format strings
            for key in ['created_at', 'updated_at', 'due_date', 'completed_at']:
                if task_dict.get(key) is not None:
                    task_dict[key] = task_dict[key].isoformat()
            return task_dict
        return super().default(obj)

class TaskDecoder(json.JSONDecoder):
    def __init__(self, *args, **kwargs):
        json.JSONDecoder.__init__(self, object_hook=self.object_hook, *args, **kwargs)

    def object_hook(self, obj):
        if 'id' in obj and 'title' in obj:
            task = Task(obj['title'], obj.get('description', ''))
            task.id = obj['id']
            task.priority = TaskPriority(obj['priority'])
            task.status = TaskStatus(obj['status'])

            # Convert ISO format strings to datetime objects
            for key in ['created_at', 'updated_at', 'completed_at']:
                if obj.get(key):
                    setattr(task, key, datetime.fromisoformat(obj[key]))

            if obj.get('due_date'):
                task.due_date = datetime.fromisoformat(obj['due_date'])

            task.tags = obj.get('tags', [])
            return task
        return obj

class TaskStorage:
    def __init__(self, storage_path="tasks.json"):
        self.storage_path = storage_path
        self.tasks = {}
        self.load()

    def load(self):
        if os.path.exists(self.storage_path):
            try:
                with open(self.storage_path, 'r') as f:
                    tasks_data = json.load(f, cls=TaskDecoder)
                    if isinstance(tasks_data, list):
                        for task in tasks_data:
                            self.tasks[task.id] = task
            except Exception as e:
                print(f"Error loading tasks: {e}")

    def save(self):
        try:
            with open(self.storage_path, 'w') as f:
                json.dump(list(self.tasks.values()), f, cls=TaskEncoder, indent=2)
        except Exception as e:
            print(f"Error saving tasks: {e}")

    def add_task(self, task):
        self.tasks[task.id] = task
        self.save()
        return task.id

    def get_task(self, task_id):
        return self.tasks.get(task_id)

    def update_task(self, task_id, **kwargs):
        task = self.get_task(task_id)
        if task:
            task.update(**kwargs)
            self.save()
            return True
        return False

    def delete_task(self, task_id):
        if task_id in self.tasks:
            del self.tasks[task_id]
            self.save()
            return True
        return False

    def get_all_tasks(self):
        return list(self.tasks.values())

    def get_tasks_by_status(self, status):
        return [task for task in self.tasks.values() if task.status == status]

    def get_tasks_by_priority(self, priority):
        return [task for task in self.tasks.values() if task.priority == priority]

    def get_overdue_tasks(self):
        return [task for task in self.tasks.values() if task.is_overdue()]

