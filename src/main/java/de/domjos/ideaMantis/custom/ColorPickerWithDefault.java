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

package de.domjos.ideaMantis.custom;

import com.intellij.ui.ColorPicker;
import com.intellij.ui.JBColor;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.JBUI;
import de.domjos.ideaMantis.lang.Lang;
import de.domjos.ideaMantis.utils.PanelCreator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ColorPickerWithDefault extends JBPanelWithEmptyText {
    private final String defaultColor;
    private String color;
    private JLabel selectedColorBtn;

    public ColorPickerWithDefault(String label, String color, String defaultColor) {
        this.setLayout(new GridBagLayout());
        this.color = color;
        this.defaultColor = defaultColor;

        this.init(label);
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
        selectedColorBtn.setText("#" + color);
        selectedColorBtn.setBackground(Color.decode(color.toUpperCase()));
    }

    private void init(String label) {
        JBLabel jbLabel = new JBLabel(label);
        this.add(jbLabel, PanelCreator.getCustomConstraint(0, 0, 0.3f, -1));
        this.add(new JBLabel(""), PanelCreator.getCustomConstraint(0, 1, 0.0f, 10));

        this.selectedColorBtn = this.createCustomLabel(this.color);
        this.selectedColorBtn.setBackground(Color.decode(this.color.toUpperCase()));
        this.selectedColorBtn.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                Color tmp = ColorPicker.showDialog(selectedColorBtn, label, Color.decode(color), false, null, false);
                if(tmp != null) {
                    color = "#" + encode(tmp);
                    selectedColorBtn.setBackground(tmp);
                    selectedColorBtn.setText(color);
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {}
            @Override
            public void mouseReleased(MouseEvent mouseEvent) {}
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {}
            @Override
            public void mouseExited(MouseEvent mouseEvent) {}
        });
        this.add(this.selectedColorBtn, PanelCreator.getCustomConstraint(0, 2, 0.4f, -1));
        this.add(new JBLabel(""), PanelCreator.getCustomConstraint(0, 3, 0.0f, 10));

        JLabel defaultColorBtn = this.createCustomLabel(Lang.RESET_TO_DEFAULT);
        defaultColorBtn.setBackground(Color.decode(this.defaultColor.toUpperCase()));
        defaultColorBtn.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                color = defaultColor;
                selectedColorBtn.setText("#" + color);
                selectedColorBtn.setBackground(Color.decode(color.toUpperCase()));
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {}
            @Override
            public void mouseReleased(MouseEvent mouseEvent) {}
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {}
            @Override
            public void mouseExited(MouseEvent mouseEvent) {}
        });
        this.add(defaultColorBtn, PanelCreator.getCustomConstraint(0, 4, 0.3f, -1));
    }

    private String encode(Color color) {
        return Integer.toHexString(color.getRGB()).substring(2);
    }

    private JLabel createCustomLabel(String text) {
        JLabel jbLabel = new JLabel(text);
        EmptyBorder emptyBorder = JBUI.Borders.empty(5, 20);
        RoundedLineBorder roundedLineBorder = new RoundedLineBorder(JBColor.border());
        CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(roundedLineBorder, emptyBorder);
        jbLabel.setBorder(compoundBorder);
        jbLabel.setOpaque(true);
        jbLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        jbLabel.setVerticalTextPosition(SwingConstants.CENTER);
        jbLabel.setForeground(JBColor.BLACK);
        return jbLabel;
    }
}
