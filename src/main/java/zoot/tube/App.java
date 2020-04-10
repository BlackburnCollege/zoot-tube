/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package zoot.tube;

import java.io.IOException;
import zoot.tube.webserver.WebServer;
import java.util.List;
import zoot.tube.googleapi.ZTPlaylist;
import zoot.tube.googleapi.SimpleYouTubeAPI;

public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) {
        Thread serverThread = new Thread(() -> {
            try {
                new WebServer();
            } catch (IOException e) {
                System.out.println("Something Ducked Up");
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
