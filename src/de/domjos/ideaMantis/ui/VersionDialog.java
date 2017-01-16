package de.domjos.ideaMantis.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import de.domjos.ideaMantis.model.MantisVersion;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class VersionDialog extends DialogWrapper {
    private MantisSoapAPI api;
    private JBTextField txtName, txtDateOrder;
    private JTextArea txtDescription;
    private JCheckBox chkObsolete, chkReleased;
    private JButton cmdDelete;
    private ResourceBundle bundle;
    private MantisVersion version = new MantisVersion();

    protected VersionDialog(@Nullable Project project, int project_id, ResourceBundle bundle) {
        super(project);
        this.api = new MantisSoapAPI(ConnectionSettings.getInstance(project));
        this.bundle = bundle;
        this.init();
        cmdDelete.setVisible(version.getId()!=0);
        this.setTitle(bundle.getString("version.name"));
        if(this.getButton(this.getOKAction())!=null) {
            this.getButton(this.getOKAction()).addActionListener((event) -> {
                version.setName(txtName.getText());
                version.setDate(txtDateOrder.getText());
                version.setDescription(txtDescription.getText());
                version.setObsolete(chkObsolete.isSelected());
                version.setReleased(chkReleased.isSelected());
                api.addVersion(version, project_id);
            });
        }
    }

    protected VersionDialog(@Nullable Project project, int project_id, ResourceBundle bundle, MantisVersion version) {
        this(project, project_id, bundle);
        this.version = version;
        txtName.setText(version.getName());
        txtDateOrder.setText(version.getDate());
        txtDescription.setText(version.getDescription());
        chkObsolete.setSelected(version.isObsolete());
        chkReleased.setSelected(version.isReleased());
        cmdDelete.setVisible(version.getId()!=0);
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

        txtName = new JBTextField();
        txtName.setName("txtName");
        txtName.setPreferredSize(new Dimension(150, 25));

        txtDateOrder = new JBTextField();
        txtDateOrder.setText(new SimpleDateFormat("dd-MM-yyyy hh:mm").format(new Date()));
        txtDateOrder.setName("txtDateOrder");
        txtDateOrder.setPreferredSize(new Dimension(150, 25));

        txtDescription = new JTextArea();
        txtDescription.setName("txtDescription");
        txtDescription.setPreferredSize(new Dimension(150, 100));

        chkReleased = new JCheckBox(bundle.getString("version.released"));
        chkReleased.setName("chkReleased");

        chkObsolete = new JCheckBox(bundle.getString("version.obsolete"));
        chkObsolete.setName("chkObsolete");

        cmdDelete = new JButton(bundle.getString("buttons.delete"));
        cmdDelete.setName("cmdDelete");
        cmdDelete.addActionListener(e -> {
            api.deleteVersion(version.getId());
            if(this.getButton(this.getOKAction())!=null)
                this.getButton(this.getOKAction()).doClick();
        });

        java.awt.Label lblName = new java.awt.Label(bundle.getString("version.name"));
        java.awt.Label lblDateOrder = new java.awt.Label(bundle.getString("version.date"));
        java.awt.Label lblDescription = new java.awt.Label(bundle.getString("version.description"));

        JPanel basicsPanel = new JPanel(new GridBagLayout());
        basicsPanel.add(lblName, labelConstraint);
        basicsPanel.add(txtName, txtConstraint);
        basicsPanel.add(lblDateOrder, labelConstraint);
        basicsPanel.add(txtDateOrder, txtConstraint);
        basicsPanel.add(lblDescription, labelConstraint);
        basicsPanel.add(txtDescription, txtConstraint);
        basicsPanel.add(chkReleased, labelConstraint);
        basicsPanel.add(chkObsolete, txtConstraint);
        basicsPanel.add(cmdDelete);

        root.add(basicsPanel);
        return root;
    }

    public JComponent getPreferredFocusedComponent() {
        return this.txtName;
    }
}
