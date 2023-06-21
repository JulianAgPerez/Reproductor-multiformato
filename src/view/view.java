package view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
//Bibliotecas de reproduccion
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javazoom.jl.player.Player;
import javax.sound.midi.*;
import javazoom.jl.decoder.JavaLayerException;
//Biblioteca para el tiempo en lo estetico
import javax.swing.Timer;

public class view extends javax.swing.JFrame {

    //Colores
    private final Color NARANJA = new Color(235, 130, 36);
    private final Color GRISOSCURO = new Color(128, 128, 128);
    //Variables para lo estetico
    private static final Color COLOR_INICIAL = Color.RED; // Color inicial
    private static final Color COLOR_FINAL = Color.BLUE; // Color final
    private Timer timer;
    private Timer timer2;
    private float interpolacion = 0.0f;
    //Para reproducir sonido
    private Clip clip;
    private Player player;
    private boolean enPausa = false;
    private Sequencer sequencer;

    public view() {
        initComponents();
        reestablecer();
        setLocationRelativeTo(null);
        cargarArchivosDeMusica();
    }

    //Asigna el nombre a la combobox con el nombre del archivo
    private void cargarArchivosDeMusica() {
        String directorio = "src/";     //Toma la ubicacion relativa
        File dir = new File(directorio);
        File[] archivos = dir.listFiles();

        if (archivos != null) {
            comboBoxCanciones.removeAllItems();
            HashSet<String> nombresCanciones = new HashSet<>(); // guarda el nombre de canciones sin duplicados

            for (File archivo : archivos) {
                if (archivo.isFile()) {
                    String nombreArchivo = archivo.getName();
                    String nombreSinExtension = nombreArchivo.substring(0, nombreArchivo.lastIndexOf('.'));

                    if (archivo.getName().endsWith(".mp3") || archivo.getName().endsWith(".mid") || archivo.getName().endsWith(".wav") ) {
                        if (!nombresCanciones.contains(nombreSinExtension)) {
                            nombresCanciones.add(nombreSinExtension);
                            comboBoxCanciones.addItem(nombreSinExtension);
                        }
                    }
                }
            }
        }
    }

