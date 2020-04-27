package de.domjos.ideaMantis.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import de.domjos.ideaMantis.model.MantisIssue;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.soap.MantisSoapAPI;
import de.domjos.ideaMantis.soap.ObjectRef;
import de.domjos.ideaMantis.ui.FixDialog;

import java.io.File;

public class ResolveToDoAsBugEditorAction extends AnAction {
    private Editor editor;
    private String content;

    @Override
    public void update(final AnActionEvent e) {

        //Get all the required data from data keys
        this.editor = e.getRequiredData(CommonDataKeys.EDITOR);

        // Get Caret-Model
        if(editor.getCaretModel().getCaretCount()>=1) {
            if(content!=null) {
                int pos = editor.getCaretModel().getCurrentCaret().getSelectionStart();
                String text = editor.getDocument().getText().substring(pos);

                if(text.contains("\n")) {
                    this.content = text.substring(0, text.indexOf("\n"));

                    e.getPresentation().setVisible(content.contains("Mantis#"));
                }
            }
        }


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
            MantisSoapAPI api = new MantisSoapAPI(ConnectionSettings.getInstance(project));
            MantisIssue issue = api.getIssue(id[0]);

            if(issue!=null) {
                FixDialog fixDialog = new FixDialog(project, issue.getId());
                fixDialog.show();

                for(ObjectRef ref : api.getEnum("status")) {
                    if(ref.getId()==80) {
                        issue.setStatus(ref.getName());
                        try {
                            api.addIssue(issue);
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                        break;
                    }
                }
            }
        }
    }
}
