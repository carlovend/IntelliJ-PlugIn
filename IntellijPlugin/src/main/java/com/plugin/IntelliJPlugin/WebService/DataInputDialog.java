package com.plugin.IntelliJPlugin.WebService;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DataInputDialog extends DialogWrapper {
    private JTextArea textArea = new JTextArea();

    DataInputDialog() {
        super(true);
        init();
        setTitle("Inserisci percorsi dei file Java");
        setSize(500,300);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel headerPanel = new JPanel(new BorderLayout());
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        return panel;
    }

     public String[] getInputPaths() {
        String input = textArea.getText();
        return input.split("\\s+"); // Divide gli input utilizzando gli spazi come separatorev
    }
}
