/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.youtubi;

import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ContentClient extends JFrame {
    private final JTextArea textArea;
    private final JLabel imageLabel;
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JButton pauseButton;
    private boolean isPaused = false;

    public static void main(String[] args) {
        new NativeDiscovery().discover(); // Descubre las bibliotecas nativas de VLC
        SwingUtilities.invokeLater(() -> {
            ContentClient client = new ContentClient();
            client.setVisible(true);
        });
    }

    public ContentClient() {
        setTitle("Cliente de Contenido");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear botones
        JButton mp3Button1 = new JButton("Reproducir MP3-1");
        JButton mp3Button2 = new JButton("Reproducir MP3-2");
        JButton mp4Button1 = new JButton("Reproducir MP4-1");
        JButton mp4Button2 = new JButton("Reproducir MP4-2");
        JButton txtButton1 = new JButton("Mostrar TXT-1");
        JButton txtButton2 = new JButton("Mostrar TXT-2");
        pauseButton = new JButton("Pausar");

        // Configurar eventos de botones
        mp3Button1.addActionListener(e -> openContentWindow("192.168.1.34", 8000, "Avispas.mp3", "src/Imagenes/Avispas.jpg"));
        mp3Button2.addActionListener(e -> openContentWindow("192.168.1.34", 8000, "My-or.mp3", "src/Imagenes/My-or.jpg"));
        mp4Button1.addActionListener(e -> openContentWindow("192.168.1.34", 8000, "java.mp4", null));
        mp4Button2.addActionListener(e -> openContentWindow("192.168.1.34", 8000, "JS.mp4", null));
        txtButton1.addActionListener(e -> requestTextFile("192.168.1.34", 8000, "dato.txt"));
        txtButton2.addActionListener(e -> requestTextFile("192.168.1.34", 8000, "dato2.txt"));
        pauseButton.addActionListener(e -> pauseMedia());

        // Crear el layout y añadir los elementos
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.add(mp3Button1);
        panel.add(mp3Button2);
        panel.add(mp4Button1);
        panel.add(mp4Button2);
        panel.add(txtButton1);
        panel.add(txtButton2);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Layout principal
        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(imageLabel, BorderLayout.EAST);

        // Inicializar VLCJ
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        add(mediaPlayerComponent, BorderLayout.SOUTH);
    }

    private void openContentWindow(String ipAddress, int port, String fileName, String imagePath) {
        JFrame contentFrame = new JFrame("Contenido");
        contentFrame.setSize(1280, 720);
        contentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        contentFrame.setLocationRelativeTo(null);

        // Crear botón para regresar
        JButton backButton = new JButton("Regresar");
        JButton localPauseButton = new JButton("Pausar/Reanudar");

        EmbeddedMediaPlayerComponent localMediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        
        backButton.addActionListener(e -> {
            localMediaPlayerComponent.mediaPlayer().controls().stop();
            contentFrame.dispose();
        });

        localPauseButton.addActionListener(e -> pauseLocalMedia(localMediaPlayerComponent, localPauseButton));

        // Crear panel de controles
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(backButton);
        controlPanel.add(localPauseButton);

        // Configurar contenido multimedia
        contentFrame.setLayout(new BorderLayout());
        contentFrame.add(controlPanel, BorderLayout.NORTH);
        contentFrame.add(localMediaPlayerComponent, BorderLayout.CENTER);

        // Mostrar imagen (si aplica)
        if (imagePath != null) {
            JLabel imageLabel = new JLabel();
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            ImageIcon imageIcon = new ImageIcon(imagePath);
            imageLabel.setIcon(imageIcon);
            contentFrame.add(imageLabel, BorderLayout.EAST);
        }

        contentFrame.setVisible(true);

        // Reproducir media
        playMedia(localMediaPlayerComponent, ipAddress, port, fileName);
    }

    private void playMedia(EmbeddedMediaPlayerComponent mediaPlayerComponent, String ipAddress, int port, String fileName) {
        // Detener cualquier reproducción actual
        this.mediaPlayerComponent.mediaPlayer().controls().stop();

        try {
            Socket socket = new Socket(ipAddress, port);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter out = new PrintWriter(outputStream, true);
            out.println("GET /" + fileName);

            File tempFile = File.createTempFile("temp", null);
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            InputStream inputStream = socket.getInputStream();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.close();

            // Reproducir el archivo de medios
            mediaPlayerComponent.mediaPlayer().media().play(tempFile.getAbsolutePath());

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestTextFile(String ipAddress, int port, String fileName) {
        try {
            Socket socket = new Socket(ipAddress, port);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter out = new PrintWriter(outputStream, true);
            out.println("GET /" + fileName);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            textArea.setText(content.toString());

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pauseMedia() {
        if (isPaused) {
            mediaPlayerComponent.mediaPlayer().controls().play();
            pauseButton.setText("Pausar");
        } else {
            mediaPlayerComponent.mediaPlayer().controls().pause();
            pauseButton.setText("Reanudar");
        }
        isPaused = !isPaused;
    }

    private void pauseLocalMedia(EmbeddedMediaPlayerComponent mediaPlayerComponent, JButton pauseButton) {
        if (isPaused) {
            mediaPlayerComponent.mediaPlayer().controls().play();
            pauseButton.setText("Pausar");
        } else {
            mediaPlayerComponent.mediaPlayer().controls().pause();
            pauseButton.setText("Reanudar");
        }
        isPaused = !isPaused;
    }
}














