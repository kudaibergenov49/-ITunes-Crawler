import java.util.List;

public class Application {

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        DBConnector connector = new DBConnector();
        List<String> applications = new Crawler().getAllData();
        connector.createTable();
        connector.save(applications);
        System.out.println(System.currentTimeMillis() - time);
    }
}
