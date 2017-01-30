package de.domjos.ideaMantis.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import de.domjos.ideaMantis.model.MantisIssue;
import org.ksoap2.serialization.SoapObject;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public abstract class Helper {

    public static void printNotification(String header, String content, NotificationType type) {
        Notification notification = new Notification(Helper.class.getName(), header, content, type);
        Notifications.Bus.notify(notification);
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
                ((JComboBox)cmp).setSelectedItem(null);
            }
            if(cmp instanceof JList) {
                ((JList)cmp).setSelectedValue(null, false);
            }
            if(cmp instanceof JTable) {
                ((JTable)cmp).getSelectionModel().clearSelection();
            }
            if(cmp instanceof JPanel) {
                resetControlsInAPanel((JPanel)cmp);
            }
        }
    }

    public static JTable disableEditingTable(JTable table) {
        for(int row = 0; row<=table.getRowCount()-1; row++) {
            for(int column = 0; column<=table.getRowCount()-1; column++) {
                table.setCellEditor(null);
            }
        }
        return table;
    }

    public static String replaceCommentByMarker(MantisIssue issue, String comment) {
        if(comment.contains("{additional_information")) {
            comment = comment.replace("{summary}", issue.getAdditional_information());
        }
        if(comment.contains("{build}")) {
            comment = comment.replace("{build}", issue.getBuild());
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
        java.util.List<Change> changeList = new LinkedList<>();
        changeListManager.getAllChanges().forEach(changeList::add);
        for(LocalChangeList localChangeList : changeListManager.getChangeLists()) {
            localChangeList.setComment(comment);
            changeListManager.commitChanges(localChangeList, changeList);
        }
    }
}
