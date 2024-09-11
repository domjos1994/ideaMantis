/*
 * Copyright (c) 2024 DOMINIC JOAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.ideaMantis.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import de.domjos.ideaMantis.lang.Lang;
import de.domjos.ideaMantis.model.MantisVersion;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("WriteOnlyObject")
public class VersionDialog extends DialogWrapper {
    private final MantisSoapAPI api;
    private JBTextField txtName, txtDateOrder;
    private JTextArea txtDescription;
    private JCheckBox chkObsolete, chkReleased;
    private JButton cmdDelete;
    private MantisVersion version = new MantisVersion();

    VersionDialog(@Nullable Project project) {
        super(project);
        assert project != null;
        this.api = new MantisSoapAPI(ConnectionSettings.getInstance(project));
        this.init();
        cmdDelete.setVisible(version.getId()!=0);
        this.setTitle(Lang.DIALOG_VERSION);
        if(this.getButton(this.getOKAction())!=null) {
            JButton button = this.getButton(this.getOKAction());
            if(button != null) {
                button.addActionListener((event) -> {
                    version.setName(txtName.getText());
                    version.setDate(txtDateOrder.getText());
                    version.setDescription(txtDescription.getText());
                    version.setObsolete(chkObsolete.isSelected());
                    version.setReleased(chkReleased.isSelected());
                    api.addVersion(version);
                });
            }
        }
    }

    VersionDialog(@Nullable Project project, MantisVersion version) {
        this(project);
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
        txtDateOrder.setText(new SimpleDateFormat(Lang.DATE_FORMAT).format(new Date()));
        txtDateOrder.setName("txtDateOrder");
        txtDateOrder.setPreferredSize(new Dimension(150, 25));

        txtDescription = new JTextArea();
        txtDescription.setName("txtDescription");
        txtDescription.setPreferredSize(new Dimension(150, 100));

        chkReleased = new JCheckBox("Released");
        chkReleased.setName("chkReleased");

        chkObsolete = new JCheckBox("Obsolete");
        chkObsolete.setName("chkObsolete");

        cmdDelete = new JButton("Delete");
        cmdDelete.setName("cmdDelete");
        cmdDelete.addActionListener(e -> {
            api.deleteVersion(version.getId());
            if(this.getButton(this.getOKAction())!=null) {
                JButton button = this.getButton(this.getOKAction());
                if(button != null) {
                    button.doClick();
                }
            }
        });

        java.awt.Label lblName = new java.awt.Label(Lang.COLUMN_NAME);
        java.awt.Label lblDateOrder = new java.awt.Label(Lang.COLUMN_DATE);
        java.awt.Label lblDescription = new java.awt.Label(Lang.COLUMN_DESCRIPTION);

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
