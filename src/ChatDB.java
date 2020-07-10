import java.sql.*;
import java.util.ArrayList;

public class ChatDB {

    private final String url = "jdbc:mysql://localhost/chatdb";
    private final String username ;
    private final String password ;

    public ChatDB(String username, String password) throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        this.username=username;
        this.password=password;
    }

    public static void main(String[]args) throws ClassNotFoundException, SQLException {
    }

    // Create a database connection
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to the DB server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Exiting...");
            System.exit(1);
        }
        return conn;
    }

    // Stores a message to the database
    public void addMessage(String message, String username) throws SQLException {

        try (Connection conn = connect()) {
            String sql = "Insert INTO chat set message=? , date_added=? , username=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, message);
            preparedStatement.setDate(2, Date.valueOf(java.time.LocalDate.now()));
            preparedStatement.setString(3, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Invalid input");
            System.out.println(e.getMessage());
        }

    }

    // Returns an array containing most recent chat messages from the database
    public ArrayList getMessages() throws SQLException {
        Connection conn= null;
        ResultSet rs = null;
        ArrayList<String> messages = new ArrayList<>();
        try{
            conn = connect();
            Statement print = conn.createStatement();
            String sql="SELECT username, message from chat order by date_added desc";
            rs = print.executeQuery(sql);

            while (rs.next()) {
                String msg = rs.getString("message");
                String username = rs.getString("username");
                String chatText = String.format("[%s]: %s", username, msg);
                messages.add(chatText);
            }


        } catch (SQLException e){
            System.out.println("Error retrieving messages: "+ e.getMessage());
        } finally {
            conn.close();

        }
        return messages;
    }
}




