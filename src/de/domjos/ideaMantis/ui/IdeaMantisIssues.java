package de.domjos.ideaMantis.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import de.domjos.ideaMantis.model.*;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.soap.ObjectRef;
import de.domjos.ideaMantis.utils.Helper;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.*;
import java.util.List;

public class IdeaMantisIssues implements ToolWindowFactory {
    private Project project;
    private ConnectionSettings settings;
    private MantisIssue currentIssue;

    private JPanel pnlIssueDescriptions, pnlMain;

    private JButton cmdIssueNew, cmdIssueEdit, cmdIssueDelete, cmdIssueSave,cmdIssueAbort;
    private JButton cmdIssueNoteNew, cmdIssueNoteEdit, cmdIssueNoteDelete, cmdIssueNoteSave, cmdIssueNoteAbort;
    private JButton cmdIssueAttachmentSearch, cmdIssueAttachmentNew, cmdIssueAttachmentEdit, cmdIssueAttachmentDelete;
    private JButton cmdIssueAttachmentSave, cmdIssueAttachmentAbort;
    private JButton cmdReload;

    private JTextField txtIssueSummary, txtIssueDate, txtIssueReporterName, txtIssueReporterEMail;

    private JComboBox<String> cmbIssueReporterName, cmbIssueNoteReporterUser;
    private JComboBox<String> cmbIssueTargetVersion, cmbIssueFixedInVersion, cmbIssueNoteViewState;
    private JComboBox<String> cmbIssuePriority, cmbIssueSeverity, cmbIssueStatus, cmbIssueCategory;

    private JTextArea txtIssueDescription, txtIssueStepsToReproduce, txtIssueAdditionalInformation, txtIssueNoteText;

    private JTextField txtIssueNoteReporterName, txtIssueNoteReporterEMail, txtIssueNoteDate, txtIssueAttachmentFileName;
    private JTextField txtIssueAttachmentSize;

    private JTable tblIssues, tblIssueAttachments, tblIssueNotes;

    private JLabel lblValidation;
    private JCheckBox chkAddVCS;
    private JTextField txtVCSComment;
    private JButton cmdVersionAdd;
    private JComboBox<String> cmbBasicsTags;
    private JTextField txtBasicsTags;
    private JLabel lblIssueFixedInVersion;
    private JLabel lblIssueStatus;
    private JButton cmdForward;
    private JButton cmdBack;
    private JButton cmdCustomFields;
    private JComboBox<String> cmbFilters;
    private JComboBox<String> cmbIssueProfile;
    private boolean state = false, loadComboBoxes = false;
    private int page = 1;

    private ChangeListManager changeListManager;
    private Map.Entry<Integer, String> access;


