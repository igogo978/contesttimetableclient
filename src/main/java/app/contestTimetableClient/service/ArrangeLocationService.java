package app.contestTimetableClient.service;

import app.contestTimetableClient.model.Candidate;
import app.contestTimetableClient.model.School;
import app.contestTimetableClient.model.Team;
import app.contestTimetableClient.repository.SchoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ArrangeLocationService {

    @Autowired
    SchoolRepository schoolrepository;

    @Autowired
    DistanceService distanceservice;

    //是否已排定試場
    public ArrayList<Candidate> hasTicket(ArrayList<Candidate> candidatelist, Team team) {
        for (Candidate candidate : candidatelist) {
//            System.out.println(String.format("%s,%s",candidate.getLocation().getSchoolid(), team.getSchoolid()));
            if (candidate.getLocation().getSchoolid().equals(team.getTicket())) {
//                System.out.println(team.getSchoolid());
                team.setDistance(0);
                candidate.getTeams().add(team);
                Integer capacity = candidate.getLocation().getCapacity() - team.getMembers();
                candidate.getLocation().setCapacity(capacity);
            }
        }

        return candidatelist;
    }

//    public Boolean hasTicket(ArrayList<Candidate> candidatelist, Team team) {
//        List<Candidate> possibleCandidatelist = new ArrayList<>();
//        Candidate candidate = candidatelist.stream().filter((c) -> c.getLocation().getCapacity() >= team.getMembers())
//                .findAny()
//                .orElse(null);
////        System.out.println(candidate.getLocation().getSchoolid());
//        if (candidate == null) {
//            return Boolean.FALSE;
//        }
//        return Boolean.TRUE;
//    }

    //尋找最短試場
    public String shortestDistanceLocation(ArrayList<Candidate> candidatelist, Team team) {
        String pos1 = schoolrepository.findBySchoolid(team.getSchoolid()).getPosition();
        Double latitude1 = Double.valueOf(pos1.split(",")[0]);
        Double longitude1 = Double.valueOf(pos1.split(",")[1]);
        String locationid = candidatelist.get(0).getLocation().getSchoolid();
        double distance = 9999999;

        for (Candidate candidate : candidatelist) {
            School location = schoolrepository.findBySchoolid(candidate.getLocation().getSchoolid());
            Double latitude2 = Double.valueOf(location.getPosition().split(",")[0]);
            Double longitude2 = Double.valueOf(location.getPosition().split(",")[1]);

            //可容納人數
            if (candidate.getLocation().getCapacity() >= team.getMembers()) {
                //找到最接近場地
                if (distanceservice.getDistance(latitude1, longitude1, latitude2, longitude2) < distance) {
                    locationid = candidate.getLocation().getSchoolid();
                    distance = distanceservice.getDistance(latitude1, longitude1, latitude2, longitude2);
                }
//                System.out.println(String.format("%s,%s,差距:%s公尺", candidate.getLocation().getName(), team.getName(), distanceservice.getDistance(latitude1, longitude1, latitude2, longitude2)));

            }

        }
//        System.out.println("最短距離：" + distance);
        return locationid;
    }

    public Boolean isHomeLocation(ArrayList<Candidate> candidatelist, Team team) {
        //承辦學校優先安排在自校
        String schoolid = null;
        Candidate candidate = candidatelist.stream().filter((c) -> c.getLocation().getSchoolid().equals(team.getSchoolid()))
                .findAny()
                .orElse(null);

        if (candidate == null) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    public ArrayList<Candidate> setHomeLocation(ArrayList<Candidate> candidatelist, Team team) {
        for (Candidate candidate : candidatelist) {
            if (candidate.getLocation().getSchoolid().equals(team.getSchoolid())) {
                team.setDistance(0);
                candidate.getTeams().add(team);
                Integer capacity = candidate.getLocation().getCapacity() - team.getMembers();
                candidate.getLocation().setCapacity(capacity);
            }
        }

        return candidatelist;
    }


    public ArrayList<Candidate> findLocation(ArrayList<Candidate> candidatelist, Team team, Double distance) {


//        System.out.println(String.format("參賽隊：%s", schoolrepository.findBySchoolid(team.getSchoolid()).getSchoolname()));
        String pos1 = schoolrepository.findBySchoolid(team.getSchoolid()).getPosition();
        Double latitude1 = Double.valueOf(pos1.split(",")[0]);
        Double longitude1 = Double.valueOf(pos1.split(",")[1]);


        String ticket;
        ArrayList<Candidate> neighborCandidates = new ArrayList<>();

        //經度longitude, 緯度latitude
        //台灣位於東經120度至122度，北緯22度至25度。
        //市立東寶國小
        //24.2151761,120.67966260000003
        //市立東山高中
        //24.1663402,120.7110263
        //找到合理的試場距離
        for (Candidate candidate : candidatelist) {

            //場地的經緯
            School location = schoolrepository.findBySchoolid(candidate.getLocation().getSchoolid());
//            location.getPosition()
//            System.out.println(location.getSchoolname());
            Double latitude2 = Double.valueOf(location.getPosition().split(",")[0]);
            Double longitude2 = Double.valueOf(location.getPosition().split(",")[1]);

            //尋找就近場地
            if (distanceservice.getDistance(latitude1, longitude1, latitude2, longitude2) <= distance) {

                //要能容納隊伍人數
                if (candidate.getLocation().getCapacity() >= team.getMembers()) {
                    neighborCandidates.add(candidate);

                }
            }

        }

        if (neighborCandidates.size() != 0) {
//            System.out.println(neighborCandidates.get(2).getLocation().getSchoolid());
            Integer i = new Random().nextInt(neighborCandidates.size());
//            決定ticket
//            System.out.println(neighborCandidates.get(i).getLocation().getSchoolid());
            ticket = neighborCandidates.get(i).getLocation().getSchoolid();
            School location = schoolrepository.findBySchoolid(ticket);
            Double latitude2 = Double.valueOf(location.getPosition().split(",")[0]);
            Double longitude2 = Double.valueOf(location.getPosition().split(",")[1]);
            team.setDistance(distanceservice.getDistance(latitude1, longitude1, latitude2, longitude2));


            candidatelist.forEach(candidate -> {
                if (candidate.getLocation().getSchoolid().equals(ticket)) {

//                    System.out.println("找到理想場地:" + ticket);
                    candidate.getTeams().add(team);
                    Integer capacity = candidate.getLocation().getCapacity() - team.getMembers();
                    candidate.getLocation().setCapacity(capacity);
                }
            });
        } else {
            //沒有理想距離內的試場, 從全部找一個最近的
            String locationid = shortestDistanceLocation(candidatelist, team);

            School location = schoolrepository.findBySchoolid(locationid);
            Double latitude2 = Double.valueOf(location.getPosition().split(",")[0]);
            Double longitude2 = Double.valueOf(location.getPosition().split(",")[1]);
            team.setDistance(distanceservice.getDistance(latitude1, longitude1, latitude2, longitude2));


            for (Candidate candidate : candidatelist) {
                if (candidate.getLocation().getSchoolid().equals(locationid) && candidate.getLocation().getCapacity() >= team.getMembers()) {
                    candidate.getTeams().add(team);
                    Integer capacity = candidate.getLocation().getCapacity() - team.getMembers();
                    candidate.getLocation().setCapacity(capacity);
                } else {
                    //完全沒場地可放
                    if (candidate.getLocation().getSchoolid().equals("999999")) {
                        candidate.getTeams().add(team);
                    }
                }
            }

//            System.out.println("找到最近地:" + locationid);

        }


        return candidatelist;
    }

}
