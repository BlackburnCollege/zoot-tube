package zoot.tube.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileIO {

    public static void saveToFile(String path, String contents) {
        File file = new File(path);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(contents);
        } catch (IOException e) {
            System.err.println(String.format("Error saving file: \"%s\"", path));
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readFileToString(String path) {
        File file = new File(path);
        BufferedReader reader = null;
        String contents = "";
        try {
            reader = new BufferedReader(new FileReader(file));
            contents = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            System.err.println(String.format("Failed to read file: \"%s\"", path));
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contents;
    }
}