    public void reproducir(String cancion) {
        String tipoArchivo = cancion.substring(cancion.lastIndexOf('.') + 1); // guarda el tipo de archivo de la cancion

        switch (tipoArchivo) {
            case "mp3" -> {
                try {
                    FileInputStream fis = new FileInputStream("src/" + cancion);
                    player = new Player(fis);
                    Thread thread = new Thread(() -> { // Nuevo hilo para no pausar el menú
                        try {
                            while (!Thread.interrupted()) {
                                if (!enPausa) {
                                    if (!player.play(1)) {
                                        break;
                                    }
                                } else {
                                    Thread.sleep(100);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    thread.start();
                } catch (FileNotFoundException | JavaLayerException e) {
                    e.printStackTrace();
                }
            }

            case "mid" -> {
                try {
                    // Obtener el secuenciador MIDI
                    sequencer = MidiSystem.getSequencer();
                    // Abrir el secuenciador
                    sequencer.open();
                    // Crear un objeto Sequence a partir del archivo MIDI
                    Sequence sequence = MidiSystem.getSequence(new File("src/" + cancion));
                    // Establecer la secuencia en el secuenciador
                    sequencer.setSequence(sequence);
                    // Crear un hilo para reproducir la secuencia MIDI
                    Thread playbackThread = new Thread(() -> {
                        try {
                            // Iniciar la reproducción
                            sequencer.start();
                            // Esperar hasta que termine la reproducción
                            while (sequencer.isRunning()) {
                                Thread.sleep(1000);
                            }
                            // Cerrar el secuenciador después de la reproducción
                            sequencer.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    // Iniciar el hilo de reproducción
                    playbackThread.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            case "wav" -> {
                try {
                    // Cargar el archivo de audio utilizando ClassLoader
                    ClassLoader classLoader = getClass().getClassLoader();
                    InputStream inputStream = classLoader.getResourceAsStream(cancion);
                    // Reproducir el sonido (solo reproduce wav)
                    clip = AudioSystem.getClip();
                    clip.open(AudioSystem.getAudioInputStream(inputStream));
                    clip.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //metodos de control de la musica
    public void pausar() {
        enPausa = true;
        if (clip != null) {
            clip.stop();
        }
        if (sequencer != null) {
            sequencer.stop();
        }
    }

    public void reanudar() {
        enPausa = false;
        clip.start();
        sequencer.start();
    }

    public void detener() {
        if (player != null) {
            player.close();
            player = null;
        }
        if (clip != null) {
            clip.close();
            clip = null;
        }
        if (sequencer != null) {
            sequencer.stop();
            sequencer.close();
            sequencer = null;
        }

    }

    //metodos para cambiar colores de la api
    private void iniciarFiesta(boolean partyOver) {
        if (partyOver) {
            timer = new Timer(50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cambiarColorGradualmente();
                }
            });
            timer.start();

            ActionListener actionListener = new ActionListener() {
                private Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, NARANJA}; // Colores a cambiar
                private int currentIndex = 0;

                public void actionPerformed(ActionEvent e) {
                    panel1.setBackground(colors[currentIndex]); // Cambiar el color de fondo
                    currentIndex = (currentIndex + 1) % colors.length; // Avanzar al siguiente color
                }
            };
            // Temporizador para ejecutar el ActionListener cada segundo
            timer2 = new Timer(888, actionListener);   //delay esta en milisegundos
            timer2.start();
        } else {
            timer.stop();
            timer2.stop();
        }
    }

    private void cambiarColorGradualmente() {
        interpolacion += 0.1f; // Incrementar la interpolación en cada ejecución

        if (interpolacion > 1.0f) {
            interpolacion = 0.0f; // Reiniciar la interpolación cuando alcanza 1.0
        }

        // Calcular el color interpolado
        int red = (int) (COLOR_INICIAL.getRed() * (1 - interpolacion) + COLOR_FINAL.getRed() * interpolacion);
        int green = (int) (COLOR_INICIAL.getGreen() * (1 - interpolacion) + COLOR_FINAL.getGreen() * interpolacion);
        int blue = (int) (COLOR_INICIAL.getBlue() * (1 - interpolacion) + COLOR_FINAL.getBlue() * interpolacion);
        Color colorInterpolado = new Color(red, green, blue);

        panel2.setBackground(colorInterpolado); // Establecer el color interpolado como fondo del panel

    }

    private void reestablecer() {
        panel2.setBackground(GRISOSCURO);
        panel1.setBackground(GRISOSCURO);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel2 = new javax.swing.JPanel();
        panel1 = new javax.swing.JPanel();
        btnStop = new javax.swing.JButton();
        btnPause = new javax.swing.JButton();
        comboBoxTipo = new javax.swing.JComboBox<>();
        comboBoxCanciones = new javax.swing.JComboBox<>();
        btnPlay = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Reproductor multiformato");

        btnStop.setText("Parar");
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        btnPause.setText("Pausar");
        btnPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPauseActionPerformed(evt);
            }
        });

        comboBoxTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "MP3", "WAV", "MID" }));

        comboBoxCanciones.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnPlay.setText("Reproducir");
        btnPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlayActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(comboBoxCanciones, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(btnStop)
                        .addGap(27, 27, 27)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(comboBoxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnPause))
                        .addGap(27, 27, 27)
                        .addComponent(btnPlay)))
                .addGap(60, 60, 60))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(comboBoxCanciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboBoxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPause)
                    .addComponent(btnStop)
                    .addComponent(btnPlay))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
        iniciarFiesta(false);
        reestablecer();
        detener();
    }//GEN-LAST:event_btnStopActionPerformed

    private void btnPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPauseActionPerformed
        iniciarFiesta(false);
        reestablecer();
        pausar();
    }//GEN-LAST:event_btnPauseActionPerformed

    private void btnPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayActionPerformed
        iniciarFiesta(true);
        if (!enPausa) {
            ///canciones
            String cancion = comboBoxCanciones.getSelectedItem().toString() + "." + comboBoxTipo.getSelectedItem().toString().toLowerCase();
            System.out.println("Cancion sonando --> " + comboBoxCanciones.getSelectedItem().toString() + "." + comboBoxTipo.getSelectedItem().toString().toLowerCase());
            reproducir(cancion);
        } else {
            reanudar();
        }
    }//GEN-LAST:event_btnPlayActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(view.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(view.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(view.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(view.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new view().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPause;
    private javax.swing.JButton btnPlay;
    private javax.swing.JButton btnStop;
    private javax.swing.JComboBox<String> comboBoxCanciones;
    private javax.swing.JComboBox<String> comboBoxTipo;
    private javax.swing.JPanel panel1;
    private javax.swing.JPanel panel2;
    // End of variables declaration//GEN-END:variables
}
