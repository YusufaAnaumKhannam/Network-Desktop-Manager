import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {

    private static final int SERVER_PORT = 13288;

    public static void main(String[] args) {
        try {
            // Bind the server socket to all available network interfaces
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT, 0, InetAddress.getByName("0.0.0.0"));

            System.out.println("Server is listening on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Create a new thread to handle communication with the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {

    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String clientId = reader.readLine();
            System.out.println("Client ID: " + clientId);

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received from client " + clientId + ": " + message);

                // Handle different client messages
                if (message.equalsIgnoreCase("EXIT")) {
                    // Acknowledge the exit request
                    writer.println("SYSTEM_MESSAGE Goodbye!");

                    // Close the connection and terminate the server
                    closeConnection();
                    System.exit(0);
                } else if (message.startsWith("NEW_COMMAND")) {
                    String parameter = message.substring("NEW_COMMAND".length()).trim();
                    System.out.println("Received new command from client " + clientId + ": " + parameter);

                    // Implement your logic for the new command here
                    // For example, you may want to send a response back to the client
                    writer.println("ADMIN_MESSAGE Executed new command: " + parameter);
                } else if (message.startsWith("CREATE_FOLDER")) {
                    String folderName = message.substring("CREATE_FOLDER".length()).trim();
                    createFolder(folderName);

                    // Send a message back to the client to confirm folder creation
                    writer.println("ADMIN_MESSAGE Folder created: " + folderName);
                } else if (message.startsWith("RENAME_FOLDER")) {
                    String[] parts = message.split(" ");
                    String oldName = parts[1];
                    String newName = parts[2];

                    // Perform the folder rename operation on the server
                    renameFolder(oldName, newName);

                    // Display a message in the client's chat area
                    Client.appendAdminMessage("Renaming folder " + oldName + " to " + newName);
                }

                // Add karna hai dusre cases

                // Continue handling other messages as needed
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            reader.close();
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Additional method to handle folder creation on the server
    private void createFolder(String folderName) {
        try {
            // Specify the path where you want to create the folder
            // Replace with the actual path on your server
            // This path should match the path used in the client code
            String serverFolderPath = "C:\\";
            Path fullPath = Paths.get(serverFolderPath, folderName);

            // Check if the folder already exists
            if (!Files.exists(fullPath)) {
                // Create the folder
                Files.createDirectory(fullPath);
                System.out.println("Folder created: " + fullPath.toString());
            } else {
                System.out.println("Folder already exists: " + fullPath.toString());
            }

        } catch (IOException e) {
            System.err.println("Error creating folder:");
            e.printStackTrace();
        }
    }
    private void renameFolder(String oldName, String newName) {
        try {
            // Specify the path where the folder is located
            // Replace with the actual path on your server
            // This path should match the path used in the client code
            String serverFolderPath = "C:\\";
            Path oldPath = Paths.get(serverFolderPath, oldName);
            Path newPath = Paths.get(serverFolderPath, newName);

            // Rename the folder
            Files.move(oldPath, newPath);
            System.out.println("Folder renamed: " + oldPath.toString() + " to " + newPath.toString());

        } catch (IOException e) {
            System.err.println("Error renaming folder:");
            e.printStackTrace();
        }
    }
}
