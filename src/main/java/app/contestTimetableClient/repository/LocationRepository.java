package app.contestTimetableClient.repository;

import app.contestTimetableClient.model.Location;
import org.springframework.data.repository.CrudRepository;

public interface LocationRepository extends CrudRepository<Location, String> {

    Location findBySchoolid(String schoolid);
}
