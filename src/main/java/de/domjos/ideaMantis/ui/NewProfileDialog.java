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
import de.domjos.ideaMantis.lang.Lang;
import de.domjos.ideaMantis.model.MantisProfile;
import de.domjos.ideaMantis.utils.Helper;
import de.domjos.ideaMantis.utils.PanelCreator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class NewProfileDialog extends DialogWrapper {
    private JBTextField txtPlatform, txtOS, txtBuild;
    private MantisProfile profile;

    public NewProfileDialog(@Nullable Project project) {
        super(project);
        this.profile = null;
        try {
            this.setTitle(Lang.DIALOG_PROFILE_HEADER);
            this.setOKButtonText(Lang.DIALOG_PROFILE_OK);
            this.init();
            if(this.getButton(this.getOKAction())!=null) {
                JButton button = this.getButton(this.getOKAction());
                if(button != null) {
                    button.addActionListener((event) -> {
                        MantisProfile profile = new MantisProfile();
                        profile.setPlatform(txtPlatform.getText());
                        profile.setOs(txtOS.getText());
                        profile.setOsBuild(txtBuild.getText());
                        this.profile = profile;
                    });
                }
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

        txtPlatform = new JBTextField();
        txtPlatform.setName("txtPlatform");
        txtPlatform.setPreferredSize(new Dimension(200, 50));

        txtOS = new JBTextField();
        txtOS.setName("txtOS");
        txtOS.setPreferredSize(new Dimension(200, 50));

        txtBuild = new JBTextField();
        txtBuild.setName("txtBuild");
        txtBuild.setPreferredSize(new Dimension(200, 50));

        java.awt.Label lblPlatform = new java.awt.Label(Lang.COLUMN_PROFILE_PLATFORM);
        java.awt.Label lblOS = new java.awt.Label(Lang.COLUMN_PROFILE_OS);
        java.awt.Label lblBuild = new java.awt.Label(Lang.COLUMN_PROFILE_Build);

        JPanel basicsPanel =
            PanelCreator.createPanel(
                Arrays.asList(
                    lblPlatform, txtPlatform, lblOS, txtOS, lblBuild, txtBuild
                )
            );

        root.add(basicsPanel);
        return root;
    }
}
