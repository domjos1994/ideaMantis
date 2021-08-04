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
import de.domjos.ideaMantis.utils.PanelCreator;
import de.domjos.ideaMantis.model.MantisProject;
import de.domjos.ideaMantis.model.MantisUser;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.soap.ObjectRef;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.Nls;
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
    private JButton cmdTestConnection;
    private JBPanelWithEmptyText newProjectPanel;
    private ComboBox<String> cmbProjects, cmbProjectViewState;
    private int projectID = 0;

    private final ConnectionSettings settings;
    private final Task.Backgroundable task;

    public IdeaMantisConfigurable(@NotNull Project project) {
        this.settings = ConnectionSettings.getInstance(project);
        this.task = new Task.Backgroundable(project, "Load data...") {
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
                    Helper.reloadToolWindow(IdeaMantisIssues.RELOAD_COMBOBOXES);
                    settings.setFastTrack(chkFastTrackEnabled.isSelected());
                    settings.setReload(chkReloadAutomatically.isSelected());

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

    @Nls
    @Override
    public String getDisplayName() {
        return "IdeaMantis";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
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
        this.txtIssuesPerPage.setName("txtIssuesPerPage");
        this.txtIssuesPerPage.setText("-1");

        JBLabel lblHostName = new JBLabel("Host-Name");
        JBLabel lblUserName = new JBLabel("User-Name");
        JBLabel lblPassword = new JBLabel("Password");
        JBLabel lblProjects = new JBLabel("Choose Project");
        JBLabel lblIssuesPerPage = new JBLabel("Issues per page (-1 for all)");
        JBLabel lblProjectName = new JBLabel("Name" + "*");
        JBLabel lblProjectDescription = new JBLabel("Description");
        JBLabel lblProjectFastTrack = new JBLabel("Copy and Paste Bugs faster");
        JBLabel lblProjectViewState = new JBLabel("State");
        this.lblConnectionState = new JBLabel("Not Connected");
        this.changeConnectionLabel(null);


        this.cmdTestConnection = new JButton("Test Connection");
        this.cmdTestConnection.addActionListener(e -> {
            Task.Modal task = new Task.Modal(Helper.getProject(), "Test Connection!", false) {
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

        this.chkFastTrackEnabled = new JBCheckBox("Enable Fast-Track-Mode");

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

        JButton cmdCreateNewProject = new JButton("New Project");
        cmdCreateNewProject.setName("cmdCreateNewProject");

        this.cmbProjects = new ComboBox<>();
        this.cmbProjects.addItemListener(event->{
            if(event.getItem()!=null) {
                projectID = Integer.parseInt(event.getItem().toString().split(":")[0]);
            }
        });

        this.chkReloadAutomatically = new JBCheckBox("Reload bugs after some time!");
        this.chkReloadAutomatically.addActionListener(e -> this.txtReloadTime.setEnabled(this.chkReloadAutomatically.isSelected()));
        this.txtReloadTime = new JBTextField();
        this.txtReloadTime.setName("txtReloadTime");
        this.txtReloadTime.setToolTipText("Time (in s)");
        this.txtReloadTime.setText("300");
        this.txtReloadTime.setEnabled(false);



        JBPanelWithEmptyText projectPanel = new JBPanelWithEmptyText(new GridBagLayout());
        projectPanel.add(lblProjects, PanelCreator.getCustomConstraint(0, 0, 0.2f, -1));
        projectPanel.add(this.cmbProjects, PanelCreator.getCustomConstraint(0, 1, 0.8f, -1));
        projectPanel.add(lblIssuesPerPage, PanelCreator.getCustomConstraint(1, 0, 0.2f, -1));
        projectPanel.add(this.txtIssuesPerPage, PanelCreator.getCustomConstraint(1, 1, 0.8f, -1));
        projectPanel.add(lblProjectFastTrack, PanelCreator.getCustomConstraint(2, 0, 0.2f, -1));
        projectPanel.add(this.chkFastTrackEnabled, PanelCreator.getCustomConstraint(2, 1, 0.8f, -1));
        projectPanel.add(this.chkReloadAutomatically, PanelCreator.getCustomConstraint(3, 0, 0.2f, -1));
        projectPanel.add(this.txtReloadTime, PanelCreator.getCustomConstraint(3, 1, 0.8f, -1));
        projectPanel.add(cmdCreateNewProject, PanelCreator.getCustomConstraint(4, 0, 1.0f, 2));
        projectPanel.setBorder(IdeBorderFactory.createTitledBorder("Project"));

        JButton cmdProjectAdd = new JButton(("Add Project"));
        cmdProjectAdd.setName("cmdProjectAdd");

        this.cmbProjectViewState = new ComboBox<>();
        this.chkProjectEnabled = new JBCheckBox("enabled");

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

        cmdProjectAdd.addActionListener(e -> {
            String oldSettings = this.temporarilyChangeSettings();

            MantisProject project = new MantisProject(txtProjectName.getText());
            project.setDescription(txtProjectDescription.getText());
            if(cmbProjectViewState.getSelectedItem()!=null)
                project.setView_state(cmbProjectViewState.getSelectedItem().toString());
            project.setEnabled(chkProjectEnabled.isSelected());
            MantisSoapAPI connection = new MantisSoapAPI(this.settings);

            if(!connection.addProject(project)) {
                Helper.printNotification("Exception", String.format("Can't add %s", project.getName()), NotificationType.ERROR);
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
        newProjectPanel.setBorder(IdeBorderFactory.createTitledBorder("New Project"));

        cmdCreateNewProject.addActionListener(e -> newProjectPanel.setVisible(true));

        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = PanelCreator.getRootConstraint();

        root.add(connPanel, constraints);
        constraints.weighty = 2.0;
        root.add(projectPanel,constraints);
        constraints.weighty = 2.0;
        root.add(newProjectPanel,constraints);
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
                !this.settings.getPassword().equals(buf.toString()) ||
                !String.valueOf(this.settings.getItemsPerPage()).equals(txtIssuesPerPage.getText()) ||
                (this.settings.getProjectID()!=projectID && projectID!=0) ||
                !this.settings.isFastTrack()==chkFastTrackEnabled.isSelected() ||
                !this.settings.isReload()==chkReloadAutomatically.isSelected() ||
                !String.valueOf(this.settings.getReloadTime()).equals(txtReloadTime.getText());
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
    }

    private boolean changeConnectionLabel(MantisUser user) {
        if(user==null) {
            this.lblConnectionState.setText("Not connected!");
            this.lblConnectionState.setForeground(JBColor.RED);
            return false;
        } else {
            this.lblConnectionState.setText(String.format("Connected as %s!", user.getUserName()));
            this.lblConnectionState.setForeground(JBColor.GREEN);
            return true;
        }
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
        if(data.length>=3) {
            this.settings.setHostName(data[0].trim());
            this.settings.setUserName(data[1].trim());
            this.settings.setPassword(data[2].trim());
        }
    }
}

