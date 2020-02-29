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
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");
    private final DateFormat sdf4 = new SimpleDateFormat("HH:mm:ss");
    private final DateFormat sdf5 = new SimpleDateFormat("HH:mm");
    private final DateFormat sdf6 = new SimpleDateFormat("yyyyMMdd");

    private int graphNum = 0;
    private StringBuilder sbGraphResult = new StringBuilder();

    private final int xSize = 10000;
    private final int ySize = (int) (xSize / 6);
    private final int xText = xSize / 500;
    private final int yText = xSize / 400;
    private final int fontSize = xSize / 120;
    private final int fontSizeX = xSize / 156;
    private final int yMarginTop = xSize / 15;
    private final int xMarginRight = xSize / 300;
    private final int xStart = xSize / 20;
    private final int xMax = xSize + xStart + xMarginRight;
    private final int lineSize = Math.max(1, xSize / 5000);

    private final String background = "#f0f0f0"; //"#dfdfdf";
    private final String[] colors = {"#009f00", "#00009f", "#9f0000", "#9f009f"};

    public Graph() {
    }

    /**
     * Данные из JSONArray
     * @param jsonArrayData
     * @return
     */
    private List<DateTimeValue> jsonToList(JSONArray jsonArrayData){
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
        return metricsList;
    }

    public String addGraph(
            String title,
            String startPeriodStr,
            String stopPeriodStr,
            JSONArray jsonArrayData,
            boolean printMetrics) {
        return addGraph(title,
                startPeriodStr,
                stopPeriodStr,
                jsonArrayData,
                null,
                null,
                printMetrics);
    }
    /**
     * Линейный график
     */
    public String addGraph(
            String title,
            String startPeriodStr,
            String stopPeriodStr,
            JSONArray jsonArrayData,
            Integer yMinConst,
            Integer yMaxConst,
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

        List<DateTimeValue> metricsList = jsonToList(jsonArrayData);
        int metricCount = metricsList.get(0).getValueSize();

//        int xSize = Math.max(10000, metricsList.size() - 1);
//        int ySize = (int) (xSize / 6);
        int yStart = (graphNum == 0 ? yMarginTop : graphNum * (ySize + fontSize) + yMarginTop);
        int yMax = ySize + yStart;

        // максимальное/минимальное значение Y и X
        long xValueMin = 999999999999999999L;
        long xValueMax = 0L;
        double yValueMin = 999999999;
        double yValueMax = 0.00;
        for (int i = 1; i < metricsList.size(); i++) {
            for (int m = 0; m < metricCount; m++) {
                yValueMax = Math.max(yValueMax, metricsList.get(i).getIntValue(m));
                yValueMin = Math.min(yValueMin, metricsList.get(i).getIntValue(m));
            }
            xValueMin = Math.min(xValueMin, metricsList.get(i).getTime());
            xValueMax = Math.max(xValueMax, metricsList.get(i).getTime());
        }
        LOG.info("Min X: {}, Max X: {}, ({}), Min Y: {}, Max Y: {}",
                sdf0.format(xValueMin),
                sdf0.format(xValueMax),
                xValueMax - xValueMin,
                yValueMin,
                yValueMax);

        if (xValueMax == 0 && yValueMax == 0){ return ""; }
        if (yMinConst != null) { yValueMin = yMinConst;}
        if (yMaxConst != null) { yValueMax = yMaxConst;}
/*
"\t\t\t\t<style type=\"text/css\">\n" +
"\t\t\t\t\t.title-text {fill: #000; font-size: " + fontSize + "px; text-anchor: middle;}\n" +
"\t\t\t\t\t.title-vertical {writing-mode: tb;}\n" +
"\t\t\t\t</style>\t \n" +
*/

        sbGraphResult.append("<!--" + title + "-->\n" +
                "<!-- Область графика -->\n" +
                "\t\t\t\t<rect " +
                "stroke=\"#0f0f0f\" " +
                "fill=\"" + background + "\" " +
                "x=\"" + xStart + "\" " +
                "y=\"" + yStart +"\" " +
                "width=\"" + xSize + "\" " +
                "height=\"" + ySize + "\"/>\n");

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

        boolean yStartFrom0 = false;
        // ось Y
        sbGraphResult.append("<!-- Ось Y -->\n");
        if (yStartFrom0) { yValueMin = 0L; } // начальное значение по оси Y = 0 или минимальному значению из списка
        yValueMin = (int) yValueMin;
        if (yValueMax > 1) {
            yValueMax = (int) (Math.ceil(yValueMax / 1.00) * 1);
        }
        int kfY = 40;
        double yValueRange = yValueMax - yValueMin;
        double yScale = Math.max(Math.min(kfY, yValueRange), 10);
        if (yValueRange > 10) {
            while (true) {
                yScale = Math.max(Math.min(kfY, yValueRange), 10);
                while (yValueRange % yScale != 0) {
                    yScale--;
                }
                if (yScale == yValueRange || yScale > 10){
                    break;
                } else {
                    yValueMax++;
                    yValueRange = yValueMax - yValueMin;
                }
//                LOG.info("{}: {} {}, {}", multiRunService.getName(), yValueMin, yValueMax, yScale);
            }
        }
        double yRatio = ySize / (yValueRange * 1.00);
        double yRatioValue = yValueRange / (yScale * 1.00);
        double yStep = ySize / (yScale * 1.00);
        double yValue = yValueMin;
        yCur = yMax;
//        LOG.info("ySize:{}; yStart: {}; yScale:{}; yRatio:{}; yRatioValue:{}; yStep:{}; yCur:{}", ySize, yStart, yScale, yRatio, yRatioValue, yStep, yCur);
        while (yValue <= yValueMax) {
            sbGraphResult.append("\t\t\t\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"#a0a0a0\" " +
                    "stroke-dasharray=\"" + xText + "\" " +
                    "stroke-width=\"" + lineSize + "\" " +
                    "points=\"" + xStart + "," + yCur + "  " + xMax + "," + yCur + "\"/>\n");
            sbGraphResult.append("\t\t\t\t<text " +
                    "font-size=\"" + fontSize + "\" " +
                    "x=\"" + (xStart / 1.5) + "\" " +
                    "y=\"" + (yCur + yText) + "\">" +
                    decimalFormat.format(yValue) + "</text>\n");
            yCur = yCur - yStep;
            yValue = yValue + yRatioValue;
        }
        sbGraphResult.append("<!-- Название графика -->\n")
                .append("\t\t\t\t<text " +
                "font-size=\"" + (fontSize*2) + "\" " +
                "writing-mode=\"tb\" " +
                "x=\"" + (fontSizeX) + "\" " +
                "y=\"" + yStart + "\">" +
//                "y=\"" + ((yMax - yStart)/2) + "\">" +
                "" + title + "</text>\n");

//<text x="100" y="1500" class="cota-text cota-vertical">TEXT TEXT TEXT TEXT TEXT TEXT TEXT</text>
        // ось X
        int xAccuracy = 60000;
        sbGraphResult.append("<!-- Ось X -->\n");
        long xValueRange = xValueMax - xValueMin;
        double xScale;
        int kfX = 60;
        while (true) {
            xScale = Math.min(kfX, xValueRange);
            while ((xValueRange / xScale) % xAccuracy != 0) {
                xScale--;
            }
            if (xScale == xValueRange/xAccuracy || xScale > 20){
                break;
            } else {
                xValueMax = xValueMax + xAccuracy;
                xValueRange = xValueMax - xValueMin;
            }
//            LOG.info("{}: {} {}, {}", multiRunService.getName(), xValueMin, xValueMax, xScale);
        }
        double xRatio = xSize / (xValueRange * 1.00);
        double xRatioValue = xValueRange / xScale;
        double xStep = xSize / xScale;
        double xCur = xStart;
        long xValue = xValueMin;
//        LOG.info("xSize:{}; xStart: {}; xScale:{}; xRatio:{}; xRatioValue:{}; xStep:{}", xSize, xStart, xScale, xRatio, xRatioValue, xStep);
        long xValueMem = 0;
        if (xStep > 0) {
            while (xValue <= xValueMax) {
//            LOG.info("xMax: {}, xCur: {}", xMax, xCur);
                if (xCur > xStart) {
                    sbGraphResult.append("\t\t\t\t<polyline " +
                            "fill=\"none\" " +
                            "stroke=\"#a0a0a0\" " +
                            "stroke-dasharray=\"" + yText + "\" " +
                            "stroke-width=\"" + lineSize + "\" " +
                            "points=\"" + xCur + "," + yStart + "  " + xCur + "," + yMax + "\"/>\n");
                }
                if (graphNum == 0) {
                    sbGraphResult.append("\t\t\t\t<text font-size=\"")
                                .append((int) (fontSizeX * 1.35))
                                .append("\" " +
                            "font-family=\"Areal\" " +
                            "letter-spacing=\"0\" " + // 0.5
                            "writing-mode=\"tb\" " +
                            "x=\"" + xCur + "\" " +
//                        "y=\"" + (yMax + yText) + "\">");
                            "y=\"" + yText + "\">");
                    if (!sdf6.format(xValueMem).equals(sdf6.format(xValue))) { // полную даты выводим 1 раз
                        sbGraphResult.append(sdf2.format(xValue)).append("</text>\n");
                        xValueMem = xValue;
                    } else {
                        sbGraphResult.append(sdf5.format(xValue)).append("</text>\n");
                    }
                }
                xCur = xCur + xStep;
                xValue = xValue + (long) xRatioValue;
            }
        }


        // рисуем график
        xCur = xStart;
        StringBuilder sbSignature = new StringBuilder("<!-- Метрики на графике -->\n"); // значения метрик на графике
        StringBuilder sbSignatureTitle = new StringBuilder("<!-- Всплывающие надписи -->\n"); // значения метрик на графике

        StringBuilder[] sbGraph = new StringBuilder[metricCount]; // графики
        for (int m = 0; m < metricCount; m++) { // перебираем метрики для отображения
            String curColor = colors[m];
            sbGraph[m] = new StringBuilder();
            sbGraph[m].append("<!-- График" + (m + 1) + " -->\n" +
                    "\t\t\t\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"" + curColor + "\" " +
                    "stroke-width=\"" + (lineSize * 2) + "\" " +
                    "points=\"\n");
//                    "points=\"" + xCur + "," + yMax + " \n");
        }

        for (int i = 1; i < metricsList.size(); i++) {
            xCur = (metricsList.get(i).getTime() - xValueMin) * xRatio + xStart;
            List<Double> yPrevList = new ArrayList<>();
            for (int m = 0; m < metricCount; m++) { // перебираем метрики для отображения
                String curColor = colors[m];
                double y = yMax - Math.round((metricsList.get(i).getValue(m) - yValueMin) * yRatio);
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
                sbSignatureTitle.append("время: " + sdf2.format(metricsList.get(i).getTime()) + "; " +
                        "значение: " + decimalFormat.format(metricsList.get(i).getValue(m)) + "</title> " +
                        "</g>\n");
            }
        }
        for (int i = 0; i < metricCount; i++) {
            sbGraph[i].append("\"/>\n");
            sbGraphResult.append(sbGraph[i].toString());
        }
        sbGraphResult.append(sbSignature.toString());
        sbGraphResult.append(sbSignatureTitle.toString());

        graphNum++;
        return getSvg();
    }


    /**
     * Получить все графики
     * @return
     */
    public String getSvg(){
        return "\t\t\t<svg " +
                "width=\"" + xMax +"\" " +
                "height=\"" + (ySize * graphNum + yMarginTop) + "\" " +
                "xmlns=\"http://www.w3.org/2000/svg\" " +
                "xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                sbGraphResult.toString() +
                "</svg>";
    }

    public String getHtml(){
        return "<html>\n" +
                "\t<head>\n" +
                "\t\t<meta charset=\"UTF-8\">\n" +
                "\t\t<style>\n" +
                "\t\t\tbody, html { width:100%; height:100%; margin:0; background:#fdfdfd}\n" +
                "\t\t\t.graph { width:95%; border-radius:5px; box-shadow: 0 0 1px 1px rgba(0,0,0,0.5); margin:50px auto; border:1px; solid #ccc; background:#fff}\n" +
                "\t\t</style>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t\t<div class=\"graph\">\n" +
                "\t\t\t<svg viewBox=\"0 0 " + xMax + " " + (ySize * graphNum + yMarginTop) +" \" class=\"chart\">\n" +
                sbGraphResult.toString() +
                "\t</body>\n" +
                "</html>";
    }
}
