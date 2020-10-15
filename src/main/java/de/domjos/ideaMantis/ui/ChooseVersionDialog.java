package de.domjos.ideaMantis.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import de.domjos.ideaMantis.model.MantisVersion;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.utils.PanelCreator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ChooseVersionDialog extends DialogWrapper {
    private final Project project;
    private ComboBox<String> cmbVersions;
    public static MantisVersion currentVersion = null;

    public ChooseVersionDialog(@Nullable Project project) {
        super(project);
        this.project = project;
        this.init();
        this.setTitle("Version-Name");
        if(this.getButton(this.getOKAction())!=null) {
            JButton button = this.getButton(this.getOKAction());
            if(button != null) {
                button.addActionListener((event) -> {
                    for(MantisVersion version : new MantisSoapAPI(ConnectionSettings.getInstance(project)).getVersions(ConnectionSettings.getInstance(project).getProjectID())) {
                        if(version.getName().equals(cmbVersions.getSelectedItem())) {
                            currentVersion = version;
                            break;
                        }
                    }
                });
            }
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints labelConstraint = PanelCreator.getLabelConstraint();
        GridBagConstraints txtConstraint = PanelCreator.getTxtConstraint();

        Label lblVersion = new Label("Fixed in Version");

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
