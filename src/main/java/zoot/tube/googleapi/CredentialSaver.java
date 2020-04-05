package zoot.tube.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import java.io.*;

public final class CredentialSaver {

    public static void saveRefreshToken(String user, String token) throws IOException {
        File file = new File(String.format("%s%s", "src/main/resources/", user));
        FileWriter writer = new FileWriter(file);
        writer.write(token);
        writer.flush();
        writer.close();
    }

    public static String getRefreshToken(String user) {
        File file = new File(String.format("%s%s", "src/main/resources/", user));
        BufferedReader reader = null;
        String token = "";
        try {
            reader = new BufferedReader(new FileReader(file));
            token = reader.readLine();
        } catch (IOException e) {
            System.err.println(String.format("File for user: \"%s\" does not exist", user));
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return token;
    }
}
