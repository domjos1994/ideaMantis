package de.domjos.ideaMantis.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class FixDialog extends DialogWrapper {
    private JBTextField txtFixed;
    private ComboBox<String> cmbState;
    private MantisSoapAPI api;
    private Project project;

    protected FixDialog(@Nullable Project project, int id) {
        super(project);
        this.project = project;
        try {
            this.api = new MantisSoapAPI(ConnectionSettings.getInstance(project));
            this.setTitle("Fix Bug");
            this.setOKButtonText("Add Issue");
            this.init();
            if(this.getButton(this.getOKAction())!=null) {
                this.getButton(this.getOKAction()).addActionListener((event) -> {
                    try {
                        api.checkInIssue(id, txtFixed.getText(), cmbState.getSelectedItem().toString());
                    } catch (Exception ex) {
                        Helper.printNotification("Exception", ex.toString(), NotificationType.ERROR);
                    }
                });
            }
        }catch (Exception ex) {
            Helper.printNotification("Exception", ex.toString(), NotificationType.ERROR);
        }
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

        txtFixed = new JBTextField();
        txtFixed.setName("txtSummary");
        txtFixed.setPreferredSize(new Dimension(150, 200));

        cmbState = new ComboBox<>();
        cmbState.setName("cmbState");
        for(String item : new MantisSoapAPI(ConnectionSettings.getInstance(project)).getEnum("view_states")) {
            cmbState.addItem(item);
        }

        java.awt.Label lblFixed = new java.awt.Label("Check In");
        java.awt.Label lblState = new java.awt.Label("Status");

        JPanel basicsPanel = new JPanel(new GridBagLayout());
        basicsPanel.add(lblFixed, labelConstraint);
        basicsPanel.add(txtFixed, txtConstraint);
        basicsPanel.add(lblState, labelConstraint);
        basicsPanel.add(cmbState, txtConstraint);

        root.add(basicsPanel);
        return root;
    }
}
