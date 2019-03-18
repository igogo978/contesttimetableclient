package app.contestTimetableClient.model;

import javax.persistence.Embeddable;

@Embeddable
public class Contestid {

    private Integer contestid;
    private Integer members;

    public Contestid() {
    }

    public Contestid(Integer contestid, Integer members) {
        this.contestid = contestid;
        this.members = members;
    }

    public Integer getContestid() {
        return contestid;
    }


    public void setContestid(Integer contestid) {
        this.contestid = contestid;
    }

    public Integer getMembers() {
        return members;
    }

    public void setMembers(Integer members) {
        this.members = members;
    }
}
