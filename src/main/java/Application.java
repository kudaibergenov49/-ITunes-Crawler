import java.util.ArrayList;
import java.util.List;

public class Application {

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        DBConnector connector = new DBConnector();
        //List<String> applications = new Crawler().getAllData();
        List<String> applications = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            applications.add(String.valueOf(i));
        }
        applications.add("ddd");
        applications.add("eee");
        connector.createTable();
        connector.save(applications);
        System.out.println(System.currentTimeMillis() - time);
    }
}
