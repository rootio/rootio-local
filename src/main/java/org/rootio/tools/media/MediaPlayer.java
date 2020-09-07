package org.rootio.tools.media;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class MediaPlayer {
    private URL streamUrl;
    private boolean isStream;
    private Runnable errorHandler, endHandler, readyHandler;
    private String filename;
    private SourceDataLine line;
    private Status status;

    MediaPlayer(String filename)
    {
        this.filename = filename;
    }

    MediaPlayer(URL path)
    {
        this.streamUrl = path;
        this.isStream = true;
    }

    public void play() {
        AudioInputStream din = null;
        AudioInputStream in = null;
        try {
            if(!isStream) {
                File file = new File(filename);
                in = AudioSystem.getAudioInputStream(file);

            }
            else
            {
                in = AudioSystem.getAudioInputStream(this.streamUrl);
            }
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    true,
                    false);
            din = AudioSystem.getAudioInputStream(decodedFormat, in);
            status = Status.PLAYING;
            rawplay(decodedFormat, din);
            status = Status.STOPPED;
            if(endHandler != null) {
                new Thread(endHandler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            status = Status.STOPPED;
            if(errorHandler != null) {
                new Thread(errorHandler).start();
            }
        }
        finally {
            try
            {
                in.close();
                din.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnError(Runnable r)
    {
            this.errorHandler = r;
    }

    public void setOnEnd(Runnable r)
    {
            this.endHandler = r;
    }

    private void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException {
        byte[] data = new byte[4096];
        line = getLine(targetFormat);
        if (line != null) {
            // Start
            line.start();
            //call the readiness handler
            if(readyHandler != null)
            {
                new Thread(readyHandler).start();
            }
            int nBytesRead = 0, nBytesWritten = 0;
            while (nBytesRead != -1) {
                nBytesRead = din.read(data, 0, data.length);
                if (nBytesRead != -1) nBytesWritten = line.write(data, 0, nBytesRead);
            }
            stop();
            din.close();
        }
    }

    public void setVolume(float volume) {
        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl vol = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            vol.setValue(vol.getValue() * volume);
        }
    }

    public void stop()
    {
        status = Status.STOPPED;
        try {
            line.stop();
            line.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

    public  Status getStatus() {
        return status;
    }

    public void setOnReady(Runnable r) {
        readyHandler = r;
    }

    public float getVolume() {
        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl vol = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            return vol.getValue();
        }
        return 0;
    }

    enum Status {PLAYING, PAUSED, STOPPED};
}

