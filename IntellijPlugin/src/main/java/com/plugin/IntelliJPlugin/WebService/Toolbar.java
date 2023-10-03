package com.plugin.pluginfinale.WebService;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.plugin.pluginfinale.Actions.HighlightAntipattern.action;


public class Toolbar extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        DataInputDialog dialog = new DataInputDialog();
        dialog.show();

        if (dialog.isOK()) {
            String[] inputPaths = dialog.getInputPaths();

            if (inputPaths[0].equals("")){
                Messages.showErrorDialog("Inserire dei path.","Errore");
                return;
            }
            // Invia i contenuti dei file al server
            File csvFile = sendFileContentsToServer(inputPaths);

            if (csvFile != null) {
                // Salva il file CSV nella cartella dei download
                saveCsvToDownloads(csvFile);
                action(e,inputPaths);
            }
        }
    }



    private File sendFileContentsToServer(String[] paths) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("http://localhost:8080/caricaFile");

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            for (String path : paths) {
                File file = new File(path);
                if (file.exists()) {
                    builder.addBinaryBody("files", file);
                }
                else {
                    Messages.showErrorDialog("Il file non esiste: "+ path,"Errore");
                }
            }
            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            HttpResponse response = httpClient.execute(httpPost);

            // Salvataggio del file CSV dalla risposta del server
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String randomFileName = "output.csv";
                File csvFile = new File(System.getProperty("user.home"), randomFileName);

                try (InputStream inputStream = responseEntity.getContent();
                     FileOutputStream outputStream = new FileOutputStream(csvFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    return csvFile;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveCsvToDownloads(File csvFile) {
        try {
            Path downloadsPath = new File(System.getProperty("user.home"), "Downloads").toPath();
            Path targetPath = downloadsPath.resolve(csvFile.getName());

            Files.copy(csvFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            Messages.showMessageDialog( "File CSV salvato nella cartella dei download.","Operazione completata",Messages.getInformationIcon());
            String url = "http://localhost:8080/eseguiOperazioni";

            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // La richiesta è stata eseguita con successo
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();

                System.out.println("Risposta dalla chiamata HTTP: " + content.toString());
            } else {
                // La richiesta ha fallito
                System.out.println("La chiamata HTTP ha restituito il codice di risposta: " + responseCode);
            }

            connection.disconnect();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


