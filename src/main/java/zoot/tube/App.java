/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package zoot.tube;

import Sockets.SimpleSocket;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) throws UnknownHostException {
        SimpleSocket socket = new SimpleSocket(8080);
        socket.addMessageHandler((String message) -> {
            System.out.println(message);
        });

        File home = new File("src\\main\\java\\zoot\\tube\\websocketwebserver\\ZootTube.html");
        String url = home.getAbsolutePath();
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(home.toURI());
            } catch (IOException e) {
                System.out.println("Open this file in your web browser: " + url);
            }
        }

//        while (true) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        Scanner input = new Scanner(System.in);
        System.out.println("Press enter to close server.");
        input.nextLine();

        socket.shutdown();
    }
}
