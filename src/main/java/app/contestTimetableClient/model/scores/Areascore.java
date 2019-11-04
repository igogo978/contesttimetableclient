package app.contestTimetableClient.model.scores;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Areascore {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String startarea;
    private String endarea;

    private Double scores;

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
}
