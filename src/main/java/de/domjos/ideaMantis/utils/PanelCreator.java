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
