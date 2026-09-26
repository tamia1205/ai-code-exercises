# Task Manager

## Build Instructions

To build the project:

```bash
./gradlew build
```

## Run instructions

To run the project:

```bash
./gradlew run --args="<commands>"
```

Available commands:
- `create <title> [description] [priority] [due_date] [tags]` - Create a new task
- `list [-s <status>] [-p <priority>] [-o]` - List tasks
- `status <task_id> <new_status>` - Update task status
- `priority <task_id> <new_priority>` - Update task priority
- `due <task_id> <new_due_date>` - Update task due date
- `tag <task_id> <tag>` - Add tag to task
- `untag <task_id> <tag>` - Remove tag from task
- `show <task_id>` - Show task details
- `delete <task_id>` - Delete task
- `stats` - Show task statistics

Examples:
```bash
./gradlew run --args="create 'Fix Code' 'I need to get this running' 1 2025-06-01 bugs"

./gradlew run --args="list"
```

## Test instructions

TODO
