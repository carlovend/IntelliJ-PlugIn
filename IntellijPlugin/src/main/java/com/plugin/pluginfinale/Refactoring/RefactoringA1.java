package com.plugin.pluginfinale.Refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import static com.plugin.pluginfinale.Refactoring.RefactoringD1.showNotification;

public class RefactoringA1 {

    private final Project project;

    public RefactoringA1(Project project) {
        this.project = project;
    }

    public static boolean modifyGetMethod(Project project, String filePath, String nome) {
        boolean flag = false;
        PsiFile psiFile = PsiManager.getInstance(project).findFile(LocalFileSystem.getInstance().findFileByPath(filePath));
        if (psiFile != null) {
            PsiClass psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);
            if (psiClass != null) {
                PsiMethod[] methods = psiClass.getMethods();
                for (PsiMethod method : methods) {
                    showNotification(method.getName());
                    if (method.getName().equals(nome)) {
                        PsiCodeBlock body = method.getBody();
                        if (body != null) {
                            // Elimina tutte le istruzioni nel corpo del metodo tranne il return
                            PsiStatement[] statements = body.getStatements();
                            for (PsiStatement statement : statements) {
                                if (!(statement instanceof PsiReturnStatement)) {
                                    statement.delete();
                                    flag = true;
                                }
                            }
                        }
                    }
                }
                if (flag) {
                    return true;
                } else {
                    PsiClass[] innerClasses = psiClass.getInnerClasses();
                    for (PsiClass innerClass : innerClasses) {
                        PsiMethod[] innerMethods = innerClass.getMethods();
                        for (PsiMethod method : innerMethods) {
                            if (method.getName().equals(nome)) {
                                PsiCodeBlock body = method.getBody();
                                if (body != null) {
                                    // Elimina tutte le istruzioni nel corpo del metodo tranne il return
                                    PsiStatement[] statements = body.getStatements();
                                    for (PsiStatement statement : statements) {
                                        if (!(statement instanceof PsiReturnStatement)) {
                                            statement.delete();
                                            flag = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (flag){
                    return  true;
                }
                return flag;
            }
        }
    return flag;
    }

}





