package de.domjos.ideaMantis.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.soap.ObjectRef;
import de.domjos.ideaMantis.utils.Helper;
import de.domjos.ideaMantis.utils.PanelCreator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class FixDialog extends DialogWrapper {
    private JBTextField txtFixed;
    private ComboBox<String> cmbState;
    private MantisSoapAPI api;
    private final Project project;

    public FixDialog(@Nullable Project project, int id) {
        super(project);
        this.project = project;
        try {
            this.api = new MantisSoapAPI(ConnectionSettings.getInstance(project));
            this.setTitle("Fix Bug");
            this.setOKButtonText("Resolve Issue");
            this.init();
            if(this.getButton(this.getOKAction())!=null) {
                JButton button = this.getButton(this.getOKAction());
                if(button != null) {
                    button.addActionListener((event) -> {
                        try {
                            api.checkInIssue(id, txtFixed.getText(), Objects.requireNonNull(cmbState.getSelectedItem()).toString());
                        } catch (Exception ex) {
                            Helper.printException(ex);
                        }
                    });
                }
            }
        }catch (Exception ex) {
            Helper.printException(ex);
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints labelConstraint = PanelCreator.getLabelConstraint();
        GridBagConstraints txtConstraint = PanelCreator.getTxtConstraint();

        txtFixed = new JBTextField();
        txtFixed.setName("txtSummary");
        txtFixed.setPreferredSize(new Dimension(150, 200));

        cmbState = new ComboBox<>();
        cmbState.setName("cmbState");
        for(ObjectRef item : new MantisSoapAPI(ConnectionSettings.getInstance(project)).getEnum("view_states")) {
            cmbState.addItem(item.getName());
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
