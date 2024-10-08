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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.impl.ContentImpl;
import de.domjos.ideaMantis.lang.Lang;
import de.domjos.ideaMantis.model.MantisIssue;
import org.ksoap2.serialization.SoapObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.*;

public abstract class Helper {
    private static Project project;

    public static String getPathToDocument(Document document) {
        final String[] pathToDocument = new String[1];
        WriteCommandAction.runWriteCommandAction(project,()->{
            String[] pathToDocumentArray = document.toString().split("file://");
            if(pathToDocumentArray.length==2) {
                pathToDocument[0] = pathToDocumentArray[1].replace("]", "").trim();
            } else {
                pathToDocument[0] = document.toString();
            }
        });
        return pathToDocument[0];
    }

    public static int getId(String content) {
        final int[] id = new int[1];
        final String[] finalContent = new String[]{content};
        WriteCommandAction.runWriteCommandAction(project,()->{
            try {
                finalContent[0] = finalContent[0].substring(finalContent[0].indexOf("Mantis#")+7);
                id[0] = Integer.parseInt(finalContent[0].split(" ")[0]);
            } catch (Exception ex) {
                id[0] = 0;
            }
        });
        return id[0];
    }

    public static void printNotification(String header, String content, NotificationType type) {
        Notification notification = new Notification(Helper.class.getName(), header, content, type);
        Notifications.Bus.notify(notification);
    }

    public static void printWrongSettingsMsg() {
        Helper.printNotification(Lang.SETTINGS_WRONG, Lang.SETTINGS_GO, NotificationType.WARNING);
    }

    public static void printException(Exception ex) {
        String faultCode = "";
        if(ex.getStackTrace().length>=1) {
            faultCode = ex.getStackTrace()[0].getClassName() + ":" + ex.getStackTrace()[0].getLineNumber();
        }
        Helper.printNotification(
                Lang.ERROR_HEADER,
                ex.getMessage() + "\nFaultCode: " + faultCode,
                NotificationType.ERROR
        );
    }

    public static String getParam(SoapObject object, String name, boolean sub, int id) {
        try {
            if(object.getProperty(name)==null) {
                return "";
            } else {
                if(sub) {
                    return ((SoapObject) object.getProperty(name)).getProperty(id).toString();
                } else {
                    return object.getProperty(name).toString();
                }
            }
        } catch (Exception ex) {
            return "";
        }
    }

    public static void disableControlsInAPanel(JComponent pnl, boolean state) {
        for(Component cmp : pnl.getComponents()) {
            if (!(cmp instanceof JTable || cmp instanceof JList)) {
                cmp.setEnabled(state);
                if (cmp instanceof JPanel) {
                    Helper.disableControlsInAPanel((JPanel) cmp, state);
                } else if (cmp instanceof JToolBar) {
                    Helper.disableControlsInAPanel((JToolBar) cmp, state);
                } else if (cmp instanceof JSplitPane) {
                    Helper.disableControlsInAPanel((JSplitPane) cmp, state);
                } else if (cmp instanceof JScrollPane) {
                    Helper.disableControlsInAPanel((JScrollPane) cmp, state);
                }
            }
        }
    }

    public static void resetControlsInAPanel(JPanel panel) {
        for(Component cmp : panel.getComponents()) {
            if(cmp instanceof JTextField) {
                ((JTextField)cmp).setText("");
            }
            if(cmp instanceof JTextArea) {
                ((JTextArea)cmp).setText("");
            }
            if(cmp instanceof JComboBox) {
                ((JComboBox<?>)cmp).setSelectedItem(null);
            }
            if(cmp instanceof JList) {
                ((JList<?>)cmp).setSelectedValue(null, false);
            }
            if(cmp instanceof JTable) {
                ((JTable)cmp).getSelectionModel().clearSelection();
            }
            if(cmp instanceof JPanel) {
                resetControlsInAPanel((JPanel)cmp);
            }
        }
    }

    public static String replaceCommentByMarker(MantisIssue issue, String comment) {
        if(comment.contains("{additional_information")) {
            comment = comment.replace("{summary}", issue.getAdditional_information());
        }
        if(comment.contains("{category}")) {
            comment = comment.replace("{category}", issue.getCategory());
        }
        if(comment.contains("{date_submitted}")) {
            comment = comment.replace("{date_submitted}", issue.getDate_submitted());
        }
        if(comment.contains("{description}")) {
            comment = comment.replace("{description}", issue.getDescription());
        }
        if(comment.contains("{fixed_in_version}")) {
            comment = comment.replace("{fixed_in_version}", issue.getDescription());
        }
        if(comment.contains("{priority}")) {
            comment = comment.replace("{priority}", issue.getPriority());
        }
        if(comment.contains("{reproducibility}")) {
            comment = comment.replace("{reproducibility}", issue.getReproducibility());
        }
        if(comment.contains("{severity}")) {
            comment = comment.replace("{severity}", issue.getSeverity());
        }
        if(comment.contains("{steps_to_reproduce}")) {
            comment = comment.replace("{steps_to_reproduce}", issue.getSteps_to_reproduce());
        }
        if(comment.contains("{target_version}")) {
            comment = comment.replace("{target_version}", issue.getTarget_version().getName());
        }
        if(comment.contains("{summary}")) {
            comment = comment.replace("{summary}", issue.getSummary());
        }
        if(comment.contains("{state}")) {
            comment = comment.replace("{state}", issue.getStatus());
        }
        if(issue.getReporter()!=null) {
            if(comment.contains("{reporter}")) {
                comment = comment.replace("{reporter}", issue.getReporter().getName());
            }
        }
        return comment;
    }

    public static void commitAllFiles(String comment, ChangeListManager changeListManager) {
        java.util.List<Change> changeList = new LinkedList<>(changeListManager.getAllChanges());
        for(LocalChangeList localChangeList : changeListManager.getChangeLists()) {
            changeListManager.editComment(localChangeList.getName(), comment);
            changeListManager.commitChanges(localChangeList, changeList);
        }
    }

    public static void setProject(Project project) {
        Helper.project = project;
    }

    public static Project getProject() {
        return Helper.project;
    }

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

    public static void setSizes(JTable table) {
        TableColumnModel model = table.getColumnModel();

        model.getColumn(0).setPreferredWidth(75);
        model.getColumn(0).setWidth(75);
        model.getColumn(0).setMaxWidth(75);

        model.getColumn(2).setPreferredWidth(65);
        model.getColumn(2).setWidth(65);
        model.getColumn(2).setMaxWidth(65);

        model.getColumn(3).setPreferredWidth(15);
        model.getColumn(3).setWidth(15);
        model.getColumn(3).setMaxWidth(15);
    }

    public static void reloadToolWindow(String description) {
        ToolWindowManager manager = ToolWindowManager.getInstance(getProject());

        ApplicationManager.getApplication().invokeLater(()->{
            ToolWindow window = manager.getToolWindow(Lang.TOOL_HEADER);
            if(window != null) {
                ContentImpl content = new ContentImpl(window.getComponent(), "", true);
                content.setDescription(description);

                window.getContentManager().addContent(content);
            }
        });
    }
}
