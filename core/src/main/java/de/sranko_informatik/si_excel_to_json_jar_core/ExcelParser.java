package de.sranko_informatik.si_excel_to_json_jar_core;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExcelParser {

    public static JSONObject getJSONObject(MultipartFile file, ActionDataSheet actionData) throws IOException, IllegalStateException, ExcelParserException {

        Logger logger = LoggerFactory.getLogger(ExcelParser.class);

        //Create an object of FileInputStream class to read excel file
        InputStream inputStream = file.getInputStream();
        logger.debug(String.format("Datei %s (%s bytes) wird bearbeitet., ", file.getOriginalFilename(), file.getSize()));

        Workbook workbook = null;

        //Find the file extension by splitting file name in substring  and getting only extension name

        String fileExtensionName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));

        //Check condition if the file is xlsx file
        if(fileExtensionName.equals(".xlsx")){

            //If it is xlsx file then create object of XSSFWorkbook class
            workbook = new XSSFWorkbook(inputStream);
            logger.debug(String.format("XSSFWorkbook erstellt, weil %s Extension ermittelt.", fileExtensionName));

        }

        //Check condition if the file is xls file
        else if(fileExtensionName.equals(".xls")){

            //If it is xls file then create object of HSSFWorkbook class
            workbook = new HSSFWorkbook(inputStream);
            logger.debug(String.format("HSSFWorkbook erstellt, weil %s Extension ermittelt.", fileExtensionName));

        }

        logger.debug(String.format("%s Sheets werden bearbeitet.", workbook.getNumberOfSheets()));

        JSONObject workbookJSON = new JSONObject();
        workbookJSON.put("name", file.getOriginalFilename());
        JSONObject sheetJSON = null;
        Sheet sheet = null;

        if (!Objects.isNull(actionData)) {

            sheet = workbook.getSheet(actionData.getSheet());
            if (sheet == null){
                logger.debug(String.format("Sheet %s in Excel nicht gefunden.", actionData.getSheet()));
                throw new ExcelParserException(String.format("Sheet %s in Excel nicht gefunden.", actionData.getSheet()));
            }

            workbookJSON = getSheetAsJSON(workbookJSON, sheet, actionData);

        } else {

            //Create a root json object
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {

                //Read sheet inside the workbook by index
                sheet = workbook.getSheetAt(s);

                workbookJSON = getSheetAsJSON(workbookJSON, sheet, new ActionDataSheet());

            }

        }

        logger.debug(String.format("Workbook %s ist fertig", file.getOriginalFilename()));

        return workbookJSON;
    }

    private static JSONObject getSheetAsJSON(JSONObject workbookJSON, Sheet sheet, ActionDataSheet actionDataSheet) throws IllegalStateException{

        Logger logger = LoggerFactory.getLogger(ExcelParser.class);

        logger.debug(String.format("Sheet: %s wird von Zeile: %s und Splate: %s bearbeitet.", sheet.getSheetName(), actionDataSheet.getStart().getRow(), actionDataSheet.getStart().getColumn()));
        if (actionDataSheet.getFieldsToUpload() != null){
            logger.debug(String.format("Nur folgende Spalten werden importiert: %s.", Arrays.toString(actionDataSheet.getFieldsToUpload())));
        }

        JSONArray sheetList = new JSONArray();

        //Find number of rows in excel file
        int rowCount = 0;
        int tableDataRow = 0;
        if (actionDataSheet.getStart().getRow() == 0) {
            rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();
        } else {
            rowCount = (sheet.getLastRowNum() + 1) - actionDataSheet.getStart().getRow();
        }

        logger.debug(String.format("%s Zeilen werden bearbeitet", rowCount));

        ArrayList<ExcelColumn> headerList = null;

        //First row in Excel ist always table head
        int headerRow = 0;
        if (actionDataSheet.getStart().getRow() != 0) {
            headerRow = actionDataSheet.getStart().getRow() - 1;
        }

        headerList = getTableColumns(sheet.getRow(headerRow), actionDataSheet.getStart().getColumn(), actionDataSheet.getFieldsToUpload());
        logger.debug(String.format("Tabelenkopf gefunden: %s.", headerList.toString()));

        if (actionDataSheet.getFieldsToUpload() != null){
            if (!existFieldToUpload(headerList)){
                logger.debug(String.format("Spalten %s nicht in Excel gefunden.", Arrays.toString(actionDataSheet.getFieldsToUpload())));
                throw new ExcelParserException(String.format("Spalten %s nicht in Excel gefunden.", Arrays.toString(actionDataSheet.getFieldsToUpload())));
            }
        }

        //Create a loop over all the rows of excel file to read it
        tableDataRow = headerRow + 1;
        for (int i = 0; i < rowCount; i++) {

            Row row = sheet.getRow(tableDataRow + i);

            if (isRowEmpty(row)) {
                continue;
            }

            List<ExcelColumn> rowColumnsList = null;
            try {
                rowColumnsList = getRowColumns(row, headerList);
            } catch (Exception e) {
                logger.debug(String.format("Error found: %s", e.toString()));
            }
            JSONObject jsonRow = new JSONObject();
            int index = 0;
            for (ExcelColumn column : rowColumnsList) {

                // Prüfung, ob Key-Spalte Daten beinhaltet. Gibt es da keine Daten, dann die nächste Zeile
                if (isKeyColumnEmpty(column, actionDataSheet.getKeyFields())) {
                    logger.debug(String.format("Zeile %s ignoriert, da die Key-Spalte: %S keinen Wert hat.", i, actionDataSheet.getKeyFields().toString()));
                    break;
                }

                if (headerList.get(index).isUpload()){
                    jsonRow.put(headerList.get(index).getName(), column.getValue());
                }
                index += 1;
            }
            logger.debug(String.format("Zeile %s erstellt: %s", i, jsonRow.toString()));
            if (!jsonRow.isEmpty()) {
                sheetList.put(jsonRow);
            }
        }

        JSONObject sheetJSON = new JSONObject();
        sheetJSON.put("name", sheet.getSheetName());
        sheetJSON.put("data", sheetList);
        workbookJSON.append("sheets", sheetJSON);

        logger.debug(String.format("Sheet %s ist fertig. %s", sheet.getSheetName(), sheetList));

        return  workbookJSON;
    }

    public static boolean isTableHead (Workbook book, Cell cell) {
        if (cell == null) {
            return false;
        }
        CellStyle style = cell.getCellStyle();
        Font font = book.getFontAt(style.getFontIndex());
        if (font.getBold()) {
            return true;
        }
        return false;
    }

    public static ArrayList<ExcelColumn> getTableColumns (Row row, int column, String[] fieldsToUpload) throws IllegalStateException{

        ArrayList<ExcelColumn> output = new ArrayList<>();
        String spaltenName = new String();

        //Create a loop to print cell values in a row
        int startCol = column;
        int c = 0;
        for (c = startCol; c < row.getLastCellNum(); c++) {

            //Print Excel data in console
            ExcelColumn excelColumn = new ExcelColumn();
            Cell cell = row.getCell(c);
            CellType cellType = cell.getCellType();
            if (cellType == CellType.FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            switch (cellType) {
                case BOOLEAN:
                    spaltenName = String.valueOf(cell.getBooleanCellValue());
                    excelColumn.setDataType(ColumnType.BOOLEAN);
                case NUMERIC:
                    spaltenName = String.valueOf(cell.getNumericCellValue());
                    excelColumn.setDataType(ColumnType.NUMBER);
                case STRING:
                    spaltenName = cell.getRichStringCellValue().getString();
                    excelColumn.setDataType(ColumnType.VARCHAR);
            }
            excelColumn.setName(spaltenName);
            excelColumn.setRowNr(row.getRowNum());
            excelColumn.setColNr(c);
            if (!istStringImArray(fieldsToUpload, spaltenName)) {
               excelColumn.setUpload(false);
            } else {
                excelColumn.setUpload(true);
            };
            output.add(excelColumn);
        }

        return output;
    }

    public static List<ExcelColumn> getRowColumns(Row row, ArrayList<ExcelColumn> headerList) {

        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));

        List<ExcelColumn> output = new ArrayList<>();

        //Create a loop to print cell values in a row
        for (int c = 0; c < headerList.size(); c++) {

            //Print Excel data in console
            Cell cell = row.getCell(c);
            if (cell == null) {
                output.add(new ExcelColumn(ColumnType.CHAR, headerList.get(c).getName(), ""));
                continue;
            }
            CellType cellType = cell.getCellType();
            if (cellType == CellType.FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            switch (cellType) {
                case BOOLEAN:
                    output.add(new ExcelColumn(ColumnType.CHAR, headerList.get(c).getName(), String.valueOf(cell.getBooleanCellValue()), row.getRowNum(), c,headerList.get(c).isUpload()));
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        output.add(new ExcelColumn(ColumnType.NUMBER, headerList.get(c).getName(), sdf.format(cell.getDateCellValue()), row.getRowNum(), c,headerList.get(c).isUpload()));
                    } else {
                        output.add(new ExcelColumn(ColumnType.NUMBER, headerList.get(c).getName(), NumberToTextConverter.toText(cell.getNumericCellValue()), row.getRowNum(), c,headerList.get(c).isUpload()));
                    }

                    break;
                case STRING:
                    output.add(new ExcelColumn(ColumnType.VARCHAR, headerList.get(c).getName(), cell.getRichStringCellValue().getString(), row.getRowNum(), c,headerList.get(c).isUpload()));
                    break;
                case BLANK:
                case _NONE:
                case ERROR:
                    output.add(new ExcelColumn(ColumnType.VARCHAR, headerList.get(c).getName(), "", row.getRowNum(), c,headerList.get(c).isUpload()));
                    break;
            }
        }

        return output;

    }

    private static boolean isRowEmpty(Row row) {
        boolean isEmpty = true;
        DataFormatter dataFormatter = new DataFormatter();

        if (row != null) {
            for (Cell cell : row) {
                if (dataFormatter.formatCellValue(cell).trim().length() > 0) {
                    isEmpty = false;
                    break;
                }
            }
        }

        return isEmpty;
    }

    private static boolean isKeyColumnEmpty(ExcelColumn column, String[] keyFields ) {
        boolean isEmpty = true;

        // Falls keine Key-Felder definiert sind, die Prüfung ignorieren
        if ( keyFields == null) {
            return false;
        }
        if ( column == null) {
            return true;
        }

        // Prüfung, ob Key-Spalte Werte beinhaltet oder nicht.
        if ( istStringImArray(keyFields, column.getName()) ) {
            if ( column.getValue() != "" ) {
                return false;
            }
        } else {
            // Falls es sich nicht um KeySpalte handelt, Prüfung ignorieren
            return false;
        }

        return isEmpty;
    }

    // Methode zur Überprüfung, ob der String im Array ist
    public static boolean istStringImArray(String[] array, String zuSuchenderString) {
        if ( array == null) {
            return true;
        }
        if (array.length == 0 ) {
            return true;
        }

        for (String element : array) {
            if (element.equals(zuSuchenderString)) {
                return true; // Der String wurde im Array gefunden
            }
        }
        return false; // Der String wurde im Array nicht gefunden
    }

    public static boolean existFieldToUpload(ArrayList<ExcelColumn> headerList) {
        for (ExcelColumn excelColumn: headerList) {
            if (excelColumn.isUpload()){
                return true;
            }
        }
        return false;
    }
}