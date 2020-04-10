package zoot.tube.googleapi;

import java.io.*;

/**
 * Handles some basic IO to save and load refresh-tokens for users.
 */
public final class RefreshTokenSaver {

    private static String RESOURCES_USERS = "src/main/resources/users/";

    /**
     * Saves a user's refresh-token to a file that can be loaded when
     * the application restarts.
     *
     * @param user         the user.
     * @param refreshToken the refresh-token.
     */
    public static void saveRefreshToken(String user, String refreshToken) {
        File file = new File(String.format("%s%s", RESOURCES_USERS, user));
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(refreshToken);
        } catch (IOException e) {
            System.err.println(String.format("Error saving user: \"%s\"'s token.", user));
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

    /**
     * Loads the specified user's refresh-token if one has been saved.
     * <p>
     * If there is no file for the user, an empty string will be returned.
     *
     * @param user the user.
     * @return the refresh-token if it exists, otherwise an empty string.
     */
    public static String loadRefreshToken(String user) {
        File file = new File(String.format("%s%s", RESOURCES_USERS, user));
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
