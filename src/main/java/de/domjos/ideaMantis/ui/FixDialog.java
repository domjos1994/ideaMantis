package de.domjos.ideaMantis.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import de.domjos.ideaMantis.model.CustomField;
import de.domjos.ideaMantis.model.CustomFieldResult;
import de.domjos.ideaMantis.model.MantisVersion;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.soap.ObjectRef;
import de.domjos.ideaMantis.utils.Helper;
import de.domjos.ideaMantis.utils.PanelCreator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class FixDialog extends DialogWrapper {
    private JBTextField txtFixed;
    private ComboBox<String> cmbVisibility, cmbStatus, cmbVersion;
    private java.awt.Label lblVersion;
    private MantisSoapAPI api;
    private final Project project;
    private int pid;
    private java.util.List<CustomFieldResult> results;

    public FixDialog(@Nullable Project project, int id, String status) {
        super(project);
        this.project = project;
        this.results = new LinkedList<>();

        try {
            ConnectionSettings connectionSettings = ConnectionSettings.getInstance(project);
            this.pid = connectionSettings.getProjectID();
            this.api = new MantisSoapAPI(connectionSettings);
            this.setTitle("Change Status");
            this.setOKButtonText("Change Status");
            this.init();
            this.changeSelectedStatus(status);


            if(this.getButton(this.getOKAction())!=null) {
                JButton button = this.getButton(this.getOKAction());
                if(button != null) {
                    button.addActionListener((event) -> {
                        try {
                            String summary = this.txtFixed.getText().trim();
                            String visibility = "";
                            if(this.cmbVisibility.getSelectedItem() != null) {
                                visibility = this.cmbVisibility.getSelectedItem().toString().trim();
                            }
                            String state = "";
                            if(this.cmbStatus.getSelectedItem() != null) {
                                state = this.cmbStatus.getSelectedItem().toString().trim();
                            }
                            String version = "";
                            if(this.cmbVersion.getSelectedItem() != null && this.cmbVersion.isVisible()) {
                                version = this.cmbVersion.getSelectedItem().toString().trim();
                            }

                            api.checkInIssue(id, summary, visibility, state, version, this.pid, this.results);
                        } catch (Exception ex) {
                            Helper.printException(ex);
                        }
                    });
                }
            }
            if(this.getButton(this.getCancelAction()) != null) {
                JButton cancelButton = this.getButton(this.getCancelAction());
                if(cancelButton != null) {
                    cancelButton.addActionListener(event -> this.dispose());
                }
            }
        }catch (Exception ex) {
            Helper.printException(ex);
        }
    }

    private void changeSelectedStatus(String status) {
        if(status != null) {
            if(!status.trim().isEmpty()) {
                status = status.trim().toLowerCase();
                if(status.equals("resolved") || status.equals("closed")) {
                    this.cmbStatus.setSelectedItem("new");
                } else {
                    this.cmbStatus.setSelectedItem("resolved");
                }
            }
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints labelConstraint = PanelCreator.getLabelConstraint();
        GridBagConstraints txtConstraint = PanelCreator.getTxtConstraint();

        JButton cmdCustomFields = new JButton();
        cmdCustomFields.setName("cmdCustomFields");
        cmdCustomFields.setText("Custom Fields");
        cmdCustomFields.addActionListener(event -> {
            if(this.cmbStatus.getSelectedItem() != null) {
                String status = this.cmbStatus.getSelectedItem().toString().trim();
                Map<CustomField, String> fieldStringMap = new LinkedHashMap<>();
                for(CustomField customField : api.getCustomFields(this.pid, true)) {
                    fieldStringMap.put(customField, "");
                }
                CustomFieldDialog customFieldDialog = new CustomFieldDialog(this.project, status, fieldStringMap);
                customFieldDialog.show();
                this.results = customFieldDialog.getResults();
            }
        });

        this.cmbVersion = new ComboBox<>();
        this.cmbVersion.setName("cmbVersion");
        for(MantisVersion version : this.api.getVersions(this.pid)) {
            this.cmbVersion.addItem(version.getName());
        }

        this.cmbStatus = new ComboBox<>();
        this.cmbStatus.setName("cmbName");
        for(ObjectRef item : this.api.getEnum("status")) {
            this.cmbStatus.addItem(item.getName());
        }

        this.cmbStatus.addActionListener(e -> {
           if(this.cmbStatus.getSelectedItem() != null) {
               String item = this.cmbStatus.getSelectedItem().toString();
               if(item != null) {
                   if(!item.trim().equals("")) {
                       if(item.toLowerCase().trim().equals("resolved") || item.toLowerCase().trim().equals("closed")) {
                           this.cmbVersion.setVisible(true);
                           this.lblVersion.setVisible(true);
                       } else {
                           this.cmbVersion.setVisible(false);
                           this.lblVersion.setVisible(false);
                       }
                   }
               }
           }
        });

        this.txtFixed = new JBTextField();
        this.txtFixed.setName("txtSummary");
        this.txtFixed.setPreferredSize(new Dimension(150, 200));

        this.cmbVisibility = new ComboBox<>();
        this.cmbVisibility.setName("cmbVisibility");
        for(ObjectRef item : this.api.getEnum("view_states")) {
            this.cmbVisibility.addItem(item.getName());
        }

        java.awt.Label lblStatus = new java.awt.Label("Change Status");
        this.lblVersion = new java.awt.Label("Fixed in Version");
        java.awt.Label lblFixed = new java.awt.Label("Add Note");
        java.awt.Label lblVisibility = new java.awt.Label("Visibility");

        JPanel basicsPanel = new JPanel(new GridBagLayout());
        basicsPanel.add(lblStatus, labelConstraint);
        basicsPanel.add(this.cmbStatus, txtConstraint);
        basicsPanel.add(this.lblVersion, labelConstraint);
        basicsPanel.add(this.cmbVersion, txtConstraint);
        basicsPanel.add(lblFixed, labelConstraint);
        basicsPanel.add(this.txtFixed, txtConstraint);
        basicsPanel.add(lblVisibility, labelConstraint);
        basicsPanel.add(this.cmbVisibility, txtConstraint);
        basicsPanel.add(cmdCustomFields, txtConstraint);

        root.add(basicsPanel);
        return root;
    }
}
