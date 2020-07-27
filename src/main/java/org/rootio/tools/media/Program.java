package org.rootio.tools.media;

import org.json.JSONArray;
import org.json.JSONException;
import org.rootio.tools.radio.ScheduleBroadcastHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Program implements Comparable<Program>, ScheduleNotifiable {

    private String title;
    private Date startDate, endDate;
    private int playingIndex;
    private ArrayList<ProgramAction> programActions;
    private boolean isLocal;
    private ScheduleBroadcastHandler alertHandler;

    public Program(String title, Date start, Date end, String structure) {
        this.title = title;
        this.startDate = start;
        this.endDate = end;
        this.loadProgramActions(structure);
    }

    public void stop() {
        try {
            this.programActions.get(this.playingIndex).stop();
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING,  e.getMessage() == null ? "Null pointer[ProgramHandler.processJSONObject]" : e.getMessage());
        }
    }

    public void pause() {
        this.programActions.get(this.playingIndex).pause();
    }

    public void resume() {
        this.programActions.get(this.playingIndex).resume();
    }

    private void loadProgramActions(String structure) {
        this.programActions = new ArrayList<>();
        JSONArray programStructure;
        try {
            programStructure = new JSONArray(structure);
            ArrayList<String> playlists = new ArrayList<>();
            ArrayList<String> streams = new ArrayList<>();
            int duration =0;
            for (int i = 0; i < programStructure.length(); i++) {
                if (programStructure.getJSONObject(i).getString("type").toLowerCase().equals("music"))//redundant, safe
                {
                    //accumulate playlists
                    playlists.add(programStructure.getJSONObject(i).getString("name"));
                    this.isLocal = true;
                }
                if (programStructure.getJSONObject(i).getString("type").toLowerCase().equals("stream"))//redundant, safe
                {
                    //accumulate playlists
                    streams.add(programStructure.getJSONObject(i).getString("stream_url"));
                    this.isLocal = true;
                }
                if(programStructure.getJSONObject(i).has("duration")) { //redundant, using optInt
                    duration = programStructure.getJSONObject(i).optInt("duration");
                }
            }

            this.programActions.add(new ProgramAction(playlists, streams, ProgramActionType.Audio, duration));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Returns the title of this program
     *
     * @return String representation of the title of this program
     */
    public String getTitle() {
        return this.title;
    }


    public void run() {
        this.runProgram(0);
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public boolean isLocal()
    {
        return this.isLocal;
    }

    @Override
    public int compareTo(Program another) {
        return this.startDate.compareTo(another.getStartDate());
    }




    @Override
    public void runProgram(int currentIndex) {
        this.programActions.get(currentIndex).run();
    }

    @Override
    public void stopProgram(Integer index) {
        this.programActions.get(index).stop();

    }

    @Override
    public boolean isExpired(int index) {
        Calendar referenceCalendar = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, this.programActions.get(index).getDuration() - 1); //fetch the duration from the DB for each program action
        return this.endDate.compareTo(referenceCalendar.getTime()) <= 0;
    }
}
