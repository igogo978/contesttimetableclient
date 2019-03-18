package app.contestTimetableClient;

import app.contestTimetableClient.repository.TeamRepository;
import app.contestTimetableClient.service.InitService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ContestTimetableClientApplicationTests {

    @Value("${config}")
    private String configfile;

    @Autowired
    InitService initservice;

    @Autowired
    TeamRepository teamrepository;

    @Test
    public void contextLoads() throws IOException {
        //單位公尺 10000 = 10公里
        Double acceptDistance = 10000.00;

        RestTemplate resttemplate = new RestTemplate();
        JsonNode node = null;
        ResponseEntity<String> response = null;
        ObjectMapper mapper = new ObjectMapper();
        String url = null;

        String cwd = System.getProperty("user.dir");
        String target;

        //計算每多少筆後,取ranking值為排前 寫入server 端
        Integer split = 2000;
        Integer ranking = 1;

        //jobpriority 為場次順序 例如第一天上半場為1, 第一天下半場為2
        node = mapper.readTree(new File(String.format("%s/%s", cwd, configfile)));
        url = node.get("url").asText();
        split = node.get("split").asInt();
        ranking = node.get("ranking").asInt();

        //        //取得參賽隊伍
        initservice.initTeam(url, "schoolteam");

        System.out.println(teamrepository.findBySchoolid("061313").getName());
        teamrepository.findBySchoolid("061313").getContestids().forEach(contestid -> {
            System.out.println(String.format("%s,%s", contestid.getContestid(), contestid.getMembers()));
        });
    }

}
