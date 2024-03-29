
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Queue;
import java.util.Set;
import java.net.*;

public class WikiCrawler {

	public static String BASE_URL = "https://en.wikipedia.org";
	String seedUrl;
	int max;
	int count = 1;
	String filename;
	String new_doc = "";
	String htmldoc = "htmldoc.txt";

	LinkedHashMap<String, LinkedHashSet<String>> map = new LinkedHashMap<String, LinkedHashSet<String>>();

	/**
	 * 
	 * @param seedUrl
	 *            relative address of the seed url (within Wiki domain)
	 * @param max
	 *            Maximum number pages to be crawled
	 * @param fileName
	 *            name of a file–The graph will be written to this file
	 */
	public WikiCrawler(String seedUrl, int max, String filename) {
		this.seedUrl = seedUrl;
		this.max = max;
		this.filename = filename;

	}

	/**
	 * Find required wikilinks from the given doc Change it to return a list of
	 * all the required URL's This method can be merged in with the extract
	 * links method.
	 * 
	 * @param doc
	 * @return
	 * @throws IOException
	 */

	public ArrayList<String> extractLinks(String doc) throws IOException {
		// PrintWriter writer = new PrintWriter("my_links.txt", "UTF-8");
		new_doc = after_p(doc);
		// new_doc = doc.substring(doc.indexOf("<p>"));

		ArrayList<String> links = new ArrayList<String>();
		String html = new_doc;

		String regex = "[(]?<a\\s?href\\s?=\\s?\"/(\\bwiki)[(/\\w.,+)%@-]+[)]?\"";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(html);
		int index = 0;
		while (matcher.find(index)) {
			String link = matcher.group();
			int x = link.indexOf("\"");
			String resulturl = link.substring(x);
			resulturl = resulturl.replaceAll("^\"|\"$", "");
			links.add(resulturl);
			index++;
			// writer.println(links);
			index = matcher.end();
		}

		return links;

	}

	/**
	 * @throws IOException,
	 * 
	 */
	public void crawl() throws IOException, MalformedURLException {

		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		writer.println(max);

		Long startTime = System.currentTimeMillis() / 1000;
		ArrayList<String> links = new ArrayList<String>();
		Set<String> modified_unmarked = new HashSet<>();
		Queue<String> q = new LinkedList<>();
		q.add(seedUrl);
		Set<String> marked = new HashSet<>();

		marked.add(seedUrl);

		int reachable_size = 0;

		while (!q.isEmpty() && count <= max) {
			String v = q.poll();

			modified_unmarked.add(v);
			v = v.replaceAll("^\"|\"$", "");

			if (!map.containsKey(v))
				map.put(v, new LinkedHashSet<String>());

			String currentPage = BASE_URL + v;

			String doc = readhtml(currentPage);
			links = extractLinks(doc);
			reachable_size = links.size();

			for (int i = 0; i < reachable_size; i++) {
				String unmarked = links.get(i);
				unmarked = unmarked.replaceAll("^\"|\"$", "");

				if (unmarked.equals(v) == false)
					map.get(v).add(unmarked);

				if (!marked.contains(unmarked) && unmarked.equals(v) == false) {

					q.add(unmarked);

					marked.add(unmarked);

				}

			}
			count++;

		}

		for (String key : map.keySet()) {

			HashSet<String> set = map.get(key);
			if (set != null) {
				Iterator<String> iterator = set.iterator();
				while (iterator.hasNext()) {
					String element = iterator.next();
					if (modified_unmarked.contains(element) == false) {
						iterator.remove();

					}

				}

			}

		}


		for (String key : map.keySet()) {
			HashSet<String> set = map.get(key);
			if (set != null) {
				Iterator<String> iterator = set.iterator();
				while (iterator.hasNext()) {
					String element = iterator.next();
					writer.println(key + " " + element);

				}

			}

		}

		writer.close();
		Long endTime = System.currentTimeMillis() / 1000;
		System.out.println("Total time: " + (endTime - startTime));
		
	}

	/**
	 * gets the HTML tag passed in and writes to a file
	 * 
	 * @param currentPage
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private String readhtml(String currentPage) throws MalformedURLException, IOException {

		// System.out.println("Current page: " +currentPage);
		URL url = new URL(currentPage);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		if (count > 1 && count % 100 == 1) {
			System.out.println("Count is: " + count);
			System.out.println("Waiting 3 secs");
			connection.setReadTimeout(3 * 1000);
		}
		StringBuilder sb = new StringBuilder();

		String inputLine = in.readLine();
		while ((inputLine != null)) {
			sb.append(inputLine);

			inputLine = in.readLine();

		}

		return sb.toString();

	}

	/**
	 * Gets the string beyond the
	 * <p>
	 * point
	 * 
	 * @param doc
	 * @return
	 * @throws IOException
	 */

	private String after_p(String doc) throws IOException {

		String modified_doc = "";

		int i = doc.indexOf("<p>");

		if (i < 0) {
			System.out.println("No p tag found");
		} else {
			modified_doc = doc.substring(i);
		}

		return modified_doc;
	}

}
