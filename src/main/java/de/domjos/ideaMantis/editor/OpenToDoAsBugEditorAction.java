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
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import de.domjos.ideaMantis.service.ConnectionSettings;
import de.domjos.ideaMantis.utils.Helper;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.URI;

public class OpenToDoAsBugEditorAction extends AnAction {
    private String content;

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }


    @Override
    public void update(final AnActionEvent e) {

        //Get all the required data from data keys
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);

        // Get Caret-Model
        if(editor.getCaretModel().getCaretCount()>=1) {
            int pos = editor.getCaretModel().getCurrentCaret().getSelectionStart();
            String text = editor.getDocument().getText().substring(pos);
            if(!text.isEmpty()) {
                this.content = text.substring(0, text.indexOf("\n"));
            }
        }

        e.getPresentation().setVisible(content.contains("Mantis#"));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getData(CommonDataKeys.PROJECT);

        //Making the replacement
        int id = Helper.getId(this.content);
        if(id!=0) {
            try {
                assert project != null;
                String hostName = ConnectionSettings.getInstance(project).getHostName();
                if(hostName.trim().endsWith("/")) {
                    hostName = hostName.substring(0, hostName.length()-1);
                }
                String url = String.format("%s/view.php?id=%s", hostName, id);
                if(Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            } catch (Exception ex) {
                Helper.printException(ex);
            }
        }
    }
}
