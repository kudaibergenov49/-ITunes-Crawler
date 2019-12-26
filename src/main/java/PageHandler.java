import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class PageHandler implements Callable<List<String>> {

    private static final String APPLICATION_DATA_PREFIX = "https://itunes.apple.com/lookup?country=ru&id=";

    private Document page;

    PageHandler(Document page) {
        this.page = page;
    }

    @Override
    public List<String> call() {
        List<String> letterData = new LinkedList<>();
        page.select("#selectedcontent")
                .select("a")
                .stream()
                .parallel()
                .map(element -> element.attr("href"))
                .forEach(ref -> {
                    try {
                        letterData.add(Jsoup.connect(APPLICATION_DATA_PREFIX
                                + ref.substring(ref.lastIndexOf("d") + 1))
                                .userAgent(Crawler.USER_AGENT)
                                .get()
                                .body()
                                .childNodes()
                                .stream()
                                .findFirst()
                                .map(Node::toString)
                                .orElse(Crawler.ERROR));
                    } catch (IOException e) {
                        letterData.add(Crawler.ERROR);
                    }
                });
        return letterData;
    }
}
