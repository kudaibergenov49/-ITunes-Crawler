import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

class Crawler {

    private static final Logger log = Logger.getLogger(Crawler.class);
    private static final String APPLICATION_DATA_PREFIX = "https://itunes.apple.com/lookup?country=ru&id=";
    private static final String APP_STORE_ROOT_LINK = "https://itunes.apple.com/us/genre/ios/id36";
    static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
    static final String ERROR = "Доступ к itunes.apple.com запрещен\n" +
            "У вас нет прав для просмотра этой страницы\nHTTP Error 403" +
            "\n link :";

    /** Пробегаемся по главной странице App Store, return все публичные данные о приложениях */
    ArrayList<String> getAllData() {
        ArrayList<String> data = new ArrayList<>();
        try {
            Jsoup.connect(APP_STORE_ROOT_LINK)
                    .userAgent(USER_AGENT)
                    .get()
                    .select(".top-level-genre")
                    .parallelStream()
                    .limit(3)//TODO для тестового запуска
                    .map(element -> element.attr("href"))
                    .forEach(genre -> data.addAll(getGenreData(genre)));
        } catch (IOException e) {
            log.info(ERROR + APP_STORE_ROOT_LINK);
        }
        return data;
    }

    /** Данные жанра */
    private ArrayList<String> getGenreData(String genre) {
        ArrayList<String> genreData = new ArrayList<>();
        try {
            Jsoup.connect(genre)
                    .userAgent(USER_AGENT)
                    .get()
                    .select("[title=\"Browse More Books\"]")
                    .parallelStream()
                    .limit(3)//TODO для тестового запуска
                    .map(element -> element.attr("href"))
                    .forEach(letter -> genreData.addAll(getLetterData(letter)));
        } catch (IOException e) {
            log.info(ERROR + genre);
        }
        return genreData;
    }

    /** Данные жанра побуквенно*/
    private ArrayList<String> getLetterData(String letter) {
        ArrayList<String> letterData = new ArrayList<>();

        try {
            Document document = Jsoup.connect(letter).userAgent(USER_AGENT).get();
            Set<String> set = new HashSet<>();
            int s = 0;//TODO для тестового запуска можно и 1у итерацию
            do {
                s++;
                set.addAll(document.select(".list.paginate")
                        .select("a")
                        .not(".paginate-more")
                        .stream()
                        .map(element -> element.attr("href"))
                        .collect(toSet()));
                String ref = document.select(".list.paginate")
                        .select("a")
                        .not(".paginate-more")
                        .last()
                        .attr("href");
                document = Jsoup.connect(ref).userAgent(USER_AGENT).get();
            } while (document.select(".paginate-more").size() != 0 && s == 0);
            set.parallelStream().forEach(ref -> {
                try {
                    letterData.addAll(getPageData(Jsoup.connect(ref).userAgent(USER_AGENT).get()));
                } catch (IOException e) {
                    log.info(ERROR + ref);
                }
            });
        } catch (IOException e) {
            log.info(ERROR + letter);
        }

        return letterData;
    }

    /** Данные одной страницы приложении*/
    private ArrayList<String> getPageData(Document page) {
        ArrayList<String> pageData = new ArrayList<>();
        page.select("#selectedcontent")
                .select("a")
                .parallelStream()
                .map(element -> element.attr("href"))
                .forEach(ref -> {
                    try {
                        Jsoup.connect(APPLICATION_DATA_PREFIX
                                + ref.substring(ref.lastIndexOf("d") + 1))
                                .userAgent(USER_AGENT)
                                .get()
                                .body()
                                .childNodes()
                                .stream()
                                .findFirst()
                                .map(Node::toString)
                                .ifPresent(pageData::add);
                    } catch (IOException e) {
                        log.info(ERROR + page);
                    }
                });
        return pageData;
    }
}
