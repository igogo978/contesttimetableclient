package app.contestTimetableClient.service;

import app.contestTimetableClient.model.Candidate;
import app.contestTimetableClient.model.Contestid;
import app.contestTimetableClient.model.School;
import app.contestTimetableClient.model.Team;
import app.contestTimetableClient.repository.SchoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class LocationService {

    @Autowired
    SchoolRepository schoolrepository;

    @Autowired
    DistanceService distanceservice;

    //是否有入場卷
    public Boolean isCapable(List<Candidate> candidatelist, Team team) {
        Integer teamid1 = 0;
        Integer teamid2 = 0;
        Integer teamid3 = 0;
        Integer teamid4 = 0;
        for (Contestid contestid : team.getContestids()) {
            if (contestid.getContestid() == 1) {
                teamid1 = contestid.getMembers();
            }
            if (contestid.getContestid() == 2) {
                teamid2 = contestid.getMembers();
            }
            if (contestid.getContestid() == 3) {
                teamid3 = contestid.getMembers();
            }
            if (contestid.getContestid() == 4) {
                teamid4 = contestid.getMembers();
            }
        }

        for (Candidate candidate : candidatelist) {
            Integer locationid1 = 0;
            Integer locationid2 = 0;
            Integer locationid3 = 0;
            Integer locationid4 = 0;
            for (Contestid contestid : candidate.getLocation().getContestids()) {
                if (contestid.getContestid() == 1) {
                    locationid1 = contestid.getMembers();
                }
                if (contestid.getContestid() == 2) {
                    locationid2 = contestid.getMembers();
                }
                if (contestid.getContestid() == 3) {
                    locationid3 = contestid.getMembers();
                }
                if (contestid.getContestid() == 4) {
                    locationid4 = contestid.getMembers();
                }
            }
//            System.out.println(candidate.getLocation().getName());
//            System.out.println(String.format("%s,%s,%s,%s,", locationid1, locationid2, locationid3, locationid4));
//            System.out.println(team.getName());
//            System.out.println(String.format("%s,%s,%s,%s,", teamid1, teamid2, teamid3, teamid4));
            if (locationid1 >= teamid1 && locationid2 >= teamid2 && locationid3 >= teamid3 && locationid4 >= teamid4) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    //找出可容納的場地
    public List<String> findCapableLocations(List<Candidate> candidatelist, Team team) {
        List<String> capableLocations = new ArrayList<>();
        Integer teamid1 = 0;
        Integer teamid2 = 0;
        Integer teamid3 = 0;
        Integer teamid4 = 0;
        for (Contestid contestid : team.getContestids()) {
            if (contestid.getContestid() == 1) {
                teamid1 = contestid.getMembers();
            }
            if (contestid.getContestid() == 2) {
                teamid2 = contestid.getMembers();
            }
            if (contestid.getContestid() == 3) {
                teamid3 = contestid.getMembers();
            }
            if (contestid.getContestid() == 4) {
                teamid4 = contestid.getMembers();
            }
        }

        for (Candidate candidate : candidatelist) {
            Integer locationid1 = 0;
            Integer locationid2 = 0;
            Integer locationid3 = 0;
            Integer locationid4 = 0;
            for (Contestid contestid : candidate.getLocation().getContestids()) {
                if (contestid.getContestid() == 1) {
                    locationid1 = contestid.getMembers();
                }
                if (contestid.getContestid() == 2) {
                    locationid2 = contestid.getMembers();
                }
                if (contestid.getContestid() == 3) {
                    locationid3 = contestid.getMembers();
                }
                if (contestid.getContestid() == 4) {
                    locationid4 = contestid.getMembers();
                }
            }

            if (locationid1 >= teamid1 && locationid2 >= teamid2 && locationid3 >= teamid3 && locationid4 >= teamid4) {
                capableLocations.add(candidate.getLocation().getSchoolid());
            }
        }
        return capableLocations;
    }


    public Candidate addPendingList(Candidate pending, Team team) {
        pending.getTeams().add(team);
        return pending;
    }

    public List<Candidate> addPriorityTeam(List<Candidate> candidatelist, Team team) {
        //members
        Integer teamid1 = null;
        Integer teamid2 = null;
        Integer teamid3 = null;
        Integer teamid4 = null;
        for (Contestid contestid : team.getContestids()) {
            if (contestid.getContestid() == 1) {
                teamid1 = contestid.getMembers();
            }
            if (contestid.getContestid() == 2) {
                teamid2 = contestid.getMembers();
            }
            if (contestid.getContestid() == 3) {
                teamid3 = contestid.getMembers();
            }
            if (contestid.getContestid() == 4) {
                teamid4 = contestid.getMembers();
            }
        }

        for (Candidate candidate : candidatelist) {
            //找到符合場地
            if (candidate.getLocation().getSchoolid().equals(team.getSchoolid())) {
                team.setDistance(0);
                for (Contestid contestid : candidate.getLocation().getContestids()) {
                    if (contestid.getContestid() == 1) {
                        contestid.setMembers(contestid.getMembers() - teamid1);
                    }
                    if (contestid.getContestid() == 2) {
                        contestid.setMembers(contestid.getMembers() - teamid2);
                    }
                    if (contestid.getContestid() == 3) {
                        contestid.setMembers(contestid.getMembers() - teamid3);
                    }
                    if (contestid.getContestid() == 4) {
                        contestid.setMembers(contestid.getMembers() - teamid4);
                    }

                }
                candidate.getTeams().add(team);
            }
        }
        return candidatelist;
    }

    public List<String> findNeighborLocation(List<String> capableLocations, Team team, Double neighborDistance) {
        String pos1 = schoolrepository.findBySchoolid(team.getSchoolid()).getPosition();
        Double latitude1 = Double.valueOf(pos1.split(",")[0]);
        Double longitude1 = Double.valueOf(pos1.split(",")[1]);
        List<String> neighborLocations = new ArrayList<>();

        capableLocations.forEach(schoolid -> {
            School location = schoolrepository.findBySchoolid(schoolid);
            Double latitude2 = Double.valueOf(location.getPosition().split(",")[0]);
            Double longitude2 = Double.valueOf(location.getPosition().split(",")[1]);
            if (distanceservice.getDistance(latitude1, longitude1, latitude2, longitude2) < neighborDistance) {
                neighborLocations.add(schoolid);
            }
        });
        return neighborLocations;
    }

    public List<Candidate> addNeighborTeam(List<Candidate> candidatelist, Team team, String neighborid) {
        //members
        Integer teamid1 = null;
        Integer teamid2 = null;
        Integer teamid3 = null;
        Integer teamid4 = null;
        for (Contestid contestid : team.getContestids()) {
            if (contestid.getContestid() == 1) {
                teamid1 = contestid.getMembers();
            }
            if (contestid.getContestid() == 2) {
                teamid2 = contestid.getMembers();
            }
            if (contestid.getContestid() == 3) {
                teamid3 = contestid.getMembers();
            }
            if (contestid.getContestid() == 4) {
                teamid4 = contestid.getMembers();
            }
        }

        for (Candidate candidate : candidatelist) {
            if (candidate.getLocation().getSchoolid().equals(neighborid)) {
                String pos1 = schoolrepository.findBySchoolid(team.getSchoolid()).getPosition();
                Double latitude1 = Double.valueOf(pos1.split(",")[0]);
                Double longitude1 = Double.valueOf(pos1.split(",")[1]);

                School location = schoolrepository.findBySchoolid(neighborid);
                Double latitude2 = Double.valueOf(location.getPosition().split(",")[0]);
                Double longitude2 = Double.valueOf(location.getPosition().split(",")[1]);
                Double distance = distanceservice.getDistance(latitude1, longitude1, latitude2, longitude2);
                team.setDistance(distance);
                for (Contestid contestid : candidate.getLocation().getContestids()) {
                    if (contestid.getContestid() == 1) {
                        contestid.setMembers(contestid.getMembers() - teamid1);
                    }
                    if (contestid.getContestid() == 2) {
                        contestid.setMembers(contestid.getMembers() - teamid2);
                    }
                    if (contestid.getContestid() == 3) {
                        contestid.setMembers(contestid.getMembers() - teamid3);
                    }
                    if (contestid.getContestid() == 4) {
                        contestid.setMembers(contestid.getMembers() - teamid4);
                    }

                }
                candidate.getTeams().add(team);

            }
        }
        return candidatelist;
    }

    //尋找最短試場, 回傳locationid, 如果沒有可容納場地, 回傳locationid:999999
    public String shortestDistanceLocation(List<Candidate> candidatelist, Team team) {
        String pos1 = schoolrepository.findBySchoolid(team.getSchoolid()).getPosition();
        Double latitude1 = Double.valueOf(pos1.split(",")[0]);
        Double longitude1 = Double.valueOf(pos1.split(",")[1]);
        String locationid = "";
        double distance = 99999999;


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

        //找不到容納場地
        if (locationid.length() == 0) {
            //完全沒場地可放
//            System.out.println("找不到場地:" + team.getSchoolid());
            locationid = "999999";
        }
        return locationid;
    }

    public Boolean isHomeLocation(List<Candidate> candidatelist, Team team) {
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

    public List<Candidate> setHomeLocation(List<Candidate> candidatelist, Team team) {
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


    public List<Candidate> findLocation(List<Candidate> candidatelist, Team team, Double nearDistance) {

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
            Double latitude2 = Double.valueOf(location.getPosition().split(",")[0]);
            Double longitude2 = Double.valueOf(location.getPosition().split(",")[1]);

            //尋找就近場地, 且能容納隊伍人數
            if (distanceservice.getDistance(latitude1, longitude1, latitude2, longitude2) <= nearDistance && candidate.getLocation().getCapacity() >= team.getMembers()) {
                neighborCandidates.add(candidate);
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

//                    System.out.println(String.format("%s找到理想場地%s: %s", team.getName(), candidate.getLocation().getName(), team.getMembers()));
                    candidate.getTeams().add(team);
                    Integer capacity = candidate.getLocation().getCapacity() - team.getMembers();
                    candidate.getLocation().setCapacity(capacity);
                }
            });
        } else {
            //沒有理想距離內的試場, 從全部找一個最近的
            String locationid = shortestDistanceLocation(candidatelist, team);
//            System.out.println("沒有理想場地,尋找最近的location:" + locationid);
//            System.out.println(String.format("schoolid:%s,人數:%s", team.getSchoolid(), team.getMembers()));

            School location = schoolrepository.findBySchoolid(locationid);
            Double latitude2 = Double.valueOf(location.getPosition().split(",")[0]);
            Double longitude2 = Double.valueOf(location.getPosition().split(",")[1]);
            team.setDistance(distanceservice.getDistance(latitude1, longitude1, latitude2, longitude2));

            if (team.getSchoolid().equals("999999")) {
                System.out.println("排不到場地");
                team.setDistance(500000);
            }


            //更新場地容納人數
            for (Candidate candidate : candidatelist) {
                if (candidate.getLocation().getSchoolid().equals(locationid)) {
                    candidate.getTeams().add(team);
                    System.out.println(String.format("%s找到可安排場地%s: %s", team.getName(), candidate.getLocation().getName(), team.getMembers()));

                    Integer capacity = candidate.getLocation().getCapacity() - team.getMembers();
                    candidate.getLocation().setCapacity(capacity);
                }
            }

//            System.out.println("找到最近地:" + locationid);

        }


        return candidatelist;
    }

}
