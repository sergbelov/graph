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

        String startPeriod = "2019-01-01 00:00";
        String stopPeriod = "2019-12-31 23:59";
        startPeriod = "2019-05-29 00:00";
//        startPeriod = "";
        stopPeriod = "";

        String fileJson = "json/1.json";
        JSONArray jsonArrayPulse = readJSONArray(fileJson);
        LOG.info("{}", jsonArrayPulse);

        fileJson = "json/2.json";
        JSONArray jsonArrayArterialPressure = readJSONArray(fileJson);
        LOG.info("{}", jsonArrayArterialPressure);




        Graph graph = new Graph();
        FileUtils fileUtils = new FileUtils();

//        graph.setBackground("#ffffff"); // задаем цвет фона
//        graph.setColor(1, "#ff0000"); // задаем цвет фона для первого графика

        String graphSvg;

        graphSvg = graph.addGraph(
                jsonArrayPulse,
                "pulse",
                startPeriod,
                stopPeriod,
                false);
        fileUtils.writeFile("Graph1.svg", graphSvg);

        graphSvg = graph.addGraph(
                jsonArrayArterialPressure,
                "ArterialPressure",
                startPeriod,
                stopPeriod,
                false);
        fileUtils.writeFile("Graph2.svg", graphSvg);


        graph.addGraph(
                jsonArrayPulse,
                "pulse",
                startPeriod,
                stopPeriod,
                40,
                180,
                60,
                90,
                false);

        graph.addGraph(
                jsonArrayArterialPressure,
                "ArterialPressure",
                startPeriod,
                stopPeriod,
                50,
                200,
                80,
                120,
                false);

        graph.addGraph(
                jsonArrayArterialPressure,
                "ArterialPressure",
                startPeriod,
                stopPeriod,
                50,
                200,
                80,
                120,
                false);
        graph.addGraph(
                jsonArrayArterialPressure,
                "ArterialPressure",
                startPeriod,
                stopPeriod,
                50,
                200,
                80,
                120,
                false);
        graph.addGraph(
                jsonArrayArterialPressure,
                "ArterialPressure",
                startPeriod,
                stopPeriod,
                50,
                200,
                80,
                120,
                false);

        fileUtils.writeFile("Graph.html", graph.getHtml());
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
