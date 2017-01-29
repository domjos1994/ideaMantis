package de.domjos.ideaMantis.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import de.domjos.ideaMantis.model.MantisIssue;
import de.domjos.ideaMantis.model.MantisVersion;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.ui.ChooseVersionDialog;
import de.domjos.ideaMantis.utils.Helper;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CreateChangeLogAction extends AnAction {
    private String content = "";
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();


        ChooseVersionDialog dialog = new ChooseVersionDialog(project);
        if(dialog.showAndGet()) {
            Map<MantisIssue, MantisVersion> changeLog = new MantisSoapAPI(ConnectionSettings.getInstance(project)).createChangeLog(dialog.currentVersion);
            Map<MantisVersion, List<MantisIssue>> resortedMap = new LinkedHashMap<>();
            for(Map.Entry<MantisIssue, MantisVersion> entry : changeLog.entrySet()) {
                if(resortedMap.containsKey(entry.getValue())) {
                    List<MantisIssue> issueList = resortedMap.get(entry.getValue());
                    issueList.add(entry.getKey());
                    resortedMap.put(entry.getValue(), issueList);
                } else {
                    List<MantisIssue> issueList = new LinkedList<>();
                    issueList.add(entry.getKey());
                    resortedMap.put(entry.getValue(), issueList);
                }
            }
            for(Map.Entry<MantisVersion, List<MantisIssue>> entry : resortedMap.entrySet()) {
                if(content.equals("")) {
                    content = "Resolved Bugs in version " + entry.getKey().getName() + ":\n";
                } else {
                    content += "\n\nResolved Bugs in version " + entry.getKey().getName() + ":\n";
                }
                for(MantisIssue issue : entry.getValue()) {
                    content += String.format("%s: %s --> %s\n", issue.getId(), issue.getSummary(), issue.getStatus());
                }
            }
            ApplicationManager.getApplication().runWriteAction(() -> document.setText(content));
        }
    }
}
