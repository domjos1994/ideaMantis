package de.domjos.ideaMantis.model;

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.*;
import de.domjos.ideaMantis.utils.PanelCreator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Arrays;

@SuppressWarnings("unused")
public class CustomField {
    private int id, typeId, min, max, readAccessID, writeAccessID;
    private String name, typeName, possibleValues, defaultValue, readAccessName, writeAccessName;
    private boolean displayReport, displayUpdate, displayResolved, displayClosed, requireReport,
                requireUpdate, requireResolved, requireClosed;

    public CustomField() {
        this.id = 0;
        this.typeId = 0;
        this.readAccessID = 0;
        this.writeAccessID = 0;
        this.name = "";
        this.possibleValues = "";
        this.defaultValue = "";
        this.displayReport = false;
        this.displayUpdate = false;
        this.displayResolved = false;
        this.displayClosed = false;
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

    public JPanel buildFieldPanel(String state, String value) {
        GridBagConstraints labelConstraint = PanelCreator.getLabelConstraint();
        GridBagConstraints txtConstraint = PanelCreator.getTxtConstraint();
        JBList<String> list = new JBList<>();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JPanel panel = new JPanel(new GridBagLayout());
        JBLabel label = new JBLabel(this.getName());

        switch (state) {
            case "report":
                if(!this.isDisplayReport()) {
                    return null;
                }
                if(this.isRequireReport()) {
                    label.setName(label.getName() + "*");
                }
                break;
            case "update":
                if(!this.isDisplayUpdate()) {
                    return null;
                }
                if(this.isRequireUpdate()) {
                    label.setName(label.getName() + "*");
                }
                break;
            case "resolved":
                if(!this.isDisplayResolved()) {
                    return null;
                }
                if(this.isRequireResolved()) {
                    label.setName(label.getName() + "*");
                }
                break;
            case "closed":
                if(!this.isDisplayClosed()) {
                    return null;
                }
                if(this.isRequireClosed()) {
                    label.setName(label.getName() + "*");
                }
                break;
        }

        panel.add(label, labelConstraint);
        switch (this.getTypeId()) {
            case 0:
            case 1:
            case 2:
            case 4:
            case 8:
                JBTextField field = new JBTextField();
                if(value.isEmpty()) {
                    if(!this.getDefaultValue().isEmpty()) {
                        field.setText(this.getDefaultValue());
                    }
                } else {
                    if(getTypeId()==8) {
                        if(value.contains("|")) {
                            String date = value.split("\\|")[1];
                            field.setText(date);
                        }
                    } else {
                        field.setText(value);
                    }
                }
                field.getDocument().addDocumentListener(new DocumentAdapter() {
                    @Override
                    protected void textChanged(@NotNull DocumentEvent documentEvent) {
                        documentEvent.getDocument().addUndoableEditListener(e -> {
                            if(getTypeId()==1) {
                                if(!field.getText().isEmpty()) {
                                    try {
                                        Integer.parseInt(field.getText());
                                        field.setText(getDefaultValue());
                                    } catch (Exception ignored) {}
                                }
                            }
                            if(getTypeId()==2) {
                                try {
                                    Double.parseDouble(field.getText());
                                } catch (Exception ex) {
                                    field.setText(getDefaultValue());
                                }
                            }
                            if(getTypeId()==8) {
                                java.util.List<String> allowed = Arrays.asList("0","1","2","3","4","5","6","7", "8","9","-",".");
                                char[] chars = field.getText().toCharArray();
                                for(char ch : chars) {
                                    String strDigit = String.valueOf(ch);
                                    if(!allowed.contains(strDigit)) {
                                        field.setText(getDefaultValue());
                                    }
                                }
                            }
                        });
                    }
                });
                panel.add(field, txtConstraint);
                break;
            case 3:
            case 6:
                if(!this.getPossibleValues().contains("|")) {
                    listModel.addElement(this.getPossibleValues());
                } else {
                    for(String item : this.getPossibleValues().split("\\|")) {
                        listModel.addElement(item);
                    }
                }
                list.setModel(listModel);
                if(value.isEmpty()) {
                    if(!this.getDefaultValue().isEmpty()) {
                        list.setSelectedIndex(listModel.indexOf(this.getDefaultValue()));
                    }
                } else {
                    list.setSelectedIndex(listModel.indexOf(value));
                }
                panel.add(list, txtConstraint);
                break;
            case 5:
                if(!this.getPossibleValues().contains("|")) {
                    JBCheckBox checkBox = new JBCheckBox(this.getPossibleValues());
                    this.selectCheckbox(checkBox, value);
                    panel.add(checkBox, txtConstraint);
                } else {
                    JPanel pnlGroup = new JPanel(new GridBagLayout());
                    for(String item : this.getPossibleValues().split("\\|")) {
                        JBCheckBox checkBox = new JBCheckBox(item);
                        this.selectCheckbox(checkBox, value);
                        pnlGroup.add(checkBox, txtConstraint);
                    }
                    panel.add(pnlGroup, txtConstraint);
                }
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
                if(value.isEmpty()) {
                    if(!this.getDefaultValue().isEmpty()) {
                        list.setSelectedIndex(listModel.indexOf(this.getDefaultValue()));
                    }
                } else {
                    if(value.contains("|")) {
                        int[] indices = new int[value.split("\\|").length];
                        int i = 0;
                        for(String selection : value.split("\\|")) {
                            indices[i] = listModel.indexOf(selection);
                        }
                        list.setSelectedIndices(indices);
                    } else {
                        list.setSelectedIndex(listModel.indexOf(value));
                    }
                }
                list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                panel.add(list, txtConstraint);
                break;
            case 9:
                if(!this.getPossibleValues().contains("|")) {
                    JBRadioButton radioButton = getButton(value);
                    panel.add(radioButton, txtConstraint);
                } else {
                    JPanel pnlGroup = new JPanel(new GridBagLayout());
                    ButtonGroup group = new ButtonGroup();
                    for(String item : this.getPossibleValues().split("\\|")) {
                        JBRadioButton radioButton = new JBRadioButton(item);
                        group.add(radioButton);
                        if(value.isEmpty()) {
                            if(!this.getDefaultValue().isEmpty()) {
                                if(item.equals(this.getDefaultValue())) {
                                    radioButton.setSelected(true);
                                }
                            }
                        } else {
                            if(value.contains("|")) {
                                for(String text : value.split("\\|")) {
                                    if(radioButton.getText().equals(text.trim())) {
                                        radioButton.setSelected(true);
                                    }
                                }
                            } else {
                                if(radioButton.getText().equals(value.trim())) {
                                    radioButton.setSelected(true);
                                }
                            }
                        }
                        pnlGroup.add(radioButton, txtConstraint);
                    }
                    panel.add(pnlGroup, txtConstraint);
                }
                break;
            case 10:
                JTextArea area = new JTextArea();
                if(value.isEmpty()) {
                    if(!this.getDefaultValue().isEmpty()) {
                        area.setText(this.getDefaultValue());
                    }
                } else {
                    area.setText(value);
                }
                panel.add(area,txtConstraint);
                break;
            default:
                System.out.println("Not Supported!");
        }
        return panel;
    }

    private @NotNull JBRadioButton getButton(String value) {
        JBRadioButton radioButton = new JBRadioButton(this.getPossibleValues());
        if(value.isEmpty()) {
            if(!this.getDefaultValue().isEmpty()) {
                if(this.getPossibleValues().equals(this.getDefaultValue())) {
                    radioButton.setSelected(true);
                }
            }
        } else {
            if(value.contains("|")) {
                for(String text : value.split("\\|")) {
                    if(radioButton.getText().equals(text.trim())) {
                        radioButton.setSelected(true);
                    }
                }
            }
        }
        return radioButton;
    }

    private void selectCheckbox(JBCheckBox checkBox, String value) {
        if(value.isEmpty()) {
            if(!this.getDefaultValue().isEmpty()) {
                setCheckBox(checkBox, this.getDefaultValue());
            }
        } else {
            if(value.contains("|")) {
                for(String text : value.split("\\|")) {
                    setCheckBox(checkBox, text.trim());
                }
            }
        }
    }

    private void setCheckBox(JBCheckBox checkBox, String value) {
        if(value.equals(checkBox.getText())) {
            checkBox.setSelected(true);
        }
    }
}
