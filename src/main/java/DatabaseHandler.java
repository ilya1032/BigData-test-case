import java.sql.*;
import java.util.Properties;

public class DatabaseHandler {

    private static final String url = "jdbc:postgresql://localhost:15432/dins";
    private Connection conn;

    DatabaseHandler() throws SQLException {

        // Get PostgreSQL driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Set connection properties
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "220698");
        conn = DriverManager.getConnection(url, props);

        // Start listening to limits_change chanel, which will notify application on limits_per_hour table change
        Statement stmt = conn.createStatement();
        stmt.execute("LISTEN limit_change;");
        stmt.close();
        new Thread(this::changeListen).start();
    }

    public Connection getConn() {
        return conn;
    }

    public void changeListen() {
        while (true) {
            try {
                // issue a dummy query to contact the backend
                // and receive any pending notifications.
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1");
                rs.close();
                stmt.close();

                org.postgresql.PGNotification[] notifications = ((org.postgresql.PGConnection)conn).getNotifications();
                if (notifications != null) {
                    Limits.getInstance().updateLimits();
                }

                // wait a while before checking again for new
                // notifications
                Thread.sleep(500);
            } catch (SQLException | InterruptedException sqle) {
                sqle.printStackTrace();
            }
        }
    }

}
