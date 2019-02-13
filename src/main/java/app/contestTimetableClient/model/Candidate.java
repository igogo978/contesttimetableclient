package app.contestTimetableClient.model;

import java.util.ArrayList;
import java.util.List;

public class Candidate {

   private Location location;
   private ArrayList<Team> teams;

    public Candidate() {
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }

    public void setTeams(ArrayList<Team> teams) {
        this.teams = teams;
    }
}
