import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

public class TagMappingExtractor {

    public static Map<String, String> extractTagMapping(String baseUrl) {
        Map<String, String> tagMap = new HashMap<>();

        try {
            // Fetch the HTML content of the base URL
            Document doc = Jsoup.connect(baseUrl).get();

            // Extract the script containing "feedData"
            Element scriptElement = doc.selectFirst("script:containsData(feedData)");
            if (scriptElement != null) {
                String scriptContent = scriptElement.html();

                // Use JavaScript engine to parse the feedData object
                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
                engine.eval(scriptContent);

                // Extract feedData from the script
                Object feedData = engine.eval("feedData");
                if (feedData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> feedDataMap = (Map<String, Object>) feedData;
                    for (Map.Entry<String, Object> entry : feedDataMap.entrySet()) {
                        String title = (String) entry.getValue();
                        String tag = entry.getKey();
                        tagMap.put(tag, title);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tagMap;
    }

    public static void main(String[] args) {
        // Define the base URL where "feedData" is available
        String baseUrl = "https://finance.sina.com.cn/7x24/";

        // Extract tag mappings
        Map<String, String> tagMapping = extractTagMapping(baseUrl);

        // Print the extracted tag mappings
        System.out.println("Extracted Tag Mapping:");
        for (Map.Entry<String, String> entry : tagMapping.entrySet()) {
            System.out.println("Tag: " + entry.getKey() + " -> Category: " + entry.getValue());
        }
    }
}
