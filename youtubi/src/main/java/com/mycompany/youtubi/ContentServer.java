/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.youtubi;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ContentServer {
    public static void main(String[] args) {
        final int PORT = 8000;
        final String CONTENT_DIRECTORY = "src/Musica-Videos-Doc"; // Directorio donde se encuentran los archivos

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor en el puerto " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde " + clientSocket.getInetAddress());

                // Crea un nuevo hilo para manejar la solicitud del cliente
                Thread clientThread = new Thread(() -> handleClientRequest(clientSocket, CONTENT_DIRECTORY));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket clientSocket, String contentDirectory) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String request = reader.readLine(); // Lee la solicitud del cliente

            // Procesa la solicitud y envía la respuesta adecuada
            if (request.startsWith("GET /")) {
                String[] parts = request.split(" ");
                String filePath = parts[1].substring(1); // Elimina el primer "/"
                File file = new File(contentDirectory, filePath);

                if (file.exists() && file.isFile()) {
                    // Envía el contenido del archivo al cliente
                    sendFileContent(clientSocket, file);
                } else {
                    // Archivo no encontrado
                    sendResponse(clientSocket, "404 Not Found", "Archivo no encontrado");
                }
            } else {
                // Solicitud no válida
                sendResponse(clientSocket, "400 Bad Request", "Solicitud no válida");
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFileContent(Socket clientSocket, File file) throws IOException {
        try (BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream())) {
            if (file.getName().endsWith(".txt") || file.getName().endsWith(".mp3") || file.getName().endsWith(".mp4")) {
                // Envía el archivo tal como está
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } else {
                // Archivo no compatible
                sendResponse(clientSocket, "400 Bad Request", "Archivo no compatible");
            }

            out.flush();
        }
    }

    private static void sendResponse(Socket clientSocket, String status, String message) throws IOException {
        String response = "HTTP/1.1 " + status + "\r\n\r\n" + message;
        clientSocket.getOutputStream().write(response.getBytes());
    }
}








