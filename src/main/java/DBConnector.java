import java.sql.*;
import java.util.List;

class DBConnector {
    private final String URL = "jdbc:mysql://localhost:3306/dbtest?useUnicode=true&useSSL=true&" +
            "useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private final String USERNAME = "kuanysh";
    private final String PASSWORD = "root";
    private final int MAX_BATCH_SIZE = 100;

    void createTable() {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.execute("create table if not exists Application (" +
                    "id int auto_increment primary key," +
                    "description TEXT)"
            );
        } catch (SQLException e) {
            System.out.println("Не загружен класс драйвера");
        }
    }

    void save(List<String> applications) {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement("insert into Application (description) values (?)")) {
            connection.setAutoCommit(false);
            int size = 0;
            for (String application : applications) {
                size++;
                statement.setString(1, application);
                statement.addBatch();
                if (size % MAX_BATCH_SIZE == 0) {
                    statement.executeBatch();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            System.out.println("Не загружен класс драйвера");
        }
    }
}
