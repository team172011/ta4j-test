/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

public class XlsTestsUtils {

    private static Sheet sheet = null;

    /**
     * Initializes the XlsTestsUtils class to use the first sheet from the workbook named fileName.
     * Must be called prior to all other class methods.
     * @param testClass the class used to locate the file resource
     * @param fileName the name of the file resource
     * @throws IOException in HSSFWorkbook constructor and close() 
     */
    public static void init(Class<?> testClass, String fileName) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook(testClass.getResourceAsStream(fileName));
        sheet = workbook.getSheetAt(0);
        workbook.close();
    }

    /**
     * Gets a TimeSeries from the data section.  Data follows a data section header and appears as
     * date, open, high, low, close, and volume in the first six columns to the end of the file.
     * @return a TimeSeries of the data
     * @throws DataFormatException
     */
    public static TimeSeries getSeries() throws DataFormatException {
        TimeSeries series = new BaseTimeSeries();
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        Duration weekDuration = Duration.ofDays(7);
        List<Row> rows = readDataAfterHeader();
        for (Row row : rows) {
            CellValue[] cellValues = new CellValue[6];
            for (int i = 0; i < 6; i++) {
                if (row.getCell(i) == null) {
                    throw new DataFormatException("empty cell in xls time series data");
                }
                cellValues[i] = evaluator.evaluate(row.getCell(i));
            }
            Date weekEndDate = HSSFDateUtil.getJavaDate(cellValues[0].getNumberValue());
            ZonedDateTime weekEndDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(weekEndDate.getTime()), ZoneId.systemDefault());
            Bar bar = new BaseBar(weekDuration, weekEndDateTime,
                    Decimal.valueOf(cellValues[1].formatAsString()),
                    Decimal.valueOf(cellValues[2].formatAsString()),
                    Decimal.valueOf(cellValues[3].formatAsString()),
                    Decimal.valueOf(cellValues[4].formatAsString()),
                    Decimal.valueOf(cellValues[5].formatAsString()));
            series.addBar(bar);
        }
        return series;
    }

    /**
     * Gets the data rows in the data section from the data section header to the end of the file.
     * Skips rows that start with "//" as data comments.
     * @return List<Row> of data rows
     * @throws DataFormatException if the data section header is not found.
     */
    private static List<Row> readDataAfterHeader() throws DataFormatException {
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        Iterator<Row> iterator = sheet.rowIterator();
        boolean noHeader = true;
        List<Row> rows = new ArrayList<Row>();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            if(row.getCell(0) == null){
                continue;
            }
            if (noHeader == false) {
                if (evaluator.evaluate(row.getCell(0)).formatAsString().compareTo("\"//\"") != 0) {
                    rows.add(row);
                }
            }
            // data section header
            if (noHeader && evaluator.evaluate(row.getCell(0)).formatAsString().contains("Date")) {
                noHeader = false;
            }
        }
        if (noHeader) {
            throw new DataFormatException("\"Date\" header row not found");
        }
        return rows;
    }

    /**
     * Gets the values in a column of the data section calculated from a set of parameters in the parameters section.
     * @param index the column to read the values from
     * @param params the parameters provided to the data source
     * @return List<Decimal> the values from the column
     * @throws DataFormatException in getValues()
     */
    public static <T> List<Decimal> getValues(int index, T... params) throws DataFormatException {
        Decimal[] decimalParams = Arrays.stream(params)
                .map(p -> Decimal.valueOf(p.toString()))
                .toArray(Decimal[]::new);
        return getValues(index, decimalParams);
    }

    /**
     * Helper that takes Decimal parameters.  Writes the parameters to the parameters section
     * then reads the resulting values from the column in the data section.
     * @param index the column to read the values from
     * @param params the parameters provided to the data source
     * @return List<Decimal> the values from the column
     * @throws Exception in writeParams() and readValues()
     */
    private static List<Decimal> getValues(int index, Decimal... params) throws DataFormatException {
        writeParams(params);
        return readValues(index);
    }

    /**
     * Reads the values from a column of the data section.
     * @param index the column to read the values from
     * @return List<Decimal> the values from the column
     * @throws DataFormatException in readDataAfterHeader()
     */
    private static List<Decimal> readValues(int index) throws DataFormatException {
        List<Decimal> values = new ArrayList<>();
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        List<Row> rows = readDataAfterHeader();
        for (Row row : rows) {
            if (row.getCell(index) == null) {
                continue;
            }
            String s = evaluator.evaluate(row.getCell(index)).formatAsString();
            values.add(Decimal.valueOf(s));
        }
        return values;
    }

    /**
     * Writes the parameters into the second column of the parameters section following the parameters section header.
     * There must be at least params.size() rows between the parameters section header and the data section header
     * or part of the data section will be overwritten. 
     * @param params the parameters to write
     * @throws DataFormatException if the parameters section header is not found
     */
    private static void writeParams(Decimal... params) throws DataFormatException {
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            if (row.getCell(0) == null) { 
                continue;
            }
            // parameters section header
            if (evaluator.evaluate(row.getCell(0)).formatAsString().contains("Param")) {
                Arrays.stream(params)
                .mapToDouble(Decimal::doubleValue)
                .forEach(d -> iterator.next().getCell(1).setCellValue(d));
                return;
            }
        }
        throw new DataFormatException("\"Param\" header row not found");
    }

}
