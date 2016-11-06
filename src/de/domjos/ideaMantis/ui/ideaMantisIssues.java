package de.domjos.ideaMantis.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ui.SelectFilesDialog;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.MouseEventHandler;
import de.domjos.ideaMantis.model.IssueAttachment;
import de.domjos.ideaMantis.model.IssueNote;
import de.domjos.ideaMantis.model.MantisIssue;
import de.domjos.ideaMantis.model.MantisUser;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Date;
import java.util.List;

public class ideaMantisIssues implements ToolWindowFactory {
    private Project project;
    private ConnectionSettings settings;
    private MantisIssue currentIssue;

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
    private JTable tblIssueAttachments;
    private JTable tblIssueNotes;
    private JComboBox<String> cmbIssueNoteViewState;

    private ToolWindow toolWindow;

    public ideaMantisIssues() {
        tblIssues.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblIssueAttachments.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblIssueNotes.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableModel tblIssueModel = this.addColumnsToIssueTable();
        DefaultTableModel tblIssueAttachmentModel = this.addColumnsToIssueAttachmentTable();
        DefaultTableModel tblIssueNoteModel = this.addColumnsToIssueNoteTable();
        controlIssues(false, false);
        cmdIssueNew.setEnabled(false);

        cmdReload.addActionListener(e -> {
            settings = ConnectionSettings.getInstance(project);
            if(!settings.validateSettings()) {
                Helper.printNotification("Settings incorrect!", "Your Settings are <b>incorrect</b>!<br/>Please change them in the ConnectionSettings!",NotificationType.ERROR);
                tblIssues.removeAll();
            } else {
                tblIssues.removeAll();
                for(int i = 0; i<=tblIssueModel.getRowCount()-1; i++) {
                    tblIssueModel.removeRow(i);
                }
                tblIssues.setModel(tblIssueModel);
                for(MantisIssue issue : new MantisSoapAPI(this.settings).getIssues(this.settings.getProjectID())) {
                    tblIssueModel.addRow(new Object[]{issue.getId(), issue.getSummary(), issue.getStatus()});
                }
                tblIssues.setModel(tblIssueModel);
                this.loadComboBoxes();
                cmdIssueNew.setEnabled(true);
            }
            resetIssues();
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

                tblIssueNotes.removeAll();
                for(int i = 0; i<=tblIssueNoteModel.getRowCount()-1; i++) {
                    tblIssueNoteModel.removeRow(i);
                }
                for(IssueNote note : issue.getIssueNoteList()) {
                    tblIssueNoteModel.addRow(new Object[]{note.getId(), note.getText(), note.getView_state()});
                }
                tblIssueNotes.setModel(tblIssueNoteModel);

                tblIssueAttachments.removeAll();
                for(int i = 0; i<=tblIssueAttachmentModel.getRowCount()-1; i++) {
                    tblIssueAttachmentModel.removeRow(i);
                }
                for(IssueAttachment attachment : issue.getIssueAttachmentList()) {
                    tblIssueAttachmentModel.addRow(new Object[]{attachment.getId(), attachment.getFilename()});
                }
                tblIssueAttachments.setModel(tblIssueAttachmentModel);
                controlIssues(true, false);
                controlNotes(false, false);
                controlAttachments(false, false);
            }
        });

        cmdIssueNew.addActionListener(e -> {
            currentIssue = new MantisIssue();
            tblIssueAttachments.removeAll();
            for(int i = 0; i<=tblIssueAttachmentModel.getRowCount()-1; i++) {
                tblIssueAttachmentModel.removeRow(i);
            }
            tblIssueNotes.removeAll();
            for(int i = 0; i<=tblIssueNoteModel.getRowCount()-1; i++) {
                tblIssueNoteModel.removeRow(i);
            }
            controlIssues(false, true);
            resetIssues();
            txtIssueDate.setText(new Date().toString());
        });
        cmdIssueEdit.addActionListener(e -> controlIssues(true, true));

        cmdIssueDelete.addActionListener(e -> {
            int id = Integer.parseInt(tblIssueModel.getValueAt(tblIssues.getSelectedRow(), 0).toString());
            if(!new MantisSoapAPI(this.settings).removeIssue(id)) {
                Helper.printNotification("Problem", "Can't delete Issue!", NotificationType.ERROR);
            }
            controlIssues(false, false);
            cmdReload.doClick();
        });

