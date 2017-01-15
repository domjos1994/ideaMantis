package de.domjos.ideaMantis.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
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

    public static ResourceBundle getBundle() {
        ResourceBundle bundle;
        try{
            bundle = ResourceBundle.getBundle("de.domjos.ideaMantis.messages.lang", Locale.getDefault());
        } catch (Exception ex) {
           bundle = null;
        }
        return bundle;
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
