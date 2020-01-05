public class Application {

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        DBConnector connector = new DBConnector();
        connector.createTable();
        connector.save(new Crawler().getAllData());
        System.out.println(System.currentTimeMillis() - time);
    }
}
