//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import ru.utils.files.FileUtils;
import ru.utils.graph.Graph;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class App {

//    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        FileUtils fileUtils = new FileUtils();

        String start = "2019-02-20";
//        String startPeriod = "2019-01-01 00:00";
        String startPeriod = "2019-02-21 00:00";
        String stopPeriod = "2019-12-31 23:59";

        startPeriod = "2019-02-20 00:00";
        stopPeriod = "2019-11-27 00:00";

        startPeriod = "2019-02-20 00:00";
        stopPeriod = "2019-06-18 00:00";

//        startPeriod = "2019-06-12 00:00";
//        stopPeriod = "2019-06-19 00:00";

        String fileJson = "json/1.json";
        JSONArray jsonArrayPulse = readJSONArray(fileJson);

        fileJson = "json/2.json";
        JSONArray jsonArrayArterialPressure = readJSONArray(fileJson);




        Graph graph = new Graph();
//        graph.setBackground("#ffffff"); // задаем цвет фона
//        graph.setColor(1, "#ff0000"); // задаем цвет фона для первого графика
        graph.setPeriod(startPeriod, stopPeriod, start); // задаем отчетный период


        graph.addTable(
                jsonArrayPulse,
                "Гемоглобин",
                75.0,
                78.0);

        graph.addTable(
                jsonArrayArterialPressure,
                "ArterialPressure");

        graph.addTable(
                jsonArrayPulse,
                "pulse");

        graph.addGraph(
                jsonArrayPulse,
                "pulse");

        graph.addGraph(
                jsonArrayArterialPressure,
                "ArterialPressure");

        fileUtils.writeFile("Graph1.html", graph.getSvg());



//        graph.clear();

        graph.addGraph(
                jsonArrayPulse,
                "pulse",
                40.0,
                180.0,
                60.0,
                90.0);

        graph.addGraph(
                jsonArrayArterialPressure,
                "ArterialPressure",
                50.0,
                200.0,
                80.0,
                120.0);

        graph.addGraph(
                jsonArrayArterialPressure,
                "ArterialPressure",
                50.0,
                200.0,
                80.0,
                120.0);
        graph.addGraph(
                jsonArrayArterialPressure,
                "ArterialPressure",
                50.0,
                200.0,
                80.0,
                120.0);

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
            e.printStackTrace();
        }

        JSONArray jsonArray = null;
        if (jsonSB.length() > 0) {
            try {
                jsonArray = new JSONArray(jsonSB.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }
}
