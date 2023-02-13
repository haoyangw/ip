package io.github.haoyangw.rica.task;

import io.github.haoyangw.rica.exception.RicaStorageException;
import io.github.haoyangw.rica.exception.RicaTaskException;
import io.github.haoyangw.rica.util.StorageManager;

import java.util.ArrayList;

public class TaskManager {
    private static final String ADD_PHRASE = " New %s I'll remember: ";
    private static final String BAD_TASK_INDEX_ERROR = " Invalid task number? Are you trying too hard to avoid work xP";
    private static final String DEADLINE_CMD = "deadline";
    private static final String DELETE_CMD = "delete";
    private static final String EVENT_CMD = "event";
    private static final String INVALID_TASK_INDEX_ERROR = " You alright? I can't mark a task that doesn't exist as done xD";
    private static final String NO_TASKS_TO_DELETE_ERROR = " Add a task first... Then ask me to delete one xD";
    private static final String NOT_A_TASK_INDEX_ERROR = " Fat fingers? That's not a task number LOL";
    private static final String SINGLE_TASK_ADDED_PHRASE = " You have %d task for now, all the best!";
    private static final String TASK_ADDED_PHRASE = " You have %d tasks for now, all the best!";
    private static final String TASK_REMAINING_PHRASE = " Wow, just 1 task left! Congratulations, come party with me when you're done with work!";
    private static final String TASK_REMOVED_PHRASE = " I have removed this task for you:";
    private static final String TASKS_REMAINING_PHRASE = " Let's see... You now have %d tasks left. Keep going!";
    private static final String TODO_CMD = "todo";
    private static final String WRONG_CMD_ERROR = " Hello wrong command for %s! Check again?";
    private static final String WRONG_TASK_TYPE = " Erm I don't think this task can be marked done xD";
    private final StorageManager storageManager;
    private ArrayList<Task> tasks;

    public TaskManager() {
        storageManager = new StorageManager();
        try {
            tasks = storageManager.getSavedTasks();
        } catch (RicaStorageException exception) {
            TaskManager.printlnWithIndent(exception.getMessage());
            tasks = new ArrayList<>();
        }
    }

    private void addTask(Task newTask) {
        if (newTask != null) {
            this.getTasks().add(newTask);
        }
    }

    private void createTask(String typeOfTask, String command) {
        switch (typeOfTask) {
        case TaskManager.TODO_CMD:
            Todo newTodo = Todo.create(command);
            this.addTask(newTodo);
            TaskManager.printlnWithIndent(String.format(TaskManager.ADD_PHRASE,
                    TaskManager.TODO_CMD));
            TaskManager.printlnWithIndent("   " + newTodo.toString());
            break;
        case TaskManager.DEADLINE_CMD:
            Deadline newDeadline = Deadline.create(command);
            this.addTask(newDeadline);
            TaskManager.printlnWithIndent(String.format(TaskManager.ADD_PHRASE,
                    TaskManager.DEADLINE_CMD));
            TaskManager.printlnWithIndent("   " + newDeadline.toString());
            break;
        case TaskManager.EVENT_CMD:
            Event newEvent = Event.create(command);
            this.addTask(newEvent);
            TaskManager.printlnWithIndent(String.format(TaskManager.ADD_PHRASE,
                    TaskManager.EVENT_CMD));
            TaskManager.printlnWithIndent("   " + newEvent.toString());
            break;
        }
    }

    private StorageManager getStorageManager() {
        return this.storageManager;
    }

    private Task getTask(int indexOfTask) {
        boolean isNegativeIndex = indexOfTask < 0;
        boolean isIndexTooLarge = indexOfTask >= this.getTasks().size();
        if (isNegativeIndex || isIndexTooLarge) {
            return null;
        }
        return this.getTasks().get(indexOfTask);
    }

    private ArrayList<Task> getTasks() {
        return this.tasks;
    }

    private boolean hasAnyTasks() {
        return !this.getTasks().isEmpty();
    }

    private void insertTask(int indexOfTask, Task newTask) {
        if (newTask != null) {
            this.getTasks().add(indexOfTask, newTask);
        }
    }

    private static void printlnWithIndent(String line) {
        System.out.print("    ");
        System.out.println(line);
    }

    private Task rmTask(int indexOfTask) throws RicaTaskException {
        boolean isNegativeIndex = indexOfTask < 0;
        boolean isIndexTooLarge = indexOfTask >= this.getTasks().size();
        if (isNegativeIndex || isIndexTooLarge) {
            throw new RicaTaskException(TaskManager.BAD_TASK_INDEX_ERROR);
        }
        return this.getTasks().remove(indexOfTask);
    }

    public void createTaskFrom(String command) {
        String[] parameters = command.split(" ");
        String typeOfTask = parameters[0];
        this.createTask(typeOfTask, command);
        this.getStorageManager().saveTasks(this.getTasks());
        int howManyTasks = this.getTasks().size();
        if (howManyTasks > 1) {
            TaskManager.printlnWithIndent(String.format(TaskManager.TASK_ADDED_PHRASE,
                    this.getTasks().size()));
        } else if (howManyTasks == 1) {
            TaskManager.printlnWithIndent(String.format(TaskManager.SINGLE_TASK_ADDED_PHRASE,
                    this.getTasks().size()));
        }
    }

