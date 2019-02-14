package app.contestTimetableClient.service;


import app.contestTimetableClient.model.Location;
import app.contestTimetableClient.model.School;
import app.contestTimetableClient.model.Team;
import app.contestTimetableClient.repository.LocationRepository;
import app.contestTimetableClient.repository.SchoolRepository;
import app.contestTimetableClient.repository.TeamRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class InitService {

    @Autowired
    SchoolRepository schoolrepository;

    @Autowired
    LocationRepository locationrepository;

    @Autowired
    TeamRepository teamrepository;


    public void initSchool(String url, String target) throws IOException {
        //取得所有學校
        RestTemplate resttemplate = new RestTemplate();
        JsonNode node = null;
        ResponseEntity<String> result = null;
        ObjectMapper mapper = new ObjectMapper();

//        String cwd = System.getProperty("user.dir");
        String site = String.format("%s/%s", url, target);

        result = resttemplate.getForEntity(site, String.class);
        node = mapper.readTree(result.getBody());

        node.forEach(school -> {
            String schoolid = school.get("schoolid").asText();
            String schoolname = school.get("schoolname").asText();
            String position = school.get("position").asText();
            schoolrepository.save(new School(schoolid, schoolname, position));
        });
    }


    public void initLocation(String url, String target) throws IOException {
        RestTemplate resttemplate = new RestTemplate();
        JsonNode node = null;
        ResponseEntity<String> result = null;
        ObjectMapper mapper = new ObjectMapper();
        String site = String.format("%s/%s", url, target);

        result = resttemplate.getForEntity(site, String.class);
        node = mapper.readTree(result.getBody());
        node.forEach(location -> {
            String schoolid = location.get("schoolid").asText();
            String name = schoolrepository.findBySchoolid(location.get("schoolid").asText()).getSchoolname();
            Integer capacity = location.get("capacity").asInt();
            locationrepository.save(new Location(schoolid, name, capacity));
        });

    }

    public void initTeam(String url, String target) throws IOException {
        RestTemplate resttemplate = new RestTemplate();
        JsonNode node = null;
        ResponseEntity<String> result = null;
        ObjectMapper mapper = new ObjectMapper();
        String site = String.format("%s/%s", url, target);
        System.out.println(site);
        result = resttemplate.getForEntity(site, String.class);
        node = mapper.readTree(result.getBody());
        node.forEach(item -> {
            String schoolid = item.get("schoolid").asText();
            String schoolname = item.get("schoolname").asText();
            Integer members = item.get("members").asInt();
            String ticket = "";
            teamrepository.save(new Team(schoolid, schoolname, members, ticket));
        });

    }

    public void initTicket(String url, String target) throws IOException {
        RestTemplate resttemplate = new RestTemplate();
        JsonNode node = null;
        ResponseEntity<String> result = null;
        ObjectMapper mapper = new ObjectMapper();
        String site = String.format("%s/%s", url, target);

        result = resttemplate.getForEntity(site, String.class);
        node = mapper.readTree(result.getBody());
        node.forEach(ticket -> {
//            System.out.println(ticket.get("schoolid").asText());
            String schoolid = ticket.get("schoolid").asText();
//            System.out.println(teamrepository.countBySchoolid(schoolid));
            if (teamrepository.countBySchoolid(schoolid) != 0) {
                Team team = teamrepository.findBySchoolid(schoolid);
                team.setTicket(ticket.get("location").asText());
                teamrepository.save(team);
            }
        });

    }


}
