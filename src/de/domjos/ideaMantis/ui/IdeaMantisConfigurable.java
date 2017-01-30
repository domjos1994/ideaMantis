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

public class IdeaMantisConfigurable implements SearchableConfigurable {
    private JBTextField txtHostName, txtUserName, txtProjectName;
    private JTextArea txtProjectDescription;
    private JBPasswordField txtPassword;
    private JBCheckBox chkProjectEnabled;
    private java.awt.Label lblConnectionState;
    private JButton cmdTestConnection;
    private JPanel newProjectPanel;
    private ComboBox<String> cmbProjects, cmbNewProjectProjects, cmbProjectViewState;
    private int projectID = 0;
    private Project project;

    public IdeaMantisConfigurable(@NotNull Project project) {
        this.project = project;
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
        return "IdeaMantis-Settings";
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


        java.awt.Label lblHostName = new java.awt.Label("Host-Name");
        java.awt.Label lblUserName = new java.awt.Label("User-Name");
        java.awt.Label lblPassword = new java.awt.Label("Password");
        java.awt.Label lblProjects = new java.awt.Label("Choose Project");
        java.awt.Label lblProjectName = new java.awt.Label("Name" + "*");
        java.awt.Label lblProjectDescription = new java.awt.Label("Description");
        java.awt.Label lblProjectViewState = new java.awt.Label("State");
        this.lblConnectionState = new Label("Not Connected");
        this.changeConnectionLabel(null);

        this.cmdTestConnection = new JButton("Test Connection");
        this.cmdTestConnection.addActionListener(e -> {
            MantisSoapAPI connection = new MantisSoapAPI(ConnectionSettings.getInstance(this.project));
            String pwd = "";
            for(char ch : txtPassword.getPassword()) {
                pwd += ch;
            }
            if(this.changeConnectionLabel(connection.testConnection(txtHostName.getText(), txtUserName.getText(), pwd))) {
                java.util.List<MantisProject> projects = connection.getProjects();
                cmbProjects.removeAllItems();
                cmbNewProjectProjects.removeAllItems();
                for(MantisProject project : projects) {
                    this.addProjectToComboBox(project, ": ");
                }
                cmbNewProjectProjects.addItem("");
                cmbNewProjectProjects.setSelectedItem("");

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
        connPanel.setBorder(IdeBorderFactory.createTitledBorder("Connection"));

        JButton cmdCreateNewProject = new JButton("New Project");
        cmdCreateNewProject.setName("cmdCreateNewProject");

        JPanel projectPanel = new JPanel(new GridBagLayout());
        projectPanel.add(lblProjects, labelConstraint);
        projectPanel.add(cmbProjects, txtConstraint);
        projectPanel.add(cmdCreateNewProject, txtConstraint);
        projectPanel.setBorder(IdeBorderFactory.createTitledBorder("Project"));

        JButton cmdProjectAdd = new JButton(("Add Issue"));
        cmdProjectAdd.setName("cmdProjectAdd");

        this.cmbProjectViewState = new ComboBox<>();
        this.chkProjectEnabled = new JBCheckBox("enabled");

        this.cmbNewProjectProjects = new ComboBox<>();
        this.cmbNewProjectProjects.setVisible(false);

        newProjectPanel = new JPanel(new GridBagLayout());
        newProjectPanel.setVisible(false);
        newProjectPanel.add(lblProjectName, labelConstraint);
        newProjectPanel.add(txtProjectName, txtConstraint);
        newProjectPanel.add(lblProjects, labelConstraint);
        newProjectPanel.add(cmbNewProjectProjects, txtConstraint);
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
            int id = 0;
            if(!cmbNewProjectProjects.getSelectedItem().toString().equals("")) {
                if(cmbNewProjectProjects.getSelectedItem()!=null)
                    id = Integer.parseInt(cmbNewProjectProjects.getSelectedItem().toString().split(": ")[0].trim());

                MantisProject parent = getProject(id, connection.getProjects());
                if(parent!=null) {
                    parent.addSubProject(project);
                    project = parent;
                }
            }

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
        });
        newProjectPanel.setBorder(IdeBorderFactory.createTitledBorder("New Project"));

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

    private void addProjectToComboBox(MantisProject project, String splitter) {
        cmbProjects.addItem(project.getId() + splitter + project.getName());
        cmbNewProjectProjects.addItem(project.getId() + splitter + project.getName());
        for(MantisProject subProject : project.getSubProjects()) {
            this.addProjectToComboBox(subProject, splitter + "-> ");
        }
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
            this.lblConnectionState.setText("Not connected!");
            this.lblConnectionState.setForeground(JBColor.RED);
            return false;
        } else {
            this.lblConnectionState.setText(String.format("Connected as %s!", user.getName()));
            this.lblConnectionState.setForeground(JBColor.GREEN);
            return true;
        }
    }

    private MantisProject getProject(int id, java.util.List<MantisProject> projects) {
        for(MantisProject tmp : projects) {
            if(tmp.getId()==id) {
                return tmp;
            }
            return getProject(id, tmp.getSubProjects());
        }
        return null;
    }
}

