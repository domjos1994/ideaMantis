package de.domjos.ideaMantis.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import de.domjos.ideaMantis.model.MantisProject;
import de.domjos.ideaMantis.model.MantisUser;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class IdeaMantisConfigurable implements SearchableConfigurable {
    private JBTextField txtHostName, txtUserName, txtProjectName;
    private JTextArea txtProjectDescription;
    private JBPasswordField txtPassword;
    private JBCheckBox chkProjectEnabled;
    private java.awt.Label lblConnectionState;
    private JButton cmdTestConnection;
    private JPanel newProjectPanel;
    private ComboBox<String> cmbProjects, cmbProjectViewState;
    private int projectID = 0;
    private Project project;
    private ResourceBundle bundle;

    public IdeaMantisConfigurable(@NotNull Project project) {
        this.project = project;
        this.bundle = Helper.getBundle();
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
        return bundle.getString("settings.header");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        GridBagConstraints labelConstraint = new GridBagConstraints();
        labelConstraint.anchor = GridBagConstraints.EAST;
        labelConstraint.insets = JBUI.insets(5, 10);
        GridBagConstraints txtConstraint = new GridBagConstraints();
        txtConstraint.weightx = 2.0;
        txtConstraint.fill = GridBagConstraints.HORIZONTAL;
        txtConstraint.gridwidth = GridBagConstraints.REMAINDER;

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


        java.awt.Label lblHostName = new java.awt.Label(bundle.getString("settings.hostName"));
        java.awt.Label lblUserName = new java.awt.Label(bundle.getString("settings.userName"));
        java.awt.Label lblPassword = new java.awt.Label(bundle.getString("settings.password"));
        java.awt.Label lblProjects = new java.awt.Label(bundle.getString("settings.chooseProject"));
        java.awt.Label lblProjectName = new java.awt.Label(bundle.getString("person.name") + "*");
        java.awt.Label lblProjectDescription = new java.awt.Label(bundle.getString("descriptions.description"));
        java.awt.Label lblProjectViewState = new java.awt.Label(bundle.getString("settings.connection.project.viewState"));
        this.lblConnectionState = new Label(bundle.getString("settings.connection.notConnected"));
        this.changeConnectionLabel(null);

        this.cmdTestConnection = new JButton(bundle.getString("settings.connection.test"));
        this.cmdTestConnection.addActionListener(e -> {
            MantisSoapAPI connection = new MantisSoapAPI(ConnectionSettings.getInstance(this.project));
            String pwd = "";
            for(char ch : txtPassword.getPassword()) {
                pwd += ch;
            }
            if(this.changeConnectionLabel(connection.testConnection(txtHostName.getText(), txtUserName.getText(), pwd))) {
                java.util.List<MantisProject> projects = connection.getProjects();
                cmbProjects.removeAllItems();
                for(MantisProject project : projects) {
                    cmbProjects.addItem(project.getId() + ": " + project.getName());
                    for(MantisProject subProject : project.getSubProjects()) {
                        cmbProjects.addItem(subProject.getId() + ": -> " + subProject.getName());
                    }
                }

                cmbProjectViewState.removeAllItems();
                for(String item : connection.getEnum("view_states")) {
                    this.cmbProjectViewState.addItem(item);
                }
            } else {
                cmbProjects.removeAllItems();
            }
        });

        this.cmbProjects = new ComboBox<>();
        this.cmbProjects.addItemListener(event->{
            if(event.getItem()!=null) {
                projectID = Integer.parseInt(event.getItem().toString().split(":")[0]);
            }
        });

        JPanel connPanel = new JPanel(new GridBagLayout());
        connPanel.add(lblHostName, labelConstraint);
        connPanel.add(txtHostName, txtConstraint);
        connPanel.add(lblUserName, labelConstraint);
        connPanel.add(txtUserName, txtConstraint);
        connPanel.add(lblPassword, labelConstraint);
        connPanel.add(txtPassword, txtConstraint);
        connPanel.add(lblConnectionState, txtConstraint);
        connPanel.add(cmdTestConnection, txtConstraint);
        connPanel.setBorder(IdeBorderFactory.createTitledBorder(bundle.getString("settings.connection.header")));

        JButton cmdCreateNewProject = new JButton(bundle.getString("settings.connection.project.new"));
        cmdCreateNewProject.setName("cmdCreateNewProject");

        JPanel projectPanel = new JPanel(new GridBagLayout());
        projectPanel.add(lblProjects, labelConstraint);
        projectPanel.add(cmbProjects, txtConstraint);
        projectPanel.add(cmdCreateNewProject, txtConstraint);
        projectPanel.setBorder(IdeBorderFactory.createTitledBorder(bundle.getString("settings.connection.project")));

        JButton cmdProjectAdd = new JButton((bundle.getString("buttons.addIssue")));
        cmdProjectAdd.setName("cmdProjectAdd");

        this.cmbProjectViewState = new ComboBox<>();
        this.chkProjectEnabled = new JBCheckBox(bundle.getString("settings.connection.project.enabled"));

        newProjectPanel = new JPanel(new GridBagLayout());
        newProjectPanel.setVisible(false);
        newProjectPanel.add(lblProjectName, labelConstraint);
        newProjectPanel.add(txtProjectName, txtConstraint);
        newProjectPanel.add(lblProjectDescription, labelConstraint);
        newProjectPanel.add(txtProjectDescription, txtConstraint);
        newProjectPanel.add(lblProjectViewState, labelConstraint);
        newProjectPanel.add(cmbProjectViewState, txtConstraint);
        newProjectPanel.add(chkProjectEnabled, txtConstraint);
        newProjectPanel.add(cmdProjectAdd, txtConstraint);

        cmdProjectAdd.addActionListener(e -> {
            MantisProject project = new MantisProject(txtProjectName.getText());
            project.setDescription(txtProjectDescription.getText());
            if(cmbProjectViewState.getSelectedItem()!=null)
                project.setView_state(cmbProjectViewState.getSelectedItem().toString());
            project.setEnabled(chkProjectEnabled.isSelected());
            MantisSoapAPI connection = new MantisSoapAPI(ConnectionSettings.getInstance(this.project));
            if(!connection.addProject(project)) {
                Helper.printNotification(bundle.getString("message.error.header"), String.format(bundle.getString("message.cantAdd"), project.getName()), NotificationType.ERROR);
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
        });
        newProjectPanel.setBorder(IdeBorderFactory.createTitledBorder(bundle.getString("settings.connection.project.new")));

        cmdCreateNewProject.addActionListener(e -> newProjectPanel.setVisible(true));

        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 2.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        root.add(connPanel, constraints);
        constraints.weighty = 2.0;
        root.add(projectPanel,constraints);
        constraints.weighty = 2.0;
        root.add(newProjectPanel,constraints);
        return root;
    }

    @Override
    public boolean isModified() {
        ConnectionSettings connection = ConnectionSettings.getInstance(this.project);
        if(connection!=null) {

            StringBuilder buf = new StringBuilder();
            for(char ch : txtPassword.getPassword()) {
                buf.append(ch);
            }
            return
                    !connection.getHostName().equals(txtHostName.getText()) ||
                            !connection.getUserName().equals(txtUserName.getText()) ||
                            !connection.getPassword().equals(buf.toString()) ||
                            (connection.getProjectID()!=projectID && projectID!=0);
        } else {
            return false;
        }
    }

    @Override
    public void apply() throws ConfigurationException {
        ConnectionSettings connection = ConnectionSettings.getInstance(this.project);
        if(connection!=null) {
            connection.setHostName(txtHostName.getText());
            connection.setUserName(txtUserName.getText());
            StringBuilder buf = new StringBuilder();
            for(char ch : txtPassword.getPassword()) {
                buf.append(ch);
            }
            connection.setPassword(buf.toString());
            connection.setProjectID(projectID);
        }
    }

    @Override
    public void reset() {
        ConnectionSettings connection = ConnectionSettings.getInstance(this.project);
        txtHostName.setText(connection.getHostName());
        txtUserName.setText(connection.getUserName());
        txtPassword.setText(connection.getPassword());
        cmdTestConnection.doClick();
        for(int i = 0; i<=cmbProjects.getItemCount()-1; i++) {
            if(Integer.parseInt(cmbProjects.getItemAt(i).split(":")[0])==connection.getProjectID()) {
                cmbProjects.setSelectedIndex(i);
            }
        }
    }

    @Override
    public void disposeUIResources() {
        UIUtil.dispose(txtHostName);
        UIUtil.dispose(txtUserName);
        UIUtil.dispose(txtPassword);
        UIUtil.dispose(cmbProjects);
        UIUtil.dispose(cmdTestConnection);
    }

    private boolean changeConnectionLabel(MantisUser user) {
        if(user==null) {
            this.lblConnectionState.setText(bundle.getString("settings.connection.notConnected"));
            this.lblConnectionState.setForeground(JBColor.RED);
            return false;
        } else {
            this.lblConnectionState.setText(String.format(bundle.getString("settings.connection.connected"), user.getName()));
            this.lblConnectionState.setForeground(JBColor.GREEN);
            return true;
        }
    }
}

