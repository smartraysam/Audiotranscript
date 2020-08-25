/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smarttechx.audiotranscript;

import ai.rev.speechtotext.RevAiWebSocketListener;
import ai.rev.speechtotext.StreamingClient;
import ai.rev.speechtotext.models.asynchronous.Element;
import ai.rev.speechtotext.models.streaming.ConnectedMessage;
import ai.rev.speechtotext.models.streaming.Hypothesis;
import ai.rev.speechtotext.models.streaming.SessionConfig;
import ai.rev.speechtotext.models.streaming.StreamContentType;

import java.awt.AWTException;
import java.io.ByteArrayInputStream;
import okio.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;

import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.swing.filechooser.FileSystemView;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Smart Raysam
 */
public class Dashboard extends javax.swing.JFrame {

    boolean stopCapture = false;
    boolean isStart = false;
    ByteArrayOutputStream byteArrayOutputStream;
    AudioFormat audioFormat;
    TargetDataLine targetDataLine;
    AudioInputStream audioInputStream;
    SourceDataLine sourceDataLine;

    StreamContentType streamContentType = new StreamContentType();
    SessionConfig sessionConfig = new SessionConfig();
    int chunk = 8000;
    String accessToken = "your rev.ai api key";
    static String appPath;
    StreamingClient streamingClient = new StreamingClient(accessToken);

    AudioFileFormat.Type fileType;
    File audioFile, jsonFile;
    static String audiodirectoryPath, jsondirectoryPath, directoryPath;
    CaptureThread captureThread;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    LocalDateTime now;
    JSONObject transcript;
    JSONArray messages;
    private static FileWriter file;

