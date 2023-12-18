package de.sranko_informatik.si_excel_to_json_jar_core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionDataSheet {
    private String sheet;
    private ActionDataSheetStart start;
    private String[] fieldsToUpload;
    private String[] keyFields;

    public ActionDataSheet() {
        this.sheet = null;
        this.start = new ActionDataSheetStart(0, 0);
        this.fieldsToUpload = null;
        this.keyFields = null;
    }

    public ActionDataSheet(String sheet, ActionDataSheetStart start, String[] fieldsToUpload, String[] keyFields) {
        this.sheet = sheet;
        this.start = start;
        this.fieldsToUpload = fieldsToUpload;
        this.keyFields = keyFields;
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

    public String[] getFieldsToUpload() {
        return fieldsToUpload;
    }

    public void setFieldsToUpload(String[] fieldsToUpload) {
        this.fieldsToUpload = fieldsToUpload;
    }

    public String[] getKeyFields() {
        return keyFields;
    }

    public void setKeyFields(String[] keyFields) {
        this.keyFields = keyFields;
    }

    @Override
    public String toString() {
        return "ActionDataSheet{" +
                "sheetName='" + sheet + '\'' +
                ", start=" + start +
                '}';
    }
}
