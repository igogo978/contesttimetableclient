package app.contestTimetableClient;

import app.contestTimetableClient.model.*;
import app.contestTimetableClient.model.scores.Areascore;
import app.contestTimetableClient.repository.AreascoreRepository;
import app.contestTimetableClient.repository.LocationRepository;
import app.contestTimetableClient.repository.SchoolRepository;
import app.contestTimetableClient.repository.TeamRepository;
import app.contestTimetableClient.service.LocationService;
import app.contestTimetableClient.service.ScoresService;
import app.contestTimetableClient.service.InitService;
import app.contestTimetableClient.service.ReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

@SpringBootApplication
public class ContestTimetableClientApplication implements CommandLineRunner {

    @Value("${config}")
    private String configfile;

    @Autowired
    SchoolRepository schoolrepository;

    @Autowired
    LocationRepository locationrepository;

    @Autowired
    TeamRepository teamrepository;

    @Autowired
    ScoresService scoresService;

    @Autowired
    LocationService locationService;

    @Autowired
    ReportService reportservice;

    @Autowired
    InitService initservice;

    @Autowired
    AreascoreRepository areascoreRepository;


    public static void main(String[] args) {

        ConfigurableApplicationContext ctx = SpringApplication.run(ContestTimetableClientApplication.class, args);
        ctx.close();
    }


    @Override
    public void run(String... args) throws Exception {
        //鄰近試場, 單位公尺 10000 = 10公里  3可接受
        Double neighborScores = 2.0;

        RestTemplate resttemplate = new RestTemplate();
        JsonNode node = null;
        ResponseEntity<String> response = null;
        ObjectMapper mapper = new ObjectMapper();
        String url = null;

        String cwd = System.getProperty("user.dir");
        String target;

        //計算每多少筆後,取ranking值為前面筆數者, 寫入server 端
        Integer split = 10;
        Integer job = 100;


        //確認設定檔
        if (new File(String.format("%s/%s", cwd, configfile)).isFile()) {
            //create ObjectMapper instance

            node = mapper.readTree(new File(String.format("%s/%s", cwd, configfile)));
            url = node.get("url").asText();
            split = node.get("split").asInt();
            job = node.get("job").asInt();

        } else {
            System.out.println("無設定檔");
            System.exit(0);
        }

        //取得所有學校
        initservice.initSchool(url, "api/school");
//
        //取得場地容納人數
        initservice.initLocation(url, "api/location");

        //參賽隊
        initservice.initTeam(url, "api/schoolteam");

        //取得有門票隊伍
        List<Ticket> tickets = initservice.initTicket(url, "/api/ticket");

        //行政區得分表
        initservice.initScoresArea(url, "/api/scores/area");

        //請求工作
        target = String.format("%s/%s", url, "job");


        response = resttemplate.getForEntity(target, String.class);
        node = mapper.readTree(response.getBody());

        String locationorder = node.get("locationorder").asText();
        String priorityorder = node.get("priorityorder").asText();
        String group1order = node.get("group1order").asText();
        String group2order = node.get("group2order").asText();


        List<String> locations = Arrays.asList(locationorder.split("-"));
        List<String> priority = Arrays.asList(priorityorder.split("-"));
        List<String> group1 = Arrays.asList(group1order.split("-"));
        List<String> group2 = Arrays.asList(group2order.split("-"));

        ArrayList<Report> reports = new ArrayList<>();

        target = url + "api/report/uuid/";
        for (int i = 0; i <= job; i++) {
            Report report = doJob(locations, priority, group1, group2, tickets, neighborScores);
            reports.add(report);
            if (i%50 == 0 && i !=0) {
                System.out.println(String.format("目前進度 已計算 %s 筆", i));
            }
            if (reports.size() == split) {
//                System.out.println(i);
                //排序
                reports.sort(Comparator.comparing(Report::getTotalscores));
//                reports.forEach(r -> System.out.println(r.getTotalscores()));

                //取出第一筆寫入
                report = reports.get(0);
                System.out.println("建議名單:\n" + mapper.writeValueAsString(report));
                if (!reportservice.isUuidExist(target, report.getUuid())) {
                    //若無則寫入此次記錄
                    System.out.println("寫入記錄");
                    reportservice.insertData(target, report);
                    reports.clear();
                }

            }
        }

    }

//    private void updateReports(List<Report> reports, String url, Integer contestid) throws IOException {
//        for (Report report : reports) {
//            //檢查uuid 是否已存在
//            String target = String.format("%s/job/%s/report", url, contestid);
//            if (!reportservice.isUuidExist(target, report.getUuid())) {
//                //若無則寫入此次記錄
//                reportservice.insertData(target, report);
//            }
//
//        }
//
//
//    }

