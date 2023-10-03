package com.plugin.pluginfinale.Refactoring;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;


public class RefactoringA3 {

    public static boolean modifySetMethod(Project project, String filePath, String nome) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(LocalFileSystem.getInstance().findFileByPath(filePath));
        if (psiFile != null) {
            PsiClass psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);
            if (psiClass != null) {
                PsiMethod[] methods = psiClass.getMethods();
                for (PsiMethod method : methods) {
                    if (method.getName().equals(nome)) {
                        PsiReturnStatement returnStatement = findReturnStatement(method);
                        returnStatement.delete();
                        PsiElementFactory elementFactory = PsiElementFactory.getInstance(project);
                        PsiTypeElement returnTypeElement = method.getReturnTypeElement();
                        if (returnTypeElement != null) {
                            PsiType voidType = PsiType.VOID;
                            returnTypeElement.replace(elementFactory.createTypeElement(voidType));
                            return true;
                        }
                    }
                }
                PsiClass[] innerClasses = psiClass.getInnerClasses();
                for (PsiClass innerClass : innerClasses) {
                    PsiMethod[] innerMethods = innerClass.getMethods();
                    for (PsiMethod method : innerMethods) {
                        if (method.getName().equals(nome)) {
                            PsiReturnStatement returnStatement = findReturnStatement(method);
                            returnStatement.delete();
                            PsiElementFactory elementFactory = PsiElementFactory.getInstance(project);
                            PsiTypeElement returnTypeElement = method.getReturnTypeElement();
                            if (returnTypeElement != null) {
                                PsiType voidType = PsiType.VOID;
                                returnTypeElement.replace(elementFactory.createTypeElement(voidType));
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
        return false;
    }


    private static PsiReturnStatement findReturnStatement(PsiMethod method) {
        for (PsiStatement statement : method.getBody().getStatements()) {
            if (statement instanceof PsiReturnStatement) {
                return (PsiReturnStatement) statement;
            }
        }
        return null;
    }

    public static void showNotification(String message) {
        Notification notification = new Notification("Custom Notification Group", "TITOLO", message, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }

}
