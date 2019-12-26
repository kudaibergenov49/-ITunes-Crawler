import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

class Crawler {

    private static final String APP_STORE_ROOT_LINK = "https://itunes.apple.com/us/genre/ios/id36";
    static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
    static final String ERROR = "Доступ к itunes.apple.com запрещен\n" +
            "У вас нет прав для просмотра этой страницы\nHTTP Error 403";

    /**
     * Пробегаемся по главной странице App Store, return все публичные данные о приложениях
     */
    List<String> getAllData() {
        List<String> data = new LinkedList<>();
        try {
            Jsoup.connect(APP_STORE_ROOT_LINK)
                    .userAgent(USER_AGENT)
                    .get()
                    .select(".top-level-genre")
                    .stream()
                    .parallel()
                    .map(element -> element.attr("href"))
                    .forEach(genre -> {
                        ExecutorService service = Executors.newSingleThreadExecutor();
                        Callable task = new GenreHandler(genre);
                        Future<List<String>> f = service.submit(task);
                        try {
                            data.addAll(f.get());
                        } catch (InterruptedException | ExecutionException e) {
                            System.out.println("error");
                        }
                    });
        } catch (IOException e) {
            System.out.println(ERROR);
        }
        return data;
    }
}
