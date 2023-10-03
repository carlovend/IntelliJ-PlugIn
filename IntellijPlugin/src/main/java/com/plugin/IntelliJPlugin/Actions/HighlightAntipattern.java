package com.plugin.IntelliJPlugin.Actions;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.plugin.IntelliJPlugin.Refactoring.RefactoringD1.showNotification;


public class HighlightAntipattern {
    public static void action(AnActionEvent e, String[] path) {
        String csvFilePath = "C:\\Users\\carlo\\Downloads\\output.csv";
        if (!checkIfEmpty(csvFilePath)) {
            showNotification("Il file csv non contiene errori!");
            return;
        }

        try (BufferedReader csvReader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int c = 0;
            String filePath = null;
            int i = 0;
            String[] tmp = new String[countLines(csvFilePath)];

            while ((line = csvReader.readLine()) != null) {
                String[] values = customSplit(line);
                tmp[i] = values[0];

                if (values.length >= 6) {
                    if (i > 0 && !(values[0].equals(tmp[i - 1]))) {
                        if (c < path.length) {
                            c++;
                            filePath = path[c];
                        }
                    } else {
                        filePath = path[c];
                    }

                    i++;
                    String descrizione = values[6].replace("\"", "") + " " + values[8].replace("\"", "") + " " + values[9].replace("\"", "") +
                            " " + "Suggerimento\n" + getTip(values[6].replace("\"", ""));

                    int startLine = Integer.parseInt(values[4].replace("\"", ""));
                    int startColumn = Integer.parseInt(values[5].replace("\"", ""));

                    Project project = e.getProject();

                    if (project != null) {
                        Editor editor = openEditorForFile(project, filePath);

                        if (editor != null) {
                            Document document = editor.getDocument();
                            int startOffset = document.getLineStartOffset(startLine - 1);
                            int endOffset = document.getLineEndOffset(startLine - 1);
                            boolean verifica = variableOrMethod(values[3]);
                            String lineText = document.getText(new TextRange(startOffset, endOffset));

                            // Controllo se c'è un'annotazione nella riga
                            boolean hasAnnotation = lineText.contains("@");
                            for(int j = 0; j<2; j++) {
                                if (hasAnnotation) {
                                    // Vado alla riga successiva
                                    startLine++;
                                    startOffset = document.getLineStartOffset(startLine - 1);
                                    endOffset = document.getLineEndOffset(startLine - 1);
                                }
                            }
                            int wordStart;

                            if (verifica) {
                                wordStart = lineText.indexOf(ottieniNomeStringa(values[2], true));
                            } else {
                                wordStart = lineText.indexOf(ottieniNomeStringa(values[2], false));
                            }

                            showNotification(ottieniNomeStringa(values[2], verifica));

                            startOffset += wordStart;
                            endOffset = startOffset + ottieniNomeStringa(values[2], verifica).length();

                            MarkupModel markupModel = editor.getMarkupModel();
                            RangeHighlighterEx highlighter = (RangeHighlighterEx) markupModel.addRangeHighlighter(startOffset, endOffset,
                                    HighlighterLayer.ERROR + 1,
                                    getAttributesForAntipattern(),
                                    HighlighterTargetArea.EXACT_RANGE
                            );

                            editor.addEditorMouseListener(new MyEditorMouseListener(editor, highlighter, descrizione, filePath, values[2]));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    private static Editor openEditorForFile(Project project, String filePath) {

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);

        if (virtualFile != null) {
            return fileEditorManager.openTextEditor(new OpenFileDescriptor(project, virtualFile), true);
        }

        return null;
    }

    private static TextAttributes getAttributesForAntipattern() {

        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setEffectColor(Color.YELLOW);
        textAttributes.setEffectType(EffectType.WAVE_UNDERSCORE);
        textAttributes.setFontType(Font.BOLD);
        return textAttributes;
    }

    private static String getTip(String categoria){
        if(categoria.contains("A.1")){
            return "Il metodo dovrebbe solo restituire l'oggetto e non fare altro.";
        }
        if(categoria.contains("A.2")){
            return "Cambiare il nome del metodo.";
        }
        if (categoria.contains("A.3")){
            return "Eliminare il return.";
        }
        if (categoria.contains("B.2")){
            return "Restituire true o false per indicare l'esito della validazione.";
        }
        if (categoria.contains("B.3")){
            return "Restituire l'oggetto indicato.";
        }
        if (categoria.contains("B.5")){
            return "Restituire l'oggetto trasformato.";
        }
        if (categoria.contains("D.1")){
            return "Cambiare nome al plurale.";
        }
        if (categoria.contains("D.2")){
            return "Cambiare nome attributo o cambiare tipo.";
        }
        if (categoria.contains("E.1")){
            return "Cambiare nome dell'attributo o cambiare tipo ma penso piu cambiare nome.";
        }
        return " ";
    }

    public static int countLines(String filePath) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while (br.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }


    //True se è una variabile false se è un metodo
    private static boolean variableOrMethod(String inputString){
        if(inputString.replace("\"", "").equals("Variable")){
            return true;
        }
        return false;
    }

    private static int ottieniNome(String input, boolean verifica){
        String nomeErrore = input.replace("\"", "");
        if (verifica) {
            int ultimoPunto = nomeErrore.lastIndexOf(".");
            String parteDopoPunto = nomeErrore.substring(ultimoPunto+1);
            return parteDopoPunto.length()+3;
        }
        int primoPunto = nomeErrore.indexOf(".");
        String parteDopoPunto = nomeErrore.substring(primoPunto+1);
        return parteDopoPunto.length()+3;
    }

    private static String ottieniNomeStringa(String input, boolean verifica){
        String nomeErrore = input.replace("\"", "");
        if (verifica) {
            int ultimoPunto = nomeErrore.lastIndexOf(".");
            String parteDopoPunto = nomeErrore.substring(ultimoPunto+1);
            return parteDopoPunto;
        }
        int primoPunto = nomeErrore.indexOf(".");
        String parteDopoPunto = nomeErrore.substring(primoPunto + 1);
        int posizioneParentesi = parteDopoPunto.indexOf("(");

        if (posizioneParentesi >= 0) {
            return parteDopoPunto.substring(0, posizioneParentesi);
        }

        return parteDopoPunto;
    }


    public static String[] customSplit(String input) {
        ArrayList<String> result = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (char c : input.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentValue.toString());
                currentValue.setLength(0);
            } else {
                currentValue.append(c);
            }
        }

        result.add(currentValue.toString());

        return result.toArray(new String[0]);
    }

    public static boolean checkIfEmpty(String path) {
        File file = new File(path);
        if(file.length()>0){
            return true;
        }
        return false;
    }




}