    /**
     * Mark a given task in the task list as done
     *
     * @param indexOfTask Index of given task in the task list
     * @return rica.Task object representing the desired task being marked as done,
     * null if not an instance of rica.Todo
     */
    public Todo markDone(int indexOfTask) throws RicaTaskException {
        boolean isNegativeIndex = indexOfTask < 0;
        boolean isIndexTooLarge = indexOfTask >= this.getTasks().size();
        if (isNegativeIndex || isIndexTooLarge) {
            throw new RicaTaskException(TaskManager.INVALID_TASK_INDEX_ERROR);
        }
        Task selectedTask = this.getTask(indexOfTask);
        boolean isTaskATodo = selectedTask instanceof Todo;
        if (!isTaskATodo) {
            throw new RicaTaskException(TaskManager.WRONG_TASK_TYPE);
        }
        // At this point, rica.Task is definitely an instance of rica.Todo. Can cast it to rica.Todo safely
        Todo selectedTodo = (Todo) selectedTask;
        if (selectedTodo.getIsDone()) {
            TaskManager.printlnWithIndent(" Take a break maybe? Alright marked as done my friend:");
            TaskManager.printlnWithIndent("    " + selectedTodo);
            return selectedTodo;
        }
        this.rmTask(indexOfTask);
        selectedTodo = selectedTodo.setDone(true);
        this.insertTask(indexOfTask, selectedTodo);
        this.getStorageManager().saveTasks(this.getTasks());
        TaskManager.printlnWithIndent(" Shall remember that this task is done:");
        TaskManager.printlnWithIndent("    " + selectedTodo);
        return selectedTodo;
    }

    /**
     * Prints out the list of tasks added so far, or inform the user if no tasks have been added
     * yet
     */
    public void printTasks() {
        if (!this.hasAnyTasks()) {
            TaskManager.printlnWithIndent(" Hope I'm not amnesiac, but I don't remember any tasks?");
        } else {
            ArrayList<Task> tasks = this.getTasks();
            TaskManager.printlnWithIndent(" I think you have these tasks:");
            for (int i = 1; i <= tasks.size(); i += 1) {
                TaskManager.printlnWithIndent(" " + i + "." + tasks.get(i - 1));
            }
        }
    }

    public void rmTask(String command) throws RicaTaskException {
        String[] parameters = command.split(" ");
        if (!parameters[0].equals(TaskManager.DELETE_CMD)) {
            String cmdType = "deleting a task";
            throw new RicaTaskException(String.format(TaskManager.WRONG_CMD_ERROR, cmdType));
        }
        if (!this.hasAnyTasks()) {
            throw new RicaTaskException(TaskManager.NO_TASKS_TO_DELETE_ERROR);
        }
        int givenIndex;
        try {
            givenIndex = Integer.parseInt(parameters[1]);
        } catch (NumberFormatException exception) {
            throw new RicaTaskException(TaskManager.NOT_A_TASK_INDEX_ERROR);
        }
        // givenIndex is 1-based, but rmTask() expects 0-based indexing, so subtract one
        //   before passing to rmTask()
        Task removedTask = this.rmTask(givenIndex - 1);
        this.getStorageManager().saveTasks(this.getTasks());
        TaskManager.printlnWithIndent(TaskManager.TASK_REMOVED_PHRASE);
        TaskManager.printlnWithIndent("   " + removedTask.toString());
        int numTasksLeft = this.getTasks().size();
        if (numTasksLeft == 1) {
            TaskManager.printlnWithIndent(TaskManager.TASK_REMAINING_PHRASE);
        } else {
            TaskManager.printlnWithIndent(String.format(TaskManager.TASKS_REMAINING_PHRASE, numTasksLeft));
        }
    }

    /**
     * Mark a given task in the task list as not done
     *
     * @param indexOfTask Index of desired task in the task list
     * @return rica.Task object representing the desired task being marked as not done,
     * null if not an instance of rica.Todo
     */
    public Todo unmarkDone(int indexOfTask) throws RicaTaskException {
        boolean isIndexNegative = indexOfTask < 0;
        boolean isIndexTooLarge = indexOfTask >= this.getTasks().size();
        if (isIndexNegative || isIndexTooLarge) {
            throw new RicaTaskException(TaskManager.INVALID_TASK_INDEX_ERROR);
        }
        Task selectedTask = this.getTask(indexOfTask);
        boolean isTaskATodo = selectedTask instanceof Todo;
        if (!isTaskATodo) {
            throw new RicaTaskException(TaskManager.WRONG_TASK_TYPE);
        }
        Todo selectedTodo = (Todo) selectedTask;
        if (!selectedTodo.getIsDone()) {
            TaskManager.printlnWithIndent(" Getting a little ahead of yourself"
                    + "are you xD It's not even done:");
            TaskManager.printlnWithIndent("    " + selectedTodo);
            return selectedTodo;
        }
        this.rmTask(indexOfTask);
        selectedTodo = selectedTodo.setDone(false);
        this.insertTask(indexOfTask, selectedTodo);
        this.getStorageManager().saveTasks(this.getTasks());
        TaskManager.printlnWithIndent(" (Why??) Anyway, I've marked this task as not done yet:");
        TaskManager.printlnWithIndent("    " + selectedTodo);
        return selectedTodo;
    }

}
