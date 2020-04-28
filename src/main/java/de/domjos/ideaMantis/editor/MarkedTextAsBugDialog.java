package de.domjos.ideaMantis.editor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import de.domjos.ideaMantis.model.IssueAttachment;
import de.domjos.ideaMantis.model.MantisIssue;
import de.domjos.ideaMantis.model.MantisVersion;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.ui.IdeaMantisIssues;
import de.domjos.ideaMantis.utils.FormHelper;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

class MarkedTextAsBugDialog extends DialogWrapper {
    private JBTextField txtSummary, txtDocumentPath, txtDate;
    private JTextArea txtDescription;
    private JCheckBox chkAttachment;
    private JComboBox<String> cmbCategory, cmbSeverity, cmbPriority, cmbStatus, cmbVersion, cmbTargetVersion, cmbFixedInVersion;
    private String description, documentPath;
    private int id;

    private MantisSoapAPI api;
    private ConnectionSettings settings;

    MarkedTextAsBugDialog(@Nullable Project project, String description) {
        this(project, description, "");
    }


    MarkedTextAsBugDialog(@Nullable Project project, String description, String documentPath) {
        super(project);
        try {
            this.description = description;
            this.documentPath = documentPath;
            this.settings = ConnectionSettings.getInstance(project);
            this.api = new MantisSoapAPI(settings);
            this.setTitle("Mark Text as Bug");
            this.setOKButtonText("Add Issue");
            this.init();
            if(this.getButton(this.getOKAction())!=null) {
                Objects.requireNonNull(this.getButton(this.getOKAction())).addActionListener((event) -> {
                    try {
                        MantisIssue issue = new MantisIssue();
                        issue.setDescription(txtDescription.getText());
                        issue.setSummary(txtSummary.getText());
                        issue.setDate_submitted(txtDate.getText());
                        issue.setCategory(Objects.requireNonNull(cmbCategory.getSelectedItem()).toString());
                        issue.setSeverity(Objects.requireNonNull(cmbSeverity.getSelectedItem()).toString());
                        issue.setPriority(Objects.requireNonNull(cmbPriority.getSelectedItem()).toString());
                        issue.setStatus(Objects.requireNonNull(cmbStatus.getSelectedItem()).toString());
                        MantisSoapAPI api = new MantisSoapAPI(settings);
                        java.util.List<MantisVersion> versions = api.getVersions(settings.getProjectID());
                        if(cmbVersion.getSelectedItem()!=null) {
                            for(MantisVersion version : versions) {
                                if(version.getName().equals(cmbVersion.getSelectedItem())) {
                                    issue.setVersion(version);
                                    break;
                                }
                            }
                        }
                        if(cmbFixedInVersion.getSelectedItem()!=null) {
                            for(MantisVersion version : versions) {
                                if(version.getName().equals(cmbFixedInVersion.getSelectedItem())) {
                                    issue.setFixed_in_version(version);
                                    break;
                                }
                            }
                        }
                        if(cmbTargetVersion.getSelectedItem()!=null) {
                            for(MantisVersion version : versions) {
                                if(version.getName().equals(cmbTargetVersion.getSelectedItem().toString())) {
                                    issue.setTarget_version(version);
                                    break;
                                }
                            }
                        }
                        IssueAttachment attachment = new IssueAttachment();
                        attachment.setFilename(txtDocumentPath.getText());
                        attachment.setSize((int) new File(txtDocumentPath.getText()).getTotalSpace());
                        issue.addAttachment(attachment);
                        api.addIssue(issue);
                        this.id = api.getIssueID();
                        FormHelper.reloadToolWindow(IdeaMantisIssues.RELOAD_ISSUES);
                    } catch (Exception ex) {
                        Helper.printException(ex);
                    }
                });
            }
        }catch (Exception ex) {
            Helper.printException(ex);
        }
    }

    int getID() {
        return this.id;
    }

