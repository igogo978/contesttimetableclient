package app.contestTimetableClient.model;

import java.util.ArrayList;

public class Report {

    private String uuid;

    private ArrayList<Candidate> candidateList;
    private double totaldistance;
    private double weightedscores;

    public Report() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ArrayList<Candidate> getCandidateList() {
        return candidateList;
    }

    public void setCandidateList(ArrayList<Candidate> candidateList) {
        this.candidateList = candidateList;
    }

    public double getTotaldistance() {
        return totaldistance;
    }

    public void setTotaldistance(double totaldistance) {
        this.totaldistance = totaldistance;
    }

    public double getWeightedscores() {
        return weightedscores;
    }

    public void setWeightedscores(double weightedscores) {
        this.weightedscores = weightedscores;
    }
}
