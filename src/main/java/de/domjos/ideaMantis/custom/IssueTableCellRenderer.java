package de.domjos.ideaMantis.custom;

import com.intellij.ui.JBColor;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class IssueTableCellRenderer extends DefaultTableCellRenderer {

    public IssueTableCellRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component current = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(column == 3) {
            String status = table.getValueAt(row,2).toString().trim();
            current.setBackground(getColorOfStatus(status));
        } else {
            current.setBackground(null);
        }
        return current;
    }

    public static Color getColorOfStatus(String status) {
        try {
            if(status != null) {
                if(!status.trim().isEmpty()) {
                    status = status.trim().toLowerCase();

                    switch (status) {
                        case "new":
                            return Color.decode("#fcbdbd".toUpperCase());
                        case "feedback":
                            return Color.decode("#e3b7eb".toUpperCase());
                        case "acknowledged":
                            return Color.decode("#ffcd85".toUpperCase());
                        case "confirmed":
                            return Color.decode("#fff494".toUpperCase());
                        case "assigned":
                            return Color.decode("#c2dfff".toUpperCase());
                        case "resolved":
                            return Color.decode("#d2f5b0".toUpperCase());
                        case "closed":
                            return Color.decode("#c9ccc4".toUpperCase());
                    }
                }
            }
        } catch (Exception ignored) {}
        return JBColor.WHITE;
    }
}