    private Report doJob(List<String> locations, List<String> priority, List<String> group1, List<String> group2, List<Ticket> tickets, Double neighborScores) throws JsonProcessingException, UnsupportedEncodingException {
        ObjectMapper mapper = new ObjectMapper();
        Double invalidScores = 999999.00;

        //建立候選場地
        List<Candidate> candidateList = new ArrayList<>();
        Candidate pending = new Candidate();

        //最後無法排入試場者,無條件加進pending, 自己獨立一個candidate
        for (String location : locations) {
            if (location.equals("999999")) {

                List<Team> teams = new ArrayList<>();
                pending.setLocation(locationrepository.findBySchoolid(location));
                pending.setTeams(teams);
            } else {
                Candidate candidate = new Candidate();
                List<Team> teams = new ArrayList<>();

                candidate.setLocation(locationrepository.findBySchoolid(location));
                candidate.setTeams(teams);
                candidateList.add(candidate);
            }

        }


        //優先要排的群組,主場或有門票者
        for (String schoolid : priority) {

            //比對優先排在主場的隊伍, /api/ticket
            Team team = teamrepository.findBySchoolid(schoolid);


            if (tickets.stream().anyMatch(ticket -> ticket.getSchoolid().equals(schoolid))) {
                Ticket ticket = tickets.stream()
                                .filter(t->t.getSchoolid().equals(schoolid)).findAny().get();
                candidateList = locationService.addTicketTeam(candidateList, ticket, team);
            } else {
                candidateList = locationService.addPriorityTeam(candidateList, team);
            }

        }


        //大群組
        for (String schoolid : group1) {
            Team team = teamrepository.findBySchoolid(schoolid);

            //是否有場地可容納
            if (locationService.isCapable(candidateList, team)) {
                //找出所有可容納的場地
                List<String> capableLocations = locationService.findCapableLocations(candidateList, team);
//                List<String> neighborLocations = locationService.findNeighborLocations(capableLocations, team, neighborScores);
                List<String> neighborLocations = locationService.findNeighborAreaLocations(capableLocations, team, neighborScores);
                if (neighborLocations.size() != 0) {
                    //如果找到理想location,隨机找一個排入
                    int randomidx = (int) (Math.random() * neighborLocations.size());
//                    candidateList = locationService.addTeam(candidateList, team, neighborLocations.get(randomidx));
                    candidateList = locationService.addTeamByArea(candidateList, team, neighborLocations.get(randomidx));
                } else {
                    //沒有鄰居, 找一個最近的
//                    String locationid = locationService.findABetterLocation(capableLocations, team);
                    Areascore area = locationService.findACommonLocationByArea(capableLocations, team, neighborScores);
                    candidateList = locationService.addTeam(candidateList, team, area);

                }

            } else {
                //加到 未排入名單
                team.setScores(invalidScores);
                pending = locationService.addPendingList(pending, team);
            }
        }


//        //小群組
        Collections.shuffle(group2);
        for (String schoolid : group2) {

            Team team = teamrepository.findBySchoolid(schoolid);

            //是否有場地可容納
            if (locationService.isCapable(candidateList, team)) {
                //找出所有可容納的場地
                List<String> capableLocations = locationService.findCapableLocations(candidateList, team);
                List<String> neighborLocations = locationService.findNeighborAreaLocations(capableLocations, team, neighborScores);
                locationService.findNeighborAreaLocations(capableLocations, team, neighborScores);
                if (neighborLocations.size() != 0) {
                    //如果找到理想location,隨机找一個排入
                    int randomidx = (int) (Math.random() * neighborLocations.size());
                    candidateList = locationService.addTeamByArea(candidateList, team, neighborLocations.get(randomidx));
                } else {
                    //沒有鄰居, 找一個最近的
                    Areascore area = locationService.findACommonLocationByArea(capableLocations, team, neighborScores);
                    candidateList = locationService.addTeam(candidateList, team, area);

                }

            } else {
                //加到 未排入名單
                team.setScores(invalidScores);
                pending = locationService.addPendingList(pending, team);
            }
        }

        candidateList.add(pending);
//        System.out.println(mapper.writeValueAsString(candidateList));


        //        List<Team> teams = new ArrayList<>();
//        group1.forEach(schoolid -> {
//            teams.add(teamrepository.findBySchoolid(schoolid));
//        });


        //uuid
        StringBuilder locationorder = new StringBuilder();
        locations.forEach(location -> locationorder.append(location + "-"));

        StringBuilder priorityorder = new StringBuilder();
        priority.forEach(teamid -> priorityorder.append(teamid + "-"));

        StringBuilder group1order = new StringBuilder();
        group1.forEach(teamid -> group1order.append(teamid + "-"));

        StringBuilder group2order = new StringBuilder();
        group2.forEach(teamid -> group2order.append(teamid + "-"));

        //需要傳送uuid 在網址列並存入資料庫, sha256計算後,資料長度短
        String uuid = org.apache.commons.codec.digest.DigestUtils.sha256Hex(locationorder.toString() + priorityorder.toString() + group1order.toString() + group2order.toString());

        Report report = new Report();
        report.setUuid(uuid);
        report.setCandidateList(candidateList);
        report.setTotalscores(scoresService.getTotalScores(report.getCandidateList()));
        report.setScoresFrequency(scoresService.getScoresFrequency(report.getCandidateList()));
        return report;
    }
}
