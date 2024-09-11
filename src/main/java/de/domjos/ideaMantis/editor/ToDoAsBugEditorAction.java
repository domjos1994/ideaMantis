/*
 * Copyright (c) 2024 DOMINIC JOAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.ideaMantis.editor;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ToDoAsBugEditorAction extends AnAction {
    private Editor editor;
    private String content, originalContent;

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(final AnActionEvent e) {

        //Get all the required data from data keys
        this.editor = e.getRequiredData(CommonDataKeys.EDITOR);

        // Get Caret-Model
        if(editor.getCaretModel().getCaretCount()>=1) {
            if(this.content!=null) {
                int pos = editor.getCaretModel().getCurrentCaret().getSelectionStart();
                String text = editor.getDocument().getText().substring(pos);
                if(this.content.contains("\n")) {
                    this.content = text.substring(0, text.indexOf("\n"));
                }
                this.originalContent = this.content;
                e.getPresentation().setVisible(content.toLowerCase().contains("todo") && !content.toLowerCase().contains("mantis#"));
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
        final Document document = this.editor.getDocument();

        //Making the replacement
        final String[] pathToDocument = new String[1];
        WriteCommandAction.runWriteCommandAction(project,()->{
            String[] pathToDocumentArray = document.toString().split("file://");
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

        String text = getText(pathToDocument, project);
        WriteCommandAction.runWriteCommandAction(project, () -> editor.getDocument().setText(text));
    }

    private @NotNull String getText(String[] pathToDocument, Project project) {
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

        return editor.getDocument().getText().replace(this.originalContent, newContent);
    }
}
