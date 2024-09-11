package de.domjos.ideaMantis.editor;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MarkedTextAsBugEditorAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

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
        String path = Helper.getPathToDocument(document);
        MarkedTextAsBugDialog markedTextAsBugDialog;
        if(new File(path).exists()) {
            markedTextAsBugDialog = new MarkedTextAsBugDialog(project, document.getText(new TextRange(start, end)), path);
        } else {
            markedTextAsBugDialog = new MarkedTextAsBugDialog(project, document.getText(new TextRange(start, end)));
        }
        markedTextAsBugDialog.show();
        selectionModel.removeSelection();
    }
}
