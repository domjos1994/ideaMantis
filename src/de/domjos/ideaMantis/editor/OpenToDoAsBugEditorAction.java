package de.domjos.ideaMantis.editor;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.utils.Helper;

import java.awt.*;
import java.net.URI;

public class OpenToDoAsBugEditorAction extends AnAction {
    private Editor editor;
    private String content;

    @Override
    public void update(final AnActionEvent e) {

        //Get all the required data from data keys
        this.editor = e.getRequiredData(CommonDataKeys.EDITOR);

        // Get Caret-Model
        if(editor.getCaretModel().getCaretCount()>=1) {
            int pos = editor.getCaretModel().getCurrentCaret().getSelectionStart();
            String text = editor.getDocument().getText().substring(pos);
            this.content = text.substring(0, text.indexOf("\n"));
        }

        e.getPresentation().setVisible(content.contains("Mantis#"));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getData(CommonDataKeys.PROJECT);

        //Making the replacement
        final int[] id = new int[1];
        WriteCommandAction.runWriteCommandAction(project,()->{
            try {
                this.content = this.content.substring(this.content.indexOf("Mantis#")+7);
                id[0] = Integer.parseInt(this.content.split(" ")[0]);
            } catch (Exception ex) {
                id[0] = 0;
            }
        });

        if(id[0]!=0) {
            try {
                String hostName = ConnectionSettings.getInstance(project).getHostName();
                if(hostName.trim().endsWith("/")) {
                    hostName = hostName.substring(0, hostName.length()-1);
                }
                String url = String.format("%s/view.php?id=%s", hostName, id[0]);
                if(Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            } catch (Exception ex) {
                Helper.printException(ex);
            }
        }
    }
}
