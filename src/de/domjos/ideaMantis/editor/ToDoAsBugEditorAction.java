package de.domjos.ideaMantis.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import org.jdom.Element;

import java.io.File;

public class ToDoAsBugEditorAction extends AnAction {
    private Editor editor;
    private String content, originalContent;

    @Override
    public void update(final AnActionEvent e) {

        //Get all the required data from data keys
        this.editor = e.getRequiredData(CommonDataKeys.EDITOR);

        // Get Caret-Model
        if(editor.getCaretModel().getCaretCount()>=1) {
            int pos = editor.getCaretModel().getCurrentCaret().getSelectionStart();
            String text = editor.getDocument().getText().substring(pos);
            this.content = text.substring(0, text.indexOf("\n"));
            this.originalContent = this.content;
        }

        e.getPresentation().setVisible(content.toLowerCase().contains("todo") && !content.toLowerCase().contains("mantis#"));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
        final Document document = this.editor.getDocument();

        //Making the replacement
        final String[] pathToDocument = new String[1];
        WriteCommandAction.runWriteCommandAction(project,()->{
            String pathToDocumentArray[] = document.toString().split("file://");
            if(pathToDocumentArray.length==2) {
                pathToDocument[0] = pathToDocumentArray[1].replace("]", "").trim();
            } else {
                pathToDocument[0] = document.toString();
            }

            // replace comment
            if(this.content.toLowerCase().contains("todo:")) {
                this.content = this.content.toLowerCase().substring(this.content.toLowerCase().indexOf("todo:") + 5).trim();
            } else if(this.content.toLowerCase().contains("todo")) {
                this.content = this.content.toLowerCase().substring(this.content.toLowerCase().indexOf("todo") + 4).trim();
            }
        });

        MarkedTextAsBugDialog markedTextAsBugDialog;
        if(new File(pathToDocument[0]).exists()) {
            markedTextAsBugDialog = new MarkedTextAsBugDialog(project, this.content, pathToDocument[0]);
        } else {
            markedTextAsBugDialog = new MarkedTextAsBugDialog(project, this.content);
        }
        markedTextAsBugDialog.show();
        int id = markedTextAsBugDialog.getID();
        int index = this.originalContent.toLowerCase().indexOf("todo");
        String sub = this.originalContent.substring(0, index + 4);
        String newContent = sub + " Mantis#" + id + " " + this.originalContent.replace(sub, "");

        String text = editor.getDocument().getText().replace(this.originalContent, newContent);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            editor.getDocument().setText(text);
        });
    }
}
