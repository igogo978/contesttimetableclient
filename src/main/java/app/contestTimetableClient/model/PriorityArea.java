package app.contestTimetableClient.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;



public class PriorityArea {

    String location;

    List<String> areas = new ArrayList<>();

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getAreas() {
        return areas;
    }
}
