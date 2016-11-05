package de.domjos.ideaMantis.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import de.domjos.ideaMantis.model.IssueAttachment;
import de.domjos.ideaMantis.model.IssueNote;
import de.domjos.ideaMantis.model.MantisIssue;
import de.domjos.ideaMantis.model.MantisUser;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.utils.Helper;
import org.jdesktop.swingx.calendar.DateSelectionModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.util.Date;
import java.util.List;

public class ideaMantisIssues implements ToolWindowFactory {
    private Project project;
    private ConnectionSettings settings;
    private MantisIssue currentIssue;

    private JList<String> lvIssueAttachments, lvIssueNotes;

    private JPanel pnlContent, pnlIssueBasics, pnlIssueDescriptions, pnlIssueNotes, pnlIssueAttachments, pnlMain;

    private JButton cmdIssueNew, cmdIssueEdit, cmdIssueDelete, cmdIssueSave,cmdIssueAbort;
    private JButton cmdIssueNoteNew, cmdIssueNoteEdit, cmdIssueNoteDelete, cmdIssueNoteSave, cmdIssueNoteAbort;
    private JButton cmdIssueAttachmentSearch, cmdIssueAttachmentNew, cmdIssueAttachmentEdit, cmdIssueAttachmentDelete;
    private JButton cmdIssueAttachmentSave, cmdIssueAttachmentAbort;
    private JButton cmdReload;

    private JToolBar tlbMain;
    private JTabbedPane tabbedPane1;

    private JTextField txtIssueSummary, txtIssueDate, txtIssueReporterName, txtIssueReporterEMail;

    private JComboBox<String> cmbIssueReporterName, cmbIssueNoteReporterUser;
    private JComboBox<String> cmbIssueTargetVersion, cmbIssueFixedInVersion;
    private JComboBox<String> cmbIssuePriority, cmbIssueSeverity, cmbIssueStatus, cmbIssueCategory;

    private JTextArea txtIssueDescription, txtIssueStepsToReproduce, txtIssueAdditionalInformation, txtIssueNoteText;

    private JTextField txtIssueNoteReporterName, txtIssueNoteReporterEMail, txtIssueNoteDate, txtIssueAttachmentFileName;
    private JTextField txtIssueAttachmentSize;
    private JTable tblIssues;

    private ToolWindow toolWindow;


