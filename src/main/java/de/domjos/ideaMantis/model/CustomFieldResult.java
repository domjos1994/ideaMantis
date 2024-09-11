package de.domjos.ideaMantis.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dominic Joas on 19.05.2017.
 */
public class CustomFieldResult {
    private CustomField field;
    private final List<String> result;

    public CustomFieldResult() {
        this.field = null;
        this.result = new LinkedList<>();
    }

    public CustomField getField() {
        return field;
    }

    public void setField(CustomField field) {
        this.field = field;
    }

    public List<String> getResult() {
        return result;
    }

    public void addResult(String result) {
        this.result.add(result);
    }
}
