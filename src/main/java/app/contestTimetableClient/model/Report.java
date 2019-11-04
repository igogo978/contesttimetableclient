package app.contestTimetableClient.model;

import java.util.List;

public class Report {

    private String uuid;


    private List<Candidate> candidateList;
    private double totaldistance;


    public Report() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public List<Candidate> getCandidateList() {
        return candidateList;
    }

    public void setCandidateList(List<Candidate> candidateList) {
        this.candidateList = candidateList;
    }

    public double getTotaldistance() {
        return totaldistance;
    }

    public void setTotaldistance(double totaldistance) {
        this.totaldistance = totaldistance;
    }

}
