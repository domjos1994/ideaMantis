package de.domjos.ideaMantis.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;

import java.io.File;

public class MarkedTextAsBugEditorAction extends AnAction {
    @Override
    public void update(final AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setVisible((project != null && editor != null && editor.getSelectionModel().hasSelection()));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //Get all the required data from data keys
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        //Access document, caret, and selection
        final Document document = editor.getDocument();
        final SelectionModel selectionModel = editor.getSelectionModel();

        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();

        //Making the replacement
        final String[] pathToDocument = new String[1];
        WriteCommandAction.runWriteCommandAction(project,()->{
            String pathToDocumentArray[] = document.toString().split("file://");
            if(pathToDocumentArray.length==2) {
                pathToDocument[0] = pathToDocumentArray[1].replace("]", "").trim();
            } else {
                pathToDocument[0] = document.toString();
            }
        });

        MarkedTextAsBugDialog markedTextAsBugDialog;
        if(new File(pathToDocument[0]).exists()) {
            markedTextAsBugDialog = new MarkedTextAsBugDialog(project, document.getText(new TextRange(start, end)), pathToDocument[0]);
        } else {
            markedTextAsBugDialog = new MarkedTextAsBugDialog(project, document.getText(new TextRange(start, end)));
        }
        markedTextAsBugDialog.show();
        selectionModel.removeSelection();
    }
}
