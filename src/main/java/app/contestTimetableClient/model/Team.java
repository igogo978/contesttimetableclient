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

    private double distance;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Contestid> contestids = new ArrayList<Contestid>();

    public Team() {
    }

    public Team(String schoolid, String name, int members, double distance, List<Contestid> contestids) {
        this.schoolid = schoolid;
        this.name = name;
        this.members = members;
        this.distance = distance;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }


    public List<Contestid> getContestids() {
        return contestids;
    }
}
