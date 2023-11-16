package de.sranko_informatik.si_excel_to_json_jar_core;

public class ExcelParserException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String msg;

    public ExcelParserException(String msg) {
        this.msg = msg;

    }

    public String getMessage() {
        return msg;
    }
}
