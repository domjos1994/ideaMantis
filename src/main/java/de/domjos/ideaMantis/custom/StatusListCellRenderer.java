package de.domjos.ideaMantis.custom;

import de.domjos.ideaMantis.utils.Helper;

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
            if (isSelected) {
                c.setBackground(Helper.getColorOfStatus(item));
            } else {
                c.setBackground(Helper.getColorOfStatus(item));
            }
        } else {
            c.setBackground(Helper.getColorOfStatus(item));
            c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        return c;
    }
}
