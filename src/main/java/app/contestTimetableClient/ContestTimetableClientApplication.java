package app.contestTimetableClient;

import app.contestTimetableClient.model.*;
import app.contestTimetableClient.model.scores.Areascore;
import app.contestTimetableClient.repository.AreascoreRepository;
import app.contestTimetableClient.repository.LocationRepository;
import app.contestTimetableClient.repository.SchoolRepository;
import app.contestTimetableClient.repository.TeamRepository;
import app.contestTimetableClient.service.InitService;
import app.contestTimetableClient.service.LocationService;
import app.contestTimetableClient.service.ReportService;
import app.contestTimetableClient.service.ScoresService;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class ContestTimetableClientApplication implements CommandLineRunner {

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
    @Value("${config}")
    private String configfile;

    public static void main(String[] args) {

        ConfigurableApplicationContext ctx = SpringApplication.run(ContestTimetableClientApplication.class, args);
        ctx.close();
    }


    @Override
    public void run(String... args) throws Exception {

        RestTemplate resttemplate = new RestTemplate();
        JsonNode node = null;
        ResponseEntity<String> response = null;
        ObjectMapper mapper = new ObjectMapper();
        String url = null;
        List<PriorityArea> priorityAreas = new ArrayList<>();

        HashMap<Integer, Integer> weightsSettings = new HashMap<>();
        Map<String, Double> priorityScores = new HashMap<>();

        String cwd = System.getProperty("user.dir");
        String target;

        //計算每多少筆後,取ranking值為前面筆數者, 寫入server 端
        Integer split = 1;
        Integer job = 10;


        //確認設定檔
        if (new File(String.format("%s/%s", cwd, configfile)).isFile()) {
            //create ObjectMapper instance

            node = mapper.readTree(new File(String.format("%s/%s", cwd, configfile)));
            url = node.get("url").asText();
            split = node.get("split").asInt();
            job = node.get("job").asInt();
            JsonNode weightsNode = node.get("weights");
            weightsNode.forEach(weight -> {
                weightsSettings.put(weight.get("scores").asInt(), weight.get("weights").asInt());
            });

            node.get("priorityArea").forEach(subnode -> {

                PriorityArea priorityArea = new PriorityArea();
                priorityArea.setLocation(subnode.get("location").asText());
                List<String> areas = Arrays.stream(subnode.get("area").asText().split(",")).map(String::trim).collect(Collectors.toList());
                priorityArea.getAreas().addAll(areas);

                priorityAreas.add(priorityArea);
            });

            node.get("priorityScores").forEach(subnode -> {
                String group = subnode.get("group").asText();
                Double scores = subnode.get("scores").asDouble();
                priorityScores.put(group, scores);
            });

        } else {
            System.out.println("無設定檔");
            System.exit(0);
        }

        //取得所有學校
        initservice.initSchool(url, "api/school");

        //取得場地容納人數
        initservice.initLocation(url, "api/location");

        //參賽隊
        initservice.initTeam(url, "api/schoolteam");

        //取得有門票隊伍
        List<Ticket> tickets = initservice.initTicket(url, "api/ticket");

        //行政區得分表
        initservice.initScoresArea(url, "api/scores/area", priorityAreas);

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

        target = String.format("%s/%s", url, "api/report/uuid/");

        for (int i = 0; i <= job; i++) {
            ZonedDateTime dateTime = ZonedDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            Report report = doJob(locations, priority, group1, group2, tickets, weightsSettings, priorityAreas, priorityScores);
            reports.add(report);
            if (i % 50 == 0 && i != 0) {
                System.out.println(String.format("目前進度 %s 筆-%s", i, dateTime.format(formatter)));
            }
            if (reports.size() == split) {
                //排序
                reports.sort(Comparator.comparing(Report::getTotalscores));

                //取出第一筆寫入
                report = reports.get(0);
//                System.out.println("建議名單:\n" + mapper.writeValueAsString(report));
                if (!reportservice.isUuidExist(target, report.getUuid())) {
                    //若無則寫入此次記錄
                    System.out.println("寫入一筆記錄:" + dateTime.format(formatter));
                    reportservice.insertData(target, report);
                    reports.clear();
                }

            }
        }

    }


    private Report doJob(List<String> locations, List<String> priority, List<String> group1, List<String> group2, List<Ticket> tickets, HashMap<Integer, Integer> weightsSettings, List<PriorityArea> priorityAreas, Map<String, Double> priorityScores) throws JsonProcessingException, UnsupportedEncodingException {
        Double invalidScores = 999999.00;
        Double neighborScores = 2.0;
        //建立候選場地
        List<Candidate> candidateList = new ArrayList<>();
        Candidate pending = new Candidate();

        //最後無法排入試場者,無條件加進pending, 自己獨立一個candidate
        for (String location : locations) {
            List<Team> teams = new ArrayList<>();

            if (location.equals("999999")) {
                pending.setLocation(locationrepository.findBySchoolid(location));
                pending.setTeams(teams);
            } else {
                Candidate candidate = new Candidate();
                candidate.setLocation(locationrepository.findBySchoolid(location));
                candidate.setTeams(teams);
                candidateList.add(candidate);
            }
        }


        //優先要排的群組,主場或有門票者
        for (String schoolid : priority) {

            //比對優先拿到入場券的隊伍, /api/ticket
            Team team = teamrepository.findBySchoolid(schoolid);
            if (tickets.stream().anyMatch(ticket -> ticket.getSchoolid().equals(schoolid))) {
                Ticket ticket = tickets.stream()
                        .filter(t -> t.getSchoolid().equals(schoolid)).findFirst().get();
                candidateList = locationService.addTicketTeam(candidateList, ticket, team);
            } else {
                candidateList = locationService.addHomeTeam(candidateList, team);
            }

        }


        //大群組
        for (String schoolid : group1) {
            Team schoolteam = teamrepository.findBySchoolid(schoolid);
            neighborScores = priorityScores.get("group1");
            //是否有場地可容納
            if (locationService.isCapable(candidateList, schoolteam)) {
                //找出所有可容納的場地
                List<String> capableLocations = locationService.findCapableLocations(candidateList, schoolteam);

                //優先區域,例如新社區, 和平區, 東勢區先排在石岡區
                String locationid = locationService.isPriorityAreaLocation(capableLocations, priorityAreas, schoolteam);
                if (locationid != null) {
                    candidateList = locationService.addTeamByArea(candidateList, schoolteam, locationid);
                } else {
                    //找出符合優先分數試場
                    List<String> priorityScoresLocations = locationService.findPriorityScoresLocations(capableLocations, schoolteam, neighborScores);
                    if (priorityScoresLocations.size() != 0) {
                        //如果找到理想location,找第一個排入
                        candidateList = locationService.addTeamByArea(candidateList, schoolteam, priorityScoresLocations.get(0));
                    } else {
                        //沒有鄰居, 找一個最近的
                        Areascore area = locationService.findACommonLocationByArea(capableLocations, schoolteam);
                        candidateList = locationService.addTeam(candidateList, schoolteam, area);
                    }
                }

            } else {
                //加到未排入名單
                schoolteam.setScores(invalidScores);
                pending = locationService.addPendingList(pending, schoolteam);
            }
        }


        //小群組
        Collections.shuffle(group2);
        neighborScores = priorityScores.get("group2");
        for (String schoolid : group2) {

            Team schoolteam = teamrepository.findBySchoolid(schoolid);
            //是否有場地可容納
            if (locationService.isCapable(candidateList, schoolteam)) {
                //找出所有可容納的場地
                List<String> capableLocations = locationService.findCapableLocations(candidateList, schoolteam);

                //優先區域,例如新社區, 和平區, 東勢區先排在石岡區
                String location = locationService.isPriorityAreaLocation(capableLocations, priorityAreas, schoolteam);
                if (location != null) {
                    candidateList = locationService.addTeamByArea(candidateList, schoolteam, location);
                } else {
                    //找鄰近分數 2.0
                    List<String> priorityScoresLocations = locationService.findPriorityScoresLocations(capableLocations, schoolteam, neighborScores);
                    if (priorityScoresLocations.size() != 0) {
                        //如果找到理想location,隨机找一個排入
                        candidateList = locationService.addTeamByArea(candidateList, schoolteam, priorityScoresLocations.get(0));
                    } else {
                        //沒有鄰居, 找一個最近的
                        Areascore area = locationService.findACommonLocationByArea(capableLocations, schoolteam);
                        candidateList = locationService.addTeam(candidateList, schoolteam, area);

                    }

                }


            } else {
                //加到 未排入名單
                schoolteam.setScores(invalidScores);
                pending = locationService.addPendingList(pending, schoolteam);
            }
        }

        candidateList.add(pending);

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
        report.setTotalscores(scoresService.getTotalScores(report.getCandidateList(), weightsSettings));
        report.setScoresFrequency(scoresService.getScoresFrequency(report.getCandidateList()));
        return report;
    }
}
