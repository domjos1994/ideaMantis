package de.domjos.ideaMantis.editor;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import de.domjos.ideaMantis.model.MantisIssue;
import de.domjos.ideaMantis.model.MantisVersion;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.ui.ChooseVersionDialog;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CreateChangeLogAction extends AnAction {
    private StringBuilder builder;

    public CreateChangeLogAction() {
        this.builder = new StringBuilder();
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();

        ConnectionSettings connectionSettings = ConnectionSettings.getInstance(project);
        if(!connectionSettings.validateSettings()) {
            Helper.printNotification("Wrong settings!", "The connection-settings are incorrect!", NotificationType.WARNING);
            return;
        }

        ChooseVersionDialog dialog = new ChooseVersionDialog(project);
        if(dialog.showAndGet()) {
            ProgressManager manager = ProgressManager.getInstance();
            Task.WithResult<String, Exception> task = new Task.WithResult<String, Exception>(project, "Load ChangeLog", true) {
                @Override
                protected String compute(@NotNull ProgressIndicator progressIndicator) {
                    progressIndicator.setFraction(0.0);
                    Map < MantisIssue, MantisVersion > changeLog = new MantisSoapAPI(connectionSettings).createChangeLog(ChooseVersionDialog.currentVersion);
                    Map<MantisVersion, List<MantisIssue>> resortedMap = new LinkedHashMap<>();
                    double factor = 1.0 / changeLog.entrySet().size();
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
                        progressIndicator.setFraction(progressIndicator.getFraction() + factor);
                    }
                    progressIndicator.setFraction(0.0);
                    factor = 1.0 / resortedMap.entrySet().size();
                    for(Map.Entry<MantisVersion, List<MantisIssue>> entry : resortedMap.entrySet()) {
                        if(builder.toString().equals("")) {
                            builder.append("Resolved Bugs in version ");
                            builder.append(entry.getKey().getName());
                            builder.append(":\n");
                        } else {
                            builder.append("\n\nResolved Bugs in version ");
                            builder.append(entry.getKey().getName());
                            builder.append(":\n");
                        }
                        for(MantisIssue issue : entry.getValue()) {
                            builder.append(String.format("%s: %s --> %s\n", issue.getId(), issue.getSummary(), issue.getStatus()));
                        }
                        progressIndicator.setFraction(progressIndicator.getFraction() + factor);
                    }
                    return builder.toString();
                }
            };
            try {
                manager.run(task);
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        document.setText(task.getResult());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                });
            } catch (Exception ex) {
                Helper.printException(ex);
            }
        }
    }
}
