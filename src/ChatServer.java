import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ChatServer {
    // setup the database connection
    ChatDB db = new ChatDB("root", "");
    public static ArrayList<Socket> clientSockets = new ArrayList<>();

    public ChatServer() throws ClassNotFoundException {
    }

    public static void main (String[] args) throws ClassNotFoundException {
        new ChatServer().run();
    }

    public void run () {
        System.out.println("Listening on port 5000...");

        try {
            // Create server port 5000
            ServerSocket serverSocket = new ServerSocket(5000);

            // Continuously listen to client requests
            while (true){
                // get socket connection for current client
                Socket clientSocket = serverSocket.accept();

                clientSockets.add(clientSocket);

                // Create a Thread for handling response to the client
                //
                // The job passed to the Thread is an instance of
                // the ClientHandler class defined within this class.
                //
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();

                System.out.println("Got connection...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // sends chat messages/history to all clients currently connected to
    // the server.
    // Each client is represented by the argument 's', a Socket.
    public void broadcast(Socket s) throws SQLException {

        // Get all messages from the databases
        ArrayList<String> messages = db.getMessages();

        for (String msg: messages){
            try {
                // create a PrintWriter object for sending data to the client
                PrintWriter writer = new PrintWriter(s.getOutputStream());
                writer.println(msg); // send message to the client
                writer.flush();
                System.out.println(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    public class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket sock;

        // Constructor - sets up reader and sock fields needed for reading data sent by client.
        public ClientHandler (Socket clientSocket) {
            try {
                // set up client socket
                sock = clientSocket;

                // Create a BufferedReader object for reading messages from client
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);

            }  catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("    " + message);

                    // Extract username and message from text sent by client
                    // That message is sent with the format: '[username] : message'
                    String[] parts = message.split(":");
                    String username = parts[0];
                    String msg = parts[1];

                    // store the client's message in the database
                    db.addMessage(msg, username);

                    // Send all messages so far in the chat to all clients
                    for (Socket s: clientSockets) {
                        broadcast(s);
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

