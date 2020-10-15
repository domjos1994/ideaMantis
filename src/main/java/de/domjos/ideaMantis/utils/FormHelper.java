package de.domjos.ideaMantis.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.impl.ContentImpl;

import javax.swing.table.DefaultTableModel;
import java.util.Objects;

public abstract class FormHelper {

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
