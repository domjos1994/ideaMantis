package de.domjos.ideaMantis.ui;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.ChangeProvider;
import com.intellij.openapi.vcs.changes.VcsDirtyScope;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vcs.ex.ProjectLevelVcsManagerEx;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

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
    private JCheckBox chkBasicsCheckIn;

    private JTable tblIssues, tblIssueAttachments, tblIssueNotes;

    private JProgressBar pbMain;
    private JLabel lblValidation;
    private ResourceBundle bundle;
    private boolean state = false;


    public IdeaMantisIssues() {
        bundle = Helper.getBundle();
        tblIssues.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblIssueAttachments.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblIssueNotes.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableModel tblIssueModel = this.addColumnsToIssueTable();
        DefaultTableModel tblIssueAttachmentModel = this.addColumnsToIssueAttachmentTable();
        DefaultTableModel tblIssueNoteModel = this.addColumnsToIssueNoteTable();
        controlIssues(false, false);
        cmdIssueNew.setEnabled(false);
        tblIssueAttachments.setDragEnabled(true);
        tblIssueAttachments = Helper.disableEditingTable(tblIssueAttachments);
        chkBasicsCheckIn.setVisible(false);


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
                    Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
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
                ProjectLevelVcsManager manager = ProjectLevelVcsManager.getInstance(project);
                if(manager.hasActiveVcss()) {
                    for(AbstractVcs vcs : manager.getAllActiveVcss()) {
                        Pair<VcsRevisionNumber, List> pair = vcs.getOutgoingChangesProvider().getOutgoingChanges(manager.getAllVersionedRoots()[0], true);

                    }
                }
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if(!settings.validateSettings()) {
                    Helper.printNotification(bundle.getString("message.wrongSettings.header"), bundle.getString("message.wrongSettings.content"),NotificationType.ERROR);
                    this.state = false;
                    tblIssues.removeAll();
                    for (int i = tblIssueModel.getRowCount() - 1; i >= 0; i--) {
                        tblIssueModel.removeRow(i);
                    }
                } else {
                    this.state = true;
                    tblIssues.removeAll();
                    for (int i = tblIssueModel.getRowCount() - 1; i >= 0; i--) {
                        tblIssueModel.removeRow(i);
                    }
                    tblIssues.setModel(tblIssueModel);
                    this.loadComboBoxes();
                    List<MantisIssue> mantisIssues = new MantisSoapAPI(this.settings).getIssues(this.settings.getProjectID());
                    pbMain.setMinimum(0);
                    pbMain.setValue(pbMain.getMinimum());
                    pbMain.setMaximum(mantisIssues.size());
                    for(MantisIssue issue : mantisIssues) {
                        tblIssueModel.addRow(new Object[]{issue.getId(), issue.getSummary(), issue.getStatus()});
                        pbMain.setValue((pbMain.getValue()+1));
                        pbMain.updateUI();
                    }
                    tblIssues.setModel(tblIssueModel);
                    cmdIssueNew.setEnabled(true);
                }
                resetIssues();
                if(tblIssues.getColumnCount()>=1) {
                    tblIssues.getColumnModel().getColumn(0).setWidth(40);
                    tblIssues.getColumnModel().getColumn(0).setMaxWidth(100);
                }
            } catch (Exception ex) {
                Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
            } finally {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                pbMain.setValue(pbMain.getMinimum());
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
                        pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        int id = Integer.parseInt(tblIssueModel.getValueAt(tblIssues.getSelectedRow(), 0).toString());
                        MantisSoapAPI api = new MantisSoapAPI(settings);
                        MantisIssue issue = api.getIssue(id);
                        currentIssue = issue;
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
                    Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
                } finally {
                    pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
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
                        Helper.printNotification(bundle.getString("message.error.header"), String.format(bundle.getString("message.cantDelete"), bundle.getString("issue.header")), NotificationType.ERROR);
                    }
                    controlIssues(false, false);
                    cmdReload.doClick();
                }
            } catch (Exception ex) {
                Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
            } finally {
                pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        cmdIssueSave.addActionListener(e -> {
           try {
               pnlMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               lblValidation.setText(this.validateIssue());
               if(lblValidation.getText().equals("")) {

                   MantisIssue issue = currentIssue;
                   issue.setSummary(txtIssueSummary.getText());
                   issue.setDate_submitted(txtIssueDate.getText());
                   issue.setAdditional_information(txtIssueAdditionalInformation.getText());
                   issue.setDescription(txtIssueDescription.getText());
                   issue.setSteps_to_reproduce(txtIssueStepsToReproduce.getText());

                   if(cmbIssueCategory.getSelectedItem()!=null)
                       issue.setCategory(cmbIssueCategory.getSelectedItem().toString());
                   if(cmbIssuePriority.getSelectedItem()!=null)
                       issue.setPriority(cmbIssuePriority.getSelectedItem().toString());
                   if(cmbIssueSeverity.getSelectedItem()!=null)
                       issue.setSeverity(cmbIssueSeverity.getSelectedItem().toString());
                   if(cmbIssueStatus.getSelectedItem()!=null)
                       issue.setStatus(cmbIssueStatus.getSelectedItem().toString());

                   if(cmbIssueFixedInVersion.getSelectedItem()!=null)
                       issue.setFixed_in_version(cmbIssueFixedInVersion.getSelectedItem().toString());
                   if(cmbIssueTargetVersion.getSelectedItem()!=null)
                       issue.setTarget_version(cmbIssueTargetVersion.getSelectedItem().toString());

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

                   if(!new MantisSoapAPI(this.settings).addIssue(issue)) {
                       Helper.printNotification(bundle.getString("message.error.header"), String.format(bundle.getString("message.cantAdd"), bundle.getString("issue.header")), NotificationType.ERROR);
                   }
                   if(chkBasicsCheckIn.isSelected()) {
                       FixDialog dialog = new FixDialog(project, issue.getId());
                       dialog.show();
                   }
                   controlIssues(false, false);
                   cmdReload.doClick();
               }
           } catch (Exception ex) {
               Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
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
                Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
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
                        Helper.printNotification(bundle.getString("message.error.header"), String.format(bundle.getString("message.cantDelete"), bundle.getString("notes.header")), NotificationType.ERROR);
                    }
                    int nid = tblIssueNotes.getSelectedRow();
                    tblIssueAttachments.getSelectionModel().clearSelection();
                    tblIssueNoteModel.removeRow(nid);
                    tblIssueNotes.setModel(tblIssueNoteModel);
                }
                controlNotes(false, false);
            } catch (Exception ex) {
                Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
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
                            Helper.printNotification(bundle.getString("message.error.header"), String.format(bundle.getString("message.cantAdd"), bundle.getString("notes.header")), NotificationType.ERROR);
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
                Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
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
                Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
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
                        Helper.printNotification(bundle.getString("message.error.header"), String.format(bundle.getString("message.cantDelete"), bundle.getString("attachments.header")), NotificationType.ERROR);
                    }
                    int aid = tblIssueAttachments.getSelectedRow();
                    tblIssueAttachments.getSelectionModel().clearSelection();
                    tblIssueAttachmentModel.removeRow(aid);
                    tblIssueAttachments.setModel(tblIssueAttachmentModel);
                    controlAttachments(false, false);
                }
            } catch (Exception ex) {
                Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
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
                            Helper.printNotification(bundle.getString("message.error.header"), String.format(bundle.getString("message.cantAdd"), bundle.getString("attachments.header")), NotificationType.ERROR);
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
                Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
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
                Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
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
                Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
            }
        });
    }

    private String validateIssue() {
        if(txtIssueDescription.getText().equals("")) {
            return String.format(bundle.getString("messages.mandatory"), bundle.getString("descriptions.description").replace("*", ""));
        }
        if(txtIssueSummary.getText().equals("")) {
            return String.format(bundle.getString("messages.mandatory"), bundle.getString("basics.summary").replace("*", ""));
        }
        if(cmbIssueCategory.getSelectedItem()==null) {
            return String.format(bundle.getString("messages.mandatory"), bundle.getString("basics.category").replace("*", ""));
        }
        return "";
    }

    private String validateAttachment() {
        if(txtIssueAttachmentFileName.getText().equals("")) {
            return String.format(bundle.getString("messages.mandatory"), bundle.getString("attachments.fileName").replace("*", ""));
        }
        return "";
    }

    private String validateNote() {
        if(txtIssueNoteText.getText().equals("")) {
            return String.format(bundle.getString("messages.mandatory"), bundle.getString("notes.text").replace("*", ""));
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
        cmdReload.doClick();
        if(state)
            this.loadComboBoxes();
        resetIssues();
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
        chkBasicsCheckIn.setEnabled(editMode);
        if(!editMode) {
            chkBasicsCheckIn.setSelected(false);
        }
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
        cmbIssueReporterName.setSelectedItem(null);
        cmbIssueCategory.setSelectedItem(null);
        cmbIssueTargetVersion.setSelectedItem(null);
        cmbIssueFixedInVersion.setSelectedItem(null);
        cmbIssuePriority.setSelectedItem(null);
        cmbIssueSeverity.setSelectedItem(null);
        cmbIssueStatus.setSelectedItem(null);
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

        cmbIssueCategory.removeAllItems();
        if(categories!=null) {
            for (String category : categories) {
                cmbIssueCategory.addItem(category);
            }
        }

        List<String> versions = api.getVersions(this.settings.getProjectID());

        if(versions!=null) {
            cmbIssueTargetVersion.removeAllItems();
            for (String version : versions) {
                cmbIssueTargetVersion.addItem(version);
            }

            cmbIssueFixedInVersion.removeAllItems();
            for (String version : versions) {
                cmbIssueFixedInVersion.addItem(version);
            }
        }

        try {
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
        } catch (Exception ex) {
            Helper.printNotification(bundle.getString("message.error.header"), ex.toString(), NotificationType.ERROR);
        }

        List<String> priorities = api.getEnum("priorities");

        if(priorities!=null) {
            cmbIssuePriority.removeAllItems();
            for(String priority : priorities) {
                cmbIssuePriority.addItem(priority);
            }
        }

        List<String> severities = api.getEnum("severities");

        if(severities!=null) {
            cmbIssueSeverity.removeAllItems();
            for(String severity : severities) {
                cmbIssueSeverity.addItem(severity);
            }
        }

        List<String> states = api.getEnum("status");

        if(states!=null) {
            cmbIssueStatus.removeAllItems();
            for(String status : states) {
                cmbIssueStatus.addItem(status);
            }
        }

        List<String> viewStates = api.getEnum("view_states");

        if(viewStates!=null) {
            cmbIssueNoteViewState.removeAllItems();
            for(String viewState : viewStates) {
                cmbIssueNoteViewState.addItem(viewState);
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
        model.addColumn(bundle.getString("id"));
        model.addColumn(bundle.getString("basics.summary").replace("*", ""));
        model.addColumn(bundle.getString("basics.status").replace("*", ""));
        return model;
    }

    private DefaultTableModel addColumnsToIssueNoteTable() {
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn(bundle.getString("id"));
        model.addColumn(bundle.getString("notes.text").replace("*", ""));
        model.addColumn(bundle.getString("notes.view").replace("*", ""));
        return model;
    }

    private DefaultTableModel addColumnsToIssueAttachmentTable() {
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn(bundle.getString("id"));
        model.addColumn(bundle.getString("attachments.fileName").replace("*", ""));
        return model;
    }
}
