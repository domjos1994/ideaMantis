package de.domjos.ideaMantis.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import de.domjos.ideaMantis.model.MantisVersion;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class ChooseVersionDialog extends DialogWrapper {
    private Project project;
    private ComboBox<String> cmbVersions;
    private ResourceBundle bundle;
    public static MantisVersion currentVersion = null;

    public ChooseVersionDialog(@Nullable Project project, ResourceBundle bundle) {
        super(project);
        this.project = project;
        this.bundle = bundle;
        this.init();
        this.setTitle(bundle.getString("version.name"));
        if(this.getButton(this.getOKAction())!=null) {
            this.getButton(this.getOKAction()).addActionListener((event) -> {
                for(MantisVersion version : new MantisSoapAPI(ConnectionSettings.getInstance(project)).getVersions(ConnectionSettings.getInstance(project).getProjectID())) {
                    if(version.getName().equals(cmbVersions.getSelectedItem())) {
                        currentVersion = version;
                        break;
                    }
                }
            });
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 2.0;
        constraints.weighty = 0.0;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        GridBagConstraints labelConstraint = new GridBagConstraints();
        labelConstraint.anchor = GridBagConstraints.EAST;
        labelConstraint.insets = JBUI.insets(5, 10);
        GridBagConstraints txtConstraint = new GridBagConstraints();
        txtConstraint.weightx = 2.0;
        txtConstraint.fill = GridBagConstraints.HORIZONTAL;
        txtConstraint.gridwidth = GridBagConstraints.REMAINDER;

        Label lblVersion = new Label(bundle.getString("basics.fixedInVersion"));

        cmbVersions = new ComboBox<>();
        for(MantisVersion version : new MantisSoapAPI(ConnectionSettings.getInstance(project)).getVersions(ConnectionSettings.getInstance(project).getProjectID())) {
            cmbVersions.addItem(version.getName());
        }
        cmbVersions.addItem("");
        cmbVersions.setSelectedItem("");

        JPanel basicsPanel = new JPanel(new GridBagLayout());
        basicsPanel.add(lblVersion, labelConstraint);
        basicsPanel.add(cmbVersions, txtConstraint);

        root.add(basicsPanel);
        return root;
    }
}
