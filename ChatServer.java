import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * [Add your documentation here]
 */
final class ChatServer {
    private static int uniqueId = 0;
    private static File BadWords;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    ArrayList<String> nameList = new ArrayList<>();

    private ChatServer(int port, File words_to_filter) {
        this.port = port;
        BadWords = words_to_filter;
    }

    public ChatServer() {
        port = 1500;
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server = new ChatServer(1500, BadWords);
        server.startUp();
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void startUp() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("port 1500 is open");
            while (true) {
                Socket socket = serverSocket.accept();
                //System.out.println("new user connected");
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;
        private boolean flag = true;


        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            try {
                username = (String) sInput.readObject();
                if (nameList.contains(username)) {
                    sOutput.writeObject("This username already exists! Bye");
                    close();
                    remove(id);
                    flag = false;
                } else {
                    nameList.add(username);
                    System.out.println(username + " has joined the chat room");
                }
                while (flag) {
                    cm = (ChatMessage) sInput.readObject();
                    if (cm.getType() == 1) {
                        sOutput.flush();
                        remove(id);
                        nameList.remove(username);
                        flag = false;
                        broadcast("logged out.");
                        close();
                    } else {
                        if (cm.getRecipient() == null) {
                            if (cm.getMessage().equals("/list")) {
                                if (userList(username).equalsIgnoreCase("")) {
                                    System.out.println("You are the only one in the connection");
                                    sOutput.writeObject("You are the only one in the connection");
                                } else {
                                    System.out.println("The users in connection: " + userList(username));
                                    sOutput.writeObject("The users in connection: " + userList(username));
                                }
                            } else {
                                broadcast(cm.getMessage());
                            }
                        } else {
                            String[] nameList = cm.getRecipient().split(", ");
                            int count = 0;
                            for (ClientThread clientThread : clients) {
                                for (int i = 0; i < nameList.length; i++) {
                                    if (nameList[i].equalsIgnoreCase(clientThread.username))
                                        count++;

                                }
                            }
                            if (count == nameList.length) {
                                String newMess = cm.getMessage();
                                boolean check = true;
                                for (int i = 0; i < nameList.length; i++) {
                                    if (nameList[i].equalsIgnoreCase(username)) {
                                        sOutput.writeObject("Don't send message to yourself!");
                                        check = false;
                                    } else {
                                        directMessage(newMess, nameList[i]);

                                    }
                                }
                                if (newMess != null && !newMess.isEmpty() && check)
                                    sOutput.writeObject(newMess);
                            } else {
                                System.out.println("At least one user has disconnected or doesn't exist");
                                sOutput.writeObject("At least one user has disconnected or doesn't exist");
                            }
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("Connection with " + username + " lost.");
            }
        }

        synchronized private void broadcast(String message) throws IOException {
            SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss");
            String newMess = "[" + date.format(new Date()) + "] " + username + " : " + message;
            System.out.println(newMess);
            try {
                ChatFilter chatFilter = new ChatFilter("Badwords.txt");
                if (writeMessage(chatFilter.filter(message))) {

                }
            } catch (Exception e) {
                System.out.println("You didn't print anything");
                sOutput.writeObject("You didn't print anything");
            }

        }

        public boolean writeMessage(String message) {
            try {
                SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss");
                String newMess = "[" + date.format(new Date()) + "] " + username + " : " + message;
                for (ClientThread clientThread : clients) {
                    clientThread.sOutput.writeObject(newMess);
                    clientThread.sOutput.flush();
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private void close() {
            try {
                socket.close();
                sInput.close();
                sOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void remove(int id) {
            synchronized (clients) {
                for (int i = 0; i < clients.size(); i++) {
                    if (clients.get(i).id == id) {
                        clients.remove(clients.get(i));
                    }
                }
            }
        }

        private void directMessage(String message, String username) {
            try {
                for (ClientThread clientThread : clients) {
                    if (clientThread.username.equalsIgnoreCase(username)) {
                        SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss");
                        String newMess = "[" + date.format(new Date()) + "] privMsg to " + username + " : " + message;
                        System.out.println(newMess);
                        clientThread.sOutput.writeObject(newMess);
                        clientThread.sOutput.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String userList(String name) {
            StringBuilder list = new StringBuilder("");
            int count = clients.size();
            int comma = 0;
            for (ClientThread client : clients) {
                if (!client.username.contains(name)) {
                    list.append(client.username);
                    comma++;
                    if (comma < count - 1)
                        list.append(", ");
                }
            }
            return list.toString();
        }

    }
}
