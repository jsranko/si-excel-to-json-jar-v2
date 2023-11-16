package de.sranko_informatik.si_excel_to_json_jar_core;

public class ExcelColumn {
    private ColumnType dataType;
    private String name;
    private String value;
    private int rowNr;
    private int colNr;
    private boolean upload;


    public ExcelColumn() {

    }

    public ExcelColumn(ColumnType dataType, String name, String value) {
        this.dataType = dataType;
        this.name = name;
        this.value = value;
    }

    public ExcelColumn(ColumnType dataType, String name, String value, int rowNr, int colNr, boolean upload) {
        this.dataType = dataType;
        this.name = name;
        this.value = value;
        this.rowNr = rowNr;
        this.colNr = colNr;
        this.upload = upload;
    }
    public void setDataType(ColumnType dataType) {
        this.dataType = dataType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnType getDataType() {
        return dataType;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getRowNr() {
        return rowNr;
    }

    public void setRowNr(int rowNr) {
        this.rowNr = rowNr;
    }

    public int getColNr() {
        return colNr;
    }

    public void setColNr(int colNr) {
        this.colNr = colNr;
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }
}
