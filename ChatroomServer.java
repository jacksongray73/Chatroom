import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatroomServer {
    private static final int PORT = 12345; // Server's listening port
    private static final Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>(); // Stores active chat rooms

    public static void main(String[] args) throws IOException {
        // ServerSocket listens on the specified port for client connections
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server started on port " + PORT);

            while (true) {
                // Accept a client connection and create a new thread for it
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        }
    }

    // Class to handle each connected client
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private String currentChatRoom = null;
        private String clientName;

        ClientHandler(Socket socket) {
            this.clientSocket = socket;
            // Assign a default name based on client's socket port
            this.clientName = "User" + clientSocket.getPort();
            System.out.println(clientName + " joined the server");
        }

        public void run() {
            try (
                // Create BufferedReader to read messages from the client
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // Create PrintWriter to send messages to the client
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String line;
                while ((line = in.readLine()) != null) {
                    // Handle JOIN command: client joins a chat room
                    if (line.startsWith("JOIN ")) {
                        String chatRoomName = line.substring(5);
                        currentChatRoom = chatRoomName;
                        ChatRoom room = chatRooms.computeIfAbsent(chatRoomName, k -> new ChatRoom(chatRoomName));
                        room.addClient(clientSocket);
                        System.out.println(clientName + " joined room " + chatRoomName);
                    }
                    // Handle LEAVE command: client leaves the chat room
                    else if (line.startsWith("LEAVE")) {
                        if (currentChatRoom != null) {
                            ChatRoom room = chatRooms.get(currentChatRoom);
                            if (room != null) {
                                room.removeClient(clientSocket);
                                currentChatRoom = null;
                                System.out.println(clientName + " left the room.");
                                break;
                            }
                        }
                    }
                    // Handle NAME command: client changes their name
                    else if (line.startsWith("NAME ")) {
                        String newName = line.substring(5);
                        System.out.println(clientName + " changed their name to " + newName);
                        clientName = newName;
                    }
                    // Handle chat messages: send message to the chat room
                    else {
                        if (currentChatRoom != null) {
                            ChatRoom room = chatRooms.get(currentChatRoom);
                            if (room != null) {
                                room.broadcastMessage(clientSocket, clientName + ": " + line);
                            }
                        } else {
                            out.println("You are not in a chat room.");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(clientName + " has disconnected.");
            } finally {
                // Clean up when the client disconnects
                if (currentChatRoom != null) {
                    ChatRoom room = chatRooms.get(currentChatRoom);
                    if (room != null) {
                        room.removeClient(clientSocket);
                    }
                }
            }
        }
    }

    // Class for managing chat rooms
    private static class ChatRoom implements Runnable {
        private final String name;
        private final Set<Socket> clients = ConcurrentHashMap.newKeySet();
        private final ExecutorService messageExecutor = Executors.newCachedThreadPool();
        private boolean running = true;

        ChatRoom(String name) {
            this.name = name;
        }

        public void run() {
            System.out.println("Chat room " + name + " is running.");
            while (running) {
                try {
                    Thread.sleep(1000); // Keep the chat room running
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Chat room " + name + " interrupted.");
                }
            }
            System.out.println("Chat room " + name + " is closing.");
        }

        // Add a client to the chat room
        void addClient(Socket socket) {
            clients.add(socket);
        }

        // Remove a client from the chat room
        void removeClient(Socket socket) {
            clients.remove(socket);
            if (clients.isEmpty()) {
                stopRoom();
            }
        }

        // Stop the chat room if empty
        void stopRoom() {
            running = false;
            messageExecutor.shutdownNow();
        }

        // Broadcast a message to all clients in the chat room
        void broadcastMessage(Socket sourceSocket, String message) {
            for (Socket socket : clients) {
                if (socket != sourceSocket) {
                    messageExecutor.submit(() -> {
                        try {
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            out.println(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }

        // Check if the chat room is empty
        boolean isEmpty() {
            return clients.isEmpty();
        }
    }
}
