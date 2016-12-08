package de.domjos.ideaMantis.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class FixDialog extends DialogWrapper {
    private JBTextField txtFixed;
    private ResourceBundle bundle;
    private MantisSoapAPI api;

    protected FixDialog(@Nullable Project project, int id) {
        super(project);
        try {
            this.api = new MantisSoapAPI(ConnectionSettings.getInstance(project));
            this.bundle = Helper.getBundle();
            this.setTitle(bundle.getString("editor.dialog.header"));
            this.setOKButtonText(bundle.getString("buttons.addIssue"));
            this.init();
            if(this.getButton(this.getOKAction())!=null) {
                this.getButton(this.getOKAction()).addActionListener((event) -> {
                    try {
                        api.checkInIssue(id, txtFixed.getText(), true);
                    } catch (Exception ex) {
                        Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
                    }
                });
            }
        }catch (Exception ex) {
            Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
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
        txtFixed.setPreferredSize(new Dimension(100, 25));

        java.awt.Label lblFixed = new java.awt.Label(bundle.getString("basics.checkIn"));

        JPanel basicsPanel = new JPanel(new GridBagLayout());
        basicsPanel.add(lblFixed, labelConstraint);
        basicsPanel.add(txtFixed, txtConstraint);

        root.add(basicsPanel);
        return root;
    }
}
