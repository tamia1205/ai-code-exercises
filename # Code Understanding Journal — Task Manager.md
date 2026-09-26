# Code Understanding Journal — Task Manager

## Part 1 — Understanding a Specific Feature: Task Creation & Status Updates

**Files involved:** `models.py` (`Task.__init__`, `TaskStatus`), `app.py` (`TaskManager.create_task`, `update_task_status`), `storage.py` (`add_task`, `update_task`, `save`), `cli.py` (`create`/`status` subcommands)

### What this component does (in simple terms)
Creating a task takes raw CLI input (strings, an integer), converts it into proper domain types (a `TaskPriority` enum, a parsed `datetime`), builds a `Task` object, and hands it to `TaskStorage`, which keeps it in an in-memory dict *and* immediately rewrites the whole `tasks.json` file. Updating a task's status does the same round-trip, but with a quirk: reaching `DONE` is special-cased.

### Execution flow — creating a task
1. `cli.py` parses the `create` subcommand args.
2. `TaskManager.create_task()` converts the priority int → `TaskPriority`, parses the due-date string → `datetime` (or bails with an error message on bad format).
3. A `Task(...)` is constructed — its `__init__` auto-generates a UUID `id`, sets `status=TODO`, and stamps `created_at`.
4. `TaskStorage.add_task(task)` stores it in `self.tasks[task.id]` and immediately calls `save()`.
5. `save()` serializes **every** task (not just the new one) via `TaskEncoder` and overwrites `tasks.json`.

### Execution flow — updating status
1. `cli.py`'s `status` subcommand calls `TaskManager.update_task_status(task_id, new_status_value)`.
2. The value is converted to a `TaskStatus` enum.
3. **Branch A (`DONE`):** fetch the task via `storage.get_task(task_id)`; if found, call `task.mark_as_done()` directly (mutates `status` + `completed_at` + `updated_at`), then call `storage.save()` explicitly.
4. **Branch B (anything else):** call `storage.update_task(task_id, status=new_status)`, which internally calls `task.update(**kwargs)` then `save()`.

**Interesting asymmetry:** these two branches persist the same kind of change through two different code paths — one mutates-then-saves manually, the other goes through a generic `update()` helper. They *look* equivalent but aren't structured the same way, and (see Part 3) don't behave identically on the "task not found" case.

### How data is stored and retrieved
- Everything lives in one in-memory dict `{task_id: Task}}`, loaded once at startup (`TaskStorage.__init__` → `load()`).
- Every mutating call triggers an immediate **full-file rewrite** of `tasks.json` — this is a write-through, no-batching persistence model.
- Retrieval is always from memory; the file is only read once, at startup.

### Design patterns discovered
- **Repository-style abstraction**: `TaskStorage` hides the JSON file entirely behind CRUD-style methods.
- **Custom Encoder/Decoder pattern**: `TaskEncoder`/`TaskDecoder` handle turning enums into `.value` strings and datetimes into ISO strings (and back), so the rest of the app never deals with raw JSON types.
- **Write-through caching**: every mutation is immediately flushed to disk rather than batched — simple, but means every operation pays a full-file I/O cost.

### Requirements for 3 small changes to validate understanding
*(Requirements only — not the actual code changes.)*
1. Confirm that `TaskStorage.save()` is called exactly once per `create_task()` call and once per `update_task_status()` call — i.e., prove the write-through behavior is real and not batched.
2. Determine whether moving a task **out of** `DONE` (e.g., back to `todo`) clears its `completed_at` timestamp, or leaves a stale completion date behind.
3. Determine whether calling `update_task_status(id, "done")` twice in a row updates `completed_at` both times, or only the first time — i.e., check whether marking-as-done is idempotent.

---

## Part 2 — Deepening Understanding: Task Prioritization (Guided Questions)

### My initial understanding (before questioning)
I assumed `TaskPriority` did more than label a task — that it probably affected sort order in `list` output, and maybe factored into whether something counted as "overdue."

### Guided questions I worked through
1. In `list_tasks()`, when both `status_filter` and `priority_filter` are passed at once, does the code actually apply both, or does the `if/elif` chain only ever honor one of them?
2. In `format_task()`, priority renders as `!` through `!!!!` — is there any sorting step anywhere before display, or is that symbol purely cosmetic?
3. In `get_statistics()`, priority counts and overdue counts are both computed — do they ever get cross-tabulated (e.g., "overdue high-priority tasks"), or are they always independent?
4. `Task.is_overdue()` never references `priority` at all — what does that imply about whether priority currently affects "overdue" status?
5. If a future rule needed HIGH/URGENT tasks to be exempt from something, which single method would need to change, and would that break any existing callers?

