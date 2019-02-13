package app.contestTimetableClient;

import app.contestTimetableClient.model.*;
import app.contestTimetableClient.repository.LocationRepository;
import app.contestTimetableClient.repository.SchoolRepository;
import app.contestTimetableClient.repository.TeamRepository;
import app.contestTimetableClient.service.ArrangeLocationService;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    ArrangeLocationService arrangeLocationService;

    @Autowired
    ReportService reportservice;

    @Autowired
    InitService initservice;


    public static void main(String[] args) {


        ConfigurableApplicationContext ctx = SpringApplication.run(ContestTimetableClientApplication.class, args);
        ctx.close();
    }


    @Override
    public void run(String... args) throws Exception {
        //單位公尺 10000 = 10公里
        Double acceptDistance = 10000.00;

        RestTemplate resttemplate = new RestTemplate();
        JsonNode node = null;
        ResponseEntity<String> response = null;
        ObjectMapper mapper = new ObjectMapper();
        String url = null;
        Integer jobpriority = null;
        String cwd = System.getProperty("user.dir");
        String target;
        Integer rounds = 0;

        //確認設定檔
        if (new File(String.format("%s/%s", cwd, configfile)).isFile()) {
            //create ObjectMapper instance

            //jobpriority 為場次順序 例如第一天上半場為1, 第一天下半場為2
            node = mapper.readTree(new File(String.format("%s/%s", cwd, configfile)));
            url = node.get("url").asText();
            jobpriority = node.get("jobpriority").asInt();
            rounds = node.get("rounds").asInt();
        } else {
            System.out.println("無設定檔");
            System.exit(0);
        }

        //取得所有學校
        initservice.initSchool(url, "school");

        //取得場地容納人數
        initservice.initLocation(url, "location");

        //取得參賽隊伍
        initservice.initTeam(url, String.format("job/%s/%s", jobpriority, "schoolteam"));

        //是否取得門票
        initservice.initTicket(url, "ticket");

        //請求工作, 要算幾輪
        for (int round = 0; round < rounds; round++) {
            target = String.format("%s/%s", url, String.format("job/%s", jobpriority));

            response = resttemplate.getForEntity(target, String.class);
            node = mapper.readTree(response.getBody());
            String jobid = node.get("jobid").asText();

            if (jobid.length() != 0) {
                String locationorder = node.get("locationorder").asText();
                String group1order = node.get("group1order").asText();
                String group2order = node.get("group2order").asText();

                Long calculatejobs = node.get("calculatejobs").asLong();

                List<String> locations = Arrays.asList(locationorder.split("-"));
                List<String> group1 = Arrays.asList(group1order.split("-"));
                List<String> group2 = Arrays.asList(group2order.split("-"));
                //locationorder, group1order 為固定排序
                //每一個jobid 會指派計算次數
                for (int job = 0; job < calculatejobs; job++) {
                    //建立候選名單 - 空的 例如:場地一可容納team1, team2, team3
//                    System.out.println(String.format("caltulate times:%s-%s", round, job));
                    Report report = doJob(locations, group1, group2, acceptDistance);
                    //
//                    //檢查uuid 是否已存在
                    target = String.format("%s/job/%s/report", url, jobpriority);

                    if (!reportservice.isUuidExist(target, report.getUuid())) {
//                        //若無則寫入此次記錄
                        System.out.println("寫入記錄");
//                        reportservice.insertData(target, uuid, candidateList);
                    }
                }
            } else {
                round--;
            }

        }


    }

    private Report doJob(List<String> locations, List<String> group1, List<String> group2, Double acceptDistance) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Candidate> candidateList = new ArrayList<>();

        for (String location : locations) {
            Candidate candidate = new Candidate();
            ArrayList<Team> teams = new ArrayList<>();
            candidate.setLocation(locationrepository.findBySchoolid(location));
            candidate.setTeams(teams);
            candidateList.add(candidate);
        }

        List<Team> teams = new ArrayList<>();
        group1.forEach(schoolid -> {
            teams.add(teamrepository.findBySchoolid(schoolid));
        });

        StringBuilder locationorder = new StringBuilder();
        locations.forEach(location -> locationorder.append(location + "-"));
        StringBuilder group1order = new StringBuilder();
        group1.forEach(schoolid -> group1order.append(schoolid + "-"));

        //打亂取群组2的顺序
        Collections.shuffle(group2);
        StringBuilder smallgrouporder = new StringBuilder();
        group2.forEach(schoolid -> {
            teams.add(teamrepository.findBySchoolid(schoolid));
            smallgrouporder.append(schoolid + "-");
        });

        //開始安排場地
        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getTicket().length() == 6) {
//                System.out.println(teams.get(i).getName());
                //已有門票者
                candidateList = arrangeLocationService.hasTicket(candidateList, teams.get(i));
            } else if (arrangeLocationService.isHomeLocation(candidateList, teams.get(i))) {
                //主場隊伍
                candidateList = arrangeLocationService.setHomeLocation(candidateList, teams.get(i));
            } else {
                //接受的距離
                candidateList = arrangeLocationService.arrangeLocation(candidateList, teams.get(i), acceptDistance);
            }
//            System.out.println(uuid);
//            System.out.println(mapper.writeValueAsString(candidateList));


        }
        String uuid = org.apache.commons.codec.digest.DigestUtils.sha256Hex(locationorder.toString() + group1order + smallgrouporder);
        System.out.println("建議名單:\n" + mapper.writeValueAsString(candidateList));
        Report report = new Report();
        report.setUuid(uuid);
        report.setCandidateList(candidateList);
        return report;
    }
}
