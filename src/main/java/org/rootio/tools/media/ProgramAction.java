package org.rootio.tools.media;

import java.util.ArrayList;

public class ProgramAction {
    private ArrayList<String> playlists, streams;
    private ProgramActionType programActionType;
    private PlayList playlist;
    private int duration;

    public ProgramAction(ArrayList<String> playlists, ArrayList<String> streams, ProgramActionType programType, int duration) {
        this.playlists = playlists;
        this.streams = streams;
        this.programActionType = programType;
        this.duration = duration;
    }

    void run() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                this.playlist = PlayList.getInstance();
                this.playlist.init(this.playlists, this.streams, this.programActionType);
                this.playlist.load(true);
                this.playlist.play();
                break;
            case Jingle:
                break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    void resume() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                try {
                    this.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            case Jingle:
                break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    void pause() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                try {
                    this.playlist.pause(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            case Jingle:
                break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    void stop() {
        switch (this.programActionType) {
            case Media:
            case Audio:
                try {
                    this.playlist.stop();
                } catch (Exception ex) {
                    //todo: log this
                }
                break;
            case Jingle:
                break;
            case Outcall:
                break;
            default:
                break;
        }
    }

    public int getDuration() {
        return this.duration;
    }


}
