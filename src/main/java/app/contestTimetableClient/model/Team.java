package app.contestTimetableClient.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Team {

    @Id
    private String schoolid;
    private String name;
    private int members;
    private String ticket;
    private double distance;



    public Team() {
    }

    public Team(String schoolid, String name, int members) {
        this.schoolid = schoolid;
        this.name = name;
        this.members = members;
    }

    public Team(String schoolid, String name, int members, String ticket) {
        this.schoolid = schoolid;
        this.name = name;
        this.members = members;
        this.ticket = ticket;
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

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}
