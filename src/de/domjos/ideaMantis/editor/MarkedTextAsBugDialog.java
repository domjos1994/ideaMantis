package de.domjos.ideaMantis.editor;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import de.domjos.ideaMantis.model.IssueAttachment;
import de.domjos.ideaMantis.model.MantisIssue;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.ResourceBundle;

class MarkedTextAsBugDialog extends DialogWrapper {
    private JBTextField txtSummary, txtDocumentPath, txtDate;
    private JTextArea txtDescription;
    private JComboBox<String> cmbCategory, cmbSeverity, cmbPriority, cmbStatus, cmbTargetVersion, cmbFixedInVersion;
    private String description, documentPath;

    private MantisSoapAPI api;
    private ConnectionSettings settings;
    private ResourceBundle bundle;

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
            bundle = Helper.getBundle();
            this.setTitle(bundle.getString("editor.dialog.header"));
            this.setOKButtonText(bundle.getString("buttons.addIssue"));
            this.init();
            if(this.getButton(this.getOKAction())!=null) {
                this.getButton(this.getOKAction()).addActionListener((event) -> {
                    try {
                        MantisIssue issue = new MantisIssue();
                        issue.setDescription(txtDescription.getText());
                        issue.setSummary(txtSummary.getText());
                        issue.setDate_submitted(txtDate.getText());
                        issue.setCategory(cmbCategory.getSelectedItem().toString());
                        issue.setSeverity(cmbSeverity.getSelectedItem().toString());
                        issue.setPriority(cmbPriority.getSelectedItem().toString());
                        issue.setStatus(cmbStatus.getSelectedItem().toString());
                        issue.setTarget_version(cmbTargetVersion.getSelectedItem().toString());
                        issue.setFixed_in_version(cmbFixedInVersion.getSelectedItem().toString());
                        IssueAttachment attachment = new IssueAttachment();
                        attachment.setFilename(txtDocumentPath.getText());
                        attachment.setSize((int) new File(txtDocumentPath.getText()).getTotalSpace());
                        issue.addAttachment(attachment);
                        api.addIssue(issue);
                    } catch (Exception ex) {
                        Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
                    }
                });
            }
        }catch (Exception ex) {
            Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
        }
    }

    @Override
    protected ValidationInfo doValidate() {
        ValidationInfo  info = null;
        if(txtSummary.getText().equals("")) {
            info = new ValidationInfo(String.format(bundle.getString("messages.mandatory"), bundle.getString("basics.summary").replace("*", "")));
        }
        if(txtDescription.getText().equals("")) {
            info = new ValidationInfo(String.format(bundle.getString("messages.mandatory"), bundle.getString("descriptions.description").replace("*", "")));
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


        java.awt.Label lblSummary = new java.awt.Label(bundle.getString("basics.summary"));
        java.awt.Label lblDate = new java.awt.Label(bundle.getString("basics.date"));
        java.awt.Label lblDescription = new java.awt.Label(bundle.getString("descriptions.description"));
        java.awt.Label lblCategory = new java.awt.Label(bundle.getString("basics.category"));


        JPanel basicsPanel = new JPanel(new GridBagLayout());
        basicsPanel.add(lblSummary, labelConstraint);
        basicsPanel.add(txtSummary, txtConstraint);
        basicsPanel.add(lblDate, labelConstraint);
        basicsPanel.add(txtDate, txtConstraint);
        basicsPanel.add(lblDescription, labelConstraint);
        basicsPanel.add(txtDescription, txtConstraint);
        basicsPanel.add(lblCategory, labelConstraint);
        basicsPanel.add(cmbCategory, txtConstraint);

        basicsPanel.setBorder(IdeBorderFactory.createTitledBorder(bundle.getString("basics.header")));
        root.add(basicsPanel, constraints);


        cmbTargetVersion = new ComboBox<>();
        cmbFixedInVersion = new ComboBox<>();
        api.getVersions(settings.getProjectID()).forEach(version -> {
            cmbFixedInVersion.addItem(version.getName());
            cmbTargetVersion.addItem(version.getName());
        });

        java.awt.Label lblFixedInVersion = new java.awt.Label(bundle.getString("basics.fixedInVersion"));
        java.awt.Label lblTargetVersion = new java.awt.Label(bundle.getString("basics.targetVersion"));

        JPanel versionPanel = new JPanel(new GridBagLayout());
        versionPanel.add(lblTargetVersion, labelConstraint);
        versionPanel.add(cmbTargetVersion, txtConstraint);
        versionPanel.add(lblFixedInVersion, labelConstraint);
        versionPanel.add(cmbFixedInVersion, txtConstraint);

        versionPanel.setBorder(IdeBorderFactory.createTitledBorder(bundle.getString("basics.version")));
        root.add(versionPanel, constraints);


        cmbPriority = new ComboBox<>();
        cmbSeverity = new ComboBox<>();
        cmbStatus = new ComboBox<>();
        api.getEnum("priorities").forEach(cmbPriority::addItem);
        api.getEnum("severities").forEach(cmbSeverity::addItem);
        api.getEnum("status").forEach(cmbStatus::addItem);

        java.awt.Label lblPriority = new java.awt.Label(bundle.getString("basics.priority"));
        java.awt.Label lblSeverity = new java.awt.Label(bundle.getString("basics.severity"));
        java.awt.Label lblStatus = new java.awt.Label(bundle.getString("basics.status"));

        JPanel statePanel = new JPanel(new GridBagLayout());
        statePanel.add(lblPriority, labelConstraint);
        statePanel.add(cmbPriority, txtConstraint);
        statePanel.add(lblSeverity, labelConstraint);
        statePanel.add(cmbSeverity, txtConstraint);
        statePanel.add(lblStatus, labelConstraint);
        statePanel.add(cmbStatus, txtConstraint);

        statePanel.setBorder(IdeBorderFactory.createTitledBorder(bundle.getString("basics.states")));
        root.add(statePanel, constraints);


        if(!this.documentPath.equals("")) {
            JPanel attachmentPanel = new JPanel();
            txtDocumentPath = new JBTextField();
            txtDocumentPath.setEnabled(false);
            txtDocumentPath.setText(this.documentPath);
            java.awt.Label lblDocumentPath = new java.awt.Label(bundle.getString("attachments.fileName"));
            attachmentPanel.add(lblDocumentPath, labelConstraint);
            attachmentPanel.add(txtDocumentPath, txtConstraint);

            attachmentPanel.setBorder(IdeBorderFactory.createTitledBorder(bundle.getString("attachments.header")));
            root.add(attachmentPanel, constraints);
        }
        return root;
    }
}
