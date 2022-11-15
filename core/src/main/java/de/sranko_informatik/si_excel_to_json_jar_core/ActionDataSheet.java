package de.sranko_informatik.si_excel_to_json_jar_core;

public class ActionDataSheet {
    private String sheet;
    private ActionDataSheetStart start;

    public ActionDataSheet() {
    }

    public ActionDataSheet(String sheet, ActionDataSheetStart start) {
        this.sheet = sheet;
        this.start = start;
    }

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public ActionDataSheetStart getStart() {
        return start;
    }

    public void setStart(ActionDataSheetStart start) {
        this.start = start;
    }

    @Override
    public String toString() {
        return "ActionDataSheet{" +
                "sheetName='" + sheet + '\'' +
                ", start=" + start +
                '}';
    }
}