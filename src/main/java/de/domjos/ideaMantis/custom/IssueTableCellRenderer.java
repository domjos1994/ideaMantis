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

import com.intellij.ui.JBColor;
import de.domjos.ideaMantis.service.ConnectionSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class IssueTableCellRenderer extends DefaultTableCellRenderer {
    private final ConnectionSettings settings;

    public IssueTableCellRenderer(ConnectionSettings settings) {
        super();
        this.settings = settings;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component current = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(column == 3) {
            String status = table.getValueAt(row,2).toString().trim();
            current.setBackground(getColorOfStatus(settings, status));
        } else {
            current.setBackground(null);
        }
        return current;
    }

    public static Color getColorOfStatus(ConnectionSettings settings, String status) {
        try {
            if(status != null) {
                if(!status.trim().isEmpty()) {
                    status = status.trim().toLowerCase();

                    switch (status) {
                        case "new":
                            return Color.decode(settings.getColorNew().toUpperCase());
                        case "feedback":
                            return Color.decode(settings.getColorFeedback().toUpperCase());
                        case "acknowledged":
                            return Color.decode(settings.getColorAcknowledged().toUpperCase());
                        case "confirmed":
                            return Color.decode(settings.getColorConfirmed().toUpperCase());
                        case "assigned":
                            return Color.decode(settings.getColorAssigned().toUpperCase());
                        case "resolved":
                            return Color.decode(settings.getColorResolved().toUpperCase());
                        case "closed":
                            return Color.decode(settings.getColorClosed().toUpperCase());
                    }
                }
            }
        } catch (Exception ignored) {}
        return JBColor.WHITE;
    }
}
