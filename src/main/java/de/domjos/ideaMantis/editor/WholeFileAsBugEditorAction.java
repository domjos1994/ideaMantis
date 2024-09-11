package de.domjos.ideaMantis.editor;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class WholeFileAsBugEditorAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(final @NotNull AnActionEvent e) {
        super.update(e);
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setVisible((project != null && editor != null));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //Get all the required data from data keys
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        //Access document, caret, and selection
        final Document document = editor.getDocument();

        //Making the replacement
        String path = Helper.getPathToDocument(document);
        MarkedTextAsBugDialog markedTextAsBugDialog;
        if(new File(path).exists()) {
            markedTextAsBugDialog = new MarkedTextAsBugDialog(project, "", path);
        } else {
            markedTextAsBugDialog = new MarkedTextAsBugDialog(project, "");
        }
        markedTextAsBugDialog.show();
    }
}
