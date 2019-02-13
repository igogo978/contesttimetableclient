package app.contestTimetableClient.repository;

import app.contestTimetableClient.model.Location;
import app.contestTimetableClient.model.Team;
import org.springframework.data.repository.CrudRepository;

public interface TeamRepository extends CrudRepository<Team, String> {

    Team findBySchoolid(String schoolid);

    Integer countBySchoolid(String schoolid);
}
