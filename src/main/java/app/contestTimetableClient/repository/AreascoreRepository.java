package app.contestTimetableClient.repository;

import app.contestTimetableClient.model.Team;
import app.contestTimetableClient.model.scores.Areascore;
import org.springframework.data.repository.CrudRepository;

import java.awt.geom.Area;
import java.util.List;

public interface AreascoreRepository extends CrudRepository<Areascore, Long> {
    List<Areascore> findByStartareaAndScoresLessThanEqual(String startarea, Double scores);
    List<Areascore> findByStartareaAndScoresGreaterThanOrderByScoresAsc(String startarea, Double scores);
    Areascore findByStartareaAndEndarea(String startarea, String endarea);

}
