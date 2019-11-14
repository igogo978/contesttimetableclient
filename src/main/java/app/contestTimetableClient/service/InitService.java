package app.contestTimetableClient.service;


import app.contestTimetableClient.model.*;
import app.contestTimetableClient.model.scores.Areascore;
import app.contestTimetableClient.repository.AreascoreRepository;
import app.contestTimetableClient.repository.LocationRepository;
import app.contestTimetableClient.repository.SchoolRepository;
import app.contestTimetableClient.repository.TeamRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Service
public class InitService {

    @Autowired
    SchoolRepository schoolrepository;

    @Autowired
    LocationRepository locationrepository;

    @Autowired
    TeamRepository teamrepository;

    @Autowired
    AreascoreRepository areascoreRepository;


    public void initSchool(String url, String target) throws IOException {
        //取得所有學校
        RestTemplate resttemplate = new RestTemplate();
        resttemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        JsonNode node = null;
        ResponseEntity<String> result = null;
        ObjectMapper mapper = new ObjectMapper();

//        String cwd = System.getProperty("user.dir");
        String site = String.format("%s/%s", url, target);
//        System.out.println("initschool:" + site);
        result = resttemplate.getForEntity(site, String.class);
        node = mapper.readTree(result.getBody());

        node.forEach(school -> {

            String schoolid = school.get("schoolid").asText();
            String schoolname = school.get("schoolname").asText();
//            System.out.println(schoolname);

//            String position = school.get("position").asText();
//            schoolrepository.save(new School(schoolid, schoolname, position));
            schoolrepository.save(new School(schoolid, schoolname));
        });
    }


    public void initLocation(String url, String target) throws IOException {
        RestTemplate resttemplate = new RestTemplate();
        resttemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
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
            List<Contestid> contestids = new ArrayList<>();
            location.get("contestids").forEach(contestidNode -> {
//               System.out.println(contestidNode.get("contestid").asInt());
                contestids.add(new Contestid(contestidNode.get("contestid").asInt(), contestidNode.get("members").asInt()));
            });
            locationrepository.save(new Location(schoolid, name, capacity, contestids));
        });

    }

    public void initTeam(String url, String target) throws IOException {
        RestTemplate resttemplate = new RestTemplate();
        resttemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        JsonNode node = null;
        ResponseEntity<String> result = null;
        ObjectMapper mapper = new ObjectMapper();
        String site = String.format("%s/%s", url, target);
        result = resttemplate.getForEntity(site, String.class);
        node = mapper.readTree(result.getBody());
        node.forEach(item -> {
            String schoolid = item.get("schoolid").asText();
            String schoolname = item.get("schoolname").asText();
            Integer members = item.get("members").asInt();
            String ticket = "";
            Double distance = 0.0;
            List<Contestid> contestids = new ArrayList<>();
            item.get("contestids").forEach(contestidNode -> {
//               System.out.println(contestidNode.get("contestid").asInt());
                contestids.add(new Contestid(contestidNode.get("contestid").asInt(), contestidNode.get("members").asInt()));
            });


            teamrepository.save(new Team(schoolid, schoolname, members, distance, contestids));
        });


    }

    public List<Ticket> initTicket(String url, String target) throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        RestTemplate resttemplate = new RestTemplate();
        resttemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        JsonNode node = null;
        ResponseEntity<String> result = null;
        ObjectMapper mapper = new ObjectMapper();
        String site = String.format("%s/%s", url, target);
        result = resttemplate.getForEntity(site, String.class);
        node = mapper.readTree(result.getBody());
        node.forEach(ticket -> {
            Ticket t = new Ticket();
            t.setLocationid(ticket.get("locationid").asText());
            t.setLocationname(ticket.get("locationname").asText());
            t.setSchoolid(ticket.get("schoolid").asText());
            t.setSchoolname(ticket.get("schoolname").asText());

            tickets.add(t);
        });

        return tickets;
    }

    public void initScoresArea(String url, String target) throws IOException {
        List<Areascore> areas = new ArrayList<>();
        RestTemplate resttemplate = new RestTemplate();
        resttemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        JsonNode root = null;
        ResponseEntity<String> result = null;
        ObjectMapper mapper = new ObjectMapper();
        String site = String.format("%s/%s", url, target);
        result = resttemplate.getForEntity(site, String.class);

        root = mapper.readTree(result.getBody());

        root.forEach(node->{
            Areascore area = new Areascore();
            area.setStartarea(node.get("startarea").asText());
            area.setEndarea(node.get("endarea").asText());
            area.setScores(node.get("scores").asDouble());
            areascoreRepository.save(area);
        });


    }


}