    public ideaMantisIssues() {
        this.controlComponents(false);
        tblIssues.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableModel tblIssueModel = this.addColumnsToTable();

        cmdReload.addActionListener(e -> {
            settings = ConnectionSettings.getInstance(project);
            if(!settings.validateSettings()) {
                Helper.printNotification("Settings incorrect!", "Your Settings are <b>incorrect</b>!<br/>Please change them in the ConnectionSettings!",NotificationType.ERROR);
                tblIssues.removeAll();
                this.controlComponents(false);
            } else {
                tblIssues.removeAll();
                for(MantisIssue issue : new MantisSoapAPI(this.settings).getIssues(this.settings.getProjectID())) {
                    tblIssueModel.addRow(new Object[]{issue.getId(), issue.getSummary(), issue.getStatus()});
                }
                tblIssues.setModel(tblIssueModel);
                this.loadComboBoxes();
                cmdIssueNew.setEnabled(true);
            }
        });

        tblIssues.getSelectionModel().addListSelectionListener(e -> {
            if(tblIssues.getSelectedRow()!=-1) {
                int id = Integer.parseInt(tblIssueModel.getValueAt(tblIssues.getSelectedRow(), 0).toString());
                MantisSoapAPI api = new MantisSoapAPI(this.settings);
                MantisIssue issue = api.getIssue(id);
                this.currentIssue = issue;
                cmdIssueEdit.setEnabled(true);
                cmdIssueDelete.setEnabled(true);
                txtIssueSummary.setText(issue.getSummary());
                txtIssueDate.setText(issue.getDate_submitted());
                txtIssueAdditionalInformation.setText(issue.getAdditional_information());
                txtIssueDescription.setText(issue.getDescription());
                txtIssueStepsToReproduce.setText(issue.getSteps_to_reproduce());
                if(issue.getReporter()!=null) {
                    cmbIssueReporterName.setSelectedItem(issue.getReporter().getUserName());
                    txtIssueReporterName.setText(issue.getReporter().getName());
                    txtIssueReporterEMail.setText(issue.getReporter().getEmail());
                }
                cmbIssueCategory.setSelectedItem(issue.getCategory());
                cmbIssueTargetVersion.setSelectedItem(issue.getTarget_version());
                cmbIssueFixedInVersion.setSelectedItem(issue.getFixed_in_version());
                cmbIssuePriority.setSelectedItem(issue.getPriority());
                cmbIssueSeverity.setSelectedItem(issue.getSeverity());
                cmbIssueStatus.setSelectedItem(issue.getStatus());

                lvIssueNotes.removeAll();
                DefaultListModel<String> noteModel = new DefaultListModel<>();
                for(IssueNote note : issue.getIssueNoteList()) {
                    noteModel.addElement(note.getText());
                }
                lvIssueNotes.setModel(noteModel);

                lvIssueAttachments.removeAll();
                DefaultListModel<String> attachmentModel = new DefaultListModel<>();
                for(IssueAttachment attachment : issue.getIssueAttachmentList()) {
                    attachmentModel.addElement(attachment.getFilename());
                }
                lvIssueAttachments.setModel(attachmentModel);
            }
        });

        cmdIssueNew.addActionListener(e -> {
            this.controlComponents(true);
            cmdIssueSave.setEnabled(true);
            cmdIssueAbort.setEnabled(true);
            cmdIssueNoteNew.setEnabled(true);
            cmdIssueAttachmentNew.setEnabled(true);
            tblIssues.getSelectionModel().clearSelection();
            this.resetComponents();
            txtIssueDate.setText(new Date().toString());
        });

        cmdIssueEdit.addActionListener(e -> {
            this.controlComponents(true);
            cmdIssueSave.setEnabled(true);
            cmdIssueAbort.setEnabled(true);
            cmdIssueNoteNew.setEnabled(true);
            cmdIssueAttachmentNew.setEnabled(true);
        });

        cmdIssueDelete.addActionListener(e -> {
            int id = tblIssues.getSelectedRow();
            int issueId = Integer.parseInt(tblIssues.getValueAt(id, 0).toString());
            System.out.println(new MantisSoapAPI(this.settings).removeIssue(issueId));
            tblIssueModel.removeRow(id);
            tblIssues.setModel(tblIssueModel);
            tblIssues.getSelectionModel().clearSelection();
            this.resetComponents();
        });

        cmdIssueAbort.addActionListener(e -> {
            this.controlComponents(true);
            cmdIssueSave.setEnabled(false);
            cmdIssueAbort.setEnabled(false);
            cmdIssueNew.setEnabled(true);
            cmdIssueNoteNew.setEnabled(false);
            cmdIssueAttachmentNew.setEnabled(false);
            cmdIssueAttachmentEdit.setEnabled(false);
            cmdIssueAttachmentDelete.setEnabled(false);
            cmdIssueNoteEdit.setEnabled(false);
            cmdIssueNoteDelete.setEnabled(false);
            tblIssues.getSelectionModel().clearSelection();
            this.resetComponents();

        });

        cmdIssueSave.addActionListener(e -> {
            if(tblIssues.getSelectedRow()==-1) {

            } else {

            }

            this.controlComponents(true);
            cmdIssueSave.setEnabled(false);
            cmdIssueAbort.setEnabled(false);
            cmdIssueNew.setEnabled(true);
            cmdIssueNoteNew.setEnabled(false);
            cmdIssueAttachmentNew.setEnabled(false);
            cmdIssueAttachmentEdit.setEnabled(false);
            cmdIssueAttachmentDelete.setEnabled(false);
            cmdIssueNoteEdit.setEnabled(false);
            cmdIssueNoteDelete.setEnabled(false);
            tblIssues.getSelectionModel().clearSelection();
            this.resetComponents();
        });

        lvIssueAttachments.addListSelectionListener(e -> {
            cmdIssueAttachmentEdit.setEnabled(true);
            cmdIssueAttachmentDelete.setEnabled(true);
        });

        lvIssueNotes.addListSelectionListener(e -> {
            cmdIssueNoteEdit.setEnabled(true);
            cmdIssueNoteDelete.setEnabled(true);
        });

        cmdIssueNoteNew.addActionListener(e -> {
            cmdIssueNoteNew.setEnabled(false);
            cmdIssueNoteEdit.setEnabled(false);
            cmdIssueNoteDelete.setEnabled(false);
            cmdIssueNoteAbort.setEnabled(true);
            cmdIssueNoteSave.setEnabled(true);
            lvIssueNotes.getSelectionModel().clearSelection();
            txtIssueNoteDate.setText(new Date().toString());
            txtIssueNoteText.setEnabled(true);
            cmbIssueNoteReporterUser.setEnabled(true);
        });

        cmdIssueNoteEdit.addActionListener(e -> {
            cmdIssueNoteNew.setEnabled(false);
            cmdIssueNoteEdit.setEnabled(false);
            cmdIssueNoteDelete.setEnabled(false);
            cmdIssueNoteAbort.setEnabled(true);
            cmdIssueNoteSave.setEnabled(true);
            txtIssueNoteText.setEnabled(true);
            cmbIssueNoteReporterUser.setEnabled(true);
        });

        cmdIssueNoteDelete.addActionListener(e -> {

        });
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(this.pnlMain, "", false);
        this.toolWindow.getContentManager().addContent(content);
    }

