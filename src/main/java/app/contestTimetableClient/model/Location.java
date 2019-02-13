package app.contestTimetableClient.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Location {
    @Id
    private String schoolid;

    private String name;
    private Integer capacity;
    public Location() {
    }

    public Location(String schoolid, String name, Integer capacity) {
        this.schoolid = schoolid;
        this.name = name;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchoolid() {
        return schoolid;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getCapacity() {
        return capacity;
    }
}
