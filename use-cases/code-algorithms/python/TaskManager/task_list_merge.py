import copy

from models import TaskStatus, TaskPriority

def merge_task_lists(local_tasks, remote_tasks):
    """
    Merge two task lists with conflict resolution.

    Args:
        local_tasks: Dictionary of tasks from local source {task_id: task}
        remote_tasks: Dictionary of tasks from remote source {task_id: task}

    Returns:
        tuple: (
            merged tasks dictionary,
            tasks to be created in remote,
            tasks to be updated in remote,
            tasks to be created in local,
            tasks to be updated in local
        )
    """
    merged_tasks = {}
    to_create_remote = {}
    to_update_remote = {}
    to_create_local = {}
    to_update_local = {}

    # Step 1: Identify all unique task IDs across both sources
    all_task_ids = set(local_tasks.keys()) | set(remote_tasks.keys())

    for task_id in all_task_ids:
        local_task = local_tasks.get(task_id)
        remote_task = remote_tasks.get(task_id)

        # Case 1: Task exists only locally - add to remote
        if local_task and not remote_task:
            merged_tasks[task_id] = local_task
            to_create_remote[task_id] = local_task

        # Case 2: Task exists only in remote - add to local
        elif not local_task and remote_task:
            merged_tasks[task_id] = remote_task
            to_create_local[task_id] = remote_task

        # Case 3: Task exists in both - resolve conflicts
        else:
            merged_task, should_update_local, should_update_remote = resolve_task_conflict(
                local_task, remote_task
            )

            merged_tasks[task_id] = merged_task

            if should_update_local:
                to_update_local[task_id] = merged_task

            if should_update_remote:
                to_update_remote[task_id] = merged_task

    return (
        merged_tasks,
        to_create_remote,
        to_update_remote,
        to_create_local,
        to_update_local
    )

def resolve_task_conflict(local_task, remote_task):
    """
    Resolve conflicts between two versions of the same task.

    Returns:
        tuple: (
            merged task,
            should_update_local (bool),
            should_update_remote (bool)
        )
    """
    # Make a copy of the local task to use as our base
    merged_task = copy.deepcopy(local_task)

    # Track if we need to update either source
    should_update_local = False
    should_update_remote = False

    # Most recent update wins for most fields
    if remote_task.updated_at > local_task.updated_at:
        # Remote task is newer, update local fields
        merged_task.title = remote_task.title
        merged_task.description = remote_task.description
        merged_task.priority = remote_task.priority
        merged_task.due_date = remote_task.due_date
        should_update_local = True
    else:
        # Local task is newer or same age, update remote fields
        should_update_remote = True

    # Special handling for completed status - completed wins over not completed
    if remote_task.status == TaskStatus.DONE and local_task.status != TaskStatus.DONE:
        merged_task.status = TaskStatus.DONE
        merged_task.completed_at = remote_task.completed_at
        should_update_local = True
    elif local_task.status == TaskStatus.DONE and remote_task.status != TaskStatus.DONE:
        # Keep local status (already in merged_task)
        should_update_remote = True
    elif remote_task.status != local_task.status:
        # Different non-completed status - most recent wins
        if remote_task.updated_at > local_task.updated_at:
            merged_task.status = remote_task.status
            should_update_local = True
        else:
            # Keep local status (already in merged_task)
            should_update_remote = True

    # Merge tags from both sources (union)
    all_tags = set(local_task.tags) | set(remote_task.tags)
    merged_task.tags = list(all_tags)

    # If tags changed in either source, update both
    if set(merged_task.tags) != set(local_task.tags):
        should_update_local = True
    if set(merged_task.tags) != set(remote_task.tags):
        should_update_remote = True

    # Update the timestamp to latest
    merged_task.updated_at = max(local_task.updated_at, remote_task.updated_at)

    return merged_task, should_update_local, should_update_remote
