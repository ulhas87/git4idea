package git4idea;
/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 *
 * Copyright 2008 MQSoftware
 * Authors: Mark Scott
 */

// THIS CLASS IS NOT USED..............

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.vcsUtil.VcsUtil;
import git4idea.commands.GitCommand;

import java.io.File;

/**
 * Listens for renames and moves.
 */
public class GitRenameListener implements RefactoringElementListener {
    private final Project project;
    private final VirtualFile vcsRoot;
    private final String originalFilename;

    GitRenameListener(String oldFilename, PsiElement elem) {
        originalFilename = oldFilename;
        project = elem.getProject();
        vcsRoot = VcsUtil.getVcsRootFor(project, new GitVirtualFile(project,oldFilename));
    }

    public void elementRenamed(PsiElement newElement) {
        if(newElement == null) return;
        // IDEA has already moved the file at this point...
        GitCommand cmd = new GitCommand(project, GitVcsSettings.getInstance(project), vcsRoot);
        VirtualFile newFile = newElement.getContainingFile().getVirtualFile();
        File newLoc = new File(newFile.getPath());
        File oldLoc = new File(originalFilename);
        try {
            if(newLoc.exists() && !oldLoc.exists()) // file already moved in local fs
                newLoc.renameTo(oldLoc);  // move back, let Git do the move
            cmd.move(new GitVirtualFile(project,originalFilename), newFile);
        } catch (SecurityException se) {
            Messages.showErrorDialog(project, se.getMessage(), "Unable to rename file, permission denied.");
        } catch (VcsException ve) {
            Messages.showErrorDialog(project, ve.getMessage(), "Error during rename ('git mv')");
        }
    }

    public void elementMoved(PsiElement newElement) {
        // implemented in GitVirtualFileAdapter
    }




}