### What I discovered by examining the code further
- `list_tasks()` uses an `if/elif/elif` chain: `show_overdue` → `status_filter` → `priority_filter` → all tasks. **If both a status and a priority filter are given, only the status branch runs — the priority filter is silently ignored.** This is a real limitation I hadn't noticed on first read.
- Nothing sorts by priority anywhere; tasks are always returned/displayed in dict insertion order. The priority symbol is purely visual.
- `get_statistics()` keeps `by_status` and `by_priority` as two completely separate dicts — there's no cross-tabulation (no "overdue-and-high-priority" count today).
- `is_overdue()` truly ignores priority, confirming priority currently has **zero effect on due-date logic**.

### Misconceptions clarified
- Priority does *not* drive sort order or overdue status today — it's currently just a filterable/display attribute.
- Discovering the `if/elif` filter gap was the biggest surprise; it means `list --status todo --priority 3` silently behaves like `list --status todo` alone.

---

## Part 3 — Mapping Data Flow: Marking a Task Complete

### Entry point
`cli.py`'s `status` subcommand, invoked as `status <task_id> done` → `main()` dispatch → `TaskManager.update_task_status(task_id, "done")`.

### Complete data flow
1. **Input**: `task_id` (string) and `"done"` (string) from CLI args.
2. `update_task_status()` converts `"done"` → `TaskStatus.DONE`.
3. Since `new_status == DONE`: `task = storage.get_task(task_id)` — a plain dict lookup.
4. If found: `task.mark_as_done()` mutates the object in place — `status → DONE`, `completed_at → now`, `updated_at → completed_at`.
5. `storage.save()` re-serializes **all** tasks via `TaskEncoder` (enums → `.value`, datetimes → `.isoformat()`) and overwrites `tasks.json` in full.
6. The boolean-ish result bubbles back to `cli.py`, which prints a success or failure message.

### Where state changes occur
- In-memory: direct attribute mutation on the `Task` object (no copy, no event/observer pattern).
- On disk: a full-file overwrite of `tasks.json`, not an incremental/append write.

### Potential points of failure I found
- **Inconsistent return value**: if `new_status == DONE` and the task **isn't found**, the function falls through without an explicit `return`, implicitly returning `None`. The non-`DONE` branch returns an explicit `True`/`False` from `storage.update_task()`. Both are falsy on failure, so `cli.py`'s `if task_manager.update_task_status(...)` still happens to work — but the inconsistent return type is fragile and would bite anyone checking `is False` instead of truthiness.
- **Silent write failures**: `storage.save()` wraps the file write in `try/except` and just prints an error — it never raises or signals failure to the caller. That means `update_task_status()` can return `True` (in-memory succeeded) while the on-disk file silently failed to update, leaving memory and disk out of sync.
- **No file locking**: two concurrent CLI invocations writing `tasks.json` at once could race and clobber each other's changes, since it's a full-file overwrite with no locking.

### How changes are persisted
Every completion writes the **entire** task list back to `tasks.json` — there's no per-record update, no transaction log, and no rollback if the write is interrupted mid-way.

---

## Part 4 — Reflection & Presentation Outline (3–5 min)

**1. High-level architecture**
A dependency-free Python CLI app in four layers: `models.py` (domain), `storage.py` (JSON persistence, repository-style), `app.py` (`TaskManager` service layer), `cli.py` (argparse presentation layer / entry point).

**2. How the three features work**
- *Creation*: CLI → validate/convert input → build `Task` → store + write-through save.
- *Prioritization*: a stored attribute (`TaskPriority`) used only for filtering and a cosmetic symbol — it doesn't affect sorting or overdue logic today.
- *Completion*: CLI → `update_task_status` → special-cased `mark_as_done()` mutation → full-file re-save.

**3. Most interesting design pattern**
The custom `TaskEncoder`/`TaskDecoder` pair, paired with write-through persistence — every single mutation triggers a full re-serialization of the entire task list to disk, trading efficiency for simplicity (no partial writes, no diffing logic to get wrong).

**4. What was most challenging, and how the prompts helped**
The hardest thing to catch by just reading the code was the **asymmetry between the `DONE` and non-`DONE` update paths** (different return types on failure) and the **silent priority-filter gap** in `list_tasks()`. The guided-questioning prompt (Prompt 2) was what surfaced the filter bug — being asked to trace the `if/elif` chain myself, rather than being told the answer, is what made me actually find it. The data-flow prompt (Prompt 3) was what surfaced the failure-mode issues (inconsistent return values, silent save failures) by forcing me to ask "what happens if this step fails?" at every stage.

**5. Prompt strategy takeaways**
- Use the *direct-explanation* prompt (Prompt 1) when you need a fast, correct mental model of a self-contained component.
- Use the *guided-questioning* prompt (Prompt 2) when you want to actually find edge cases and bugs yourself, not just be told about them.
- Use the *data-flow* prompt (Prompt 3) specifically for tracing state changes and failure points across multiple files — it's the best fit when the risk is "what happens when this breaks," not just "what does this do."
