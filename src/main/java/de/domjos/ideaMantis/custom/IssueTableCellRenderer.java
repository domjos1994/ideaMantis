package de.domjos.ideaMantis.custom;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;

public class IssueTableCellRenderer extends DefaultTableCellRenderer {
    private final Map<Integer, Color> colorMap;

    public IssueTableCellRenderer(Map<Integer, Color> colorMap) {
        super();
        this.colorMap = colorMap;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setBackground(this.colorMap.getOrDefault(row, JBColor.WHITE));
        return c;
    }
}
