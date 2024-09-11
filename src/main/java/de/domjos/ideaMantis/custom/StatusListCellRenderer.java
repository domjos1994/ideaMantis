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

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class StatusListCellRenderer extends BasicComboBoxRenderer {
    private final ListCellRenderer defaultRenderer;

    public StatusListCellRenderer(ListCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }


    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = this.defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String item = "";
        if(value instanceof String) {
            item = ((String) value).trim().toLowerCase();
        } else {
            if(value != null) {
                item = value.toString().trim().toLowerCase();
            }
        }
        if (c instanceof JLabel) {
            c.setBackground(IssueTableCellRenderer.getColorOfStatus(item));
        } else {
            c.setBackground(IssueTableCellRenderer.getColorOfStatus(item));
            c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        return c;
    }
}
