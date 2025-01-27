// Multithreaded Chat Application

// Server Code
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Chat server started on port 12345...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket);
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clientHandlers.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                this.out = out;

                // Ask for the client's name
                out.println("Enter your name: ");
                this.clientName = in.readLine();
                System.out.println(clientName + " has joined the chat.");
                broadcast(clientName + " has joined the chat.", this);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }
                    broadcast(clientName + ": " + message, this);
                }

            } catch (IOException e) {
                System.err.println("Error in client handler: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
                removeClientHandler(this);
                broadcast(clientName + " has left the chat.", this);
            }
        }

        private void broadcast(String message, ClientHandler sender) {
            for (ClientHandler clientHandler : clientHandlers) {
                if (clientHandler != sender) {
                    clientHandler.out.println(message);
                }
            }
        }

        private void removeClientHandler(ClientHandler clientHandler) {
            clientHandlers.remove(clientHandler);
        }
    }
}