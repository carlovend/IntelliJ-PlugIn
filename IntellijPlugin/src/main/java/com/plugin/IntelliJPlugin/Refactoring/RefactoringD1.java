package com.plugin.IntelliJPlugin.Refactoring;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.*;

import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class RefactoringD1 {

    private final Project project;
    private static String oldName;
    private String filePath;
    public RefactoringD1(@NotNull Project project, String oldName,String filePath) {
        this.project = project;
        this.oldName = oldName;
        this.filePath = filePath;
    }

    public static boolean modifyVariable(Project project, String filePath, String nome) {
        boolean flag = false;
        PsiFile psiFile = PsiManager.getInstance(project).findFile(LocalFileSystem.getInstance().findFileByPath(filePath));
        if (psiFile != null){
            PsiClass psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);
            if (psiClass != null) {
                PsiMethod[] methods = psiClass.getMethods();
                for (PsiMethod method : methods){
                    if (method.getName().equals(nome)){
                        @NotNull Collection<PsiIdentifier> identifiers = PsiTreeUtil.findChildrenOfType(method.getBody(), PsiIdentifier.class);
                        for (PsiIdentifier identifier : identifiers) {
                            if (oldName.equals(identifier.getText())) {
                                PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
                                PsiIdentifier newIdentifier = factory.createIdentifier(oldName+"s");
                                identifier.replace(newIdentifier);
                                flag = true;
                            }
                        }
                        return flag;
                        }
                    }
                }
            }
        return false;
        }


    public static void showNotification(String message) {
        Notification notification = new Notification("Custom Notification Group", "Messaggio", message, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }

    public String getOldName() {
        return oldName;
    }
}
