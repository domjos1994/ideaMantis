/*
 * Copyright (c) 2024 DOMINIC JOAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.ideaMantis.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import de.domjos.ideaMantis.lang.Lang;
import de.domjos.ideaMantis.model.CustomField;
import de.domjos.ideaMantis.model.CustomFieldResult;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class CustomFieldDialog extends DialogWrapper {
    private MantisSoapAPI api;
    private final String state;
    private JPanel panel;
    private java.util.List<CustomField> customFieldList = null;
    private final java.util.List<CustomFieldResult> resultList;
    private final Map<CustomField, String> customFields;

    CustomFieldDialog(@Nullable Project project, String state, Map<CustomField, String> customFields) {
        super(project);
        this.resultList = new LinkedList<>();
        this.state = state;
        this.customFields = customFields;
        try {
            assert project != null;
            this.api = new MantisSoapAPI(ConnectionSettings.getInstance(project));
            this.setTitle(Lang.DIALOG_CUSTOM_HEADER);
            this.setOKButtonText(Lang.DIALOG_CUSTOM_OK);
            this.init();
            if(this.getButton(this.getOKAction())!=null) {
                Objects.requireNonNull(this.getButton(this.getOKAction())).addActionListener((event) -> {
                    for(int i = 0; i<=this.panel.getComponentCount()-1; i++) {
                        if(this.panel.getComponent(i) instanceof JPanel child) {
                            CustomFieldResult result = new CustomFieldResult();
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
                                    if(result.getField().getTypeId()==8) {
                                        try {
                                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Lang.DATE_ONLY_FORMAT_1);
                                            LocalDate localDate = LocalDate.parse(value, dtf);
                                            result.addResult(String.valueOf(Timestamp.valueOf(localDate.atTime(0,0)).getTime()/1000));
                                        } catch (Exception ex) {
                                            try {
                                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Lang.DATE_ONLY_FORMAT_2);
                                                LocalDate localDate = LocalDate.parse(value, dtf);
                                                result.addResult(String.valueOf(Timestamp.valueOf(localDate.atTime(0,0)).getTime()/1000));
                                            } catch (Exception ex2) {
                                                result.addResult(String.valueOf(Timestamp.valueOf(LocalDateTime.now()).getTime()/1000));
                                            }
                                        }
                                        System.out.println(result.getResult().get(result.getResult().size()-1));
                                    }
                                    result.addResult(value);
                                }
                                if(child.getComponent(j) instanceof JTextArea) {
                                    String value = ((JTextArea) child.getComponent(j)).getText();
                                    result.addResult(value);
                                }
                                if(child.getComponent(j) instanceof JList<?> list) {
                                    if(list.getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
                                        for(Object selection : list.getSelectedValuesList()) {
                                            result.addResult((String) selection);
                                        }
                                    } else {
                                        result.addResult(list.getSelectedValue().toString());
                                    }
                                }
                                if(child.getComponent(j) instanceof JPanel) {
                                    for(Component component : ((JPanel)child.getComponent(j)).getComponents()) {
                                        if(component instanceof JCheckBox checkBox) {
                                            if(checkBox.isSelected()) {
                                                result.addResult(checkBox.getText());
                                            }
                                        }
                                        if(component instanceof JRadioButton radioButton) {
                                            if(radioButton.isSelected()) {
                                                result.addResult(radioButton.getText());
                                            }
                                        }
                                    }
                                }
                            }
                            this.resultList.add(result);
                        }
                    }
                });
            }
        } catch (Exception ex) {
            Helper.printException(ex);
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
        this.customFieldList = api.getCustomFields(false);
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
