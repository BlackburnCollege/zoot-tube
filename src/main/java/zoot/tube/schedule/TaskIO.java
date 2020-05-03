package zoot.tube.schedule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import zoot.tube.util.FileIO;

public class TaskIO {

    private static final String RESOURCES_TASKS = "src/main/resources/tasks";

    private static int offsetCounter = 0;

    public static void saveTask(Task task) {
        Gson gson = new GsonBuilder().create();
        String taskAsJson = gson.toJson(task);
        TaskIO.offsetCounter = (offsetCounter + 1) % Short.MAX_VALUE;

        FileIO.saveToFile(
                String.format("%s%s%s", TaskIO.RESOURCES_TASKS, "/", (System.currentTimeMillis() + TaskIO.offsetCounter)),
                taskAsJson
        );
    }

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
}
