package com.plugin.pluginfinale.Actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.plugin.pluginfinale.Refactoring.RefactoringA1;
import com.plugin.pluginfinale.Refactoring.RefactoringA2;
import com.plugin.pluginfinale.Refactoring.RefactoringA3;
import com.plugin.pluginfinale.Refactoring.RefactoringD1;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;


public class MyEditorMouseListener implements EditorMouseListener {

    private Editor editor = null;
    private static final Logger LOG = Logger.getInstance(MyEditorMouseListener.class);
    private MouseEvent lastMouseEvent;

    private RangeHighlighterEx highlighter = null;
    private Balloon balloon;
    private String descrizione;
    private String metodoErrore;
    private String filePath;

    public MyEditorMouseListener() {

    }

    public MyEditorMouseListener(Editor editor, RangeHighlighterEx highlighter, String descrizione,
                                 String filePath, String metodoErrore) {
        this.descrizione = descrizione;
        this.editor = editor;
        this.highlighter = highlighter;
        this.filePath = filePath;
        this.metodoErrore = metodoErrore;
    }

    private void removeHighlighter() {
        if (editor != null && highlighter != null) {
            MarkupModel markupModel = editor.getMarkupModel();
            markupModel.removeHighlighter(highlighter);
        }
    }

    public void showNotification(String message) {
        Notification notification = new Notification("Custom Notification Group", "Mouse Entered", message, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }


    @Override
    public void mouseExited(@NotNull EditorMouseEvent e) {
        // Nascondi il tooltip quando il mouse esce dall'area evidenziata
        hideTooltip();
    }


    @Override
    public void mouseClicked(@NotNull EditorMouseEvent e) {
        if (e.getEditor() == editor && e.getMouseEvent().getSource() == editor.getContentComponent()) {

            LogicalPosition cursorLogicalPos = editor.xyToLogicalPosition(e.getMouseEvent().getPoint());
            int cursorOffset = editor.logicalPositionToOffset(cursorLogicalPos);
            if (highlighter.getStartOffset() <= cursorOffset &&
                    cursorOffset <= highlighter.getEndOffset()) {
                // Il mouse è sopra l'area evidenziata, mostra il tooltip
                showTooltip(e.getMouseEvent(), descrizione);
            }
        }
    }

    private void showTooltip(MouseEvent e, String tooltipText) {

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        content.setBackground(JBColor.background());

        JLabel label = new JLabel("<html><body style='width:300px'>" + tooltipText + "</body></html>");
        label.setForeground(JBColor.foreground());
        content.setBackground(JBColor.border());
        content.add(label, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editButton = new JButton();
        String categoria = descrizione.substring(0,3);
        if (categoria.equals("A.1")){
            editButton.setText("Modifica il metodo Get");
        }
        else if (categoria.equals("A.2")){
            editButton.setText("Rimuovi is");
        }
        else if (categoria.equals("A.3")){
            editButton.setText("Modifica il metodo Set");
        }
        else if (categoria.equals("D.1")){
            editButton.setText("Aggiungi s finale");
        }
        else {
            editButton.hide();
        }
        editButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (categoria.equals("A.1")) {
                    String nomeErrore = metodoErrore.replace("\"", "");
                    int inizioMetodo = nomeErrore.indexOf(".");
                    int fineMetodo = nomeErrore.indexOf("(",inizioMetodo);
                    String nomeMetodo = nomeErrore.substring(inizioMetodo+1,fineMetodo);
                    showNotification(nomeMetodo);
                    WriteCommandAction.runWriteCommandAction(ProjectManager.getInstance().getDefaultProject(), () -> {
                      boolean ris =  RefactoringA1.modifyGetMethod(ProjectManager.getInstance().getDefaultProject(), filePath,nomeMetodo );
                        if (ris){
                            removeHighlighter();
                        }else {
                            showNotification("Errore nella risoluzione del problema");
                        }
                    });
                }

                if (categoria.equals("D.1")){
                    String nomeErrore = metodoErrore.replace("\"", "");
                    int inizioSottoStringa = nomeErrore.lastIndexOf(".");
                    String oldName = nomeErrore.substring(inizioSottoStringa+1);
                    int inizioMetodo = nomeErrore.indexOf(".");
                    int fineMetodo = nomeErrore.indexOf("(",inizioMetodo);
                    String nomeMetodo = nomeErrore.substring(inizioMetodo+1,fineMetodo);
                    WriteCommandAction.runWriteCommandAction(ProjectManager.getInstance().getDefaultProject(), () -> {
                        RefactoringD1 d1 = new RefactoringD1(ProjectManager.getInstance().getDefaultProject(),oldName,filePath);
                       boolean ris = d1.modifyVariable(ProjectManager.getInstance().getDefaultProject(), filePath,nomeMetodo);
                        if (ris){
                            removeHighlighter();
                        }else {
                            showNotification("Errore nella risoluzione del problema");
                        }
                    });
                }
                if (categoria.equals("A.2")) {
                    String nomeErrore = metodoErrore.replace("\"", "");
                    int inizioMetodo = nomeErrore.indexOf(".");
                    int fineMetodo = nomeErrore.indexOf("(",inizioMetodo);
                    String nomeMetodo = nomeErrore.substring(inizioMetodo+1,fineMetodo);
                    WriteCommandAction.runWriteCommandAction(ProjectManager.getInstance().getDefaultProject(), () -> {
                       boolean ris = RefactoringA2.modifyIsMethod(ProjectManager.getInstance().getDefaultProject(), filePath,nomeMetodo);
                       if (ris){
                           removeHighlighter();
                       }else {
                           showNotification("Errore nella risoluzione del problema");
                       }
                    });
                }
                if (categoria.equals("A.3")) {
                    String nomeErrore = metodoErrore.replace("\"", "");
                    int inizioMetodo = nomeErrore.indexOf(".");
                    int fineMetodo = nomeErrore.indexOf("(",inizioMetodo);
                    String nomeMetodo = nomeErrore.substring(inizioMetodo+1,fineMetodo);
                    WriteCommandAction.runWriteCommandAction(ProjectManager.getInstance().getDefaultProject(), () -> {
                     boolean ris =  RefactoringA3.modifySetMethod(ProjectManager.getInstance().getDefaultProject(), filePath,nomeMetodo );
                     if (ris){
                         removeHighlighter();
                     } else {
                       showNotification("Errore nella risoluzione del problema");
                     }
                    });
                }
            }
        });
        buttonPanel.add(editButton);
        content.add(buttonPanel, BorderLayout.SOUTH);

        BalloonBuilder balloonBuilder = JBPopupFactory.getInstance()
                .createBalloonBuilder(content)
                .setHideOnClickOutside(true)
                .setHideOnAction(true)
                .setHideOnKeyOutside(true)
                .setBlockClicksThroughBalloon(true)
                .setCloseButtonEnabled(false) // Rimuovi il pulsante di chiusura
                .setBorderColor(JBColor.border()) // Colore del bordo di IntelliJ
                .setBorderInsets(new Insets(5, 5, 5, 5))
                .setShadow(true);

        Balloon balloon = balloonBuilder.createBalloon();
        balloon.show(new RelativePoint(e.getComponent(), e.getPoint()), Balloon.Position.above);

    }



    private void hideTooltip() {
        if (balloon != null) {
            balloon.hide();
            balloon = null;
        }
    }



}
