# Task Manager CLI

A command-line interface for managing tasks with features for creating, updating, listing, and analyzing tasks.

## Prerequisites

- Node.js (v12 or higher recommended)
- npm (comes with Node.js)

## Installation

1. Clone this repository or download the source code
2. Navigate to the project directory
3. Install dependencies:

```bash
npm install
```

This will install the required dependencies:
- commander: For command-line interface
- uuid: For generating unique IDs

## Running the CLI

The main entry point is `cli.js`. You can run it directly with Node.js:

```bash
node cli.js [command] [options]
```

For convenience, you can make the script executable (on Unix-based systems):

```bash
chmod +x cli.js
./cli.js [command] [options]
```

Running the script without any commands will display the help menu.

## Available Commands

### Create a new task

```bash
node cli.js create <title> [options]
```

Options:
- `-d, --description <description>`: Task description (default: empty)
- `-p, --priority <priority>`: Task priority (1-4, where 1=LOW, 2=MEDIUM, 3=HIGH, 4=URGENT) (default: 2)
- `-u, --due <due_date>`: Due date in YYYY-MM-DD format
- `-t, --tags <tags>`: Comma-separated list of tags

Example:
```bash
node cli.js create "Complete project" -d "Finish the task manager project" -p 3 -u 2023-12-31 -t "work,coding,important"
```

### List tasks

```bash
node cli.js list [options]
```

Options:
- `-s, --status <status>`: Filter by status (todo, in_progress, review, done)
- `-p, --priority <priority>`: Filter by priority (1-4)
- `-o, --overdue`: Show only overdue tasks

Examples:
```bash
# List all tasks
node cli.js list

# List only tasks with "todo" status
node cli.js list -s todo

# List high priority tasks
node cli.js list -p 3

# List overdue tasks
node cli.js list -o
```

### Update task status

```bash
node cli.js status <task_id> <status>
```

Available statuses:
- `todo`: Task not started
- `in_progress`: Task in progress
- `review`: Task waiting for review
- `done`: Task completed

Example:
```bash
node cli.js status abc123 in_progress
```

### Update task priority

```bash
node cli.js priority <task_id> <priority>
```

Available priorities:
- `1`: LOW
- `2`: MEDIUM
- `3`: HIGH
- `4`: URGENT

Example:
```bash
node cli.js priority abc123 3
```

### Update task due date

```bash
node cli.js due <task_id> <due_date>
```

Example:
```bash
node cli.js due abc123 2023-12-31
```

### Add tag to task

```bash
node cli.js tag <task_id> <tag>
```

Example:
```bash
node cli.js tag abc123 important
```

### Remove tag from task

```bash
node cli.js untag <task_id> <tag>
```

Example:
```bash
node cli.js untag abc123 important
```

### Show task details

```bash
node cli.js show <task_id>
```

Example:
```bash
node cli.js show abc123
```

### Delete a task

```bash
node cli.js delete <task_id>
```

Example:
```bash
node cli.js delete abc123
```

### Show task statistics

```bash
node cli.js stats
```

This command displays:
- Total number of tasks
- Tasks by status
- Tasks by priority
- Number of overdue tasks
- Number of tasks completed in the last 7 days

## Running Tests

The project includes a comprehensive test suite built with Jest. The tests cover all major components of the application:

- Task model tests
- TaskManager tests
- TaskStorage tests
- Integration tests

### Running All Tests

TODO

### Running Specific Test Files

TODO

## Data Storage

Tasks are stored in a JSON file named `tasks.json` in the project directory. This file is created automatically when you add your first task.
