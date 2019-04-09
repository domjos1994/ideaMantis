package de.domjos.ideaMantis.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import de.domjos.ideaMantis.model.MantisProfile;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class NewProfileDialog extends DialogWrapper {
    private JBTextField txtPlatform, txtOS, txtBuild;
    private MantisProfile profile;

    public NewProfileDialog(@Nullable Project project) {
        super(project);
        this.profile = null;
        try {
            this.setTitle("Custom Profile");
            this.setOKButtonText("Add");
            this.init();
            if(this.getButton(this.getOKAction())!=null) {
                this.getButton(this.getOKAction()).addActionListener((event) -> {
                    MantisProfile profile = new MantisProfile();
                    profile.setPlatform(txtPlatform.getText());
                    profile.setOs(txtOS.getText());
                    profile.setOsBuild(txtBuild.getText());
                    this.profile = profile;
                });
            }
        }catch (Exception ex) {
            Helper.printException(ex);
        }
    }

    public MantisProfile getProfile() {
        return this.profile;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel root = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraint = Helper.getLabelConstraint();
        GridBagConstraints txtConstraint = Helper.getTextConstraint();

        txtPlatform = new JBTextField();
        txtPlatform.setName("txtPlatform");
        txtPlatform.setPreferredSize(new Dimension(200, 50));

        txtOS = new JBTextField();
        txtOS.setName("txtOS");
        txtOS.setPreferredSize(new Dimension(200, 50));

        txtBuild = new JBTextField();
        txtBuild.setName("txtBuild");
        txtBuild.setPreferredSize(new Dimension(200, 50));

        java.awt.Label lblPlatform = new java.awt.Label("Platform");
        java.awt.Label lblOS = new java.awt.Label("OS");
        java.awt.Label lblBuild = new java.awt.Label("Build");

        JPanel basicsPanel = new JPanel(new GridBagLayout());
        basicsPanel.add(lblPlatform, labelConstraint);
        basicsPanel.add(txtPlatform, txtConstraint);
        basicsPanel.add(lblOS, labelConstraint);
        basicsPanel.add(txtOS, txtConstraint);
        basicsPanel.add(lblBuild, labelConstraint);
        basicsPanel.add(txtBuild, txtConstraint);

        root.add(basicsPanel);
        return root;
    }
}
