package app.contestTimetableClient.model;

import java.util.List;

public class Report {

    private String uuid;


    private List<Candidate> candidateList;
    private double totalscores;

    private String scoresFrequency;

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

    public double getTotalscores() {
        return totalscores;
    }

    public void setTotalscores(double totalscores) {
        this.totalscores = totalscores;
    }

    public String getScoresFrequency() {
        return scoresFrequency;
    }

    public void setScoresFrequency(String scoresFrequency) {
        this.scoresFrequency = scoresFrequency;
    }
}
