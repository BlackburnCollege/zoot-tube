package zoot.tube.schedule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import zoot.tube.util.FileIO;

public class TaskIO {

    private static final String RESOURCES_TASKS = "src/main/resources/tasks";
    private static final String RESOURCES_TASKIO_CONFIG = "src/main/resources/taskioconfig";
    private static final String RESOURCES_TASK_RESOURCES = "src/main/resources/taskresources";
    private static int nextOffsetCounter = -1;

    /**
     * Saves the given Task as a JSON file.
     *
     * @param task the Task to save.
     */
    public static void saveTask(Task task) {
        Gson gson = new GsonBuilder().create();
        String taskAsJson = gson.toJson(task);

        FileIO.saveToFile(
                String.format("%s%s%s", TaskIO.RESOURCES_TASKS, "/", task.getID()),
                taskAsJson
        );
    }

    /**
     * Deletes a saved Task.
     *
     * @param task the Task to delete.
     */
    public static void deleteTask(Task task) {
        File file = new File(String.format("%s%s%s", TaskIO.RESOURCES_TASKS, "/", task.getID()));
        file.delete();
    }

    /**
     * Loads all Tasks that have been saved.
     *
     * @return all Tasks that have been saved.
     */
    public static List<Task> loadSavedTasks() {
        Gson gson = new GsonBuilder().create();
        File taskFolder = new File(RESOURCES_TASKS);
        File[] taskFiles = taskFolder.listFiles();
        List<Task> tasks = new ArrayList<>();

        if (taskFiles != null) {
            for (File taskFile : taskFiles) {
                String taskAsJson = FileIO.readFileToString(taskFile.getPath());
                tasks.add(gson.fromJson(taskAsJson, Task.class));
            }
        }

        return tasks;
    }

    /**
     * Saved data as JSON under the Task.
     * <p>
     * Will overwrite previously saved data.
     *
     * @param task    the Task the content is for.
     * @param content the content to save. MUST be a JSON String.
     */
    public static void saveContentUnderTask(Task task, String content) {
        FileIO.saveToFile(
                String.format("%s%s%s", TaskIO.RESOURCES_TASK_RESOURCES, "/", task.getID()),
                content
        );
    }

    /**
     * Loads data stored under the Task.
     *
     * @param task the Task the data is stored under.
     * @return a JSON string of the data.
     */
    public static String loadContentUnderTask(Task task) {
        return FileIO.readFileToString(String.format("%s%s%s", TaskIO.RESOURCES_TASK_RESOURCES, "/", task.getID()));
    }

    /**
     * Deletes the data stored under the Task.
     *
     * @param task the Task the data is stored under.
     */
    public static void deleteContentUnderTask(Task task) {
        File file = new File(String.format("%s%s%s", TaskIO.RESOURCES_TASK_RESOURCES, "/", task.getID()));
        file.delete();
    }

    /**
     * Creates a unique ID for a Task.
     *
     * @return a unique ID for a Task.
     */
    public static String getNewTaskID() {
        if (nextOffsetCounter < 0) {
            String counterAsString = FileIO.readFileToString(RESOURCES_TASKIO_CONFIG);
            nextOffsetCounter = Integer.parseInt(counterAsString);
        }
        FileIO.saveToFile(RESOURCES_TASKIO_CONFIG, String.valueOf(++nextOffsetCounter));
        return String.valueOf(nextOffsetCounter);
    }
}
