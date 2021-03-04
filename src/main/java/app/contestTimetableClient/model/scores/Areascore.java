package app.contestTimetableClient.model.scores;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Areascore {

    @Id
    private String id;

    private String startarea;
    private String endarea;

    private Double scores;

    private Boolean isPriorityArea;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartarea() {
        return startarea;
    }

    public void setStartarea(String startarea) {
        this.startarea = startarea;
    }

    public String getEndarea() {
        return endarea;
    }

    public void setEndarea(String endarea) {
        this.endarea = endarea;
    }

    public Double getScores() {
        return scores;
    }

    public void setScores(Double scores) {
        this.scores = scores;
    }

    public Boolean getPriorityArea() {
        return isPriorityArea;
    }

    public void setPriorityArea(Boolean priorityArea) {
        isPriorityArea = priorityArea;
    }
}
