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
import java.util.Iterator;
import java.util.List;

public class Graph {
    private static final Logger LOG = LogManager.getLogger(Graph.class);
    private final NumberFormat decimalFormat = NumberFormat.getInstance();
    private final DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final DateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final DateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");
    private final DateFormat sdf4 = new SimpleDateFormat("HH:mm:ss");
    private final DateFormat sdf5 = new SimpleDateFormat("HH:mm");
    private final DateFormat sdf6 = new SimpleDateFormat("yyyyMMdd");

    private int graphNum = 0;
    private StringBuilder sbGraphResult = new StringBuilder();
    private List<String> metricsNameList = new ArrayList<>();

    private long startPeriod = 0L;
    private long stopPeriod = 0L;

    private final int xSize = 10000;
    private final int ySize = (int) (xSize / 5);
    private final int xText = xSize / 500;
    private final int yText = xSize / 400;
    private final int fontSize = xSize / 120;
    private final int fontSizeX = xSize / 156;
    private final int yMarginTop = xSize / 15;
    private final int xMarginRight = xSize / 300;
    private final int xStart = xSize / 20;
    private final int xMax = xSize + xStart + xMarginRight;
    private final int lineSize = Math.max(1, xSize / 5000);

    private String background = "#ffffff";
    private List<String> colors = new ArrayList<>();

    public Graph() {
        colors.add("#009f00");
        colors.add("#00009f");
        colors.add("#9f0000");
        colors.add("#9f009f");
        for (int i = 0; i < 10; i++){ // запас
            colors.add("#009f00");
        }
    }

    /**
     * Цвет фона
     * @param background
     */
    public void setBackground(String background){
        this.background = background;
    }

    /**
     * Цвет линий списком
     * @param colors
     */
    public void setColor(List<String> colors){
        this.colors = colors;
    }

    /**
     * Цвет линии по номерц
     * @param num
     * @param color
     */
    public void setColor(int num, String color){
        while (colors.size() < num){
            colors.add("#009f00"); // цвет по умолчанию
        }
        colors.set(num - 1, color);
    }

