import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatroomClient {
    private static String SERVER_ADDRESS = "localhost"; // Server IP address
    private static final int SERVER_PORT = 12345; // Server port number
    private static volatile boolean isConnected = true; // Flag to indicate if connected

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            SERVER_ADDRESS = args[0];
        }
        // Establish a socket connection to the server
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to chat server");
            // Start a new thread to read messages from the server
            new Thread(new ReadMessage(socket)).start();

            try (
                    // Set up a PrintWriter to send messages to the server
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    // Set up a Scanner to read input from the console
                    Scanner scanner = new Scanner(System.in)) {

                System.out.print("Enter your username: ");
                String name = scanner.nextLine();
                out.println("NAME " + name); // Send the username to the server

                while (true) {
                    // Read messages from the console and send them to the server
                    String input = scanner.nextLine();
                    out.println(input);
                    if (input.startsWith("LEAVE")) {
                        isConnected = false; // Set the flag before breaking
                        break; // Break the loop if the user wants to leave the chat
                    }
                }
            } finally {
                socket.close(); // Close the socket when done
            }
        } catch (UnknownHostException ex) {
            System.err.printf("Could not resolve %s", SERVER_ADDRESS);
            System.exit(0);
        } catch (ConnectException ex) {
            System.err.printf(ex.getMessage());
            System.exit(0);
        }
    }

    // Class to handle reading messages from the server
    private static class ReadMessage implements Runnable {
        private final Socket socket;

        ReadMessage(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                    // Set up a BufferedReader to read messages from the server
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while (isConnected && (message = reader.readLine()) != null) {
                    System.out.println(message); // Print each message received from the server
                }
            } catch (IOException e) {
                if (isConnected) {
                    System.out.println("Error reading from server: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
