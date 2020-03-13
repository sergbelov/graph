package ru.utils.graph;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.utils.graph.data.DateTimeValue;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Graph {
    private final NumberFormat decimalFormat = NumberFormat.getInstance();
    private final DateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private DateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
    private final DateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final DateFormat sdf3 = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private final DateFormat sdf4 = new SimpleDateFormat("HH:mm:ss");
    private final DateFormat sdf5 = new SimpleDateFormat("HH:mm");
    private final DateFormat sdf6 = new SimpleDateFormat("yyyyMMdd");

//    private final long xAccuracy = 60000; // минимальный временной шаг (мс)
//    private final long xAccuracy = 60000 * 60; // минимальный временной шаг (мс)
    private long xAccuracy; // минимальный временной шаг 1 день (мс)

    private int graphNum = 0;
    private StringBuilder sbGraphResult = new StringBuilder();
    private List<String> metricsNameList = new ArrayList<>();

    private long start;             // начало срока
    private long startPeriod = 0L;  // начало периода
    private long stopPeriod = 0L;   // конец периода
    private double xScale;          // количество интервалов по X
    private List<Long> weekList = new ArrayList<>(); // список недель

    private final int xSize = 10000;
    private final int ySize = (int) (xSize / 5);
    private final int xText = xSize / 500;
    private final int yText = xSize / 400;
    private final int fontSize = xSize / 120;
    private final int fontSizeX = xSize / 156;
    private final int yMarginTop = xSize / 15;
    private final int xMarginRight = xSize / 300;
    private final int xStart = xSize / 15;
    private final int xMax = xSize + xStart + xMarginRight;
    private final int lineSize = Math.max(1, xSize / 5000);
    private final int maxStepInX = 60; // максимальное количество шагов по оси X
    private final int maxStepInY = 30;
    private int yStart = yMarginTop;

    private boolean printMetrics = false;
    private String background = "#ffffff";
    private List<String> colors = new ArrayList<>();


    /**
     * Инициализация
     */
    public Graph() {
        // цвета по умолчанию
        colors.add("#009f00");
        colors.add("#00009f");
        colors.add("#9f0000");
        colors.add("#9f009f");
        for (int i = 0; i < 10; i++) { // запас
            colors.add("#009f00");
        }

/*
        // точность периода по X
        if (xAccuracy == 60000 * 60 * 24) {
            sdf0 = new SimpleDateFormat("yyyy-MM-dd");
            sdf1 = new SimpleDateFormat("dd-MM-yyyy");
        } else if (xAccuracy == 60000 * 60) {
            sdf0 = new SimpleDateFormat("yyyy-MM-dd HH");
            sdf1 = new SimpleDateFormat("dd-MM-yyyy HH");
        } else {
            sdf0 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf1 = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        }
*/
    }

    /**
     * Цвет фона
     * @param background
     */
    public void setBackground(String background) {
        this.background = background;
    }

    /**
     * Цвет линий списком
     * @param colors
     */
    public void setColor(List<String> colors) {
        this.colors = colors;
    }

    /**
     * Цвет линии по номеру
     * @param num
     * @param color
     */
    public void setColor(int num, String color) {
        while (colors.size() < num) {
            colors.add("#009f00"); // цвет по умолчанию
        }
        colors.set(num - 1, color);
    }

    /**
     * Добавляем элемент jsonObject в metricsList
     * @param jsonObject
     */
    private DateTimeValue getElementFromJSONObject(JSONObject jsonObject) throws Exception {
        long date = 0L;
        Iterator<?> keys = jsonObject.sortedKeys();
        List<Number> dataList = new ArrayList<>();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (key.equals("date")) {
                date = sdf2.parse(jsonObject.getString("date")).getTime();
            } else {
                int index = -1;
                if (metricsNameList.size() > 0) {
                    index = metricsNameList.indexOf(key);
                }
                if (index == -1) {
                    metricsNameList.add(key);
                    index = metricsNameList.size() - 1;
                }
                dataList.add(jsonObject.getDouble(key));
            }
        }
        return new DateTimeValue(date, dataList);
    }

    /**
     * Данные из JSONArray
     * @param jsonArrayData
     * @return
     */
    private List<DateTimeValue> jsonToList(JSONArray jsonArrayData) throws Exception {
        List<DateTimeValue> metricsList = new ArrayList<>();
        for (int i = 0; i < jsonArrayData.length(); i++) {
            JSONObject jsonObject = jsonArrayData.getJSONObject(i);
            metricsList.add(getElementFromJSONObject(jsonObject));
        }
        return metricsList;
    }


    /**
     * Задаем период (строковые параметры)
     * @param startPeriodStr    // Начало периода
     * @param stopPeriodStr     // Конец периода
     * @throws Exception
     */
    public void setPeriod(String startPeriodStr, String stopPeriodStr) throws Exception {
        setPeriod(startPeriodStr, stopPeriodStr, "");
    }
    /**
     * Задаем период (строковые параметры)
     * @param startPeriodStr    // Начало периода
     * @param stopPeriodStr     // Конец периода
     * @param startStr          // Начало срока
     * @throws Exception
     */
    public void setPeriod(String startPeriodStr, String stopPeriodStr, String startStr) throws Exception {
        long startPeriod = 0L;
        long stopPeriod = 0L;
        long start = 0L;
        if (!startPeriodStr.isEmpty()) {
            startPeriod = sdf0.parse(startPeriodStr).getTime();
        }
        if (!stopPeriodStr.isEmpty()) {
            stopPeriod = sdf0.parse(stopPeriodStr).getTime();
        }
        if (!startStr.isEmpty()) {
            start = sdf0.parse(startStr).getTime();
        }
        setPeriod(startPeriod, stopPeriod, start);
    }

    /**
     * Задаем период (long параметры)
     * @param startPeriod // начало периоды
     * @param stopPeriod  // конец периода
     * @throws Exception
     */
    public void setPeriod(long startPeriod, long stopPeriod) throws Exception {
        setPeriod(startPeriod, stopPeriod, 0L);
    }
    /**
     * Задаем период (long параметры)
     * @param startPeriod // начало периоды
     * @param stopPeriod  // конец периода
     * @param start       // начало срока
     * @throws Exception
     */
    public void setPeriod(long startPeriod, long stopPeriod, long start) throws Exception {
        boolean setStartPeriod = false;
        boolean setStopPeriod = false;
        int xScaleW = 60;
        weekList.clear();
        if (startPeriod > 0 && startPeriod < stopPeriod && start <= startPeriod) {
            if (start != 0L){ this.start = start;}
            if (start > 0){ // задано начало срока, формируем недельные интервалы
                long step = 1000*60*60*24*7; // 7 дней
                xAccuracy = step;
                long startW = start;
                for (int i = 0; i < 41; i++){
//                    System.out.println(i+1 + " " + sdf0.format(startW));
                    weekList.add(startW);
                    if (!setStartPeriod && (startW + step) > startPeriod){
                        startPeriod = startW;
                        setStartPeriod = true;
                    }
                    startW = startW + step;
                    if (!setStopPeriod &&  startW > stopPeriod){
                        stopPeriod = startW;
                        setStopPeriod = true;
                    }
                }
//                if (startPeriod == start && stopPeriod == weekList.get(40)) {
                if (start > 0) {
                    xScaleW = (int) ((stopPeriod - startPeriod) / step);
                }
/*
                while (start+step < startPeriod){
                    start = start + step;
                }
                startPeriod = start;
*/
//                System.out.println("2019-02-20" + " " +getNumWeek(sdf0.parse("2019-02-20").getTime()));
            } else {
                xAccuracy = 1000 * 60 * 60 * 24; // минимальный временной шаг 1 день (мс)
            }
            // округляем время начала периода
            if (start == 0) {
                this.startPeriod = sdf0.parse(sdf0.format(startPeriod)).getTime();
                // округляем время окончания периода
                this.stopPeriod = (long) (Math.ceil((stopPeriod + xAccuracy) / xAccuracy * 1.00) * xAccuracy);
                this.stopPeriod = sdf0.parse(sdf0.format(this.stopPeriod)).getTime();
            } else {
                this.startPeriod = startPeriod;
                this.stopPeriod = stopPeriod;
            }
            while (true) {
                long xValueRange = this.stopPeriod - this.startPeriod;
                xScale = Math.min(maxStepInX, xValueRange);
                xScale = Math.min(xScale, xScaleW);
                while (xScale > 0 && (xValueRange / xScale) % xAccuracy != 0) {
                    xScale--;
                }
                if (xScale == xValueRange / xAccuracy || xScale > 20) {
                    break;
                } else {
                    this.stopPeriod = this.stopPeriod + xAccuracy;
                }
            }
            graphNum = 0;
            sbGraphResult.setLength(0);
        } else {
            throw new Exception("Ошибка в датах: " + sdf2.format(startPeriod) + " " + sdf2.format(stopPeriod) + "(" + sdf2.format(start) + ")");
        }
    }

    /**
     * Номер недели
     * @param date
     * @return
     */
    private int getNumWeek(long date){
        for (int i = 0; i < weekList.size(); i++){
            if (weekList.get(i) > date){
                return i;
            }
        }
        return 0;
    }

    /**
     * Очистка графиков и таблиц
     */
    public void clear(){
        graphNum = 0;
        yStart = yMarginTop;
        sbGraphResult.setLength(0);
    }

    /**
     * Добавляем новый график
     * @param jsonArrayDataString список метрик
     * @param title               название графика
     * @return
     */
    public String addGraph(
            String jsonArrayDataString,
            String title) throws Exception {
        JSONArray jsonArrayData = new JSONArray(jsonArrayDataString);
        return addGraph(jsonArrayData, title);
    }
    /**
     * Добавляем новый график
     * @param jsonArrayData список метрик
     * @param title         название графика
     * @return
     */
    public String addGraph(
            JSONArray jsonArrayData,
            String title) throws Exception {
        return addGraph(jsonArrayData,
                title,
                null,
                null,
                null,
                null);
    }


    /**
     * Добавляем новый график
     * @param jsonArrayDataString список метрик
     * @param title         название графика
     * @param yMinConst     минимальное значение диапазона Y
     * @param yMaxConst     макимальное значение диапазона Y
     * @param yMinNorm      минимальное допустимое значение Y
     * @param yMaxNorm      максимальное допустимое значение Y
     * @return
     */
    public String addGraph(
            String jsonArrayDataString,
            String title,
            Double yMinConst,
            Double yMaxConst,
            Double yMinNorm,
            Double yMaxNorm) throws Exception {
        JSONArray jsonArrayData = new JSONArray(jsonArrayDataString);
        return addGraph(jsonArrayData,
                title,
                yMinConst,
                yMaxConst,
                yMinNorm,
                yMaxNorm);
    }
    /**
     * Добавляем новый график
     * @param jsonArrayData список метрик
     * @param title         название графика
     * @param yMinConst     минимальное значение диапазона Y
     * @param yMaxConst     макимальное значение диапазона Y
     * @param yMinNorm      минимальное допустимое значение Y
     * @param yMaxNorm      максимальное допустимое значение Y
     * @return
     */
    public String addGraph(
            JSONArray jsonArrayData,
            String title,
            Double yMinConst,
            Double yMaxConst,
            Double yMinNorm,
            Double yMaxNorm) throws Exception {

        if (startPeriod > stopPeriod) {
            throw new Exception("Не верно задан период для отчета");
        }
        List<DateTimeValue> metricsList = jsonToList(jsonArrayData);
        int metricCount = metricsList.get(0).getValueSize();

//        yStart = (graphNum == 0 ? yMarginTop : graphNum * (ySize + fontSize) + yMarginTop);
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

        if (xValueMax == 0 || yValueMax == 0) {
            return "";
        }
        if (yMinConst != null) {
            yValueMin = yMinConst;
        }
        if (yMaxConst != null) {
            yValueMax = yMaxConst;
        }

        xValueMin = startPeriod;
        xValueMax = stopPeriod;

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
        double yValueRange = yValueMax - yValueMin;
        double yScale = Math.max(Math.min(maxStepInY, yValueRange), 10);
        if (yValueRange > 10) {
            while (true) {
                yScale = Math.max(Math.min(maxStepInY, yValueRange), 10);
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
            double y = yMax - Math.round((yMinNorm - yValueMin) * yRatio);
            sbGraphResult.append("\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"#000000\" " +
                    "stroke-dasharray=\"" + xText + "\" " +
                    "stroke-width=\"" + lineSize + "\" " +
                    "points=\"" + xStart + "," + y + "  " + xMax + "," + y + "\"/>\n");
        }
        if (yMaxNorm != null) {
            sbGraphResult.append("<!-- Норма Y max -->\n");
            double y = yMax - Math.round((yMaxNorm - yValueMin) * yRatio);
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
        sbGraphResult.append("<!-- Ось X -->\n");
        long xValueRange = xValueMax - xValueMin;
        double xRatio = xSize / (xValueRange * 1.00);
        double xRatioValue = xValueRange / xScale;
        double xStep = xSize / xScale;
        double xCur = xStart;
        long xValue = xValueMin;
//        LOG.info("xSize:{}; xStart: {}; xScale:{}; xRatio:{}; xRatioValue:{}; xStep:{}", xSize, xStart, xScale, xRatio, xRatioValue, xStep);
        long xValueMem = 0;
        if (xStep > 0) {
            int weekPrev = 0;
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
                        sbGraphResult.append(sdf1.format(xValue)).append("</text>\n");
                        xValueMem = xValue;
                    } else {
                        sbGraphResult.append(sdf5.format(xValue)).append("</text>\n");
                    }
                    int week = getNumWeek(xValue);
                    if (week > weekPrev) {
                        sbGraphResult.append("\t<text font-size=\"")
                                .append((int) (fontSizeX * 1.35))
                                .append("\" " +
                                        "font-family=\"Areal\" font-weight=\"bold\"" +
                                        "x=\"" + (xCur + fontSize) + "\" " +
                                        "y=\"" + (yMarginTop - fontSize) + "\">")
                                .append(week)
                                .append("</text>");
                        weekPrev = week;
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
                    sbSignatureTitle.append("время: " + sdf3.format(metricsList.get(i).getTime()) + "; " +
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
        yStart = yMax + fontSize;
        return getSvg();
    }


    /**
     * Таблица
     * @param jsonArrayDataString список метрик
     * @param title         название
     * @return
     */
    public String addTable(
            String jsonArrayDataString,
            String title
    ) throws Exception {
        JSONArray jsonArrayData = new JSONArray(jsonArrayDataString);
        return addTable(jsonArrayData, title);
    }
    /**
     * Таблица
     * @param jsonArrayData список метрик
     * @param title         название
     * @return
     */
    public String addTable(
            JSONArray jsonArrayData,
            String title
    ) throws Exception {
        return addTable(
                jsonArrayData,
                title,
                null,
                null);
    }

    /**
     * Таблица
     * @param jsonArrayDataString список метрик
     * @param title         название графика
     * @param yMinNorm      минимальное допустимое значение Y
     * @param yMaxNorm      максимальное допустимое значение Y
     * @return
     */
    public String addTable(
            String jsonArrayDataString,
            String title,
            Double yMinNorm,
            Double yMaxNorm
    ) throws Exception{
        JSONArray jsonArrayData = new JSONArray(jsonArrayDataString);
        return addTable(jsonArrayData,
                title,
                yMinNorm,
                yMaxNorm);
    }
    /**
     * Таблица
     * @param jsonArrayData список метрик
     * @param title         название графика
     * @param yMinNorm      минимальное допустимое значение Y
     * @param yMaxNorm      максимальное допустимое значение Y
     * @return
     */
    public String addTable(
            JSONArray jsonArrayData,
            String title,
            Double yMinNorm,
            Double yMaxNorm
    ) throws Exception{
        if (startPeriod > stopPeriod) {
            throw new Exception("Не верно задан период для отчета");
        }
        List<DateTimeValue> metricsList = jsonToList(jsonArrayData);
        int metricCount = metricsList.get(0).getValueSize();

        int ySizeTable = fontSize * metricCount;
        int yMax = yStart + ySizeTable;

        long xValueMin = startPeriod;
        long xValueMax = stopPeriod;

        sbGraphResult.append("<!--" + title + "-->\n" +
                "<!-- Область таблицы -->\n" +
                "\t<rect " +
                "stroke=\"#0f0f0f\" " +
                "fill=\"" + background + "\" " +
                "x=\"" + xStart + "\" " +
                "y=\"" + yStart + "\" " +
                "width=\"" + xSize + "\" " +
                "height=\"" + ySizeTable + "\"/>\n");

        double yCur = yStart;
        sbGraphResult.append("\t<polyline " +
                "fill=\"none\" " +
                "stroke=\"#000000\" " +
                "stroke-width=\"" + (lineSize * 2) + "\" " +
                "points=\"" + xStart + "," + yCur + "  " + xMax + "," + yCur + "\"/>\n");

        for (int i = 0; i < metricCount; i++) {
            yCur = yCur + fontSize;
            sbGraphResult.append("\t<polyline " +
                    "fill=\"none\" " +
                    "stroke=\"#000000\" " +
                    "stroke-width=\"" + lineSize * 2 + "\" " +
                    "points=\"" + xStart + "," + yCur + "  " + xMax + "," + yCur + "\"/>\n");
            sbGraphResult.append("\t<text font-size=\"")
                    .append(fontSize)
                    .append("\" " +
                            "font-family=\"Areal\" " +
                            "x=1 " +
                            "y=\"" + (yStart + fontSize * (i + 1) - fontSize / 5) + "\">")
                    .append(title)
                    .append("</text>\n");
        }

        // ось X
        // заполняем таблицу
        sbGraphResult.append("<!-- Ось X -->\n");
        long xValueRange = xValueMax - xValueMin;
        double xRatio = xSize / (xValueRange * 1.00);
        double xRatioValue = xValueRange / xScale;
        double xStep = xSize / xScale;
        double xCur = xStart;
        long xValue = xValueMin;
        long xValuePrev = xValue;
        long xValueMem = 0;
        int weekPrev = 0;
        if (xStep > 0) {
            while (xValue <= xValueMax) {
                if (xValue != xValuePrev) {
                    for (int i = 0; i < metricCount; i++) {
                        double val = 0;
                        for (int v = 0; v < metricsList.size(); v++) {
                            if (metricsList.get(v).getTime() > xValuePrev && metricsList.get(v).getTime() <= xValue) {
                                val = metricsList.get(v).getDoubleValue(i);
                            }
                        }
                        if (val > 0) {
                            sbGraphResult.append("\t<text font-size=\"")
                                    .append(fontSize)
                                    .append("\" " +
                                            "font-family=\"Areal\" " +
                                            "font-weight=\"bold\" " +
                                            "x=\"")
                                    .append(xCur - fontSize * 2)
                                    .append("\" ")
                                    .append("y=\"")
                                    .append((yStart + fontSize * (i + 1) - fontSize / 5))
                                    .append("\"");

                            if ( (yMinNorm != null && val < yMinNorm) ||
                                 (yMaxNorm != null && val > yMaxNorm) ){
                                sbGraphResult.append(" fill=\"#ff0000\"");
                            }
                            sbGraphResult.append(">")
                                        .append(decimalFormat.format(val))
                                        .append("</text>\n");
                        }
                    }
                    xValuePrev = xValue;
                }
                if (xCur > xStart) {
                    sbGraphResult.append("\t<polyline " +
                            "fill=\"none\" " +
                            "stroke=\"#a0a0a0\" " +
                            "stroke-width=\"" + (lineSize * 2) + "\" " +
                            "points=\"" + xCur + ", " + yStart + " " + xCur + "," + yMax + "\"/>\n");
                }
                if (graphNum == 0) {
                    sbGraphResult.append("\t<text font-size=\"")
                            .append((int) (fontSizeX * 1.35))
                            .append("\" " +
                                    "font-family=\"Areal\" " +
                                    "letter-spacing=\"0\" " + // 0.5
                                    "writing-mode=\"tb\" " +
                                    "x=\"" + xCur + "\" " +
                                    "y=\"" + yText + "\">");
                    if (!sdf6.format(xValueMem).equals(sdf6.format(xValue))) { // полную дату выводим 1 раз
                        sbGraphResult.append(sdf1.format(xValue)).append("</text>\n");
                        xValueMem = xValue;
                    } else {
                        sbGraphResult.append(sdf5.format(xValue)).append("</text>\n");
                    }
                    int week = getNumWeek(xValue);
                    if (week > weekPrev) {
                        sbGraphResult.append("\t<text font-size=\"")
                                .append((int) (fontSizeX * 1.35))
                                .append("\" " +
                                        "font-family=\"Areal\" font-weight=\"bold\"" +
                                        "x=\"" + (xCur + fontSize) + "\" " +
                                        "y=\"" + (yMarginTop - fontSize) + "\">")
                                .append(week)
                                .append("</text>");
                        weekPrev = week;
                    }
                }
                xCur = xCur + xStep;
                xValue = xValue + (long) xRatioValue;
            }
        }

        graphNum++;
        yStart = yMax + fontSize;
        return getSvg();
    }

    /**
     * Получить все графики
     * @return
     */
    public String getSvg() {
        return //"<svg width=\"" + xMax + "\" height=\"" + yStart + "\" " +
                "<svg viewBox=\"0 0 " + xMax + " " + yStart + "\" " +
                "xmlns=\"http://www.w3.org/2000/svg\" " +
                "xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                sbGraphResult.toString() +
                "</svg>";
    }

    /**
     * Получить графики в формате html
     * @return
     */
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
//                "<svg viewBox=\"0 0 " + xMax + " " + yStart + "\" class=\"chart\">\n" +
                "<svg viewBox=\"0 0 " + xMax + " " + yStart + "\">\n" +
                sbGraphResult.toString() +
                "\t</body>\n" +
                "</html>";
    }

}