        cmdIssueSave.addActionListener(e -> {
            MantisIssue issue = currentIssue;
            issue.setSummary(txtIssueSummary.getText());
            issue.setDate_submitted(txtIssueDate.getText());
            issue.setAdditional_information(txtIssueAdditionalInformation.getText());
            issue.setDescription(txtIssueDescription.getText());
            issue.setSteps_to_reproduce(txtIssueStepsToReproduce.getText());
            issue.setCategory(cmbIssueCategory.getSelectedItem().toString());
            issue.setPriority(cmbIssuePriority.getSelectedItem().toString());
            issue.setSeverity(cmbIssueSeverity.getSelectedItem().toString());
            issue.setFixed_in_version(cmbIssueFixedInVersion.getSelectedItem().toString());
            issue.setTarget_version(cmbIssueTargetVersion.getSelectedItem().toString());
            if(!txtIssueReporterName.getText().equals("")) {
                for(MantisUser user : new MantisSoapAPI(this.settings).getUsers(this.settings.getProjectID())) {
                    if(user.getUserName().equals(cmbIssueReporterName.getSelectedItem().toString())) {
                        issue.setReporter(user);
                        break;
                    }
                }
            }

            if(!tblIssues.getSelectionModel().isSelectionEmpty()) {
                issue.setId(Integer.parseInt(tblIssues.getValueAt(tblIssues.getSelectedRow(), 0).toString()));
            }

            if(!new MantisSoapAPI(this.settings).addIssue(issue)) {
                Helper.printNotification("Problem", "Can't add Issue!", NotificationType.ERROR);
            }
            controlIssues(false, false);
            cmdReload.doClick();
        });

        cmdIssueAbort.addActionListener(e -> controlIssues(false, false));



        tblIssueNotes.getSelectionModel().addListSelectionListener(e -> {
            if(tblIssueNotes.getSelectedRow()!=-1) {
                int id = Integer.parseInt(tblIssueNoteModel.getValueAt(tblIssueNotes.getSelectedRow(), 0).toString());
                for(IssueNote note : currentIssue.getIssueNoteList()) {
                    if(note.getId()==id) {
                        txtIssueNoteDate.setText(note.getDate());
                        txtIssueNoteText.setText(note.getText());
                        cmbIssueNoteViewState.setSelectedItem(note.getView_state());
                        if(note.getReporter()!=null) {
                            cmbIssueNoteReporterUser.setSelectedItem(note.getReporter().getUserName());
                            txtIssueNoteReporterName.setText(note.getReporter().getName());
                            txtIssueNoteReporterEMail.setText(note.getReporter().getEmail());
                        }
                        break;
                    }
                }
                controlNotes(true, false);
            }
        });

        cmdIssueNoteNew.addActionListener(e -> {
            controlNotes(false, true);
            txtIssueNoteDate.setText(new Date().toString());
        });
        cmdIssueNoteEdit.addActionListener(e -> controlNotes(true, true));

        cmdIssueNoteDelete.addActionListener(e -> {
            int id = Integer.parseInt(tblIssueNoteModel.getValueAt(tblIssueNotes.getSelectedRow(), 0).toString());
            if(!new MantisSoapAPI(this.settings).removeNote(id)) {
                Helper.printNotification("Problem", "Can't delete Note!", NotificationType.ERROR);
            }
            int nid = tblIssueNotes.getSelectedRow();
            tblIssueAttachments.getSelectionModel().clearSelection();
            tblIssueNoteModel.removeRow(nid);
            tblIssueNotes.setModel(tblIssueNoteModel);
            controlNotes(false, false);
        });

