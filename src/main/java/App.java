import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import ru.utils.files.FileUtils;
import ru.utils.graph.Graph;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class App {

    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) {

        String startPeriod = "2019-01-01 00:00:00";
        String stopPeriod = "2019-12-31 23:59:59";

        String fileJson = "json/1.json";
        JSONArray jsonArray = readJSONArray(fileJson);
        LOG.info("{}", jsonArray);

        Graph graph = new Graph();

        graph.addGraph(
                "pulse",
                startPeriod,
                stopPeriod,
                jsonArray,
                false);

        graph.addGraph(
                "pulse",
                startPeriod,
                stopPeriod,
                jsonArray,
                false);

        graph.addGraph(
                "pulse",
                startPeriod,
                stopPeriod,
                jsonArray,
                false);

        String graphSvg = graph.get();

        FileUtils fileUtils = new FileUtils();
        fileUtils.writeFile("Graph.html",
                "<html>\n" +
                        "\t<head>\n" +
                        "\t\t<meta charset=\"UTF-8\">\n" +
                        "\t\t<style>\n" +
                        "\t\t\tbody, html { width:100%; height:100%; margin:0; background:#fdfdfd}\n" +
                        "\n" +
                        "\t\t\t.graph { width:95%; border-radius:5px; box-shadow: 0 0 1px 1px rgba(0,0,0,0.5); margin:50px auto; border:1px; solid #ccc; background:#fff}\n" +
                        "\n" +
                        "\t\t\ttable { border: solid 1px; border-collapse: collapse;}\n" +
                        "\t\t\tcaption {font-size: 10;}\n" +
                        "\t\t\ttd { border: solid 1px;}\n" +
                        "\t\t\tth { border: solid 1px; background: #f0f0f0; font-size: 12;}\n" +
                        "\t\t\t.td_red { border: solid 1px; background-color: rgb(255, 192, 192);}\n" +
                        "\t\t\t.td_green { border: solid 1px; background-color: rgb(192, 255, 192);}\n" +
                        "\t\t\t.td_yellow { border: solid 1px; background-color: rgb(255, 255, 192);}\n" +
                        "\t\t\ttable.scroll { border-spacing: 0; border: 1px solid black;}\n" +
                        "\t\t\ttable.scroll tbody,\n" +
                        "\t\t\ttable.scroll thead { display: block; }\n" +
                        "\t\t\ttable.scroll tbody { height: 100px; overflow-y: auto; overflow-x: hidden;}\n" +
                        "\t\t\ttbody td:last-child, thead th:last-child { border-right: none;}\n" +
                        "\t\t</style>\n" +
                        "\t</head>\n" +
                        "\t<body>\n" +
                        graphSvg +
                        "\t</body>\n" +
                        "</html>");
    }


    private static JSONArray readJSONArray(String fileName){
        String currEncoding = "UTF-8";

        int bytesRead = -1;
        byte[] buffer = new byte[1024];
        StringBuilder jsonSB = new StringBuilder();

        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                jsonSB.append(new String(Arrays.copyOf(buffer, bytesRead), currEncoding));
            }
        } catch (IOException e) {
            LOG.error("Ошибка при чтении данных из файла {}", fileName, e);
        }

        JSONArray jsonArray = null;
        if (jsonSB.length() > 0) {
            try {
                jsonArray = new JSONArray(jsonSB.toString());
            } catch (JSONException e) {
                LOG.error("Ошибка в формате данных", e);
            }
        }
        return jsonArray;
    }
}
