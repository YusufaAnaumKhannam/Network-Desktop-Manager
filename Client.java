import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 13288;

    private static final JFrame frame = new JFrame("Client");
    private static final JTextArea chatArea = new JTextArea();
    private static final JTextField messageField = new JTextField();
    private static final JButton sendButton = new JButton("Send");
    private static final JButton newCommandButton = new JButton("New Command");

    private static ServerCommunication serverCommunication;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            initializeGUI();

            try {
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);

                // Register client with a unique ID (you might have a better way to identify clients)
                String clientId = "Client1";
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(clientId);

                // Handle server communication in a separate thread
                serverCommunication = new ServerCommunication(socket);
                new Thread(serverCommunication).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void initializeGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.add(newCommandButton, BorderLayout.WEST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Add a button click listener to trigger the new command
        newCommandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Replace "parameterValue" with the actual value or input from the user
                sendNewCommand("parameterValue");
            }
        });

        frame.setVisible(true);
    }

    private static void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            chatArea.append("You: " + message + "\n");
            messageField.setText("");

            // Send the message to the server using the ServerCommunication class
            serverCommunication.sendUserInput(message);

            // Check if the user wants to exit
            if (message.equalsIgnoreCase("EXIT")) {
                // Close the connection and exit the program
                serverCommunication.closeConnection();
                System.exit(0);
            }
        }
    }

    private static void sendNewCommand(String parameter) {
        // Assuming "NEW_COMMAND" is the new command identifier
        serverCommunication.sendUserInput("NEW_COMMAND " + parameter);
    }

    public static void appendAdminMessage(String adminMessage) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("Admin: " + adminMessage + "\n");
        });
    }

    public static void appendUserMessage(String userMessage) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("User: " + userMessage + "\n");
        });
    }

    public static void appendSystemMessage(String systemMessage) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("System: " + systemMessage + "\n");
        });
    }
}

class ServerCommunication implements Runnable {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ServerCommunication(Socket socket) {
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received from server: " + message);

                // Handle different server messages
                if (message.equalsIgnoreCase("EXIT")) {
                    // Acknowledge the exit request
                    Client.appendSystemMessage("Goodbye!");

                    // Close the connection and terminate the client
                    closeConnection();
                    System.exit(0);
                } else if (message.startsWith("ADMIN_MESSAGE")) {
                    String adminMessage = message.substring("ADMIN_MESSAGE".length() + 1);
                    Client.appendAdminMessage(adminMessage);
                } else if (message.startsWith("USER_MESSAGE")) {
                    String userMessage = message.substring("USER_MESSAGE".length() + 1);
                    Client.appendUserMessage(userMessage);
                } else if (message.startsWith("SYSTEM_MESSAGE")) {
                    String systemMessage = message.substring("SYSTEM_MESSAGE".length() + 1);
                    Client.appendSystemMessage(systemMessage);
                } else if (message.startsWith("CREATE_FOLDER")) {
                    String folderName = message.substring("CREATE_FOLDER".length()).trim();
                    createFolder(folderName);

                    // Display a message in the client's chat area
                    Client.appendAdminMessage("Creating folder: " + folderName);
                } else if (message.startsWith("RENAME_FOLDER")) {
                    String[] parts = message.split(" ");
                    Client.appendAdminMessage("Renaming folder " + parts[1] + " to " + parts[2]);
                }
                // Add more cases for different server messages

                // Continue handling other messages as needed
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUserInput(String userInput) {
        writer.println(userInput);
    }

    public void closeConnection() {
        try {
            reader.close();
            writer.close();
            socket.close();
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
}