    public IdeaMantisIssues() {
        tblIssues.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblIssueAttachments.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblIssueNotes.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableModel tblIssueModel = this.addColumnsToIssueTable();
        DefaultTableModel tblIssueAttachmentModel = this.addColumnsToIssueAttachmentTable();
        DefaultTableModel tblIssueNoteModel = this.addColumnsToIssueNoteTable();
        controlIssues(false, false);
        cmdIssueNew.setEnabled(false);
        tblIssueAttachments.setDragEnabled(true);
        txtVCSComment.setEnabled(false);
        tblIssueAttachments.setCellEditor(null);

        cmdCustomFields.addActionListener(e -> {
            String state = "report";
            if(cmbIssueStatus.getSelectedItem()!=null) {
                List<ObjectRef> refs = new MantisSoapAPI(settings).getEnum("status");
                for(ObjectRef ref : refs) {
                    if(ref.getName().equals(cmbIssueStatus.getSelectedItem().toString())) {
                        switch (ref.getId()) {
                            case 10:
                            case 50:
                                state = "report";
                                break;
                            case 20:
                            case 30:
                            case 40:
                                state = "update";
                                break;
                            case 80:
                                state = "resolved";
                                break;
                            case 90:
                                state = "closed";
                                break;
                            default:
                                state = "";
                        }
                    }
                }
            }

            CustomFieldDialog dialog = new CustomFieldDialog(project, state, currentIssue.getCustomFields());
            dialog.show();
            for(CustomFieldResult fieldResult : dialog.getResults()) {
                String result = StringUtils.join(fieldResult.getResult(), "|");
                this.currentIssue.addCustomField(fieldResult.getField(), result);
            }
        });

        cmdBack.addActionListener(e -> {
            if(this.page==1) {
                this.page = 1;
            } else {
                this.page = this.page-1;
            }
            this.loadList(tblIssueModel, ProgressManager.getInstance());
        });

        cmdForward.addActionListener(e -> {
            this.page = this.page+1;
            this.loadList(tblIssueModel, ProgressManager.getInstance());
        });

        cmdVersionAdd.addActionListener(e -> {
            VersionDialog dialog = new VersionDialog(project, settings.getProjectID());
            if(dialog.showAndGet())
                this.loadVersions(new MantisSoapAPI(ConnectionSettings.getInstance(project)));
        });

        cmbBasicsTags.addActionListener(e -> {
            try {
                if(txtBasicsTags.getText().equals("")) {
                    txtBasicsTags.setText(cmbBasicsTags.getSelectedItem().toString());
                } else {
                    txtBasicsTags.setText(txtBasicsTags.getText() + ", " + cmbBasicsTags.getSelectedItem().toString());
                }
            } catch (Exception ex) {
                txtBasicsTags.setText("");
            }
        });

        tblIssueAttachments.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if (e.getClickCount() == 2 && !e.isConsumed()) {
                        if (!tblIssueAttachments.getSelectionModel().isSelectionEmpty()) {
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            MantisIssue issue = new MantisSoapAPI(settings).getIssue(Integer.parseInt(tblIssues.getValueAt(tblIssues.getSelectedRow(), 0).toString()));
                            for (IssueAttachment attachment : issue.getIssueAttachmentList()) {
                                if (attachment.getId() == Integer.parseInt(tblIssueAttachments.getValueAt(tblIssueAttachments.getSelectedRow(), 0).toString())) {
                                    if (Desktop.isDesktopSupported()) {
                                        Desktop.getDesktop().browse(new URI(attachment.getDownload_url()));
                                    } else {
                                        StringSelection selection = new StringSelection(attachment.getDownload_url());
                                        clipboard.setContents(selection, selection);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    Helper.printException(ex);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        cmdReload.addActionListener(e -> {
            try {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                ProgressManager manager = ProgressManager.getInstance();
                Task task =
                    new Task.Backgroundable(project, "Load Issues...") {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        try {
                            if(!settings.validateSettings()) {
                                Helper.printNotification("Wrong settings!", "The connection-settings are incorrect!", NotificationType.WARNING);
                                state = false;
                                tblIssues.removeAll();
                                for (int i = tblIssueModel.getRowCount() - 1; i >= 0; i--) {
                                    tblIssueModel.removeRow(i);
                                }
                            } else {
                                checkRights();
                                controlPagination();
                                state = true;
                                tblIssues.removeAll();
                                for (int i = tblIssueModel.getRowCount() - 1; i >= 0; i--) {
                                    tblIssueModel.removeRow(i);
                                }
                                tblIssues.setModel(tblIssueModel);
                                if(!loadComboBoxes)
                                    loadComboBoxes();
                                if(settings.getItemsPerPage()==-1) {
                                    page = 1;
                                }
                                String filterID = "";
                                if(cmbFilters.getSelectedItem()!=null) {
                                    if(!cmbFilters.getSelectedItem().toString().equals("")) {
                                        filterID = cmbFilters.getSelectedItem().toString().split(":")[0].trim();
                                    }
                                }
                                List<MantisIssue> mantisIssues = new MantisSoapAPI(settings).getIssues(settings.getProjectID(), page, filterID);
                                progressIndicator.setFraction(0.0);
                                double factor = 100.0 / mantisIssues.size();
                                for(MantisIssue issue : mantisIssues) {
                                    tblIssueModel.addRow(new Object[]{issue.getId(), issue.getSummary(), issue.getStatus()});
                                    progressIndicator.setFraction(progressIndicator.getFraction() + factor);
                                }
                                tblIssues.setModel(tblIssueModel);
                                cmdIssueNew.setEnabled(true);
                            }
                        } catch (Exception ex) {
                            Helper.printException(ex);
                        } finally {
                            pnlMain.setCursor(Cursor.getDefaultCursor());
                        }
                    }
                };
                manager.run(task);
                resetIssues();
                if(tblIssues.getColumnCount()>=1) {
                    tblIssues.getColumnModel().getColumn(0).setWidth(40);
                    tblIssues.getColumnModel().getColumn(0).setMaxWidth(100);
                }
            } catch (Exception ex) {
                Helper.printException(ex);
            }
        });

        tblIssues.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    if(tblIssues.getSelectedRow()!=-1) {
                        if(!loadComboBoxes)
                            loadComboBoxes();
                        pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        int id = Integer.parseInt(tblIssueModel.getValueAt(tblIssues.getSelectedRow(), 0).toString());
                        MantisSoapAPI api = new MantisSoapAPI(settings);
                        MantisIssue issue = api.getIssue(id);
                        currentIssue = issue;
                        cmdCustomFields.setVisible(!api.getCustomFields(settings.getProjectID()).isEmpty());
                        cmdIssueEdit.setEnabled(true);
                        cmdIssueDelete.setEnabled(true);
                        txtIssueSummary.setText(issue.getSummary());
                        txtIssueDate.setText(issue.getDate_submitted());
                        txtIssueAdditionalInformation.setText(issue.getAdditional_information());
                        txtIssueDescription.setText(issue.getDescription());
                        txtIssueStepsToReproduce.setText(issue.getSteps_to_reproduce());
                        txtBasicsTags.setText(issue.getTags());
                        if(issue.getReporter()!=null) {
                            if(cmbIssueReporterName.getItemCount()==0) {
                                cmbIssueReporterName.addItem(issue.getReporter().getUserName());
                            }
                            cmbIssueReporterName.setSelectedItem(issue.getReporter().getUserName());
                            txtIssueReporterName.setText(issue.getReporter().getName());
                            txtIssueReporterEMail.setText(issue.getReporter().getEmail());
                        }
                        if(issue.getProfile()!=null) {
                            cmbIssueProfile.setSelectedItem(null);
                            cmbIssueProfile.firePopupMenuWillBecomeVisible();
                            for(int i = 0; i<=cmbIssueProfile.getItemCount()-1; i++) {
                                if(cmbIssueProfile.getItemAt(i).contains(": ")) {
                                    String item = cmbIssueProfile.getItemAt(i).split(": ")[1];
                                    if(String.format("%s %s %s", issue.getProfile().getPlatform(), issue.getProfile().getOs(), issue.getProfile().getOsBuild()).equals(item)) {
                                        cmbIssueProfile.setSelectedIndex(i);
                                    }
                                }
                            }
                        } else {
                            cmbIssueProfile.setSelectedItem(null);
                        }
                        cmbIssueCategory.setSelectedItem(issue.getCategory());
                        if(issue.getTarget_version()!=null) {
                            for(int i = 0; i<=cmbIssueTargetVersion.getItemCount()-1 ;i++) {
                                if(cmbIssueTargetVersion.getItemAt(i).contains(issue.getTarget_version().getName())) {
                                    cmbIssueTargetVersion.setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                        if(issue.getFixed_in_version()!=null) {
                            for(int i = 0; i<=cmbIssueFixedInVersion.getItemCount()-1 ;i++) {
                                if(cmbIssueFixedInVersion.getItemAt(i).contains(issue.getFixed_in_version().getName())) {
                                    cmbIssueFixedInVersion.setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                        cmbIssuePriority.setSelectedItem(issue.getPriority());
                        cmbIssueSeverity.setSelectedItem(issue.getSeverity());
                        cmbIssueStatus.setSelectedItem(issue.getStatus());

                        tblIssueNotes.removeAll();
                        for (int i = tblIssueNoteModel.getRowCount() - 1; i >= 0; i--) {
                            tblIssueNoteModel.removeRow(i);
                        }
                        for(IssueNote note : issue.getIssueNoteList()) {
                            tblIssueNoteModel.addRow(new Object[]{note.getId(), note.getText(), note.getView_state()});
                        }
                        tblIssueNotes.setModel(tblIssueNoteModel);

                        tblIssueAttachments.removeAll();
                        for (int i = tblIssueAttachmentModel.getRowCount() - 1; i >= 0; i--) {
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
                } catch (Exception ex) {
                    Helper.printException(ex);
                } finally {
                    pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        cmbIssueTargetVersion.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2) {
                    MantisSoapAPI api = new MantisSoapAPI(ConnectionSettings.getInstance(project));
                    for(MantisVersion version : api.getVersions(ConnectionSettings.getInstance(project).getProjectID())) {
                        if(version.getId()==Integer.parseInt(cmbIssueTargetVersion.getSelectedItem().toString().split(":")[0])) {
                            VersionDialog dialog = new VersionDialog(project, settings.getProjectID(), version);
                            if(dialog.showAndGet())
                                loadVersions(api);
                            break;
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        cmbIssueFixedInVersion.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2) {
                    MantisSoapAPI api = new MantisSoapAPI(ConnectionSettings.getInstance(project));
                    for(MantisVersion version : api.getVersions(ConnectionSettings.getInstance(project).getProjectID())) {
                        if(cmbIssueFixedInVersion.getSelectedItem()!=null) {
                            if (version.getId() == Integer.parseInt(cmbIssueFixedInVersion.getSelectedItem().toString().split(":")[0])) {
                                VersionDialog dialog = new VersionDialog(project, settings.getProjectID(), version);
                                if (dialog.showAndGet())
                                    loadVersions(api);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        cmdIssueNew.addActionListener(e -> {
            currentIssue = new MantisIssue();
            tblIssueAttachments.removeAll();
            for (int i = tblIssueAttachmentModel.getRowCount() - 1; i >= 0; i--) {
                tblIssueAttachmentModel.removeRow(i);
            }
            tblIssueNotes.removeAll();
            for (int i = tblIssueNoteModel.getRowCount() - 1; i >= 0; i--) {
                tblIssueNoteModel.removeRow(i);
            }
            controlIssues(false, true);
            resetIssues();
            txtIssueDate.setText(new Date().toString());
        });
        cmdIssueEdit.addActionListener(e -> controlIssues(true, true));

        cmdIssueDelete.addActionListener(e -> {
            try {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if(tblIssues.getSelectedRow()!=-1) {
                    int id = Integer.parseInt(tblIssueModel.getValueAt(tblIssues.getSelectedRow(), 0).toString());
                    if(!new MantisSoapAPI(this.settings).removeIssue(id)) {
                        Helper.printNotification("Exception", String.format("Can't delete %s!", "Issue"), NotificationType.ERROR);
                    }
                    controlIssues(false, false);
                    cmdReload.doClick();
                }
            } catch (Exception ex) {
                Helper.printException(ex);
            } finally {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        cmdIssueSave.addActionListener(e -> {
           try {
               pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               lblValidation.setText(this.validateIssue());
               if(lblValidation.getText().equals("")) {
                   MantisSoapAPI api = new MantisSoapAPI(settings);
                   MantisIssue issue = currentIssue;
                   issue.setSummary(txtIssueSummary.getText());
                   issue.setDate_submitted(txtIssueDate.getText());
                   issue.setAdditional_information(txtIssueAdditionalInformation.getText());
                   issue.setDescription(txtIssueDescription.getText());
                   issue.setSteps_to_reproduce(txtIssueStepsToReproduce.getText());
                   issue.setTags(txtBasicsTags.getText());

                   if(cmbIssueProfile.getSelectedItem()!=null) {
                       if(!cmbIssueProfile.getSelectedItem().toString().equals("")) {
                           if(cmbIssueProfile.getSelectedItem().toString().contains(": ")) {
                               int id = Integer.parseInt(cmbIssueProfile.getSelectedItem().toString().split(": ")[0]);
                               for(MantisProfile profile : api.getProfiles()) {
                                   if(profile.getId()==id) {
                                       issue.setProfile(profile);
                                   }
                               }
                           } else {
                               issue.setProfile(null);
                           }
                       } else {
                           issue.setProfile(null);
                       }
                   } else {
                       issue.setProfile(null);
                   }

                   if(cmbIssueCategory.getSelectedItem()!=null)
                       issue.setCategory(cmbIssueCategory.getSelectedItem().toString());
                   if(cmbIssuePriority.getSelectedItem()!=null)
                       issue.setPriority(cmbIssuePriority.getSelectedItem().toString());
                   if(cmbIssueSeverity.getSelectedItem()!=null)
                       issue.setSeverity(cmbIssueSeverity.getSelectedItem().toString());
                   if(cmbIssueStatus.getSelectedItem()!=null)
                       issue.setStatus(cmbIssueStatus.getSelectedItem().toString());

                   List<MantisVersion> versions = api.getVersions(settings.getProjectID());
                   if(cmbIssueFixedInVersion.getSelectedItem()!=null) {
                       for(MantisVersion version : versions) {
                           if(version.getName().equals(cmbIssueFixedInVersion.getSelectedItem().toString().split(": ")[1])) {
                               issue.setFixed_in_version(version);
                               break;
                           }
                       }
                   }
                   if(cmbIssueTargetVersion.getSelectedItem()!=null) {
                       for(MantisVersion version : versions) {
                           if(version.getName().equals(cmbIssueTargetVersion.getSelectedItem().toString().split(": ")[1])) {
                               issue.setTarget_version(version);
                               break;
                           }
                       }
                   }

                   if(!txtIssueReporterName.getText().equals("")) {
                       for(MantisUser user : new MantisSoapAPI(this.settings).getUsers(this.settings.getProjectID())) {
                           if(user.getUserName().equals(cmbIssueReporterName.getSelectedItem().toString())) {
                               issue.setReporter(user);
                               break;
                           }
                       }
                   } else {
                       issue.setReporter(null);
                   }

                   if(!tblIssues.getSelectionModel().isSelectionEmpty()) {
                       issue.setId(Integer.parseInt(tblIssues.getValueAt(tblIssues.getSelectedRow(), 0).toString()));
                   }

                   if(issue.getId()!=0) {
                       MantisIssue mantisIssue = api.getIssue(issue.getId());
                       if(!mantisIssue.getStatus().equals(issue.getStatus())) {
                           FixDialog dialog = new FixDialog(project, issue.getId());
                           dialog.show();
                       }
                   }

                   if(!api.addIssue(issue)) {
                       Helper.printNotification("Exception", "Can't add or update Issue!", NotificationType.ERROR);
                   }
                   if(chkAddVCS.isSelected()) {
                       Helper.commitAllFiles(Helper.replaceCommentByMarker(issue, txtVCSComment.getText()), this.changeListManager);
                   }

                  if(!issue.getTags().equals("")) {
                      for(MantisIssue mantisIssue : api.getIssues(this.settings.getProjectID())) {
                          if(issue.getSummary().equals(mantisIssue.getSummary()) && issue.getStatus().equals(mantisIssue.getStatus())) {
                              api.addTagToIssue(mantisIssue.getId(), issue.getTags());
                              break;
                          }
                      }
                  }

                   controlIssues(false, false);
                   cmdReload.doClick();
               }
           } catch (Exception ex) {
               Helper.printException(ex);
           } finally {
               pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
           }
        });

        cmdIssueAbort.addActionListener(e -> {
            lblValidation.setText("");
            controlIssues(false, false);
        });



        tblIssueNotes.getSelectionModel().addListSelectionListener(e -> {
            try {
                if(tblIssueNotes.getSelectedRow()!=-1) {
                    pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    int id = Integer.parseInt(tblIssueNoteModel.getValueAt(tblIssueNotes.getSelectedRow(), 0).toString());
                    for(IssueNote note : currentIssue.getIssueNoteList()) {
                        if(note.getId()==id) {
                            txtIssueNoteDate.setText(note.getDate());
                            txtIssueNoteText.setText(note.getText());
                            cmbIssueNoteViewState.setSelectedItem(note.getView_state());
                            if(note.getReporter()!=null) {
                                if(cmbIssueNoteReporterUser.getItemCount()==0) {
                                    cmbIssueNoteReporterUser.addItem(note.getReporter().getUserName());
                                }
                                cmbIssueNoteReporterUser.setSelectedItem(note.getReporter().getUserName());
                                txtIssueNoteReporterName.setText(note.getReporter().getName());
                                txtIssueNoteReporterEMail.setText(note.getReporter().getEmail());
                            }
                            break;
                        }
                    }
                    controlNotes(true, false);
                }
            } catch (Exception ex) {
                Helper.printException(ex);
            } finally {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        cmdIssueNoteNew.addActionListener(e -> {
            controlNotes(false, true);
            txtIssueNoteDate.setText(new Date().toString());
        });
        cmdIssueNoteEdit.addActionListener(e -> controlNotes(true, true));

        cmdIssueNoteDelete.addActionListener(e -> {
            try {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if(tblIssueNotes.getSelectedRow()!=-1) {
                    int id = Integer.parseInt(tblIssueNoteModel.getValueAt(tblIssueNotes.getSelectedRow(), 0).toString());
                    if(!new MantisSoapAPI(this.settings).removeNote(id)) {
                        Helper.printNotification("Exception", "Can't delete Note!", NotificationType.ERROR);
                    }
                    int nid = tblIssueNotes.getSelectedRow();
                    tblIssueAttachments.getSelectionModel().clearSelection();
                    tblIssueNoteModel.removeRow(nid);
                    tblIssueNotes.setModel(tblIssueNoteModel);
                }
                controlNotes(false, false);
            } catch (Exception ex) {
                Helper.printException(ex);
            } finally {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        cmdIssueNoteSave.addActionListener(e -> {
            try {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                lblValidation.setText(this.validateNote());
                if(lblValidation.getText().equals("")) {
                    IssueNote note = new IssueNote();
                    note.setText(txtIssueNoteText.getText());
                    note.setDate(txtIssueNoteDate.getText());

                    if(cmbIssueNoteViewState.getSelectedItem()!=null)
                        note.setView_state(cmbIssueNoteViewState.getSelectedItem().toString());

                    if(!txtIssueNoteReporterName.getText().equals("")) {
                        for(MantisUser user : new MantisSoapAPI(this.settings).getUsers(this.settings.getProjectID())) {
                            if(user.getUserName().equals(cmbIssueNoteReporterUser.getSelectedItem().toString())) {
                                note.setReporter(user);
                                break;
                            }
                        }
                    } else {
                        note.setReporter(null);
                    }

                    if(!tblIssueNotes.getSelectionModel().isSelectionEmpty()) {
                        note.setId(Integer.parseInt(tblIssueNotes.getValueAt(tblIssueNotes.getSelectedRow(), 0).toString()));
                    }

                    if(currentIssue.getId()!=0) {
                        if(!new MantisSoapAPI(this.settings).addNote(currentIssue.getId(), note)) {
                            Helper.printNotification("Exception", "Can't delete Note!", NotificationType.ERROR);
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
                }
            } catch (Exception ex) {
                Helper.printException(ex);
            } finally {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        cmdIssueNoteAbort.addActionListener(e -> {
            lblValidation.setText("");
            controlNotes(false, false);
        });



        tblIssueAttachments.getSelectionModel().addListSelectionListener(e -> {
            try {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
            } catch (Exception ex) {
                Helper.printException(ex);
            } finally {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        cmdIssueAttachmentNew.addActionListener(e -> controlAttachments(false, true));
        cmdIssueAttachmentEdit.addActionListener(e -> controlAttachments(true, true));

        cmdIssueAttachmentDelete.addActionListener(e -> {
            try {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if(tblIssueAttachments.getSelectedRow()!=-1) {
                    int id = Integer.parseInt(tblIssueAttachmentModel.getValueAt(tblIssueAttachments.getSelectedRow(), 0).toString());
                    if(!new MantisSoapAPI(this.settings).removeAttachment(id)) {
                        Helper.printNotification("Exception", "Can't delete Attachment!", NotificationType.ERROR);
                    }
                    int aid = tblIssueAttachments.getSelectedRow();
                    tblIssueAttachments.getSelectionModel().clearSelection();
                    tblIssueAttachmentModel.removeRow(aid);
                    tblIssueAttachments.setModel(tblIssueAttachmentModel);
                    controlAttachments(false, false);
                }
            } catch (Exception ex) {
                Helper.printException(ex);
            } finally {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        cmdIssueAttachmentSave.addActionListener(e -> {
            try {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                lblValidation.setText(this.validateAttachment());
                if(lblValidation.getText().equals("")) {
                    IssueAttachment attachment = new IssueAttachment();
                    attachment.setFilename(txtIssueAttachmentFileName.getText());
                    attachment.setSize(Integer.parseInt(txtIssueAttachmentSize.getText()));

                    if(!tblIssueAttachments.getSelectionModel().isSelectionEmpty()) {
                        attachment.setId(Integer.parseInt(tblIssueAttachments.getValueAt(tblIssueAttachments.getSelectedRow(), 0).toString()));
                    }

                    if(currentIssue.getId()!=0) {
                        if(!new MantisSoapAPI(this.settings).addAttachment(currentIssue.getId(), attachment)) {
                            Helper.printNotification("Exception", "Can't delete Attachment!", NotificationType.ERROR);
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
                }
            } catch (Exception ex) {
                Helper.printException(ex);
            } finally {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        cmdIssueAttachmentAbort.addActionListener(e -> {
            lblValidation.setText("");
            controlAttachments(false, false);
        });

        cmdIssueAttachmentSearch.addActionListener(e ->
            FileChooser.chooseFile(new FileChooserDescriptor(true, false, false, false, false, false), project, null, (virtualFile) -> {
                if(virtualFile!=null) {
                    txtIssueAttachmentFileName.setText(virtualFile.getPath());
                    txtIssueAttachmentSize.setText(String.valueOf(virtualFile.getLength()));
                }
            })
        );


        cmbIssueReporterName.addItemListener(e -> {
            try {
                if(e.getItem().toString().equals("")) {
                    txtIssueReporterName.setText("");
                    txtIssueReporterEMail.setText("");
                } else {
                    for(MantisUser user : new MantisSoapAPI(this.settings).getUsers(this.settings.getProjectID())) {
                        if(user.getUserName().equals(e.getItem().toString())) {
                            txtIssueReporterEMail.setText(user.getEmail());
                            txtIssueReporterName.setText(user.getName());
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Helper.printException(ex);
            }
        });

        cmbIssueNoteReporterUser.addItemListener(e -> {
            try {
                if(e.getItem().toString().equals("")) {
                    txtIssueNoteReporterEMail.setText("");
                    txtIssueNoteReporterName.setText("");
                } else {
                    for(MantisUser user : new MantisSoapAPI(this.settings).getUsers(this.settings.getProjectID())) {
                        if(user.getUserName().equals(e.getItem().toString())) {
                            txtIssueNoteReporterEMail.setText(user.getEmail());
                            txtIssueNoteReporterName.setText(user.getName());
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Helper.printException(ex);
            }
        });

        chkAddVCS.addActionListener(e -> txtVCSComment.setEnabled(chkAddVCS.isSelected()));

        cmbFilters.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                cmbFilters.removeAllItems();
                cmbFilters.addItem("");
                List<MantisFilter> filters = new MantisSoapAPI(settings).getFilters(settings.getProjectID());
                if(filters!=null) {
                    for(MantisFilter filter : filters) {
                        cmbFilters.addItem(String.format("%4s: %s", filter.getId(), filter.getName()));
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });

        cmbIssueProfile.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                cmbIssueProfile.removeAllItems();
                cmbIssueProfile.addItem("");
                List<MantisProfile> mantisProfiles = new MantisSoapAPI(settings).getProfiles();
                if(mantisProfiles!=null) {
                    for(MantisProfile profile : mantisProfiles) {
                        cmbIssueProfile.addItem(String.format("%s: %s %s %s", profile.getId(), profile.getPlatform(), profile.getOs(), profile.getOsBuild()));
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
    }

    private String validateIssue() {
        if(txtIssueDescription.getText().equals("")) {
            return String.format("%s is a mandatory field!", "Description");
        }
        if(txtIssueSummary.getText().equals("")) {
            return String.format("%s is a mandatory field!", "Summary");
        }
        if(cmbIssueCategory.getSelectedItem()==null) {
            return String.format("%s is a mandatory field!", "Category");
        }
        return "";
    }

    private String validateAttachment() {
        if(txtIssueAttachmentFileName.getText().equals("")) {
            return String.format("%s is a mandatory field!", "Attachment-Content");
        }
        return "";
    }

    private String validateNote() {
        if(txtIssueNoteText.getText().equals("")) {
            return String.format("%s is a mandatory field!", "Note-Text");
        }
        return "";
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(this.pnlMain, "", false);
        toolWindow.getContentManager().addContent(content);
        settings = ConnectionSettings.getInstance(project);
        MantisSoapAPI api = new MantisSoapAPI(settings);
        controlPagination();
        this.changeListManager = ChangeListManager.getInstance(project);
        access = api.getRightsFromProject(settings.getProjectID());
        checkRights();
        cmdReload.doClick();
        if(state) {
            if(!loadComboBoxes)
                this.loadComboBoxes();
            cmdCustomFields.setVisible(!api.getCustomFields(settings.getProjectID()).isEmpty());
        }

        api.getProfiles();

        toolWindow.getContentManager().addContentManagerListener(new ContentManagerListener() {
            @Override
            public void contentAdded(ContentManagerEvent contentManagerEvent) {
                for(Content content : toolWindow.getContentManager().getContents()) {
                    if(content!=null) {
                        if(content.getDescription()!=null) {
                            if (content.getDescription().equals("reload comboBoxes")) {
                                MantisSoapAPI api = new MantisSoapAPI(settings);
                                access = api.getRightsFromProject(settings.getProjectID());
                                checkRights();
                                loadComboBoxes();
                                cmdReload.doClick();
                                cmdCustomFields.setVisible(!api.getCustomFields(settings.getProjectID()).isEmpty());
                                toolWindow.getContentManager().removeContent(content, true);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void contentRemoved(ContentManagerEvent contentManagerEvent) {}

            @Override
            public void contentRemoveQuery(ContentManagerEvent contentManagerEvent) {}

            @Override
            public void selectionChanged(ContentManagerEvent contentManagerEvent) {}
        });
        resetIssues();
    }

    private void controlIssues(boolean selected, boolean editMode) {
        txtIssueSummary.setEnabled(editMode);
        chkAddVCS.setEnabled(editMode);
        if(!editMode) {
            txtVCSComment.setEnabled(false);
        }
        cmdIssueNoteNew.setEnabled(editMode);
        cmdCustomFields.setEnabled(editMode);
        cmdIssueAttachmentNew.setEnabled(editMode);
        cmbIssueReporterName.setEnabled(editMode);
        cmbIssueCategory.setEnabled(editMode);
        cmbIssueTargetVersion.setEnabled(editMode);
        cmbIssueFixedInVersion.setEnabled(editMode);
        cmbIssuePriority.setEnabled(editMode);
        cmbIssueSeverity.setEnabled(editMode);
        cmbIssueStatus.setEnabled(editMode);
        cmdVersionAdd.setEnabled(editMode);
        cmbBasicsTags.setEnabled(editMode);
        txtBasicsTags.setEnabled(editMode);
        tblIssues.setEnabled(!editMode);
        cmbIssueProfile.setEnabled(
                editMode);
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
        txtVCSComment.setText("VCS-Comment");
        cmbIssueReporterName.setSelectedItem(null);
        cmbIssueCategory.setSelectedItem(null);
        cmbIssueTargetVersion.setSelectedItem(null);
        cmbIssueFixedInVersion.setSelectedItem(null);
        cmbIssuePriority.setSelectedItem(null);
        cmbIssueSeverity.setSelectedItem(null);
        cmbIssueStatus.setSelectedItem(null);
        cmbBasicsTags.setSelectedItem(null);
        cmbIssueProfile.setSelectedItem(null);
        txtBasicsTags.setText("");
        tblIssueNotes.removeAll();
        tblIssueAttachments.removeAll();
        txtIssueReporterEMail.setText("");
        txtIssueReporterName.setText("");
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

        List<String> categories = api.getCategories(this.settings.getProjectID());

        cmbBasicsTags.removeAllItems();
        api.getTags().forEach(tag -> cmbBasicsTags.addItem(tag.getName()));
        cmbBasicsTags.addItem("");
        txtBasicsTags.setText("");
        cmbBasicsTags.setSelectedItem("");

        cmbIssueCategory.removeAllItems();
        if(categories!=null) {
            for (String category : categories) {
                cmbIssueCategory.addItem(category);
            }
        }

        this.loadVersions(api);

        try {
            if(!access.getValue().equals("viewer") && !access.getValue().equals("reporter") && !access.getValue().equals("updater")) {
                List<MantisUser> user = api.getUsers(this.settings.getProjectID());

                if (user != null) {
                    cmbIssueReporterName.removeAllItems();
                    for (MantisUser usr : user) {
                        cmbIssueReporterName.addItem(usr.getUserName());
                    }
                    cmbIssueReporterName.addItem("");
                    cmbIssueNoteReporterUser.removeAllItems();
                    for (MantisUser usr : user) {
                        cmbIssueNoteReporterUser.addItem(usr.getUserName());
                    }
                    cmbIssueNoteReporterUser.addItem("");
                }
            }
        } catch (Exception ex) {
            Helper.printException(ex);
        }

        List<ObjectRef> priorities = api.getEnum("priorities");

        if(priorities!=null) {
            cmbIssuePriority.removeAllItems();
            for(ObjectRef priority : priorities) {
                cmbIssuePriority.addItem(priority.getName());
            }
        }

        List<ObjectRef> severities = api.getEnum("severities");

        if(severities!=null) {
            cmbIssueSeverity.removeAllItems();
            for(ObjectRef severity : severities) {
                cmbIssueSeverity.addItem(severity.getName());
            }
        }

        List<ObjectRef> states = api.getEnum("status");

        if(states!=null) {
            cmbIssueStatus.removeAllItems();
            for(ObjectRef status : states) {
                cmbIssueStatus.addItem(status.getName());
            }
        }

        List<ObjectRef> viewStates = api.getEnum("view_states");

        if(viewStates!=null) {
            cmbIssueNoteViewState.removeAllItems();
            for(ObjectRef viewState : viewStates) {
                cmbIssueNoteViewState.addItem(viewState.getName());
            }
        }

        this.loadComboBoxes = true;
    }

    private void loadVersions(MantisSoapAPI api) {
        List<MantisVersion> versions = api.getVersions(this.settings.getProjectID());

        if(versions!=null) {
            cmbIssueTargetVersion.removeAllItems();
            for (MantisVersion version : versions) {
                cmbIssueTargetVersion.addItem(version.getId() + ": " + version.getName());
            }

            cmbIssueFixedInVersion.removeAllItems();
            for (MantisVersion version : versions) {
                cmbIssueFixedInVersion.addItem(version.getId() + ": " + version.getName());
            }
        }
    }

    private void checkRights() {
        if(access!=null) {
            switch (access.getValue()) {
                case "viewer":
                    cmdIssueDelete.setVisible(false);
                    cmdIssueEdit.setVisible(false);
                    cmdIssueNew.setVisible(false);
                    cmdIssueSave.setVisible(false);
                    cmdIssueAbort.setVisible(false);
                    cmdIssueNoteEdit.setVisible(false);
                    cmdIssueNoteDelete.setVisible(false);
                    cmdIssueNoteNew.setVisible(false);
                    cmdIssueNoteSave.setVisible(false);
                    cmdIssueNoteAbort.setVisible(false);
                    cmdIssueAttachmentEdit.setVisible(false);
                    cmdIssueAttachmentDelete.setVisible(false);
                    cmdIssueAttachmentNew.setVisible(false);
                    cmdIssueAttachmentSave.setVisible(false);
                    cmdIssueAttachmentAbort.setVisible(false);
                    cmbIssueStatus.setVisible(true);
                    lblIssueStatus.setVisible(true);
                    lblIssueFixedInVersion.setVisible(true);
                    cmbIssueFixedInVersion.setVisible(true);
                    break;
                case "reporter":
                    cmdIssueDelete.setVisible(false);
                    cmdIssueEdit.setVisible(false);
                    cmdIssueNew.setVisible(true);
                    cmdIssueSave.setVisible(true);
                    cmdIssueAbort.setVisible(true);
                    cmdIssueNoteEdit.setVisible(false);
                    cmdIssueNoteDelete.setVisible(false);
                    cmdIssueNoteNew.setVisible(true);
                    cmdIssueNoteSave.setVisible(true);
                    cmdIssueNoteAbort.setVisible(true);
                    cmdIssueAttachmentEdit.setVisible(false);
                    cmdIssueAttachmentDelete.setVisible(false);
                    cmdIssueAttachmentNew.setVisible(true);
                    cmdIssueAttachmentSave.setVisible(true);
                    cmdIssueAttachmentAbort.setVisible(true);
                    cmbIssueStatus.setVisible(false);
                    lblIssueStatus.setVisible(false);
                    lblIssueFixedInVersion.setVisible(false);
                    cmbIssueFixedInVersion.setVisible(false);
                    break;
                case "updater":
                    cmdIssueDelete.setVisible(false);
                    cmdIssueEdit.setVisible(true);
                    cmdIssueNew.setVisible(true);
                    cmdIssueSave.setVisible(true);
                    cmdIssueAbort.setVisible(true);
                    cmdIssueNoteEdit.setVisible(false);
                    cmdIssueNoteDelete.setVisible(false);
                    cmdIssueNoteNew.setVisible(true);
                    cmdIssueNoteSave.setVisible(true);
                    cmdIssueNoteAbort.setVisible(true);
                    cmdIssueAttachmentEdit.setVisible(false);
                    cmdIssueAttachmentDelete.setVisible(false);
                    cmdIssueAttachmentNew.setVisible(true);
                    cmdIssueAttachmentSave.setVisible(true);
                    cmdIssueAttachmentAbort.setVisible(true);
                    cmbIssueStatus.setVisible(false);
                    lblIssueStatus.setVisible(false);
                    lblIssueFixedInVersion.setVisible(false);
                    cmbIssueFixedInVersion.setVisible(false);
                    break;
                case "developer":
                    cmdIssueDelete.setVisible(true);
                    cmdIssueEdit.setVisible(true);
                    cmdIssueNew.setVisible(true);
                    cmdIssueSave.setVisible(true);
                    cmdIssueAbort.setVisible(true);
                    cmdIssueNoteEdit.setVisible(false);
                    cmdIssueNoteDelete.setVisible(false);
                    cmdIssueNoteNew.setVisible(true);
                    cmdIssueNoteSave.setVisible(true);
                    cmdIssueNoteAbort.setVisible(true);
                    cmdIssueAttachmentEdit.setVisible(false);
                    cmdIssueAttachmentDelete.setVisible(false);
                    cmdIssueAttachmentNew.setVisible(true);
                    cmdIssueAttachmentSave.setVisible(true);
                    cmdIssueAttachmentAbort.setVisible(true);
                    cmbIssueStatus.setVisible(false);
                    lblIssueStatus.setVisible(false);
                    lblIssueFixedInVersion.setVisible(false);
                    cmbIssueFixedInVersion.setVisible(false);
                    break;
                default:
                    cmdIssueDelete.setVisible(true);
                    cmdIssueEdit.setVisible(true);
                    cmdIssueNew.setVisible(true);
                    cmdIssueSave.setVisible(true);
                    cmdIssueAbort.setVisible(true);
                    cmdIssueNoteEdit.setVisible(true);
                    cmdIssueNoteDelete.setVisible(true);
                    cmdIssueNoteNew.setVisible(true);
                    cmdIssueNoteSave.setVisible(true);
                    cmdIssueNoteAbort.setVisible(true);
                    cmdIssueAttachmentEdit.setVisible(true);
                    cmdIssueAttachmentDelete.setVisible(true);
                    cmdIssueAttachmentNew.setVisible(true);
                    cmdIssueAttachmentSave.setVisible(true);
                    cmdIssueAttachmentAbort.setVisible(true);
                    cmbIssueStatus.setVisible(true);
                    lblIssueStatus.setVisible(true);
                    lblIssueFixedInVersion.setVisible(true);
                    cmbIssueFixedInVersion.setVisible(true);
                    break;
            }
        }
    }

    private DefaultTableModel addColumnsToIssueTable() {
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("ID");
        model.addColumn("Summary");
        model.addColumn("Status");
        return model;
    }

    private DefaultTableModel addColumnsToIssueNoteTable() {
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("ID");
        model.addColumn("Text");
        model.addColumn("View");
        return model;
    }

    private DefaultTableModel addColumnsToIssueAttachmentTable() {
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("ID");
        model.addColumn("File-Name");
        return model;
    }

    private void loadList(DefaultTableModel tblIssueModel, ProgressManager manager) {
        pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Task task =
                new Task.Backgroundable(project, "Load Issues...", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        try {
                            controlPagination();
                            if(!settings.validateSettings()) {
                                Helper.printNotification("Wrong settings!", "The connection-settings are incorrect!", NotificationType.WARNING);
                                state = false;
                                tblIssues.removeAll();
                                for (int i = tblIssueModel.getRowCount() - 1; i >= 0; i--) {
                                    tblIssueModel.removeRow(i);
                                }
                            } else {

                                state = true;
                                tblIssues.removeAll();
                                for (int i = tblIssueModel.getRowCount() - 1; i >= 0; i--) {
                                    tblIssueModel.removeRow(i);
                                }
                                tblIssues.setModel(tblIssueModel);
                                String filterID = "";
                                if(cmbFilters.getSelectedItem()!=null) {
                                    if(!cmbFilters.getSelectedItem().toString().equals("")) {
                                        filterID = cmbFilters.getSelectedItem().toString().split(":")[0].trim();
                                    }
                                }
                                List<MantisIssue> mantisIssues = new MantisSoapAPI(settings).getIssues(settings.getProjectID(), page, filterID);
                                progressIndicator.setFraction(0.0);
                                double factor = 100.0 / mantisIssues.size();
                                for(MantisIssue issue : mantisIssues) {
                                    tblIssueModel.addRow(new Object[]{issue.getId(), issue.getSummary(), issue.getStatus()});
                                    progressIndicator.setFraction(progressIndicator.getFraction() + factor);
                                }
                                tblIssues.setModel(tblIssueModel);
                                cmdIssueNew.setEnabled(true);
                            }
                        } catch (Exception ex) {
                            Helper.printException(ex);
                        } finally {
                            pnlMain.setCursor(Cursor.getDefaultCursor());
                        }
                    }
                };
        manager.run(task);
    }

    private void controlPagination() {
        if(settings.getItemsPerPage()==-1) {
            cmdBack.setEnabled(false);
            cmdForward.setEnabled(false);
        } else {
            cmdBack.setEnabled(true);
            cmdForward.setEnabled(true);
        }
    }
}
