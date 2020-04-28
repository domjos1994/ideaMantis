package de.domjos.ideaMantis.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.content.impl.ContentImpl;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class FormHelper {

    public static DefaultTableModel addColumnsToTable(String... columns) {
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for(String column : columns) {
            model.addColumn(column);
        }
        return model;
    }

    public static GridBagConstraints getLabelConstraint() {
        GridBagConstraints lblConstraint = new GridBagConstraints();
        lblConstraint.anchor = GridBagConstraints.EAST;
        lblConstraint.insets = JBUI.insets(5, 10);
        return lblConstraint;
    }

    public static GridBagConstraints getTextConstraint() {
        GridBagConstraints txtConstraint = new GridBagConstraints();
        txtConstraint.weightx = 2.0;
        txtConstraint.fill = GridBagConstraints.HORIZONTAL;
        txtConstraint.gridwidth = GridBagConstraints.REMAINDER;
        return txtConstraint;
    }

    public static GridBagConstraints getRootConstraint() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 2.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        return constraints;
    }

    public static JPanel createPanel(List<Component> components) {
        GridBagConstraints lblConstraint = FormHelper.getLabelConstraint();
        GridBagConstraints txtConstraint = FormHelper.getTextConstraint();

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

    public static void reloadToolWindow(String description) {
        ToolWindowManager manager = ToolWindowManager.getInstance(Helper.getProject());
        ApplicationManager.getApplication().invokeLater(()->{
            ToolWindow window = manager.getToolWindow("Show MantisBT-Issues");
            ContentImpl content = new ContentImpl(null, "", true);
            content.setDescription(description);
            Objects.requireNonNull(window).getContentManager().addContent(content);
        });
    }
}
