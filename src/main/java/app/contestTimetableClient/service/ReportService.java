package app.contestTimetableClient.service;


import app.contestTimetableClient.model.Report;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class ReportService {

    @Autowired
    ScoresService distanceservice;


    public Boolean isUuidExist(String url, String uuid) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate resttemplate = new RestTemplate();
        String target = String.format("%s/%s", url, uuid);
//        System.out.println(target);
        ResponseEntity<String> result = resttemplate.getForEntity(target, String.class);
        JsonNode node = mapper.readTree(result.getBody());

//        System.out.println(result.getBody());

        if (node.get("uuid").isNull()) {
            return Boolean.FALSE;

        } else {
            return Boolean.TRUE;
        }
    }

    public void insertData(String url, Report report) {
        RestTemplate resttemplate = new RestTemplate();

        String target = String.format("%s/%s", url, report.getUuid());
//        System.out.println(target);
//        report.setTotaldistance(distanceservice.getTotalDistance(report.getCandidateList()));
//        System.out.println(target);
//        HttpEntity<Report> request = new HttpEntity<>(report);
//        ResponseEntity<Report> response = resttemplate
//                .exchange(url, HttpMethod.POST, request, Report.class);

        resttemplate.postForObject(target, report, String.class);

    }

}