    /**
     * Добавляем элемент jsonObject в metricsList
     * @param jsonObject
     */
    private DateTimeValue getElementFromJSONObject(JSONObject jsonObject) {
        long date = 0L;
        Iterator<?> keys = jsonObject.sortedKeys();
        List<Number> dataList = new ArrayList<>();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            try {
                LOG.trace("{}: {}", key, jsonObject.getString(key));
            } catch (JSONException e) {
                LOG.error("Ошибка в формате json", e);
            }
            if (key.equals("date")) {
                try {
                    date = sdf0.parse(jsonObject.getString("date")).getTime();
                } catch (Exception e) {
                    LOG.error("Ошибка в формате даты");
                }
            } else {
                int index = -1;
                if (metricsNameList.size() > 0) {
                    index = metricsNameList.indexOf(key);
                }
                if (index == -1) {
                    metricsNameList.add(key);
                    index = metricsNameList.size() - 1;
                }
                try {
                    dataList.add(jsonObject.getDouble(key));
                } catch (Exception e) {
                    LOG.error("Ошибка в формате json\n", e);
                }
            }
        }
        return new DateTimeValue(date, dataList);
    }

    /**
     * Данные из JSONArray
     * @param jsonArrayData
     * @return
     */
    private List<DateTimeValue> jsonToList(JSONArray jsonArrayData) {
        List<DateTimeValue> metricsList = new ArrayList<>();
        for (int i = 0; i < jsonArrayData.length(); i++) {
            try {
                JSONObject jsonObject = jsonArrayData.getJSONObject(i);
                metricsList.add(getElementFromJSONObject(jsonObject));
            } catch (JSONException e) {
                LOG.error("Ошибка в формате данных");
            }
        }
        return metricsList;
    }

    /**
     * @param jsonArrayData
     * @param title
     * @param printMetrics
     * @return
     */
    public String addGraph(
            JSONArray jsonArrayData,
            String title,
            boolean printMetrics) {
        return addGraph(jsonArrayData,
                title,
                "",
                "",
                null,
                null,
                null,
                null,
                printMetrics);
    }

    /**
     * @param jsonArrayData
     * @param title
     * @param startPeriodStr
     * @param stopPeriodStr
     * @param printMetrics
     * @return
     */
    public String addGraph(
            JSONArray jsonArrayData,
            String title,
            String startPeriodStr,
            String stopPeriodStr,
            boolean printMetrics) {
        return addGraph(jsonArrayData,
                title,
                startPeriodStr,
                stopPeriodStr,
                null,
                null,
                null,
                null,
                printMetrics);
    }

    /**
     * Линейный график
     */
    public String addGraph(
            JSONArray jsonArrayData,
            String title,
            String startPeriodStr,
            String stopPeriodStr,
            Number yMinConst,
            Number yMaxConst,
            Number yMinNorm,
            Number yMaxNorm,
            boolean printMetrics) {

        if (!startPeriodStr.isEmpty() && startPeriod == 0L) {
            try {
                startPeriod = sdf.parse(startPeriodStr).getTime();
            } catch (ParseException e) {
                LOG.error("Ошибка в формате даты: {}", startPeriodStr);
                return "";
            }
        }
        if (!stopPeriodStr.isEmpty() && stopPeriod == 0L) {
            try {
                stopPeriod = sdf.parse(stopPeriodStr).getTime();
            } catch (ParseException e) {
                LOG.error("Ошибка в формате даты: {}", stopPeriodStr);
                return "";
            }
        }

        LOG.info("Формирование графика {} ({} - {})",
                title,
                sdf.format(startPeriod),
                sdf.format(stopPeriod));

        List<DateTimeValue> metricsList = jsonToList(jsonArrayData);
        int metricCount = metricsList.get(0).getValueSize();

        int yStart = (graphNum == 0 ? yMarginTop : graphNum * (ySize + fontSize) + yMarginTop);
        int yMax = yStart + ySize;

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
        LOG.debug("Min X: {} ({}), Max X: {} ({}), Min Y: {} ({}), Max Y: {} ({})",
                sdf0.format(xValueMin),
                startPeriod > 0L ? sdf0.format(startPeriod) : "",
                sdf0.format(xValueMax),
                startPeriod > 0L ? sdf0.format(stopPeriod) : "",
                yValueMin,
                yMinConst,
                yValueMax,
                yMaxConst);

        if (xValueMax == 0 || yValueMax == 0) {
            return "";
        }
        if (yMinConst != null) {
            yValueMin = yMinConst.doubleValue();
        }
        if (yMaxConst != null) {
            yValueMax = yMaxConst.doubleValue();
        }

        if (startPeriod > 0) {
            xValueMin = startPeriod;
        } else {
            startPeriod = xValueMin;
        }
        if (stopPeriod > 0) {
            xValueMax = stopPeriod;
        } else {
            stopPeriod = xValueMax;
        }

        sbGraphResult.append("<!--" + title + "-->\n" +
                "<!-- Область графика -->\n" +
                "\t<rect " +
                "stroke=\"#0f0f0f\" " +
                "fill=\"" + background + "\" " +
                "x=\"" + xStart + "\" " +
                "y=\"" + yStart + "\" " +
                "width=\"" + xSize + "\" " +
                "height=\"" + ySize + "\"/>\n");

        // описание
        double yCur = fontSize / 1.5;
/*        for (int i = 0; i < metricViewGroup.getMetricsCount(); i++) {
            if (!metricViewGroup.getMetricView(i).getTitle().isEmpty()) {
                sbResult.append(
                        "\t<polyline fill=\"none\" stroke=\"" + metricViewGroup.getMetricView(i).getColor() + "\" stroke-width=\"" + (lineSize * 4) + "\" points=\"" + xStart + "," + yCur + " " + xStart * 3 + "," + yCur + "\"/>\n" +
                        "\t<text font-size=\"" + fontSize + "\" font-weight=\"bold\" x=\"" + ((xStart * 3) + 10) + "\" y=\"" + yCur + "\">" + metricViewGroup.getMetricView(i).getTitle() + "</text>\n");
                yCur = yCur + fontSize;
            }
        }
*/

        // ось Y
        sbGraphResult.append("<!-- Ось Y -->\n");
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
                if (yScale == yValueRange || yScale > 10) {
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
        double yPrev = yCur + fontSize / 1.4 + 1;
//        LOG.info("ySize:{}; yStart: {}; yScale:{}; yRatio:{}; yRatioValue:{}; yStep:{}; yCur:{}", ySize, yStart, yScale, yRatio, yRatioValue, yStep, yCur);
        while (yValue <= yValueMax) {
            sbGraphResult.append("\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"#a0a0a0\" " +
                    "stroke-dasharray=\"" + xText + "\" " +
                    "stroke-width=\"" + lineSize + "\" " +
                    "points=\"" + xStart + "," + yCur + "  " + xMax + "," + yCur + "\"/>\n");
            if (yCur < (yPrev - fontSize / 1.4)) {
                sbGraphResult.append("\t<text " +
                        "font-size=\"" + fontSize + "\" " +
                        "x=\"" + (xStart / 1.5) + "\" " +
                        "y=\"" + (yCur + yText) + "\">" +
                        decimalFormat.format(yValue) + "</text>\n");
                yPrev = yCur;
            }
            yCur = yCur - yStep;
            yValue = yValue + yRatioValue;
        }
        // норма
        if (yMinNorm != null) {
            sbGraphResult.append("<!-- Норма Y min -->\n");
            double y = yMax - Math.round((yMinNorm.doubleValue() - yValueMin) * yRatio);
            sbGraphResult.append("\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"#000000\" " +
                    "stroke-dasharray=\"" + xText + "\" " +
                    "stroke-width=\"" + lineSize + "\" " +
                    "points=\"" + xStart + "," + y + "  " + xMax + "," + y + "\"/>\n");
        }
        if (yMaxNorm != null) {
            sbGraphResult.append("<!-- Норма Y max -->\n");
            double y = yMax - Math.round((yMaxNorm.doubleValue() - yValueMin) * yRatio);
            sbGraphResult.append("\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"#000000\" " +
                    "stroke-dasharray=\"" + xText + "\" " +
                    "stroke-width=\"" + lineSize + "\" " +
                    "points=\"" + xStart + "," + y + "  " + xMax + "," + y + "\"/>\n");
        }


        sbGraphResult.append("<!-- Название графика -->\n")
                .append("\t<text " +
                        "font-size=\"" + (fontSize * 2) + "\" " +
                        "writing-mode=\"tb\" " +
                        "x=\"" + (fontSizeX) + "\" " +
                        "y=\"" + yStart + "\">" +
//                "y=\"" + ((yMax - yStart)/2) + "\">" +
                        "" + title + "</text>\n");

        // ось X
        int xAccuracy = 60000;
        sbGraphResult.append("<!-- Ось X -->\n");
        long xValueRange = xValueMax - xValueMin;
        if (xValueRange < 1) {
            return  "";
        }
        double xScale;
        int kfX = 60;
        while (true) {
            xScale = Math.min(kfX, xValueRange);
            while (xScale > 0 && (xValueRange / xScale) % xAccuracy != 0) {
                xScale--;
            }
            if (xScale == xValueRange / xAccuracy || xScale > 20) {
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
                    sbGraphResult.append("\t<polyline " +
                            "fill=\"none\" " +
                            "stroke=\"#a0a0a0\" " +
                            "stroke-dasharray=\"" + yText + "\" " +
                            "stroke-width=\"" + lineSize + "\" " +
                            "points=\"" + xCur + "," + yStart + "  " + xCur + "," + yMax + "\"/>\n");
                }
                if (graphNum == 0) {
                    sbGraphResult.append("\t<text font-size=\"")
                            .append((int) (fontSizeX * 1.35))
                            .append("\" " +
                                    "font-family=\"Areal\" " +
                                    "letter-spacing=\"0\" " + // 0.5
                                    "writing-mode=\"tb\" " +
                                    "x=\"" + xCur + "\" " +
//                        "y=\"" + (yMax + yText) + "\">");
                                    "y=\"" + yText + "\">");
                    if (!sdf6.format(xValueMem).equals(sdf6.format(xValue))) { // полную дату выводим 1 раз
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
        StringBuilder sbSignature = new StringBuilder("<!-- Метрики на графике -->\n"); // значения метрик на графике
        StringBuilder sbSignatureTitle = new StringBuilder("<!-- Всплывающие надписи -->\n"); // значения метрик на графике

        StringBuilder[] sbGraph = new StringBuilder[metricCount]; // графики
        for (int m = 0; m < metricCount; m++) { // перебираем метрики для отображения
            String curColor = colors.get(m);
            sbGraph[m] = new StringBuilder();
            sbGraph[m].append("<!-- График" + (m + 1) + " -->\n" +
                    "\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"" + curColor + "\" " +
                    "stroke-width=\"" + (lineSize * 2) + "\" " +
                    "points=\"\n");
        }

        for (int i = 1; i < metricsList.size(); i++) {
            if (metricsList.get(i).getTime() >= startPeriod && metricsList.get(i).getTime() <= stopPeriod) {
                xCur = (metricsList.get(i).getTime() - xValueMin) * xRatio + xStart;
                List<Double> yPrevList = new ArrayList<>();
                for (int m = 0; m < metricCount; m++) { // перебираем метрики для отображения
                    String curColor = colors.get(m);
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
                                sbSignature.append("\t<text " +
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
                    sbSignatureTitle.append("\t<g> " +
                            "<circle stroke=\"" + curColor + "\" cx=\"" + xCur + "\" cy=\"" + y + "\" r=\"" + (lineSize * 5) + "\"/> " +
                            "<title>");
                    sbSignatureTitle.append("время: " + sdf2.format(metricsList.get(i).getTime()) + "; " +
                            "значение: " + decimalFormat.format(metricsList.get(i).getValue(m)) + "</title> " +
                            "</g>\n");
                }
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
     *
     * @return
     */
    public String getSvg() {
        return "<svg " +
                "width=\"" + xMax + "\" " +
                "height=\"" + (graphNum * (ySize + fontSize) + yMarginTop) + "\" " +
                "xmlns=\"http://www.w3.org/2000/svg\" " +
                "xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                sbGraphResult.toString() +
                "</svg>";
    }

    public String getHtml() {
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
                "<svg viewBox=\"0 0 " + xMax + " " + (graphNum * (ySize + fontSize) + yMarginTop) + " \" class=\"chart\">\n" +
                sbGraphResult.toString() +
                "\t</body>\n" +
                "</html>";
    }

}
