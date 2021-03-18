import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * [Add your documentation here]
 *
 * @author Yu Nie & Lesi He, Lab 03
 * @version 4/27/2020
 */
final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;
    private boolean flag = true;
    Scanner scan;
    ChatMessage chatMessage;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    public ChatClient(String username, int port) {
        this.username = username;
        this.port = port;
        server = "localhost";
    }

    public ChatClient(String username) {
        this.username = username;
        port = 1500;
        server = "localhost";
    }

    public ChatClient() {
        username = "Anonymous";
        port = 1500;
        server = "localhost";
    }

    /*
     * This starts the Chat Client
     */
    private boolean startUp() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            System.out.println("Please start the Server first!");
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.print("");
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        try {
            String name;
            System.out.println("please enter your username: ");
            scan = new Scanner(System.in);
            name = scan.nextLine();
            sOutput.writeObject(name);
            sOutput.flush();
            Thread.sleep(300);
            if (!socket.isClosed()) {
                System.out.println(name + ", welcome to the chat room! " + "\nEnter /logout to quit " +
                        "\nEnter /msg<recipient1, recipient2, ...> for private messages " +
                        "\nEnter /list to check all the current users in connection");
            }

            while (flag) {
                String read = scan.nextLine();
                if (read.equalsIgnoreCase("/logout")) {
                    chatMessage = new ChatMessage("/logout", 1, null);
                    sendMessage(chatMessage);
                    flag = false;
                } else if (read.contains("/msg")) {
                    int first = read.indexOf("<");
                    int second = read.indexOf(">");
                    chatMessage = new ChatMessage(read.substring(second + 1, read.length()), 0,
                            read.substring(first + 1, second));
                    sendMessage(chatMessage);
                } else if (read.equals("/list")) {
                    chatMessage = new ChatMessage("/list", 0, null);
                    sendMessage(chatMessage);
                } else {
                    chatMessage = new ChatMessage(read, 0, null);
                    sendMessage(chatMessage);
                }
            }
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) throws IOException {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            System.out.println("Oops! The server or client is not connected. Please restart!");
            socket.close();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Create your client and start it
        ChatClient client = new ChatClient("localhost", 1500, "CS 180 Student");
        try {
            client.startUp();
        } catch (NullPointerException e) {
            System.out.print("");
        }
    }


    /**
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     *
     * @author Yu Nie & Lesi He, Lab 03
     * @version 4/27/2020
     */
    private final class ListenFromServer implements Runnable {
        synchronized public void run() {
            try {
                while (flag) {

                    if (socket.isClosed())
                        break;
                    String msg = (String) sInput.readObject();
                    System.out.println(msg);
                }

            } catch (Exception e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.out.println("User logged out.");

            }
        }
    }
}
