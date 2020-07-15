package org.rootio.tools.telephony;

import org.rootio.tools.utils.Utils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class CallRecorder {
    private AudioFormat format;
    TargetDataLine line;
    private boolean stopped;

    public CallRecorder() {
    }

    private synchronized String getFileName() {
        String datePart = Utils.getCurrentDateAsString("yyyyMMddHHmmss");
        String filePath = "/mnt/extSdCard/calls";
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return String.format("%s/%s.3gp", filePath, datePart);
    }

    private TargetDataLine getAudioInput() throws LineUnavailableException {
        format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                format); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) {
            throw new IllegalStateException("Selected audio input is not available");
        }
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            return line;
        } catch (LineUnavailableException e) {
            throw e;
        }

    }

    AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }


    public void startRecording() throws IOException, LineUnavailableException {

        File recordingFile = new File(this.getFileName());
        try {
            TargetDataLine line = getAudioInput();
            line.open(format);
            AudioInputStream instr = new AudioInputStream(line);
            line.start();
            AudioSystem.write(instr, AudioFileFormat.Type.WAVE, recordingFile);
        } catch (LineUnavailableException | IOException ex) {
            throw ex;
        }
    }

    public void stopRecording() {
        line.close();
    }
}
