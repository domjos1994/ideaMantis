package de.domjos.ideaMantis.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.ksoap2.serialization.SoapObject;

import javax.swing.*;
import java.awt.*;

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
}