    /**
     * Creates new form Dashboard
     */
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    public Dashboard() {//constructor
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButtonStart = new javax.swing.JButton();
        jLabelStatus = new javax.swing.JLabel();
        log = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Audio Transcript");
        setAlwaysOnTop(true);
        setLocationByPlatform(true);

        jButtonStart.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        jButtonStart.setText("Start");
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartActionPerformed(evt);
            }
        });

        jLabelStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelStatus.setText("Click start to begin");

        log.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(125, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonStart, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(109, 109, 109))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(log, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addComponent(jButtonStart, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabelStatus)
                .addGap(18, 18, 18)
                .addComponent(log, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed
        // TODO add your handling code here:
        if (!isStart) {
            System.out.println("start clicked");
            now = LocalDateTime.now();

            isStart = true;
            jButtonStart.setText("Stop");
            jLabelStatus.setText("Connecting. Please wait....");
            jButtonStart.setFocusable(false);
            stopCapture = false;
            // Configure the streaming content type
            streamContentType.setContentType("audio/x-raw"); // audio content type
            streamContentType.setLayout("interleaved"); // layout
            streamContentType.setFormat("S16LE"); // format
            streamContentType.setRate(16000); // sample rate
            streamContentType.setChannels(1); // channels

            // Setup the SessionConfig with any optional parameters
            sessionConfig.setMetaData("Streaming from the Java SDK");
            sessionConfig.setFilterProfanity(true);

            // Initialize your client with your access token
            // Initialize your WebSocket listener
            WebSocketListener webSocketListener = new WebSocketListener();

            // Begin the streaming session
            streamingClient.connect(webSocketListener, streamContentType, sessionConfig);

        } else {
            while (isStart) {
                jButtonStart.setFocusable(false);
                streamingClient.close();
                isStart = false;
                jLabelStatus.setText("Disconnecting");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        // Set the number of bytes to send in each message
    }//GEN-LAST:event_jButtonStartActionPerformed

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.awt.AWTException
     * @throws java.lang.InterruptedException
     */
    public static void main(String args[]) throws IOException,
            AWTException, InterruptedException {
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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>
        appPath = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
        directoryPath = appPath + "\\Audiotranscript";
        File directoryaudio = new File(directoryPath);
        if (!directoryaudio.exists()) {
            directoryaudio.mkdir();
        }
        audiodirectoryPath = appPath + "\\Audiotranscript\\Audio";
        jsondirectoryPath = appPath + "\\Audiotranscript\\Json";
        File directory = new File(audiodirectoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        File directoryjson = new File(jsondirectoryPath);
        if (!directoryjson.exists()) {
            directoryjson.mkdir();
        }
        System.out.println(jsondirectoryPath);

        java.awt.EventQueue.invokeLater(() -> {
            Dashboard frame = new Dashboard();
            frame.pack();
            frame.setLocationRelativeTo(null);  // *** this will center your app ***
            frame.setVisible(true);
        });
    }

    private void saveAudio(File wavFile) {
        try {
            //Get everything set up for playback.
            //Get the previously-saved data into a byte
            // array object.
            if (!wavFile.exists()) {
                wavFile.createNewFile();
            }
            byte audioData[] = byteArrayOutputStream.
                    toByteArray();
            //Get an input stream on the byte array
            // containing the data
            InputStream byteArrayInputStream
                    = new ByteArrayInputStream(audioData);
            AudioFormat audioFormat = getAudioFormat();
            audioInputStream = new AudioInputStream(
                    byteArrayInputStream,
                    audioFormat,
                    audioData.length / audioFormat.
                            getFrameSize());

            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);

        } catch (Exception e) {
            System.out.println(e);
            streamingClient.close();
        }//end catch
    }//end playAudio

    private JSONObject responseToJson(Hypothesis hypothesis) {
        JSONObject obj = new JSONObject();
        obj.put("ts", hypothesis.getTs());
        obj.put("endTs", hypothesis.getEndTs());

        JSONArray elements = new JSONArray();
        int hypoLength = hypothesis.getElements().length;
        for (int i = 0; i < hypoLength; i++) {
            Element element = hypothesis.getElements()[i];
            JSONObject ele = new JSONObject();
            ele.put("startTimestamp", element.getStartTimestamp());
            ele.put("endTimestamp", element.getEndTimestamp());
            ele.put("type", element.getType());
            ele.put("value", element.getValue());
            ele.put("confidence", element.getConfidence());
            elements.add(ele);
        }
        obj.put("elements", elements);

        return obj;
    }
//This method captures audio input from a
    // microphone and saves it in a
    // ByteArrayOutputStream object.

    private void captureAudio() {
        if (!stopCapture) {
            try {
                //Get and display a list of
                // available mixers.
                transcript = new JSONObject();
                messages = new JSONArray();
                Mixer.Info[] mixerInfo
                        = AudioSystem.getMixerInfo();
                System.out.println("Available mixers:");
                Mixer.Info rMixer = null;
                for (Mixer.Info mixerInfo1 : mixerInfo) {
                    System.out.println(mixerInfo1.getName());
                    log.setText(mixerInfo1.getName());
                    if (mixerInfo1.getName().equals("Primary Sound Capture Driver")) {
                        rMixer = mixerInfo1;
                    }
                } //end for loop

                //Get everything set up for capture
                audioFormat = getAudioFormat();
                System.out.println(audioFormat.toString());
                DataLine.Info dataLineInfo
                        = new DataLine.Info(
                                TargetDataLine.class,
                                audioFormat);

                //Select one of the available
                // mixers.
                Mixer mixer = AudioSystem.
                        getMixer(rMixer);

                log.setText("Using: " + rMixer.getName());
                log.setFocusable(false);
                //Get a TargetDataLine on the selected
                // mixer.
                targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
                //Prepare the line for use.

                targetDataLine.open(audioFormat);
                targetDataLine.start();

                //Create a thread to capture the microphone
                // data and start it running.  It will run
                // until the Stop button is clicked.
                captureThread = new CaptureThread();
                captureThread.start();

                // captureThread.start();
            } catch (Exception e) {
                System.out.println("error cap:" + e);
                log.setText("Audio Error: " + e.getMessage());
                streamingClient.close();
                // System.exit(0);
            }//end catch
        }

    }//end captureAudio method

    //This method creates and returns an
    // AudioFormat object for a given set of format
    // parameters.  If these parameters don't work
    // well for you, try some of the other
    // allowable parameter values, which are shown
    // in comments following the declarations.
    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(
                sampleRate,
                sampleSizeInBits,
                channels,
                signed,
                bigEndian);
    }//end getAudioFormat

//Inner class to capture data from microphone
    class CaptureThread implements Runnable {

        Thread t;

        CaptureThread() {
            t = new Thread(this);
            stopCapture = false;
        }
        byte tempBuffer[] = new byte[10000];

        @Override
        public void run() {
            byteArrayOutputStream
                    = new ByteArrayOutputStream();
            //stopCapture = false;
            try {//Loop until stopCapture is set by
                // another thread that services the Stop
                // button.
                while (!stopCapture) {
                    //Read data from the internal buffer of
                    // the data line.
                    int cnt = targetDataLine.read(tempBuffer,
                            0,
                            tempBuffer.length);

                    if (cnt > 0) {
                        streamDataToServer(tempBuffer);
                        byteArrayOutputStream.write(tempBuffer,
                                0,
                                cnt);

                    }//end if
                }//end while
                byteArrayOutputStream.close();
            } catch (Exception e) {
                System.out.println("error stream: " + e);
                log.setText("Stream Error: " + e);
                streamingClient.close();
            }//end catch
        }//end run

        public void start() {
            System.out.println("capture start");
            t.start();
        }

        public void stop() {
            stopCapture = true;
        }
    }//end inner class CaptureThread

    public void streamDataToServer(byte[] fileByteArray) throws InterruptedException {

        // Stream the file in the configured chunk size
        for (int start = 0; start < fileByteArray.length; start += chunk) {
            streamingClient.sendAudioData(
                    ByteString.of(
                            ByteBuffer.wrap(
                                    Arrays.copyOfRange(
                                            fileByteArray, start, Math.min(fileByteArray.length, start + chunk)))));
        }

        // Wait to make sure all responses are received
        // Thread.sleep(5000);       
    }

    public class WebSocketListener implements RevAiWebSocketListener {

        @Override
        public void onConnected(ConnectedMessage message) {
            System.out.println("Connected: " + message);
            jLabelStatus.setText("Connected");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            }

            captureAudio();

        }

        @Override
        public void onHypothesis(Hypothesis hypothesis) {

            if (hypothesis.toString().contains("messageType='final'")) {
                //  System.out.println("Response: " + hypothesis.toString());

                int hypoLength = hypothesis.getElements().length;
                for (int i = 0; i < hypoLength; i++) {
                    Element element = hypothesis.getElements()[i];
                    // System.out.print(element.getValue());
                    Keyboard keyboard;
                    try {
                        keyboard = new Keyboard();
                        if (element.getValue().equals(".") || element.getValue().equals(""
                                + "?")) {
                            keyboard.type(element.getValue());
                            keyboard.type('\n');
                        } else {

                            keyboard.type(element.getValue());
                        }

                    } catch (AWTException ex) {
                        Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                messages.add(responseToJson(hypothesis));

                // System.out.println(messages.toJSONString());
            }

        }

        @Override
        public void onError(Throwable t, Response response) {
            System.out.println("error: " + response);
            //log.setText("Network Error: "+ response);
        }

        @Override
        public void onClose(int code, String reason) {
            System.out.println("onclose: " + reason);
            jLabelStatus.setText("Disconnected");
            jButtonStart.setText("Start");
            isStart = false;
            captureThread.stop();
            targetDataLine.stop();
            targetDataLine.close();
            audioFile = new File(audiodirectoryPath + "\\" + dtf.format(now) + ".wav");
            saveAudio(audioFile);
            //  transcript.put(messages);
            jsonFile = new File(jsondirectoryPath + "\\" + dtf.format(now) + ".json");

            try {
                file = new FileWriter(jsonFile);
                file.write(messages.toJSONString());
                file.flush();
                file.close();
            } catch (IOException ex) {
                Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        @Override
        public void onOpen(Response response) {
            System.out.println("onopen: " + response.toString());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonStart;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JLabel log;
    // End of variables declaration//GEN-END:variables
}
