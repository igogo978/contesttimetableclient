package app.contestTimetableClient.model;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Location {
    @Id
    private String schoolid;

    private String name;
    private Integer capacity;

    @ElementCollection(fetch= FetchType.EAGER)
    private List<Contestid> contestids = new ArrayList<Contestid>();

    public Location() {
    }

    public Location(String schoolid, String name, Integer capacity, List<Contestid> contestids) {
        this.schoolid = schoolid;
        this.name = name;
        this.capacity = capacity;
        this.contestids = contestids;
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

    public List<Contestid> getContestids() {
        return contestids;
    }
}
