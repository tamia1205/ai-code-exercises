# task_manager/models.py
from datetime import datetime
from enum import Enum
import uuid

class TaskPriority(Enum):
    LOW = 1
    MEDIUM = 2
    HIGH = 3
    URGENT = 4

class TaskStatus(Enum):
    TODO = "todo"
    IN_PROGRESS = "in_progress"
    REVIEW = "review"
    DONE = "done"

class Task:
    def __init__(self, title, description="", priority=TaskPriority.MEDIUM,
                 due_date=None, tags=None):
        self.id = str(uuid.uuid4())
        self.title = title
        self.description = description
        self.priority = priority
        self.status = TaskStatus.TODO
        self.created_at = datetime.now()
        self.updated_at = self.created_at
        self.due_date = due_date
        self.completed_at = None
        self.tags = tags or []

    def update(self, **kwargs):
        for key, value in kwargs.items():
            if hasattr(self, key):
                setattr(self, key, value)
        self.updated_at = datetime.now()

    def mark_as_done(self):
        self.status = TaskStatus.DONE
        self.completed_at = datetime.now()
        self.updated_at = self.completed_at

    def is_overdue(self):
        if not self.due_date:
            return False
        return self.due_date < datetime.now() and self.status != TaskStatus.DONE

