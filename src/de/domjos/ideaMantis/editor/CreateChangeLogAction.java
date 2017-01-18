package de.domjos.ideaMantis.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import de.domjos.ideaMantis.model.MantisIssue;
import de.domjos.ideaMantis.model.MantisVersion;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.ui.ChooseVersionDialog;
import de.domjos.ideaMantis.utils.Helper;

import java.util.Map;

public class CreateChangeLogAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //Get all the required data from data keys
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        //Access document, caret, and selection
        final Document document = editor.getDocument();

        ChooseVersionDialog dialog = new ChooseVersionDialog(project, Helper.getBundle());
        if(dialog.showAndGet()) {
            Map<MantisIssue, MantisVersion> changeLog = new MantisSoapAPI(ConnectionSettings.getInstance(project)).createChangeLog(dialog.currentVersion);
            for(MantisIssue issue : changeLog.keySet()) {
                document.insertString(0, issue.getSummary());
            }
        }
    }
}
