package com.egc.bot.audio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PCMtoWAVConverter {

    public static void convertToWavFile(byte[] pcmData, String outputFilePath) {
        int sampleRate = 16000; // Whisper supports 16kHz
        int numChannels = 1; // Mono
        int bitsPerSample = 16; // 16-bit audio

        int byteRate = sampleRate * numChannels * (bitsPerSample / 8);
        int subChunk2Size = pcmData.length;
        int chunkSize = 36 + subChunk2Size;

        try (FileOutputStream out = new FileOutputStream(new File(outputFilePath))) {
            // Write WAV Header
            out.write("RIFF".getBytes()); // RIFF chunk
            out.write(intToLittleEndian(chunkSize)); // File size
            out.write("WAVE".getBytes()); // WAV format

            // Format Subchunk
            out.write("fmt ".getBytes()); // fmt chunk
            out.write(intToLittleEndian(16)); // Subchunk1 size
            out.write(shortToLittleEndian((short) 1)); // PCM format
            out.write(shortToLittleEndian((short) numChannels)); // Number of channels
            out.write(intToLittleEndian(sampleRate)); // Sample rate
            out.write(intToLittleEndian(byteRate)); // Byte rate
            out.write(shortToLittleEndian((short) (numChannels * (bitsPerSample / 8)))); // Block align
            out.write(shortToLittleEndian((short) bitsPerSample)); // Bits per sample

            // Data Subchunk
            out.write("data".getBytes()); // Data header
            out.write(intToLittleEndian(subChunk2Size)); // Subchunk2 size
            out.write(pcmData); // PCM data

            System.out.println("WAV file saved: " + outputFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Error writing WAV file", e);
        }
    }

    private static byte[] intToLittleEndian(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    private static byte[] shortToLittleEndian(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }
}