        cmdIssueNoteSave.addActionListener(e -> {
            IssueNote note = new IssueNote();
            note.setText(txtIssueNoteText.getText());
            note.setDate(txtIssueNoteDate.getText());
            note.setView_state(cmbIssueNoteViewState.getSelectedItem().toString());
            if(!txtIssueNoteReporterName.getText().equals("")) {
                for(MantisUser user : new MantisSoapAPI(this.settings).getUsers(this.settings.getProjectID())) {
                    if(user.getUserName().equals(cmbIssueNoteReporterUser.getSelectedItem().toString())) {
                        note.setReporter(user);
                    }
                }
            }
            if(!tblIssueNotes.getSelectionModel().isSelectionEmpty()) {
                note.setId(Integer.parseInt(tblIssueNotes.getValueAt(tblIssueNotes.getSelectedRow(), 0).toString()));
            }
            if(currentIssue.getId()!=0) {
                if(new MantisSoapAPI(this.settings).addNote(currentIssue.getId(), note)) {
                    Helper.printNotification("Problem", "Can't add Attachment!", NotificationType.ERROR);
                    return;
                }
                int id = tblIssues.getSelectedRow();
                tblIssues.getSelectionModel().clearSelection();
                tblIssues.setRowSelectionInterval(0, id);
            } else {
                if (tblIssueNotes.getSelectionModel().isSelectionEmpty()) {
                    tblIssueNoteModel.addRow(new Object[]{0, txtIssueNoteText.getText(), cmbIssueNoteViewState.getSelectedItem()});
                    currentIssue.addNote(note);
                } else {
                    int id = Integer.parseInt(tblIssueNoteModel.getValueAt(tblIssueNotes.getSelectedRow(), 0).toString());
                    String text = tblIssueNoteModel.getValueAt(tblIssueNotes.getSelectedRow(), 1).toString();
                    tblIssueNoteModel.setValueAt(txtIssueNoteText.getText(), tblIssueNotes.getSelectedRow(), 1);
                    tblIssueNoteModel.setValueAt(cmbIssueNoteViewState.getSelectedItem(), tblIssueNotes.getSelectedRow(), 2);

                    for(int i = 0; i<=currentIssue.getIssueNoteList().size()-1; i++) {
                        if(currentIssue.getIssueNoteList().get(i).getText().equals(text)&&currentIssue.getIssueNoteList().get(i).getId()==id) {
                            currentIssue.getIssueNoteList().set(i, note);
                        }
                    }
                }
                tblIssueNotes.setModel(tblIssueNoteModel);
            }
            controlNotes(false, false);
        });

        cmdIssueNoteAbort.addActionListener(e -> controlNotes(false, false));



        tblIssueAttachments.getSelectionModel().addListSelectionListener(e -> {
            if(tblIssueAttachments.getSelectedRow()!=-1) {
                int id = Integer.parseInt(tblIssueAttachmentModel.getValueAt(tblIssueAttachments.getSelectedRow(), 0).toString());
                for(IssueAttachment attachment : currentIssue.getIssueAttachmentList()) {
                    if(attachment.getId()==id) {
                        txtIssueAttachmentSize.setText(String.valueOf(attachment.getSize()));
                        txtIssueAttachmentFileName.setText(attachment.getFilename());
                        break;
                    }
                }
                controlAttachments(true, false);
            }
        });

        cmdIssueAttachmentNew.addActionListener(e -> controlAttachments(false, true));
        cmdIssueAttachmentEdit.addActionListener(e -> controlAttachments(true, true));

        cmdIssueAttachmentDelete.addActionListener(e -> {
            int id = Integer.parseInt(tblIssueAttachmentModel.getValueAt(tblIssueAttachments.getSelectedRow(), 0).toString());
            if(!new MantisSoapAPI(this.settings).removeAttachment(id)) {
                Helper.printNotification("Problem", "Can't delete Note!", NotificationType.ERROR);
            }
            int aid = tblIssueAttachments.getSelectedRow();
            tblIssueAttachments.getSelectionModel().clearSelection();
            tblIssueAttachmentModel.removeRow(aid);
            tblIssueAttachments.setModel(tblIssueAttachmentModel);
            controlAttachments(false, false);
        });

