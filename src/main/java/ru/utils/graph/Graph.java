package ru.utils.graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.utils.graph.data.DateTimeValue;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Graph {
    private static final Logger LOG = LogManager.getLogger(Graph.class);
    private final NumberFormat decimalFormat = NumberFormat.getInstance();
    private final DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private final DateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");
    private final DateFormat sdf4 = new SimpleDateFormat("HH:mm:ss.SSS");
    private final DateFormat sdf5 = new SimpleDateFormat("yyyyMMdd");

    public Graph() {
    }


    /**
     * Линейный график
     */
    public String addGraph(
            String title,
            String startPeriodStr,
            String stopPeriodStr,
            JSONArray jsonArrayData,
            boolean printMetrics) {

        long startPeriod;
        long stopPeriod;

        try {
            startPeriod = sdf0.parse(startPeriodStr).getTime();
        } catch (ParseException e) {
            LOG.error("Ошибка в формате даты: {}", startPeriodStr);
            return "";
        }

        try {
            stopPeriod = sdf0.parse(stopPeriodStr).getTime();
        } catch (ParseException e) {
            LOG.error("Ошибка в формате даты: {}", stopPeriodStr);
            return "";
        }

        LOG.info("Формирование графика {} ({} - {})",
                title,
                startPeriodStr,
                startPeriodStr);

        List<DateTimeValue> metricsList = new ArrayList<>();
        for (int i = 0; i < jsonArrayData.length(); i++) {
            try {
                JSONObject jsonObject = jsonArrayData.getJSONObject(i);
                if (jsonObject.has("date")) {
                    long date = 0L;
                    try {
                        date = sdf0.parse(jsonObject.getString("date")).getTime();
                    } catch (ParseException e) {
                        LOG.error("Ошибка в формате даты: {}", jsonObject.getString("date"));
                    }
                    if (date > 0L) {
                        int value = 0;
                        if (jsonObject.has("value")) {
                            value = jsonObject.getInt("value");
                        }
                        LOG.debug("{}: {}", sdf0.format(date), value);
                        metricsList.add(new DateTimeValue(date, Arrays.asList(value)));
                    }
                } else {
                    LOG.error("Ошибка в формате данных {}", jsonObject);
                }
            } catch (JSONException e) {
                LOG.error("Ошибка в формате данных");
            }
        }

        int metricCount = metricsList.get(0).getValueSize();

        int xSize = Math.max(10000, metricsList.size() - 1);
        int ySize = (int) (xSize / 2.8);
        int xStart = xSize / 30;
        int yStart = xSize / 20;
        int xMax = xSize + xStart;
        int yMax = ySize + yStart;
        int xMarginRight = xSize / 300;
        int yMarginBottom = xSize / 11;
        int xText = xSize / 500;
        int yText = xSize / 400;
        int fontSize = xSize / 120;
        int fontSizeX = xSize / 156;
        int fontAxisSize = xSize / 110;
        int lineSize = Math.max(1, xSize / 5000);
        String background = "#f0f0f0"; //"#dfdfdf";

        // максимальное/минимальное значение Y и X
        long startTime = 999999999999999999L;
        long xValueMax = 0L;
        double yValueMin = 999999999;
        double yValueMax = 0.00;
        for (int i = 1; i < metricsList.size(); i++) {
            for (int m = 0; m < metricCount; m++) {
                yValueMax = Math.max(yValueMax, metricsList.get(i).getIntValue(m));
                yValueMin = Math.min(yValueMin, metricsList.get(i).getIntValue(m));
            }
            startTime = Math.min(startTime, metricsList.get(i).getTime());
            xValueMax = Math.max(xValueMax, metricsList.get(i).getTime());
        }
        LOG.info("Min X: {}, Max X: {}, Min Y: {}, Max Y: {}",
                sdf0.format(startTime),
                sdf0.format(xValueMax),
                yValueMin,
                yValueMax);
        xValueMax = xValueMax - startTime;
//        yValueMax = yValueMax - yValueMin; // ToDo

        StringBuilder sbResult = new StringBuilder("<!--" + title + "-->\n" +
                "\t\t\t<svg viewBox=\"0 0 " + (xMax + xMarginRight) + " " + (yMax + yMarginBottom) + "\" class=\"chart\">\n" +
                "\t\t\t\t<text " +
                "font-size=\"" + (fontSize * 2) + "\" " +
                "x=\"" + (xSize / 2 - (title.length() * xText) / 2) + "\" " +
                "y=\"" + (yStart - fontSize * 2) + "\">" +
                "" + title + "</text>\n" +
                "<!-- Область графика -->\n" +
                "\t\t\t\t<rect " +
                "stroke=\"#0f0f0f\" " +
                "fill=\"" + background + "\" " +
                "x=\"" + xStart + "\" " +
                "y=\"" + yStart + "\" " +
                "width=\"" + xSize + "\" " +
                "height=\"" + ySize + "\"/>\n" +
                "<!-- Описание -->\n");

        // описание графиков
        double yCur = fontSize / 1.5;
/*        for (int i = 0; i < metricViewGroup.getMetricsCount(); i++) {
            if (!metricViewGroup.getMetricView(i).getTitle().isEmpty()) {
                sbResult.append(
                        "\t\t\t\t<polyline fill=\"none\" stroke=\"" + metricViewGroup.getMetricView(i).getColor() + "\" stroke-width=\"" + (lineSize * 4) + "\" points=\"" + xStart + "," + yCur + " " + xStart * 3 + "," + yCur + "\"/>\n" +
                        "\t\t\t\t<text font-size=\"" + fontSize + "\" font-weight=\"bold\" x=\"" + ((xStart * 3) + 10) + "\" y=\"" + yCur + "\">" + metricViewGroup.getMetricView(i).getTitle() + "</text>\n");
                yCur = yCur + fontSize;
            }
        }
*/

        // ось Y
        sbResult.append("<!-- Ось Y -->\n");
        if (yValueMax > 1) {
            yValueMax = (int) (Math.ceil(yValueMax / 5.00) * 5); // максимальное значение на графике - ближайшее большее кратное 5
        }
        int kfY = 40;
        double yScale = Math.max(Math.min(kfY, yValueMax), 10);
        if (yValueMax > 10) {
            while (yValueMax % yScale != 0) {
                yScale--;
            }
        }
        double yRatio = ySize / (yValueMax * 1.00);
        double yRatioValue = yValueMax / (yScale * 1.00);
        double yStep = ySize / (yScale * 1.00);
        double yValue = 0.00;
        yCur = yMax;
        double yValueMem = yValue;
//        LOG.info("ySize:{}; yStart: {}; yScale:{}; yRatio:{}; yRatioValue:{}; yStep:{}; yCur:{}", ySize, yStart, yScale, yRatio, yRatioValue, yStep, yCur);

        while (yCur > (yStart + yStep / 2)) {
            yCur = yCur - yStep;
            yValue = yValue + yRatioValue;
            sbResult.append("\t\t\t\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"#a0a0a0\" " +
                    "stroke-dasharray=\"" + xText + "\" " +
                    "stroke-width=\"" + lineSize + "\" " +
                    "points=\"" + xStart + "," + yCur + "  " + xMax + "," + yCur + "\"/>\n");
            if (yValueMem != yValue) {
                sbResult.append("\t\t\t\t<text " +
                        "font-size=\"" + fontSize + "\" " +
                        "x=\"0\" " +
                        "y=\"" + (yCur + yText) + "\">" +
                        decimalFormat.format(yValue) + "</text>\n");
            }
            yValueMem = yValue;
        }

        // ось X
        sbResult.append("<!-- Ось X -->\n");
        xValueMax = (long) (Math.ceil(xValueMax / 60000.00) * 60000); // максимальное значение на графике - ближайшее большее кратное 1 мин
        int kfX = 60;
        double xScale = Math.min(kfX, xValueMax);
        while (xValueMax % xScale != 0) {
            xScale--;
        }
        xScale = Math.min(xScale, metricsList.size() - 1);
        double xRatio = xSize / (xValueMax * 1.00);
        double xRatioValue = xValueMax / xScale;
        double xStep = xSize / xScale;
        double xCur = xStart;
        long xValue = startTime;
        LOG.info("xSize:{}; xStart: {}; xScale:{}; xRatio:{}; xRatioValue:{}; xStep:{}, xValueMax: {}",
                xSize,
                xStart,
                xScale,
                xRatio,
                xRatioValue,
                xStep,
                sdf0.format(xValueMax));

        long xValueMem = 0;
        while ((int) xCur <= xMax) {
//            LOG.info("xMax: {}, xCur: {}", xMax, xCur);
            if (xCur > xStart) {
                sbResult.append("\t\t\t\t<polyline " +
                        "fill=\"none\" " +
                        "stroke=\"#a0a0a0\" " +
                        "stroke-dasharray=\"" + yText + "\" " +
                        "stroke-width=\"" + lineSize + "\" " +
                        "points=\"" + xCur + "," + yStart + "  " + xCur + "," + yMax + "\"/>\n");
            }
            sbResult.append("\t\t\t\t<text " +
                    "font-size=\"");
            if (!sdf5.format(xValueMem).equals(sdf5.format(xValue))) { // шрифт для полной даты
                sbResult.append(fontSizeX);
            } else {
                sbResult.append(fontSizeX + fontSizeX / 10);
            }
            sbResult.append("\" " +
                    "font-family=\"Courier New\" " +
                    "letter-spacing=\"0\" " + // 0.5
                    "writing-mode=\"tb\" " +
                    "x=\"" + xCur + "\" " +
                    "y=\"" + (yMax + yText) + "\">");
            if (!sdf5.format(xValueMem).equals(sdf5.format(xValue))) { // полную даты выводим 1 раз
                sbResult.append(sdf1.format(xValue)).append("</text>\n");
                xValueMem = xValue;
            } else {
                sbResult.append(sdf4.format(xValue)).append("</text>\n");
            }
            xCur = xCur + xStep;
            xValue = xValue + (long) xRatioValue;
        }

        // рисуем график
        xCur = xStart;
        StringBuilder sbSignature = new StringBuilder("<!-- Метрики на графике -->\n"); // значения метрик на графике
        StringBuilder sbSignatureTitle = new StringBuilder("<!-- Всплывающие надписи -->\n"); // значения метрик на графике

        StringBuilder[] sbGraph = new StringBuilder[metricCount]; // графики
        for (int m = 0; m < metricCount; m++) { // перебираем метрики для отображения
            String curColor = "#10ff10"; // ToDo
            sbGraph[m] = new StringBuilder();
            sbGraph[m].append("<!-- График" + (m + 1) + " -->\n" +
                    "\t\t\t\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"" + curColor + "\" " +
                    "stroke-width=\"" + (lineSize * 2) + "\" " +
                    "points=\"" + xCur + "," + yMax + " \n");
        }

        for (int i = 1; i < metricsList.size(); i++) {
            xCur = (metricsList.get(i).getTime() - startTime) * xRatio + xStart;
            List<Double> yPrevList = new ArrayList<>();
            for (int m = 0; m < metricCount; m++) { // перебираем метрики для отображения
                String curColor = "#10ff10"; // ToDo
                double y = yMax - Math.round(metricsList.get(i).getValue(m) * yRatio);
                // график
                sbGraph[m].append(xCur + "," + y + " \n");
                // значение отличается от предыдущего
                if (i == 1 || metricsList.get(i - 1).getValue(m) != metricsList.get(i).getValue(m)) {
                    // значение метрики
                    if (printMetrics) {
                        // надписи не пересекаются
                        boolean print = true;
                        for (int p = 0; p < yPrevList.size(); p++) {
                            if (Math.abs(y - yPrevList.get(p)) < yText * 4) {
                                print = false;
                                break;
                            }
                        }
                        if (print) {
                            sbSignature.append("\t\t\t\t<text " +
                                    "font-size=\"" + fontSize + "\" " +
                                    "fill=\"#000000\" " +
//                                    "font-weight=\"bold\" " +
                                    "x=\"" + (xCur - xText) + "\" " +
                                    "y=\"" + (y - yText) + "\">" +
                                    decimalFormat.format(metricsList.get(i).getValue(m)) + "</text>\n");
                            yPrevList.add(y);
                        }
                    }
                }
                // точка с всплывающим описанием
                sbSignatureTitle.append("<g> " +
                        "<circle stroke=\"" + curColor + "\" cx=\"" + xCur + "\" cy=\"" + y + "\" r=\"" + (lineSize * 5) + "\"/> " +
                        "<title>");
                sbSignatureTitle.append("время: " + sdf1.format(metricsList.get(i).getTime()) + "; " +
                        "значение: " + decimalFormat.format(metricsList.get(i).getValue(m)) + "</title> " +
                        "</g>\n");
            }
        }
        for (int i = 0; i < metricCount; i++) {
            sbGraph[i].append("\"/>\n");
            sbResult.append(sbGraph[i].toString());
        }
        sbResult.append(sbSignature.toString());
        sbResult.append(sbSignatureTitle.toString());

        sbResult.append("\t\t\t</svg>\n");
        return sbResult.toString();
    }

}
