import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient implements ActionListener {
    JTextField outgoing;
    JTextArea incoming;

    BufferedReader reader; // reads from server
    PrintWriter writer; // send data to server

    Socket sock;

    String username; // client's username
    String host; // server location (ip address)

    public ChatClient (String username, String host) {
        this.username = username;
        this.host = host;
    }

    public void run () {
        JFrame frame = new JFrame("Chat Client: [" + username + "]");

        JPanel mainPanel = new JPanel();

        incoming = new JTextArea(15, 25);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(true);

        JScrollPane qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        outgoing = new JTextField(20);
        JButton sendButton = new JButton("send");
        sendButton.addActionListener(this);

        mainPanel.add(qScroller);
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);


        connectToServer(host);

        Thread readerThread = new Thread(new ServerMessages());
        readerThread.start();

        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(400, 500);
        frame.setVisible(true);
    }

    public void connectToServer (String host) {
        // make a socket, then make a PrintWriter
        // assign the PrintWriter to writer instance variable
        try {
            sock = new Socket(host, 5000); // connect to server
            InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(streamReader);

            writer = new PrintWriter(sock.getOutputStream());

            System.out.println("Connection established...");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void actionPerformed (ActionEvent ev) {
        // get text from text field
        // send it to server using writer
        try {
            String textMsg = String.format("%s: %s", username, outgoing.getText());
            writer.println(textMsg); // send to server
            writer.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        outgoing.setText("");
        outgoing.requestFocus();
    }

    public class ServerMessages implements Runnable {

        public synchronized void run () {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                    incoming.append(message + "\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            incoming.setText("");
        }

    }

}