    @Override
    protected ValidationInfo doValidate() {
        ValidationInfo  info = null;
        if(txtSummary.getText().equals("")) {
            info = new ValidationInfo(String.format("%s is a mandatory field!", "Summary"));
        }
        if(txtDescription.getText().equals("")) {
            info = new ValidationInfo(String.format("%s is a mandatory field!", "Description"));
        }

        return info;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        txtSummary.requestFocus();
        return txtSummary;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {


        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 2.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        GridBagConstraints labelConstraint = new GridBagConstraints();
        labelConstraint.anchor = GridBagConstraints.EAST;
        labelConstraint.insets = JBUI.insets(5, 10);
        GridBagConstraints txtConstraint = new GridBagConstraints();
        txtConstraint.weightx = 2.0;
        txtConstraint.fill = GridBagConstraints.HORIZONTAL;
        txtConstraint.gridwidth = GridBagConstraints.REMAINDER;

        txtSummary = new JBTextField();
        txtSummary.setName("txtSummary");
        txtDate = new JBTextField();
        txtDate.setName("txtDate");
        txtDate.setEnabled(false);
        txtDate.setText(new Date().toString());
        txtDescription = new JTextArea();
        txtDescription.setName("txtDescription");
        txtDescription.setText(this.description);
        cmbCategory = new ComboBox<>();
        api.getCategories(settings.getProjectID()).forEach(cmbCategory::addItem);


        java.awt.Label lblSummary = new java.awt.Label("Summary");
        java.awt.Label lblDate = new java.awt.Label("Date");
        java.awt.Label lblDescription = new java.awt.Label("Description");
        java.awt.Label lblCategory = new java.awt.Label("Category");


        JPanel basicsPanel = new JPanel(new GridBagLayout());
        basicsPanel.add(lblSummary, labelConstraint);
        basicsPanel.add(txtSummary, txtConstraint);
        basicsPanel.add(lblDate, labelConstraint);
        basicsPanel.add(txtDate, txtConstraint);
        basicsPanel.add(lblDescription, labelConstraint);
        basicsPanel.add(txtDescription, txtConstraint);
        basicsPanel.add(lblCategory, labelConstraint);
        basicsPanel.add(cmbCategory, txtConstraint);

        basicsPanel.setBorder(IdeBorderFactory.createTitledBorder("Basics"));
        root.add(basicsPanel, constraints);


        cmbVersion = new ComboBox<>();
        cmbTargetVersion = new ComboBox<>();
        cmbFixedInVersion = new ComboBox<>();
        api.getVersions(settings.getProjectID()).forEach(version -> {
            cmbVersion.addItem(version.getName());
            cmbFixedInVersion.addItem(version.getName());
            cmbTargetVersion.addItem(version.getName());
        });

        java.awt.Label lblVersion = new java.awt.Label("Version");
        java.awt.Label lblFixedInVersion = new java.awt.Label("Fixed in Version");
        java.awt.Label lblTargetVersion = new java.awt.Label("Target Version");

        JPanel versionPanel = new JPanel(new GridBagLayout());
        versionPanel.add(lblVersion, labelConstraint);
        versionPanel.add(cmbVersion, txtConstraint);
        versionPanel.add(lblTargetVersion, labelConstraint);
        versionPanel.add(cmbTargetVersion, txtConstraint);
        versionPanel.add(lblFixedInVersion, labelConstraint);
        versionPanel.add(cmbFixedInVersion, txtConstraint);

        versionPanel.setBorder(IdeBorderFactory.createTitledBorder("Version"));
        root.add(versionPanel, constraints);


        cmbPriority = new ComboBox<>();
        cmbSeverity = new ComboBox<>();
        cmbStatus = new ComboBox<>();
        api.getEnum("priorities").forEach(objectRef -> cmbPriority.addItem(objectRef.getName()));
        api.getEnum("severities").forEach(objectRef -> cmbSeverity.addItem(objectRef.getName()));
        api.getEnum("status").forEach(objectRef -> cmbStatus.addItem(objectRef.getName()));

        java.awt.Label lblPriority = new java.awt.Label("Priority");
        java.awt.Label lblSeverity = new java.awt.Label("Severity");
        java.awt.Label lblStatus = new java.awt.Label("Status");

        JPanel statePanel = new JPanel(new GridBagLayout());
        statePanel.add(lblPriority, labelConstraint);
        statePanel.add(cmbPriority, txtConstraint);
        statePanel.add(lblSeverity, labelConstraint);
        statePanel.add(cmbSeverity, txtConstraint);
        statePanel.add(lblStatus, labelConstraint);
        statePanel.add(cmbStatus, txtConstraint);

        statePanel.setBorder(IdeBorderFactory.createTitledBorder("View-State"));
        root.add(statePanel, constraints);

        if(!this.documentPath.equals("")) {
            JPanel attachmentPanel = new JPanel(new GridBagLayout());
            txtDocumentPath = new JBTextField();
            txtDocumentPath.setEnabled(false);
            java.awt.Label lblDocumentPath = new java.awt.Label("File-Name");
            attachmentPanel.add(lblDocumentPath, labelConstraint);
            attachmentPanel.add(txtDocumentPath, txtConstraint);

            chkAttachment = new JCheckBox();
            chkAttachment.setText("Add File as Attachment!");
            chkAttachment.addActionListener(e -> {
                if(chkAttachment.isSelected()) {
                    txtDocumentPath.setText(this.documentPath);
                } else {
                    txtDocumentPath.setText("");
                }
            });
            attachmentPanel.add(new Label(), labelConstraint);
            attachmentPanel.add(chkAttachment, txtConstraint);

            attachmentPanel.setBorder(IdeBorderFactory.createTitledBorder("Attachment"));
            root.add(attachmentPanel, constraints);
        }
        return root;
    }
}
