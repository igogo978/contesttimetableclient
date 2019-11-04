package app.contestTimetableClient;

import app.contestTimetableClient.model.*;
import app.contestTimetableClient.model.scores.Areascore;
import app.contestTimetableClient.repository.AreascoreRepository;
import app.contestTimetableClient.repository.LocationRepository;
import app.contestTimetableClient.repository.SchoolRepository;
import app.contestTimetableClient.repository.TeamRepository;
import app.contestTimetableClient.service.LocationService;
import app.contestTimetableClient.service.DistanceService;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    DistanceService distanceservice;

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
        Integer split = 2000;
        Integer ranking = 1;

        //確認設定檔
        if (new File(String.format("%s/%s", cwd, configfile)).isFile()) {
            //create ObjectMapper instance

            node = mapper.readTree(new File(String.format("%s/%s", cwd, configfile)));
            url = node.get("url").asText();
            split = node.get("split").asInt();
            ranking = node.get("ranking").asInt();
        } else {
            System.out.println("無設定檔");
            System.exit(0);
        }

        //取得所有學校
        initservice.initSchool(url, "school");
//
        //取得場地容納人數
        initservice.initLocation(url, "location");

        //參賽隊
        initservice.initTeam(url, "schoolteam");

        //取得有門票隊伍
        List<Ticket> tickets = initservice.initTicket(url, "/api/ticket");

        //行政區得分表
        initservice.initScoresArea(url, "/scores/area");

        //請求工作
        target = String.format("%s/%s", url, "job");


        response = resttemplate.getForEntity(target, String.class);
        node = mapper.readTree(response.getBody());

        String locationorder = node.get("locationorder").asText();
        String priorityorder = node.get("priorityorder").asText();
        String group1order = node.get("group1order").asText();
        String group2order = node.get("group2order").asText();

        //計算工作次數
        Integer count = node.get("count").asInt();

        List<String> locations = Arrays.asList(locationorder.split("-"));
        List<String> priority = Arrays.asList(priorityorder.split("-"));
        List<String> group1 = Arrays.asList(group1order.split("-"));
        List<String> group2 = Arrays.asList(group2order.split("-"));
        Integer counter = 0;
        ArrayList<Report> reports = new ArrayList<>();

        Report report = doJob(locations, priority, group1, group2, tickets, neighborScores);

        reports.add(report);
        System.out.println("建議名單:\n" + mapper.writeValueAsString(report));

        target = url + "api/report/uuid/";
//        System.out.println(reportservice.isUuidExist(target, report.getUuid()));

        if (!reportservice.isUuidExist(target, report.getUuid())) {
            //若無則寫入此次記錄
            System.out.println("寫入記錄");
            reportservice.insertData(target, report);
        }

//
//
//        //是否取得門票
//        initservice.initTicket(url, "ticket");
//
//
//        //請求工作, 要算幾輪
//        for (int round = 0; round < rounds; round++) {
//            target = String.format("%s/%s?action=true", url, String.format("job/%s", contestid));
//
//            response = resttemplate.getForEntity(target, String.class);
//            node = mapper.readTree(response.getBody());
//            String jobid = node.get("jobid").asText();
//
//
//            String locationorder = node.get("locationorder").asText();
//            String group1order = node.get("group1order").asText();
//            String group2order = node.get("group2order").asText();
//
//            Long calculatejob = node.get("calculatejob").asLong();
//
//            List<String> locations = Arrays.asList(locationorder.split("-"));
//            List<String> group1 = Arrays.asList(group1order.split("-"));
//            List<String> group2 = Arrays.asList(group2order.split("-"));
//            //locationorder, group1order 為固定排序
//            //每一個jobid 會指派計算次數
//            Integer counter = 0;
//            ArrayList<Report> reports = new ArrayList<>();
//            for (int job = 0; job < calculatejob; job++) {
//                //建立候選名單 - 空的 例如:場地一可容納team1, team2, team3
////                    System.out.println(String.format("caltulate times:%s-%s", round, job));
//                Report report = doJob(locations, group1, group2, acceptDistance);
//
//                reports.add(report);
//
//                counter++;
//                if (counter >= split) {
//                    counter = 0;
//                    reports.sort(Comparator.comparing(Report::getTotaldistance));
//
//                    reports.forEach(r -> System.out.println(r.getTotaldistance()));
//
//                    System.out.println("排序後寫入理想筆數：" + ranking);
//                    updateReports(reports.subList(0, ranking), url, contestid);
//                    //清空reports, 重新計算
//                    reports.clear();
//                }
//
//            }
//
//
//        }


    }

    private void updateReports(List<Report> reports, String url, Integer contestid) throws IOException {
        for (Report report : reports) {
            //檢查uuid 是否已存在
            String target = String.format("%s/job/%s/report", url, contestid);
            if (!reportservice.isUuidExist(target, report.getUuid())) {
                //若無則寫入此次記錄
                reportservice.insertData(target, report);
            }

        }


    }

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
                candidateList = locationService.addTicketTeam(candidateList, schoolid, team);
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
                    Areascore area = locationService.findACommonLocationByArea(capableLocations,team, neighborScores);
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
                    Areascore area = locationService.findACommonLocationByArea(capableLocations,team, neighborScores);
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

        //        System.out.println("uuid:" + locationorder.toString() + priorityorder.toString() + group1order.toString() + group2order.toString());
//        StringBuilder group1order = new StringBuilder();
//        group1.forEach(schoolid -> group1order.append(schoolid + "-"));
//
//        //打亂取群组2的顺序
//        Collections.shuffle(group2);
//        StringBuilder smallgrouporder = new StringBuilder();
//        group2.forEach(schoolid -> {
//            teams.add(teamrepository.findBySchoolid(schoolid));
//            smallgrouporder.append(schoolid + "-");
//        });
//
//
//
//        //開始安排場地
//        for (int i = 0; i < teams.size(); i++) {
//            System.out.println(String.format("安排%s", teams.get(i).getName()));
//            //主場隊伍
//            if (locationService.isHomeLocation(candidateList, teams.get(i))) {
//                candidateList = locationService.setHomeLocation(candidateList, teams.get(i));
////                System.out.println(String.format("%s is in home:%s", teams.get(i).getName(), teams.get(i).getMembers()));
//            } else if (locationService.hasTicketAndCapacity(candidateList, teams.get(i))) {
//                //已有門票者
////                System.out.println("已有門票喔");
//                candidateList = locationService.setTicketLocation(candidateList, teams.get(i));
//
//            } else {
//                //找到一場地為最短的距離
////                System.out.println(String.format("%s find a location.", teams.get(i).getName()));
//                candidateList = locationService.findLocation(candidateList, teams.get(i), acceptDistance);
//            }
////            System.out.println(uuid);
////            System.out.println(mapper.writeValueAsString(candidateList));
//        }
//
//
//
//        String uuid = org.apache.commons.codec.digest.DigestUtils.sha256Hex(locationorder.toString() + group1order + smallgrouporder);
//        System.out.println("建議名單:\n" + mapper.writeValueAsString(candidateList));
        Report report = new Report();
        report.setUuid(uuid);
        report.setCandidateList(candidateList);
        report.setTotaldistance(distanceservice.getTotalDistance(report.getCandidateList()));
        return report;
    }
}
