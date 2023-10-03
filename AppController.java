package com.WebService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class AppController {


    public  ResponseEntity<byte[]> downloadFile() throws IOException {
        String risultato = runIdealProcess();
       String file ="C:\\Users\\carlo\\Desktop\\progettoTesi\\env\\progetto\\src\\apps\\IDEAL\\IDEAL_Results.csv";
       byte[] fileBytes = Files.readAllBytes(Path.of(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=IDEAL_Results.csv");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileBytes);
    }

    private String runIdealProcess() {
        String batchFilePath = "C:\\Users\\carlo\\Desktop\\progettoTesi\\env\\progetto\\src\\apps\\IDEAL\\run.cmd";

        ProcessBuilder processBuilder = new ProcessBuilder(batchFilePath);
        processBuilder.directory(new File("C:\\Users\\carlo\\Desktop\\progettoTesi\\env\\progetto\\src\\apps\\IDEAL"));

        try {
            Process process = processBuilder.start();

            // Cattura l'output della console del processo
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println(line);
                if(line.startsWith("Analysis")) {
                    int exitCode = 0;
                    String risultato = "Codice di uscita: " + exitCode + "\n";
                    risultato += "Output della console:\n" + output.toString();
                    return risultato;
                }
            }

            int exitCode = process.waitFor();

            String risultato = "Codice di uscita: " + exitCode + "\n";
            risultato += "Output della console:\n" + output.toString();

            return risultato;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/caricaFile")
    public ResponseEntity<byte[]> uploadFile(@RequestParam("files") MultipartFile[] files) {
        try {
            // Imposta il percorso della directory di destinazione
            String destinazione = "C:\\Users\\carlo\\Desktop\\ricevuti";

            for (MultipartFile file : files) {
                String percorsoDestinazione = destinazione + File.separator + file.getOriginalFilename();
                file.transferTo(new File(percorsoDestinazione));
            }
            writeOnInput();
            return downloadFile();

        } catch (IOException e) {

        }
        return null;
    }

    public static void writeOnInput() throws IOException {
        String pathCartella = "C:\\Users\\carlo\\Desktop\\ricevuti";
        File cartella = new File(pathCartella);

        if(cartella.isDirectory()) {
            File[] files = cartella.listFiles();

            if (files!=null) {
                for (File file: files) {
                    BufferedWriter writer = new BufferedWriter(new
                            FileWriter("C:\\Users\\carlo\\Desktop\\progettoTesi\\env\\progetto\\src\\apps\\IDEAL\\input.csv", true));
                    writer.newLine();
                    writer.write(file.getAbsolutePath());
                    writer.close();
                }
            }
        }
    }



    private static void deleteRowResult() {
        File file = new File("C:\\Users\\carlo\\Desktop\\progettoTesi\\env\\progetto\\src\\apps\\IDEAL\\IDEAL_Results.csv");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteRowInput() {
        File inputFile = new File("C:\\Users\\carlo\\Desktop\\progettoTesi\\env\\progetto\\src\\apps\\IDEAL\\input.csv");
        try {
            RandomAccessFile raf = new RandomAccessFile(inputFile, "rw");
            String primaRiga = raf.readLine();

            raf.seek(0);
            raf.writeBytes(primaRiga);
            raf.setLength(primaRiga.length());
            raf.close();

            System.out.println("Tutte le righe tranne la prima sono state cancellate con successo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteRecivedFile() {
        String percorsoCartella = ("C:\\Users\\carlo\\Desktop\\ricevuti\\");
        File cartella = new File(percorsoCartella);

        if (!cartella.exists() || !cartella.isDirectory()) {
            System.out.println("La cartella specificata non esiste.");
            return;
        }
        File[] files = cartella.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("La cartella è già vuota.");
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                boolean eliminato = file.delete();
                if (eliminato) {
                    System.out.println("File eliminato: " + file.getName());
                } else {
                    System.out.println("Impossibile eliminare il file: " + file.getName());
                }
            }
        }
    }

    @GetMapping("/eseguiOperazioni")
    public ResponseEntity<String> deleteOperation() {
        try {
            deleteRowResult();
            deleteRowInput();
            deleteRecivedFile();
            return ResponseEntity.ok("Operazioni eseguite con successo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Si è verificato un errore durante l'esecuzione delle operazioni");
        }
    }


}



