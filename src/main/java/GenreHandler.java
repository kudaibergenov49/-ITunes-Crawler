import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Пробегаемся по страницам жанра, return все публичные данные о приложениях по текущему жанру
 */
public class GenreHandler implements Callable<List<String>> {

    private String genre;

    GenreHandler(String genre) {
        this.genre = genre;
    }

    @Override
    public List<String> call() {
        System.out.println("genre");
        List<String> genreData = new LinkedList<>();
        try {
            Jsoup.connect(genre)
                    .userAgent(Crawler.USER_AGENT)
                    .get()
                    .select("[title=\"Browse More Books\"]")
                    .stream()
                    .parallel()
                    .map(element -> element.attr("href"))
                    .forEach(letter -> genreData.addAll(getLetterData(letter)));
        } catch (IOException e) {
            System.out.println(Crawler.ERROR);
        }
        return genreData;
    }

    private List<String> getLetterData(String letter) {
        System.out.println("alpha");
        List<String> letterData = new LinkedList<>();
        List<Document> pages = new LinkedList<>();
        try {
            Document currentPage = Jsoup.connect(letter).get();
            pages.add(currentPage);
            while (currentPage.select(".paginate-more").size() > 0) {
                currentPage = Jsoup.connect(currentPage.select(".paginate-more").attr("href")).get();
                pages.add(currentPage);
            }

            pages.forEach(page -> {
                ExecutorService service = Executors.newFixedThreadPool(2);
                Callable task = new PageHandler(page);
                Future<List<String>> f = service.submit(task);
                try {
                    letterData.addAll(f.get());
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("error");
                }
                service.shutdown();
            });
        } catch (IOException e) {
            System.out.println(Crawler.ERROR);
        }

        return letterData;
    }
}
