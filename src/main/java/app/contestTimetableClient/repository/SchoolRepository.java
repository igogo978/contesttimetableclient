package app.contestTimetableClient.repository;

import app.contestTimetableClient.model.School;
import org.springframework.data.repository.CrudRepository;

public interface SchoolRepository extends CrudRepository<School, String> {

    School findBySchoolid(String schoolid);
}