        cmdIssueAttachmentSave.addActionListener(e -> {
            IssueAttachment attachment = new IssueAttachment();
            attachment.setFilename(txtIssueAttachmentFileName.getText());
            attachment.setSize(Integer.parseInt(txtIssueAttachmentSize.getText()));
            if(!tblIssueAttachments.getSelectionModel().isSelectionEmpty()) {
                attachment.setId(Integer.parseInt(tblIssueAttachments.getValueAt(tblIssueAttachments.getSelectedRow(), 0).toString()));
            }
            if(currentIssue.getId()!=0) {
                if(new MantisSoapAPI(this.settings).addAttachment(currentIssue.getId(), attachment)) {
                    Helper.printNotification("Problem", "Can't add Attachment!", NotificationType.ERROR);
                    return;
                }
                int id = tblIssues.getSelectedRow();
                tblIssues.getSelectionModel().clearSelection();
                tblIssues.setRowSelectionInterval(0, id);
            } else {
                if (tblIssueAttachments.getSelectionModel().isSelectionEmpty()) {
                    tblIssueAttachmentModel.addRow(new Object[]{0, txtIssueAttachmentFileName.getText()});
                    currentIssue.addAttachment(attachment);
                } else {
                    int id = Integer.parseInt(tblIssueNoteModel.getValueAt(tblIssueNotes.getSelectedRow(), 0).toString());
                    String fileName = tblIssueNoteModel.getValueAt(tblIssueNotes.getSelectedRow(), 1).toString();
                    tblIssueAttachmentModel.setValueAt(txtIssueAttachmentFileName.getText(), tblIssueAttachments.getSelectedRow(), 1);

                    for(int i = 0; i<=currentIssue.getIssueAttachmentList().size()-1; i++) {
                        if(currentIssue.getIssueAttachmentList().get(i).getFilename().equals(fileName)&&currentIssue.getIssueAttachmentList().get(i).getId()==id) {
                            currentIssue.getIssueAttachmentList().set(i, attachment);
                        }
                    }
                }
                tblIssueAttachments.setModel(tblIssueAttachmentModel);
            }
            controlAttachments(false, false);
        });

        cmdIssueAttachmentAbort.addActionListener(e -> controlAttachments(false, false));

