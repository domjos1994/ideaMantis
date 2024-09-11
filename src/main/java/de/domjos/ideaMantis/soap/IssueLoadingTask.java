package de.domjos.ideaMantis.soap;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import de.domjos.ideaMantis.custom.IssueTableCellRenderer;
import de.domjos.ideaMantis.lang.Lang;
import de.domjos.ideaMantis.model.MantisIssue;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class IssueLoadingTask extends Task.Backgroundable {
    private Action before, after;
    private final ConnectionSettings settings;
    private boolean state;
    private final JTable tblIssues;
    private final DefaultTableModel tblIssueModel;
    private final JComboBox<String> cmbFilters;
    private final int page;
    private final MantisSoapAPI api;

    public IssueLoadingTask(ConnectionSettings settings, JTable tblIssues, DefaultTableModel tblIssueModel, JComboBox<String> cmbFilters, int page) {
        super(Helper.getProject(), Lang.RELOAD_ISSUES, true);

        this.settings = settings;
        this.tblIssues = tblIssues;
        this.tblIssueModel = tblIssueModel;
        this.cmbFilters = cmbFilters;
        this.page = page;
        this.state = false;

        this.api = new MantisSoapAPI(this.settings);
    }

    public void before(Action action) {
        this.before = action;
    }

    public void after(Action action) {
        this.after = action;
    }

    public boolean getState() {
        return this.state;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        try {
            if(this.before != null) {
                this.before.execute();
            }

            if(!settings.validateSettings()) {
                Helper.printWrongSettingsMsg();
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
                    if(!cmbFilters.getSelectedItem().toString().isEmpty()) {
                        filterID = cmbFilters.getSelectedItem().toString().split(":")[0].trim();
                    }
                }
                List<MantisIssue> mantisIssues = this.api.getIssues(this.page, filterID);
                progressIndicator.setFraction(0.0);

                double factor = 100.0 / mantisIssues.size();
                for(MantisIssue issue : mantisIssues) {
                    tblIssueModel.addRow(new Object[]{getStringId(issue.getId()), issue.getSummary(), issue.getStatus()});
                    progressIndicator.setFraction(progressIndicator.getFraction() + factor);
                }
                tblIssues.setModel(tblIssueModel);
                tblIssues.setDefaultRenderer(Object.class, new IssueTableCellRenderer());
            }
        } catch (Exception ex) {
            this.setCancelText(ex.getMessage());
        } finally {
            if(this.after != null) {
                this.after.execute();
            }
        }
    }

    @FunctionalInterface
    public interface Action {
        void execute();
    }

    private String getStringId(int id) {
        String idWithoutZeros = String.valueOf(id);

        StringBuilder idWithZeros = new StringBuilder(idWithoutZeros);
        for(int i = idWithoutZeros.length(); i<=7; i++) {
            idWithZeros.insert(0, "0");
        }
        return idWithZeros.toString();
    }
}
