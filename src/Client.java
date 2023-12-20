import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 8080;
    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, PORT);
                BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter clientWriter = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to the chat server.");

            System.out.print("Enter your username: ");
            String userName = userInput.readLine();
            clientWriter.println(userName);

            Thread serverListener = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = serverReader.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Connection to the server is lost.");
                }
            });
            serverListener.start();

            String userInputMessage;
            while ((userInputMessage = userInput.readLine()) != null) {
                clientWriter.println(userInputMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
