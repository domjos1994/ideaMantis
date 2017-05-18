package de.domjos.ideaMantis.model;

import com.intellij.ui.components.*;

import javax.swing.*;
import java.awt.*;

public class CustomField {
    private int id, typeId, min, max, readAccessID, writeAccessID;
    private String name, typeName, possibleValues, defaultValue, readAccessName, writeAccessName;
    private boolean displayReport, displayUpdate, displayResolved, displayClosed, requireReport,
                requireUpdate, requireResolved, requireClosed;

    public CustomField() {
        this.id = 0;
        this.typeId = 0;
        this.min = 0;
        this.max = 0;
        this.readAccessID = 0;
        this.writeAccessID = 0;
        this.name = "";
        this.typeName = "";
        this.possibleValues = "";
        this.defaultValue = "";
        this.readAccessName = "";
        this.writeAccessName = "";
        this.displayReport = false;
        this.displayUpdate = false;
        this.displayResolved = false;
        this.displayClosed = false;
        this.requireReport = false;
        this.requireUpdate = false;
        this.requireReport = false;
        this.requireClosed = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getReadAccessID() {
        return readAccessID;
    }

    public void setReadAccessID(int readAccessID) {
        this.readAccessID = readAccessID;
    }

    public int getWriteAccessID() {
        return writeAccessID;
    }

    public void setWriteAccessID(int writeAccessID) {
        this.writeAccessID = writeAccessID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(String possibleValues) {
        this.possibleValues = possibleValues;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getReadAccessName() {
        return readAccessName;
    }

    public void setReadAccessName(String readAccessName) {
        this.readAccessName = readAccessName;
    }

    public String getWriteAccessName() {
        return writeAccessName;
    }

    public void setWriteAccessName(String writeAccessName) {
        this.writeAccessName = writeAccessName;
    }

    public boolean isDisplayReport() {
        return displayReport;
    }

    public void setDisplayReport(boolean displayReport) {
        this.displayReport = displayReport;
    }

    public boolean isDisplayUpdate() {
        return displayUpdate;
    }

    public void setDisplayUpdate(boolean displayUpdate) {
        this.displayUpdate = displayUpdate;
    }

    public boolean isDisplayResolved() {
        return displayResolved;
    }

    public void setDisplayResolved(boolean displayResolved) {
        this.displayResolved = displayResolved;
    }

    public boolean isDisplayClosed() {
        return displayClosed;
    }

    public void setDisplayClosed(boolean displayClosed) {
        this.displayClosed = displayClosed;
    }

    public boolean isRequireReport() {
        return requireReport;
    }

    public void setRequireReport(boolean requireReport) {
        this.requireReport = requireReport;
    }

    public boolean isRequireUpdate() {
        return requireUpdate;
    }

    public void setRequireUpdate(boolean requireUpdate) {
        this.requireUpdate = requireUpdate;
    }

    public boolean isRequireResolved() {
        return requireResolved;
    }

    public void setRequireResolved(boolean requireResolved) {
        this.requireResolved = requireResolved;
    }

    public boolean isRequireClosed() {
        return requireClosed;
    }

    public void setRequireClosed(boolean requireClosed) {
        this.requireClosed = requireClosed;
    }

    public JPanel buildFieldPanel() {
        JBList<String> list = new JBList<>();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JPanel panel = new JPanel();
        BorderLayout layout = new BorderLayout();
        panel.setLayout(layout);
        JBLabel label = new JBLabel(this.getName());
        panel.add(label);
        switch (this.getTypeId()) {
            case 0:
            case 1:
            case 2:
            case 4:
            case 8:
                JBTextField field = new JBTextField();
                if(!this.getDefaultValue().isEmpty()) {
                    field.setText(this.getDefaultValue());
                }
                panel.add(field);
                break;
            case 3:
                if(!this.getPossibleValues().contains("|")) {
                    listModel.addElement(this.getPossibleValues());
                } else {
                    for(String item : this.getPossibleValues().split("\\|")) {
                        listModel.addElement(item);
                    }
                }
                list.setModel(listModel);
                if(!this.getDefaultValue().isEmpty()) {
                    list.setSelectedIndex(listModel.indexOf(this.getDefaultValue()));
                }
                panel.add(list);
                break;
            case 5:
                if(!this.getPossibleValues().contains("|")) {
                    JBCheckBox checkBox = new JBCheckBox(this.getPossibleValues());
                    if(!this.getDefaultValue().isEmpty()) {
                        if(this.getPossibleValues().equals(this.getDefaultValue())) {
                            checkBox.setSelected(true);
                        }
                    }
                    panel.add(checkBox);
                } else {
                    for(String item : this.getPossibleValues().split("\\|")) {
                        JBCheckBox checkBox = new JBCheckBox(item);
                        if(!this.getDefaultValue().isEmpty()) {
                            if(item.equals(this.getDefaultValue())) {
                                checkBox.setSelected(true);
                            }
                        }
                        panel.add(checkBox);
                    }
                }
                break;
            case 6:
                if(!this.getPossibleValues().contains("|")) {
                    listModel.addElement(this.getPossibleValues());
                } else {
                    for(String item : this.getPossibleValues().split("\\|")) {
                        listModel.addElement(item);
                    }
                }
                list.setModel(listModel);
                if(!this.getDefaultValue().isEmpty()) {
                    list.setSelectedIndex(listModel.indexOf(this.getDefaultValue()));
                }
                panel.add(list);
                break;
            case 7:
                if(!this.getPossibleValues().contains("|")) {
                    listModel.addElement(this.getPossibleValues());
                } else {
                    for(String item : this.getPossibleValues().split("\\|")) {
                        listModel.addElement(item);
                    }
                }
                list.setModel(listModel);
                if(!this.getDefaultValue().isEmpty()) {
                    list.setSelectedIndex(listModel.indexOf(this.getDefaultValue()));
                }
                list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                panel.add(list);
                break;
            case 9:
                if(!this.getPossibleValues().contains("|")) {
                    JBRadioButton radioButton = new JBRadioButton(this.getPossibleValues());
                    if(!this.getDefaultValue().isEmpty()) {
                        if(this.getPossibleValues().equals(this.getDefaultValue())) {
                            radioButton.setSelected(true);
                        }
                    }
                    panel.add(radioButton);
                } else {
                    for(String item : this.getPossibleValues().split("\\|")) {
                        JBRadioButton radioButton = new JBRadioButton(item);
                        if(!this.getDefaultValue().isEmpty()) {
                            if(item.equals(this.getDefaultValue())) {
                                radioButton.setSelected(true);
                            }
                        }
                        panel.add(radioButton);
                    }
                }
                break;
            case 10:
                JTextArea area = new JTextArea();
                if(!this.getDefaultValue().isEmpty()) {
                    area.setText(this.getDefaultValue());
                }
                panel.add(area);
                break;
            default:
                System.out.println("Not Supported!");
        }
        return panel;
    }
}