    private void loadComboBoxes() {
        MantisSoapAPI api = new MantisSoapAPI(this.settings);

        cmbIssueCategory.removeAllItems();
        for(String category : api.getCategories(this.settings.getProjectID())) {
            cmbIssueCategory.addItem(category);
        }

        List<String> versions = api.getVersions(this.settings.getProjectID());

        cmbIssueTargetVersion.removeAllItems();
        for(String version : versions) {
            cmbIssueTargetVersion.addItem(version);
        }
        cmbIssueFixedInVersion.removeAllItems();
        for(String version : versions) {
            cmbIssueFixedInVersion.addItem(version);
        }

        List<MantisUser> user = api.getUsers(this.settings.getProjectID());

        cmbIssueReporterName.removeAllItems();
        for(MantisUser usr : user) {
            cmbIssueReporterName.addItem(usr.getUserName());
        }
        cmbIssueNoteReporterUser.removeAllItems();
        for(MantisUser usr : user) {
            cmbIssueNoteReporterUser.addItem(usr.getUserName());
        }

        cmbIssuePriority.removeAllItems();
        for(String priority : api.getEnum("priorities")) {
            cmbIssuePriority.addItem(priority);
        }

        cmbIssueSeverity.removeAllItems();
        for(String severities : api.getEnum("severities")) {
            cmbIssueSeverity.addItem(severities);
        }

        cmbIssueStatus.removeAllItems();
        for(String status : api.getEnum("status")) {
            cmbIssueStatus.addItem(status);
        }
    }

    private DefaultTableModel addColumnsToTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Summary");
        model.addColumn("Status");
        return model;
    }

    private void controlComponents(boolean state) {
        Helper.disableControlsInAPanel(tlbMain, state);
        Helper.disableControlsInAPanel(pnlIssueBasics, state);
        Helper.disableControlsInAPanel(pnlIssueDescriptions, state);
        Helper.disableControlsInAPanel(pnlIssueNotes, false);
        Helper.disableControlsInAPanel(pnlIssueAttachments, false);

        txtIssueReporterEMail.setEnabled(false);
        txtIssueReporterName.setEnabled(false);
        txtIssueNoteReporterEMail.setEnabled(false);
        txtIssueNoteReporterName.setEnabled(false);
        txtIssueDate.setEnabled(false);
        txtIssueNoteDate.setEnabled(false);
    }

    private void resetComponents() {
        Helper.resetControlsInAPanel(pnlIssueBasics);
        Helper.resetControlsInAPanel(pnlIssueDescriptions);
        Helper.resetControlsInAPanel(pnlIssueNotes);
        Helper.resetControlsInAPanel(pnlIssueAttachments);
    }
}
