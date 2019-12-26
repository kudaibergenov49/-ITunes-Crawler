import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.io.IOException;
import java.util.ArrayList;

class Crawler {

    private static final String APPLICATION_DATA_PREFIX = "https://itunes.apple.com/lookup?country=ru&id=";
    private static final String APP_STORE_ROOT_LINK = "https://itunes.apple.com/us/genre/ios/id36";
    static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
    static final String ERROR = "Доступ к itunes.apple.com запрещен\n" +
            "У вас нет прав для просмотра этой страницы\nHTTP Error 403";

    /**
     * Пробегаемся по главной странице App Store, return все публичные данные о приложениях
     */
    ArrayList<String> getAllData() {
        System.out.println("all");
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
            System.out.println(ERROR);
        }
        return data;
    }

    /**Данные жанра*/
    private ArrayList<String> getGenreData(String genre) {
        System.out.println("genre");
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
            System.out.println(ERROR);
        }
        return genreData;
    }

    /** Данные жанра побуквенно*/
    private ArrayList<String> getLetterData(String letter) {
        System.out.println("letter");
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
							   .collect(Collectors.toSet()));
			String ref = document.select(".list.paginate")
					.select("a")
					.not(".paginate-more")
					.last()
					.attr("href");
			document = Jsoup.connect(ref).userAgent(USER_AGENT).get();
		} while (document.select(".paginate-more").size() != 0 && s == 0);
		System.out.println(set);

		set.parallelStream().forEach(ref -> {
			try {
				letterData.addAll(getPageData(Jsoup.connect(ref).userAgent(USER_AGENT).get()));
			} catch (IOException e) {
				System.out.println(ERROR);
			}
		});

	} catch (IOException e) {
		System.out.println(ERROR);
	}

	return letterData;
    }

    /** Данные одной страницы приложении*/
    private ArrayList<String> getPageData(Document page) {
        System.out.println("page");
        ArrayList<String> pageData = new ArrayList<>();
        page.select("#selectedcontent")
                .select("a")
                .parallelStream()
                .map(element -> element.attr("href"))
                .forEach(ref -> {
                    try {
                        pageData.add(Jsoup.connect(APPLICATION_DATA_PREFIX
                                + ref.substring(ref.lastIndexOf("d") + 1))
                                .userAgent(USER_AGENT)
                                .get()
                                .body()
                                .childNodes()
                                .stream()
                                .findFirst()
                                .map(Node::toString)
                                .orElse(ERROR));
                    } catch (IOException e) {
                        pageData.add(ERROR);
                    }
                });
        return pageData;
    }
}
