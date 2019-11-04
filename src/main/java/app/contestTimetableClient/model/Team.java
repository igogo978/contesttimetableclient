package app.contestTimetableClient.model;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Team {

    @Id
    private String schoolid;
    private String name;
    private int members;

    private double scores;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Contestid> contestids = new ArrayList<Contestid>();

    public Team() {
    }

    public Team(String schoolid, String name, int members, double scores, List<Contestid> contestids) {
        this.schoolid = schoolid;
        this.name = name;
        this.members = members;
        this.scores = scores;
        this.contestids = contestids;
    }



    public String getSchoolid() {
        return schoolid;
    }

    public void setSchoolid(String schoolid) {
        this.schoolid = schoolid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public double getScores() {
        return scores;
    }

    public void setScores(double scores) {
        this.scores = scores;
    }

    public List<Contestid> getContestids() {
        return contestids;
    }
}
