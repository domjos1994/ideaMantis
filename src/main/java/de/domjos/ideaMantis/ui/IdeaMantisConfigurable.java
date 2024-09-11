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

package de.domjos.ideaMantis.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.*;
import com.intellij.util.ui.UIUtil;
import de.domjos.ideaMantis.custom.ColorPickerWithDefault;
import de.domjos.ideaMantis.lang.Lang;
import de.domjos.ideaMantis.utils.PanelCreator;
import de.domjos.ideaMantis.model.MantisProject;
import de.domjos.ideaMantis.model.MantisUser;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.soap.ObjectRef;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class IdeaMantisConfigurable implements SearchableConfigurable {
    private JComponent component;
    private JBTextField txtHostName, txtUserName, txtProjectName, txtIssuesPerPage, txtReloadTime;
    private JTextArea txtProjectDescription;
    private JBPasswordField txtPassword;
    private JBCheckBox chkProjectEnabled, chkFastTrackEnabled, chkReloadAutomatically;
    private JBLabel lblConnectionState;
    private JButton cmdTestConnection, cmdCreateNewProject;
    private ColorPickerWithDefault newCP, feedbackCP, acknowledgedCP, confirmedCP, assignedCP, resolvedCP, closedCP;
    private JBPanelWithEmptyText newProjectPanel, projectPanel;
    private ComboBox<String> cmbProjects, cmbProjectViewState;
    private int projectID = 0;
    private boolean isConnected = false;

    private final ConnectionSettings settings;
    private final Task.Backgroundable task;

    public IdeaMantisConfigurable(@NotNull Project project) {
        this.settings = ConnectionSettings.getInstance(project);
        this.task = initSaveTask(project);
    }

    @NotNull
    @Override
    public String getId() {
        return getClass().getName();
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return Lang.NAME;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        this.projectPanel = new JBPanelWithEmptyText(new GridBagLayout());
        this.txtHostName = new JBTextField();
        this.txtHostName.setName("txtHostName");
        this.txtUserName = new JBTextField();
        this.txtUserName.setName("txtUserName");
        this.txtPassword = new JBPasswordField();
        this.txtPassword.setName("txtPassword");
        this.txtProjectName = new JBTextField();
        this.txtProjectName.setName("txtProjectName");
        this.txtProjectDescription = new JTextArea();
        this.txtProjectDescription.setName("txtProjectDescription");

        this.txtIssuesPerPage = new JBTextField();
        this.cmdCreateNewProject = new JButton(Lang.SETTINGS_PROJECT_NEW);
        this.cmbProjects = new ComboBox<>();
        this.chkFastTrackEnabled = new JBCheckBox(Lang.SETTINGS_FAST_TRACK);
        this.chkReloadAutomatically = new JBCheckBox(Lang.SETTINGS_RELOAD);
        this.txtReloadTime = new JBTextField();
        this.lblConnectionState = new JBLabel(Lang.SETTINGS_CONNECT_NO);

        this.txtIssuesPerPage.setName("txtIssuesPerPage");
        this.txtIssuesPerPage.setText("-1");

        JBLabel lblHostName = new JBLabel(Lang.SETTINGS_HOST);
        JBLabel lblUserName = new JBLabel(Lang.SETTINGS_USER);
        JBLabel lblPassword = new JBLabel(Lang.SETTINGS_PWD);
        JBLabel lblProjects = new JBLabel(Lang.SETTINGS_PROJECT_CHOOSE);
        JBLabel lblIssuesPerPage = new JBLabel(Lang.SETTINGS_PAGE);
        JBLabel lblProjectName = new JBLabel(Lang.COLUMN_NAME + Lang.VAL_STAR);
        JBLabel lblProjectDescription = new JBLabel(Lang.COLUMN_DESCRIPTION);
        JBLabel lblProjectFastTrack = new JBLabel(Lang.SETTINGS_FAST_TRACK_DESCR);
        JBLabel lblProjectViewState = new JBLabel(Lang.COLUMN_STATUS);

        this.changeConnectionLabel(null);


        this.cmdTestConnection = new JButton(Lang.SETTINGS_CONNECT_TEST);
        this.cmdTestConnection.addActionListener(e -> {
            Task.Modal task = new Task.Modal(Helper.getProject(), Lang.SETTINGS_CONNECT_TEST, false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try {
                        String oldSettings = temporarilyChangeSettings();

                        MantisSoapAPI connection = new MantisSoapAPI(settings);
                        if(changeConnectionLabel(connection.testConnection())) {
                            java.util.List<MantisProject> projects = connection.getProjects();
                            cmbProjects.removeAllItems();
                            for(MantisProject project : projects) {
                                addProjectToComboBox(project, ": ");
                            }
                            cmbProjectViewState.removeAllItems();
                            for(ObjectRef item : connection.getEnum("view_states")) {
                                cmbProjectViewState.addItem(item.getName());
                            }

                            for(int i = 0; i<=cmbProjects.getItemCount()-1; i++) {
                                if(cmbProjects.getItemAt(i).startsWith(settings.getProjectID() + ":")) {
                                    cmbProjects.setSelectedItem(cmbProjects.getItemAt(i));
                                    break;
                                }
                            }
                        } else {
                            cmbProjects.removeAllItems();
                        }

                        temporarilyChangeSettingsBack(oldSettings);
                    } finally {
                        if(cmdTestConnection != null) {
                            if(cmdTestConnection.getRootPane() != null) {
                                cmdTestConnection.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            }
                        }
                    }
                }
            };
            if(this.cmdTestConnection != null) {
                if(this.cmdTestConnection.getRootPane() != null) {
                    this.cmdTestConnection.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                }
            }
            ProgressManager.getInstance().run(task);
        });

        JBPanelWithEmptyText connPanel = new JBPanelWithEmptyText(new GridBagLayout());
        connPanel.add(lblHostName, PanelCreator.getCustomConstraint(0, 0, 0.2f, -1));
        connPanel.add(this.txtHostName, PanelCreator.getCustomConstraint(0, 1, 0.8f, -1));
        connPanel.add(lblUserName, PanelCreator.getCustomConstraint(1, 0, 0.2f, -1));
        connPanel.add(this.txtUserName, PanelCreator.getCustomConstraint(1, 1, 0.8f, -1));
        connPanel.add(lblPassword, PanelCreator.getCustomConstraint(2, 0, 0.2f, -1));
        connPanel.add(this.txtPassword, PanelCreator.getCustomConstraint(2, 1, 0.8f, -1));
        connPanel.add(this.lblConnectionState, PanelCreator.getCustomConstraint(3, 0, 1.0f, 3));
        connPanel.add(this.cmdTestConnection, PanelCreator.getCustomConstraint(4, 0, 1.0f, 3));
        connPanel.setBorder(IdeBorderFactory.createTitledBorder("Connection"));

        this.cmdCreateNewProject.setName("cmdCreateNewProject");


        this.cmbProjects.addItemListener(event->{
            if(event.getItem()!=null) {
                projectID = Integer.parseInt(event.getItem().toString().split(":")[0]);
            }
        });

        this.chkReloadAutomatically.addActionListener(e -> this.txtReloadTime.setEnabled(this.chkReloadAutomatically.isSelected()));
        this.txtReloadTime.setName("txtReloadTime");
        this.txtReloadTime.setToolTipText(Lang.SETTINGS_RELOAD_HEADER);
        this.txtReloadTime.setText("300");
        this.txtReloadTime.setEnabled(false);



        this.projectPanel.add(lblProjects, PanelCreator.getCustomConstraint(0, 0, 0.2f, -1));
        this.projectPanel.add(this.cmbProjects, PanelCreator.getCustomConstraint(0, 1, 0.8f, -1));
        this.projectPanel.add(lblIssuesPerPage, PanelCreator.getCustomConstraint(1, 0, 0.2f, -1));
        this.projectPanel.add(this.txtIssuesPerPage, PanelCreator.getCustomConstraint(1, 1, 0.8f, -1));
        this.projectPanel.add(lblProjectFastTrack, PanelCreator.getCustomConstraint(2, 0, 0.2f, -1));
        this.projectPanel.add(this.chkFastTrackEnabled, PanelCreator.getCustomConstraint(2, 1, 0.8f, -1));
        this.projectPanel.add(this.chkReloadAutomatically, PanelCreator.getCustomConstraint(3, 0, 0.2f, -1));
        this.projectPanel.add(this.txtReloadTime, PanelCreator.getCustomConstraint(3, 1, 0.8f, -1));
        this.projectPanel.add(this.cmdCreateNewProject, PanelCreator.getCustomConstraint(4, 0, 1.0f, 2));
        this.projectPanel.setBorder(IdeBorderFactory.createTitledBorder(Lang.SETTINGS_PROJECT_LABEL));

        JButton cmdProjectAdd = new JButton((Lang.SETTINGS_PROJECT_ADD));
        cmdProjectAdd.setName("cmdProjectAdd");

        this.cmbProjectViewState = new ComboBox<>();
        this.chkProjectEnabled = new JBCheckBox(Lang.SETTINGS_ENABLED);
        this.newProjectPanel = new JBPanelWithEmptyText(new GridBagLayout());
        this.newProjectPanel.add(lblProjectName, PanelCreator.getCustomConstraint(0, 0, 0.2f, -1));
        this.newProjectPanel.add(this.txtProjectName, PanelCreator.getCustomConstraint(0, 1, 0.8f, -1));
        this.newProjectPanel.add(lblProjectDescription, PanelCreator.getCustomConstraint(1, 0, 0.2f, -1));
        this.newProjectPanel.add(this.txtProjectDescription, PanelCreator.getCustomConstraint(1, 1, 0.8f, -1));
        this.newProjectPanel.add(lblProjectViewState, PanelCreator.getCustomConstraint(2, 0, 0.2f, -1));
        this.newProjectPanel.add(this.cmbProjectViewState, PanelCreator.getCustomConstraint(2, 1, 0.8f, -1));
        this.newProjectPanel.add(this.chkProjectEnabled, PanelCreator.getCustomConstraint(3, 0, .0f, 2));
        this.newProjectPanel.add(cmdProjectAdd, PanelCreator.getCustomConstraint(4, 0, 1.0f, 2));
        this.newProjectPanel.setVisible(false);

        JBPanelWithEmptyText colorPanel = new JBPanelWithEmptyText(new GridBagLayout());
        colorPanel.setBorder(IdeBorderFactory.createTitledBorder(Lang.SETTINGS_COLORS));

        this.newCP = new ColorPickerWithDefault(Lang.SETTINGS_COLORS_NEW, Lang.SETTINGS_COLORS_NEW_DEFAULT, Lang.SETTINGS_COLORS_NEW_DEFAULT);
        this.feedbackCP = new ColorPickerWithDefault(Lang.SETTINGS_COLORS_FEEDBACK, Lang.SETTINGS_COLORS_FEEDBACK_DEFAULT, Lang.SETTINGS_COLORS_FEEDBACK_DEFAULT);
        this.acknowledgedCP = new ColorPickerWithDefault(Lang.SETTINGS_COLORS_ACKNOWLEDGED, Lang.SETTINGS_COLORS_ACKNOWLEDGED_DEFAULT, Lang.SETTINGS_COLORS_ACKNOWLEDGED_DEFAULT);
        this.confirmedCP = new ColorPickerWithDefault(Lang.SETTINGS_COLORS_CONFIRMED, Lang.SETTINGS_COLORS_CONFIRMED_DEFAULT, Lang.SETTINGS_COLORS_CONFIRMED_DEFAULT);
        this.assignedCP = new ColorPickerWithDefault(Lang.SETTINGS_COLORS_ASSIGNED, Lang.SETTINGS_COLORS_ASSIGNED_DEFAULT, Lang.SETTINGS_COLORS_ASSIGNED_DEFAULT);
        this.resolvedCP = new ColorPickerWithDefault(Lang.SETTINGS_COLORS_RESOLVED, Lang.SETTINGS_COLORS_RESOLVED_DEFAULT, Lang.SETTINGS_COLORS_RESOLVED_DEFAULT);
        this.closedCP = new ColorPickerWithDefault(Lang.SETTINGS_COLORS_CLOSED, Lang.SETTINGS_COLORS_CLOSED_DEFAULT, Lang.SETTINGS_COLORS_CLOSED_DEFAULT);
        colorPanel.add(this.newCP, PanelCreator.getCustomConstraint(0, 0, 1.0f, -1));
        colorPanel.add(this.feedbackCP, PanelCreator.getCustomConstraint(1, 0, 1.0f, -1));
        colorPanel.add(this.acknowledgedCP, PanelCreator.getCustomConstraint(2, 0, 1.0f, -1));
        colorPanel.add(this.confirmedCP, PanelCreator.getCustomConstraint(3, 0, 1.0f, -1));
        colorPanel.add(this.assignedCP, PanelCreator.getCustomConstraint(4, 0, 1.0f, -1));
        colorPanel.add(this.resolvedCP, PanelCreator.getCustomConstraint(5, 0, 1.0f, -1));
        colorPanel.add(this.closedCP, PanelCreator.getCustomConstraint(6, 0, 1.0f, -1));

        cmdProjectAdd.addActionListener(e -> {
            String oldSettings = this.temporarilyChangeSettings();

            MantisProject project = new MantisProject(txtProjectName.getText());
            project.setDescription(txtProjectDescription.getText());
            if(cmbProjectViewState.getSelectedItem()!=null)
                project.setView_state(cmbProjectViewState.getSelectedItem().toString());
            project.setEnabled(chkProjectEnabled.isSelected());
            MantisSoapAPI connection = new MantisSoapAPI(this.settings);

            if(!connection.addProject(project)) {
                Helper.printNotification(Lang.ERROR_HEADER, String.format(Lang.ERROR_UPDATE, project.getName()), NotificationType.ERROR);
            } else {
                txtProjectDescription.setText("");
                txtProjectName.setText("");
                cmbProjectViewState.setSelectedIndex(0);
                newProjectPanel.setVisible(false);
                cmdTestConnection.doClick();
                for(int i = 0; i<=cmbProjects.getItemCount()-1; i++) {
                    if(cmbProjects.getItemAt(i).endsWith(project.getName())) {
                        cmbProjects.setSelectedItem(cmbProjects.getItemAt(i));
                        break;
                    }
                }
            }

            this.temporarilyChangeSettingsBack(oldSettings);
        });
        newProjectPanel.setBorder(IdeBorderFactory.createTitledBorder(Lang.SETTINGS_PROJECT_NEW));

        cmdCreateNewProject.addActionListener(e -> newProjectPanel.setVisible(!newProjectPanel.isVisible()));

        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = PanelCreator.getRootConstraint();

        root.add(connPanel, constraints);
        constraints.weighty = 2.0;
        root.add(projectPanel,constraints);
        constraints.weighty = 2.0;
        root.add(newProjectPanel,constraints);
        constraints.weighty = 2.0;
        root.add(colorPanel, constraints);
        this.component = root;
        return root;
    }

    private void addProjectToComboBox(MantisProject project, String splitter) {
        cmbProjects.addItem(project.getId() + splitter + project.getName());
        for(MantisProject subProject : project.getSubProjects()) {
            this.addProjectToComboBox(subProject, splitter + "-> ");
        }
    }

    @Override
    public boolean isModified() {
        if(this.settings!=null) {

            StringBuilder buf = new StringBuilder();
            for(char ch : txtPassword.getPassword()) {
                buf.append(ch);
            }

            return
                !this.settings.getHostName().equals(txtHostName.getText()) ||
                !this.settings.getUserName().equals(txtUserName.getText()) ||
                !this.settings.getPassword().contentEquals(buf) ||
                !String.valueOf(this.settings.getItemsPerPage()).equals(txtIssuesPerPage.getText()) ||
                (this.settings.getProjectID()!=projectID && projectID!=0) ||
                !this.settings.isFastTrack()==chkFastTrackEnabled.isSelected() ||
                !this.settings.isReload()==chkReloadAutomatically.isSelected() ||
                !String.valueOf(this.settings.getReloadTime()).equals(txtReloadTime.getText()) ||
                !this.settings.getColorNew().equals(this.newCP.getColor()) ||
                !this.settings.getColorFeedback().equals(this.feedbackCP.getColor()) ||
                !this.settings.getColorAcknowledged().equals(this.acknowledgedCP.getColor()) ||
                !this.settings.getColorConfirmed().equals(this.confirmedCP.getColor()) ||
                !this.settings.getColorAssigned().equals(this.assignedCP.getColor()) ||
                !this.settings.getColorResolved().equals(this.resolvedCP.getColor()) ||
                !this.settings.getColorClosed().equals(this.closedCP.getColor());
        } else {
            return false;
        }
    }

    @Override
    public void apply() {
        if(this.settings!=null) {
            this.component.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            ProgressManager.getInstance().run(this.task);
        }
    }

    @Override
    public void reset() {
        txtHostName.setText(this.settings.getHostName());
        txtUserName.setText(this.settings.getUserName());
        txtPassword.setText(this.settings.getPassword());
        chkFastTrackEnabled.setSelected(this.settings.isFastTrack());
        chkReloadAutomatically.setSelected(this.settings.isReload());
        txtReloadTime.setEnabled(chkReloadAutomatically.isSelected());
        txtReloadTime.setText(String.valueOf(this.settings.getReloadTime()));
        txtIssuesPerPage.setText(String.valueOf(this.settings.getItemsPerPage()));
        newCP.setColor(this.settings.getColorNew());
        feedbackCP.setColor(this.settings.getColorFeedback());
        acknowledgedCP.setColor(this.settings.getColorAcknowledged());
        confirmedCP.setColor(this.settings.getColorConfirmed());
        assignedCP.setColor(this.settings.getColorAssigned());
        resolvedCP.setColor(this.settings.getColorResolved());
        closedCP.setColor(this.settings.getColorClosed());
        cmdTestConnection.doClick();
        for(int i = 0; i<=cmbProjects.getItemCount()-1; i++) {
            if(Integer.parseInt(cmbProjects.getItemAt(i).split(":")[0])==this.settings.getProjectID()) {
                cmbProjects.setSelectedIndex(i);
            }
        }
    }

    @Override
    public void disposeUIResources() {
        UIUtil.dispose(txtHostName);
        UIUtil.dispose(txtUserName);
        UIUtil.dispose(txtPassword);
        UIUtil.dispose(txtIssuesPerPage);
        UIUtil.dispose(cmbProjects);
        UIUtil.dispose(cmdTestConnection);
        UIUtil.dispose(chkFastTrackEnabled);
        UIUtil.dispose(chkReloadAutomatically);
        UIUtil.dispose(txtReloadTime);
        UIUtil.dispose(newCP);
        UIUtil.dispose(feedbackCP);
        UIUtil.dispose(acknowledgedCP);
        UIUtil.dispose(confirmedCP);
        UIUtil.dispose(assignedCP);
        UIUtil.dispose(resolvedCP);
        UIUtil.dispose(closedCP);
    }

    private boolean changeConnectionLabel(MantisUser user) {
        if(user==null) {
            this.lblConnectionState.setText(Lang.SETTINGS_CONNECT_NO);
            this.lblConnectionState.setForeground(JBColor.RED);
            this.isConnected = false;
        } else {
            this.lblConnectionState.setText(String.format(Lang.SETTINGS_CONNECT_SUCCESS, user.getUserName()));
            this.lblConnectionState.setForeground(JBColor.GREEN);
            this.isConnected = true;

        }
        this.hideControls();
        return this.isConnected;
    }

    private String temporarilyChangeSettings() {
        StringBuilder pwd = new StringBuilder();
        for(char ch : txtPassword.getPassword()) {
            pwd.append(ch);
        }

        String  hostName = this.settings.getHostName(),
                userName = this.settings.getUserName(),
                password = this.settings.getPassword();
        this.settings.setHostName(txtHostName.getText().trim());
        this.settings.setUserName(txtUserName.getText().trim());
        this.settings.setPassword(pwd.toString());

        return String.format("%s;-;%s;-;%s", hostName, userName, password);
    }

    private void temporarilyChangeSettingsBack(String oldSettings) {
        String[] data = oldSettings.split(";-;");
        if (data.length >= 3) {
            this.settings.setHostName(data[0].trim());
            this.settings.setUserName(data[1].trim());
            this.settings.setPassword(data[2].trim());
        }
    }

    private void hideControls() {
        this.projectPanel.setEnabled(this.isConnected);
        this.cmbProjects.setEnabled(this.isConnected);
        this.txtIssuesPerPage.setEnabled(this.isConnected);
        this.chkFastTrackEnabled.setEnabled(this.isConnected);
        this.chkReloadAutomatically.setEnabled(this.isConnected);
        this.txtReloadTime.setEnabled(this.isConnected);
        this.cmdCreateNewProject.setEnabled(this.isConnected);
    }

    private Task.Backgroundable initSaveTask(Project project) {
        return new Task.Backgroundable(project, Lang.RELOAD_DATA) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    settings.setHostName(txtHostName.getText());
                    settings.setUserName(txtUserName.getText());
                    StringBuilder buf = new StringBuilder();
                    for(char ch : txtPassword.getPassword()) {
                        buf.append(ch);
                    }
                    settings.setPassword(buf.toString());
                    int itemsPerPage;
                    try {
                        itemsPerPage = Integer.parseInt(txtIssuesPerPage.getText());
                    } catch (Exception ex) {
                        itemsPerPage = -1;
                    }
                    settings.setItemsPerPage(itemsPerPage);
                    settings.setProjectID(projectID);
                    Helper.reloadToolWindow(Lang.RELOAD_COMBO_BOXES);
                    settings.setFastTrack(chkFastTrackEnabled.isSelected());
                    settings.setReload(chkReloadAutomatically.isSelected());
                    settings.setColorNew(newCP.getColor());
                    settings.setColorFeedback(feedbackCP.getColor());
                    settings.setColorAcknowledged(acknowledgedCP.getColor());
                    settings.setColorConfirmed(confirmedCP.getColor());
                    settings.setColorAssigned(assignedCP.getColor());
                    settings.setColorResolved(resolvedCP.getColor());
                    settings.setColorClosed(closedCP.getColor());

                    int reloadTime;
                    try {
                        reloadTime = Integer.parseInt(txtReloadTime.getText());
                    } catch (Exception ex) {
                        reloadTime = 300;
                    }
                    settings.setReloadTime(reloadTime);
                } catch (Exception ex) {
                    Helper.printException(ex);
                } finally {
                    component.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
    }
}

