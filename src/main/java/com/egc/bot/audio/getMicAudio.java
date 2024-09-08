package com.egc.bot.audio;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static com.egc.bot.Bot.*;

public class getMicAudio {

    public void run() throws InterruptedException {
        //System.out.println(recievedBytes.size());
        recievedBytes.clear();
        //man.openAudioConnection(client.getVoiceChannelById(keys.get("MAIN_VC_ID")));
        record=true;
        System.out.println(recievedBytes.size());
        while (record) {
            Thread.onSpinWait();
        }
      //System.out.println("Resumed");
        System.out.println(recievedBytes.size());
        try {
            int size=0;
            for (byte[] bs : recievedBytes) {
                size+=bs.length;
            }
            byte[] decodedData=new byte[size];
            int i=0;
            for (byte[] bs : recievedBytes) {
                for (int j = 0; j < bs.length; j++) {
                    decodedData[i++]=bs[j];
                }
            }
            getWavFile(decodedData);
        } catch (IOException|OutOfMemoryError e) {
            e.printStackTrace();
        }
}
    public void setUp(){
        //System.out.println(recievedBytes.size());
        recievedBytes.clear();
        man.openAudioConnection(client.getVoiceChannelById(keys.get("MAIN_VC_ID")));
        try {
            record=true;
            Thread.sleep((1000L));
        } catch (InterruptedException e) {
            System.out.println(e);
            Thread.currentThread().interrupt();
        }
    record=false;

        try {
            int size = 0;
            for (byte[] bs : recievedBytes) {
                size += bs.length;
            }
            byte[] decodedData = new byte[size];
            int i = 0;
            for (byte[] bs : recievedBytes) {
                for (int j = 0; j < bs.length; j++) {
                    decodedData[i++] = bs[j];
                }
            }
            getWavFile(decodedData);
        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
        }
        man.openAudioConnection(client.getVoiceChannelById(keys.get("MAIN_VC_ID")));
    }
    private void getWavFile(byte[] decodedData) throws IOException {
        AudioFormat format = new AudioFormat(48000.0F, 16, 2, true, true);
        boolean convertable = AudioSystem.isConversionSupported(
                new AudioFormat(16000, 16, 1, true, false), format);
        //System.out.println("Can be converted: " + convertable);
        AudioSystem.write(new AudioInputStream(
                        new ByteArrayInputStream(decodedData), format, decodedData.length),
                AudioFileFormat.Type.WAVE, new File("out.wav"));
        File converted = new File("converted.wav");
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                    new AudioFormat(16000, 16, 1, true, false),
                    AudioSystem.getAudioInputStream(new File("out.wav")));
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, converted);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}