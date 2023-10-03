package com.plugin.IntelliJPlugin.Refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

public class RefactoringA2 {

    public static boolean modifyIsMethod(Project project, String filePath, String nome) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(LocalFileSystem.getInstance().findFileByPath(filePath));
        if (psiFile != null) {
            PsiClass psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);
            if (psiClass != null) {
                PsiMethod[] methods = psiClass.getMethods();
                for (PsiMethod method : methods) {
                    if (method.getName().equals(nome)) {
                        String newMethodName = method.getName().substring(2);
                        method.setName(newMethodName);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
