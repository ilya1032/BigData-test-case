import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class Limits {

    private static Limits instance = null;
    private int minTrafficSize;
    private int maxTrafficSize;

    private Limits() {
        updateLimits();
    }

    public void updateLimits() {

        // Connect to a database
        Connection connection = null;
        try {
            connection = new DatabaseHandler().getConn();
        } catch (SQLException e) {
            System.out.println("Connection to DB failed. Exiting now.");
            e.printStackTrace();
            System.exit(1);
        }

        Statement stmt;
        try {

            // Get lower bound of traffic size
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(String.format(
                    "SELECT max(effective_date) as max_date, limit_value as min_limit FROM traffic_limits.limits_per_hour as lph " +
                            "WHERE lph.limit_name = 'min' AND lph.effective_date <= '%s'" +
                            " GROUP BY limit_value ORDER BY max_date DESC;",
                    Application.dtf.format(LocalDateTime.now())
            ));
            if (rs.next())
                minTrafficSize = rs.getInt("min_limit");

            // Get uper bound of traffic size
            rs = stmt.executeQuery(String.format(
                    "SELECT max(effective_date) as max_date, limit_value as max_limit FROM traffic_limits.limits_per_hour as lph" +
                            " WHERE lph.limit_name = 'max' AND lph.effective_date <= '%s'" +
                            " GROUP BY limit_value ORDER BY max_date DESC;",
                    Application.dtf.format(LocalDateTime.now())
            ));
            if (rs.next())
                maxTrafficSize = rs.getInt("max_limit");

            if (stmt != null)
                stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Limits getInstance() {
        if (instance == null)
            instance = new Limits();
        return instance;
    }

    public int getMinTrafficSize() {
        return minTrafficSize;
    }

    public int getMaxTrafficSize() {
        return maxTrafficSize;
    }
}
