package de.domjos.ideaMantis.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import de.domjos.ideaMantis.model.CustomField;
import de.domjos.ideaMantis.model.CustomFieldResult;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Map;

public class CustomFieldDialog extends DialogWrapper {
    private MantisSoapAPI api;
    private Project project;
    private String state;
    private JPanel panel;
    private java.util.List<CustomField> customFieldList = null;
    private java.util.List<CustomFieldResult> resultList = null;
    private Map<CustomField, String> customFields;

    CustomFieldDialog(@Nullable Project project, String state, Map<CustomField, String> customFields) {
        super(project);
        this.resultList = new LinkedList<>();
        this.project = project;
        this.state = state;
        this.customFields = customFields;
        try {
            this.api = new MantisSoapAPI(ConnectionSettings.getInstance(project));
            this.setTitle("Fill in Custom Fields");
            this.setOKButtonText("Finish");
            this.init();
            if(this.getButton(this.getOKAction())!=null) {
                this.getButton(this.getOKAction()).addActionListener((event) -> {
                    for(int i = 0; i<=this.panel.getComponentCount()-1; i++) {
                        if(this.panel.getComponent(i) instanceof JPanel) {
                             CustomFieldResult result = new CustomFieldResult();
                             JPanel child = (JPanel) this.panel.getComponent(i);
                             String name = ((JLabel) child.getComponent(0)).getText();
                             if(name.endsWith("*")) {
                                 name = name.replace("*", "").trim();
                             }
                             for(CustomField customField : this.customFieldList) {
                                 if(name.equals(customField.getName())) {
                                     result.setField(customField);
                                 }
                             }

                             for(int j = 1; j<=child.getComponentCount()-1; j++) {
                                 if(child.getComponent(j) instanceof JTextField) {
                                     String value = ((JTextField) child.getComponent(j)).getText();
                                     result.addResult(value);
                                 }
                                 if(child.getComponent(j) instanceof JTextArea) {
                                     String value = ((JTextArea) child.getComponent(j)).getText();
                                     result.addResult(value);
                                 }
                                 if(child.getComponent(j) instanceof JList) {
                                     JList<String> list = (JList<String>) child.getComponent(j);
                                     if(list.getSelectionMode()== ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
                                         for(String selection : list.getSelectedValuesList()) {
                                             result.addResult(selection);
                                         }
                                     } else {
                                         result.addResult(list.getSelectedValue());
                                     }
                                 }
                                 if(child.getComponent(j) instanceof JCheckBox) {
                                     JCheckBox checkBox = (JCheckBox) child.getComponent(j);
                                     if(checkBox.isSelected()) {
                                         result.addResult(checkBox.getText());
                                     }
                                 }
                                 if(child.getComponent(j) instanceof JRadioButton) {
                                     JRadioButton radioButton = (JRadioButton) child.getComponent(j);
                                     if(radioButton.isSelected()) {
                                         result.addResult(radioButton.getText());
                                     }
                                 }
                             }
                             this.resultList.add(result);
                        }
                    }
                });
            }


        }catch (Exception ex) {
            Helper.printNotification("Exception", ex.toString(), NotificationType.ERROR);
        }
    }

    java.util.List<CustomFieldResult> getResults() {
        return this.resultList;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        this.panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 2.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        this.customFieldList = api.getCustomFields(ConnectionSettings.getInstance(project).getProjectID());
        for(CustomField field : customFieldList) {
            String entry = "";
            for(Map.Entry<CustomField, String> valueEntry : this.customFields.entrySet()) {
                if(valueEntry.getKey().getName().equals(field.getName())) {
                    entry = valueEntry.getValue();
                }
            }
            JPanel panel = field.buildFieldPanel(this.state, entry);
            if(panel!=null) {
                this.panel.add(panel, constraints);
            }
        }

        return this.panel;
    }
}
