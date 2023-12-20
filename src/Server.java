import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 8080;
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientAddress = getClientAddress(clientSocket);
                System.out.println("New client connected from: " + clientAddress);

                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.add(writer);

                Thread clientHandler = new Thread(new ClientHandler(clientSocket, writer));
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;
        private BufferedReader reader;
        private String userName;

        public ClientHandler(Socket socket, PrintWriter writer) {
            this.clientSocket = socket;
            this.writer = writer;
            try {
                this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                userName = reader.readLine();
                notifyAllClients(userName + " joined the chat.");

                String clientMessage;
                while ((clientMessage = reader.readLine()) != null) {
                    notifyAllClients(userName + ": " + clientMessage);
                }

            } catch (IOException e) {
                System.err.println(userName + " left the chat.");
            } finally {
                if (writer != null) {
                    clientWriters.remove(writer);
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                notifyAllClients(userName + " left the chat.");
            }
        }
    }

    private synchronized static void notifyAllClients(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }

    private static String getClientAddress(Socket socket) {
        InetAddress address = socket.getInetAddress();
        return address.getHostName();
    }
}
