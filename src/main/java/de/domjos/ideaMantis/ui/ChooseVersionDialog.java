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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import de.domjos.ideaMantis.lang.Lang;
import de.domjos.ideaMantis.model.MantisVersion;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.utils.PanelCreator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ChooseVersionDialog extends DialogWrapper {
    private ComboBox<String> cmbVersions;
    public static MantisVersion currentVersion = null;
    private final MantisSoapAPI api;

    public ChooseVersionDialog(@Nullable Project project) {
        super(project);
        this.init();
        this.setTitle(Lang.COLUMN_VERSION);

        assert project != null;
        this.api = new MantisSoapAPI(ConnectionSettings.getInstance(project));

        if(this.getButton(this.getOKAction())!=null) {
            JButton button = this.getButton(this.getOKAction());
            if(button != null) {
                button.addActionListener((event) -> {
                    for(MantisVersion version : this.api.getVersions()) {
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

        Label lblVersion = new Label(Lang.COLUMN_VERSION_FIXED);

        cmbVersions = new ComboBox<>();
        for(MantisVersion version : this.api.getVersions()) {
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