        cmdIssueAttachmentSearch.addActionListener(e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(new FileChooserDescriptor(true, false, false, false, false, true), project, null);
            if(virtualFile!=null) {
                txtIssueAttachmentFileName.setText(virtualFile.getPath());
                txtIssueAttachmentSize.setText(String.valueOf(virtualFile.getLength()));
            }
        });

        cmbIssueReporterName.addItemListener(e -> {
            for(MantisUser user : new MantisSoapAPI(this.settings).getUsers(this.settings.getProjectID())) {
                if(user.getUserName().equals(e.getItem().toString())) {
                    txtIssueReporterEMail.setText(user.getEmail());
                    txtIssueReporterName.setText(user.getName());
                    break;
                }
            }
        });

        cmbIssueNoteReporterUser.addItemListener(e -> {
            for(MantisUser user : new MantisSoapAPI(this.settings).getUsers(this.settings.getProjectID())) {
                if(user.getUserName().equals(e.getItem().toString())) {
                    txtIssueNoteReporterEMail.setText(user.getEmail());
                    txtIssueNoteReporterName.setText(user.getName());
                    break;
                }
            }
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

    private void controlIssues(boolean selected, boolean editMode) {
        txtIssueSummary.setEnabled(editMode);
        cmdIssueNoteNew.setEnabled(editMode);
        cmdIssueAttachmentNew.setEnabled(editMode);
        cmbIssueReporterName.setEnabled(editMode);
        cmbIssueCategory.setEnabled(editMode);
        cmbIssueTargetVersion.setEnabled(editMode);
        cmbIssueFixedInVersion.setEnabled(editMode);
        cmbIssuePriority.setEnabled(editMode);
        cmbIssueSeverity.setEnabled(editMode);
        cmbIssueStatus.setEnabled(editMode);
        tblIssues.setEnabled(!editMode);
        Helper.disableControlsInAPanel(pnlIssueDescriptions, editMode);

        if(editMode) {
            if(!tblIssueNotes.getSelectionModel().isSelectionEmpty()) {
                cmdIssueNoteEdit.setEnabled(true);
                cmdIssueNoteDelete.setEnabled(true);
            }
            if(!tblIssueAttachments.getSelectionModel().isSelectionEmpty()) {
                cmdIssueAttachmentEdit.setEnabled(true);
                cmdIssueAttachmentDelete.setEnabled(true);
            }
        }

        cmdIssueNew.setEnabled(!editMode);
        cmdIssueSave.setEnabled(editMode);
        cmdIssueAbort.setEnabled(editMode);
        cmdIssueAttachmentNew.setEnabled(editMode);
        cmdIssueNoteNew.setEnabled(editMode);
        if(selected) {
            cmdIssueEdit.setEnabled(!editMode);
            cmdIssueDelete.setEnabled(!editMode);
        } else {
            cmdIssueEdit.setEnabled(false);
            cmdIssueDelete.setEnabled(false);
            resetIssues();
            controlNotes(false, false);
            controlAttachments(false, false);
        }
    }

    private void resetIssues() {
        tblIssues.getSelectionModel().clearSelection();
        txtIssueSummary.setText("");
        txtIssueReporterEMail.setText("");
        txtIssueReporterName.setText("");
        cmbIssueReporterName.setSelectedItem(null);
        cmbIssueCategory.setSelectedItem(null);
        cmbIssueTargetVersion.setSelectedItem(null);
        cmbIssueFixedInVersion.setSelectedItem(null);
        cmbIssuePriority.setSelectedItem(null);
        cmbIssueSeverity.setSelectedItem(null);
        cmbIssueStatus.setSelectedItem(null);
        tblIssueNotes.removeAll();
        tblIssueAttachments.removeAll();
        Helper.resetControlsInAPanel(pnlIssueDescriptions);
        this.resetAttachments();
        this.resetNotes();
        tblIssueAttachments.setModel(addColumnsToIssueAttachmentTable());
        tblIssueNotes.setModel(addColumnsToIssueNoteTable());
    }

    private void controlNotes(boolean selected, boolean editMode) {
        txtIssueNoteText.setEnabled(editMode);
        cmbIssueNoteReporterUser.setEnabled(editMode);
        cmbIssueNoteViewState.setEnabled(editMode);
        tblIssueNotes.setEnabled(!editMode);

        if(cmdIssueSave.isEnabled()) {
            cmdIssueNoteNew.setEnabled(!editMode);
        } else {
            cmdIssueNoteNew.setEnabled(false);
        }
        cmdIssueNoteSave.setEnabled(editMode);
        cmdIssueNoteAbort.setEnabled(editMode);
        if(selected && cmdIssueNoteNew.isEnabled()) {
            cmdIssueNoteEdit.setEnabled(!editMode);
            cmdIssueNoteDelete.setEnabled(!editMode);
        } else if(selected && !cmdIssueNoteNew.isEnabled()) {
            cmdIssueNoteEdit.setEnabled(false);
            cmdIssueNoteDelete.setEnabled(false);
        } else {
            cmdIssueNoteEdit.setEnabled(false);
            cmdIssueNoteDelete.setEnabled(false);
            resetNotes();
        }
    }

    private void resetNotes() {
        txtIssueNoteDate.setText("");
        txtIssueNoteText.setText("");
        cmbIssueNoteReporterUser.setSelectedItem(null);
        cmbIssueNoteViewState.setSelectedItem(null);
        txtIssueNoteReporterEMail.setText("");
        txtIssueNoteReporterName.setText("");
    }

    private void controlAttachments(boolean selected, boolean editMode) {
        cmdIssueAttachmentSearch.setEnabled(editMode);
        tblIssueAttachments.setEnabled(!editMode);

        if(cmdIssueSave.isEnabled()) {
            cmdIssueAttachmentNew.setEnabled(!editMode);
        } else {
            cmdIssueAttachmentNew.setEnabled(false);
        }
        cmdIssueAttachmentSave.setEnabled(editMode);
        cmdIssueAttachmentAbort.setEnabled(editMode);
        if(selected && cmdIssueAttachmentNew.isEnabled()) {
            cmdIssueAttachmentEdit.setEnabled(!editMode);
            cmdIssueAttachmentDelete.setEnabled(!editMode);
        } else if(selected && !cmdIssueAttachmentNew.isEnabled()) {
            cmdIssueAttachmentEdit.setEnabled(false);
            cmdIssueAttachmentDelete.setEnabled(false);
        } else {
            cmdIssueAttachmentEdit.setEnabled(false);
            cmdIssueAttachmentDelete.setEnabled(false);
            resetAttachments();
        }
    }

    private void resetAttachments() {
        txtIssueAttachmentFileName.setText("");
        txtIssueAttachmentSize.setText("");
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

        cmbIssueNoteViewState.removeAllItems();
        for(String viewState : api.getEnum("view_states")) {
            cmbIssueNoteViewState.addItem(viewState);
        }
    }

    private DefaultTableModel addColumnsToIssueTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Summary");
        model.addColumn("Status");
        return model;
    }

    private DefaultTableModel addColumnsToIssueNoteTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Text");
        model.addColumn("ViewState");
        return model;
    }

    private DefaultTableModel addColumnsToIssueAttachmentTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("FileName");
        return model;
    }
}
