package app.contestTimetableClient.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class School {

    @Id
    private String schoolid;

    private String schoolname;
//    private String position;

    public School() {
    }

    public School(String schoolid, String schoolname) {
        this.schoolid = schoolid;
        this.schoolname = schoolname;
    }

    //    public School(String schoolid, String schoolname, String position) {
//        this.schoolid = schoolid;
//        this.schoolname = schoolname;
//        this.position = position;
//    }


    public String getSchoolname() {
        return schoolname;
    }

//    public String getPosition() {
//        return position;
//    }
}
