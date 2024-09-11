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

package de.domjos.ideaMantis.utils;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public abstract class PanelCreator {

    public static GridBagConstraints getRootConstraint() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 2.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        return constraints;
    }

    public static GridBagConstraints getLabelConstraint() {
        GridBagConstraints labelConstraint = new GridBagConstraints();
        labelConstraint.anchor = GridBagConstraints.EAST;
        labelConstraint.insets = JBUI.insets(5, 10);
        return labelConstraint;
    }

    public static GridBagConstraints getTxtConstraint() {
        GridBagConstraints txtConstraint = new GridBagConstraints();
        txtConstraint.weightx = 2.0;
        txtConstraint.fill = GridBagConstraints.HORIZONTAL;
        txtConstraint.gridwidth = GridBagConstraints.REMAINDER;
        return txtConstraint;
    }

    public static GridBagConstraints getCustomConstraint(int row, int column, float weight, int width) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = column;
        gridBagConstraints.gridy = row;
        gridBagConstraints.weightx = weight;
        if(width != -1) {
            gridBagConstraints.gridwidth = width;
        }
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        return gridBagConstraints;
    }

    public static JPanel createPanel(List<Component> components) {
        GridBagConstraints lblConstraint = getLabelConstraint();
        GridBagConstraints txtConstraint = getTxtConstraint();

        JPanel jPanel = new JPanel(new GridBagLayout());
        for(Component component : components) {
            if(component instanceof JLabel) {
                jPanel.add(component, lblConstraint);
            } else {
                jPanel.add(component, txtConstraint);
            }
        }
        return jPanel;
    }
}
