package app.contestTimetableClient.service;

import app.contestTimetableClient.model.*;
import app.contestTimetableClient.model.scores.Areascore;
import app.contestTimetableClient.repository.AreascoreRepository;
import app.contestTimetableClient.repository.SchoolRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    SchoolRepository schoolrepository;

    @Autowired
    ScoresService distanceservice;

    @Autowired
    AreascoreRepository areascoreRepository;

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
        Integer teamMembersid1 = null;
        Integer teamMembersid2 = null;
        Integer teamMembersid3 = null;
        Integer teamMembersid4 = null;
        for (Contestid contestid : team.getContestids()) {
            if (contestid.getContestid() == 1) {
                teamMembersid1 = contestid.getMembers();
            }
            if (contestid.getContestid() == 2) {
                teamMembersid2 = contestid.getMembers();
            }
            if (contestid.getContestid() == 3) {
                teamMembersid3 = contestid.getMembers();
            }
            if (contestid.getContestid() == 4) {
                teamMembersid4 = contestid.getMembers();
            }
        }

        for (Candidate candidate : candidatelist) {
            Integer locationid1 = null;
            Integer locationid2 = null;
            Integer locationid3 = null;
            Integer locationid4 = null;
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


            if (locationid1 >= teamMembersid1 && locationid2 >= teamMembersid2 && locationid3 >= teamMembersid3 && locationid4 >= teamMembersid4) {

                capableLocations.add(candidate.getLocation().getSchoolid());
            }
        }
        return capableLocations;
    }


    public Candidate addPendingList(Candidate pending, Team team) {
        pending.getTeams().add(team);
        return pending;
    }


    //列入優先排學校,例如承辦比賽學校
    public List<Candidate> addHomeTeam(List<Candidate> candidatelist, Team team) throws JsonProcessingException {

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


        //主場
        for (Candidate candidate : candidatelist) {
            if (candidate.getLocation().getSchoolid().equals(team.getSchoolid())) {
//                System.out.println("主場學校:"+team.getName());

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
                team.setScores(0.0);
                candidate.getTeams().add(team);
            }
        }


//        System.out.println("加入參賽隊伍:"+mapper.writeValueAsString(candidatelist));
        return candidatelist;
    }

    public List<Candidate> addTicketTeam(List<Candidate> candidatelist, Ticket ticket, Team team) throws JsonProcessingException {
        //擁有門票者
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
            if (candidate.getLocation().getSchoolid().equals(ticket.getLocationid())) {
//                System.out.println("location:" + team.getName() + "-" + candidate.getLocation().getName());
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
                team.setScores(0.0);
                candidate.getTeams().add(team);
            }
        }

        return candidatelist;
    }


    public List<String> findPriorityScoresLocations(List<String> capableLocations, Team team, Double priorityScores) {
        List<String> priorityLocations = new ArrayList<>();
        List<String> priorityAreas = new ArrayList<>();

        //("(?<=區)") regex look behind
        String startarea = team.getName().split("(?<=區)")[0];
        areascoreRepository.findByStartareaAndScoresEquals(startarea, priorityScores).forEach(area -> {
            priorityAreas.add(area.getEndarea());
        });

        Optional<String> priorityLocation = capableLocations.stream()
                .filter(location -> priorityAreas.contains(schoolrepository.findBySchoolid(location).getSchoolname().split("(?<=區)")[0]))
                .findFirst();
        if (priorityLocation.isPresent()) {
            priorityLocations.add(priorityLocation.get());
        }

        return priorityLocations;
    }


    public String isPriorityAreaLocation(List<String> capableLocations, List<PriorityArea> priorityAreas, Team schoolteam) {

        String startarea = schoolteam.getName().split("(?<=區)")[0];
        PriorityArea priorityArea = priorityAreas.stream().filter(priorityArea1 -> priorityArea1.getAreas().stream().anyMatch(area -> area.equals(startarea)))
                .findFirst()
                .orElse(null);
        if (priorityArea != null) {
            String location = capableLocations.stream().filter(capableLocation -> schoolrepository.findBySchoolid(capableLocation).getSchoolname().split("(?<=區)")[0].equals(priorityArea.getLocation()))
                    .findFirst()
                    .orElse(null);

            return location;

        }

        return null;
    }

    public Areascore findPriorityAreaLocation(List<String> capableLocations, String location, Team schoolteam) {
        Areascore areascore = new Areascore();
        String startarea = schoolteam.getName().split("(?<=區)")[0];
        String endarea = schoolrepository.findBySchoolid(location).getSchoolname().split("(?<=區)")[0];
        String id = String.format("%s%s", startarea, endarea);

        return areascoreRepository.findById(id).get();
    }


    public List<Candidate> addTeam(List<Candidate> candidatelist, Team team, Areascore area) {
        team.setScores(area.getScores());

        //members
        Integer teamMembersid1 = null;
        Integer teamMembersid2 = null;
        Integer teamMembersid3 = null;
        Integer teamMembersid4 = null;


        for (Contestid contestid : team.getContestids()) {
            if (contestid.getContestid() == 1) {
                teamMembersid1 = contestid.getMembers();
            }
            if (contestid.getContestid() == 2) {
                teamMembersid2 = contestid.getMembers();
            }
            if (contestid.getContestid() == 3) {
                teamMembersid3 = contestid.getMembers();
            }
            if (contestid.getContestid() == 4) {
                teamMembersid4 = contestid.getMembers();
            }
        }

//        teamMembersid1 = team.getContestids().stream().filter(contest -> contest.getContestid() == 1).findFirst().get().getMembers();
//        teamMembersid2 = team.getContestids().stream().filter(contest -> contest.getContestid() == 2).findFirst().get().getMembers();
//        teamMembersid3 = team.getContestids().stream().filter(contest -> contest.getContestid() == 3).findFirst().get().getMembers();
//        teamMembersid4 = team.getContestids().stream().filter(contest -> contest.getContestid() == 4).findFirst().get().getMembers();
//        Optional<Contestid> contestid = team.getContestids().stream().filter(contest -> contest.getContestid() == 1).findFirst();
//        System.out.println("id1: " + id1.get().getContestid() + ":" + id1.get().getMembers() + "-" + teamid1);

        for (Candidate candidate : candidatelist) {
            if (candidate.getLocation().getSchoolid().equals(area.getEndarea())) {
                for (Contestid contestid : candidate.getLocation().getContestids()) {
                    if (contestid.getContestid() == 1) {
                        contestid.setMembers(contestid.getMembers() - teamMembersid1);
                    }
                    if (contestid.getContestid() == 2) {
                        contestid.setMembers(contestid.getMembers() - teamMembersid2);

                    }
                    if (contestid.getContestid() == 3) {
                        contestid.setMembers(contestid.getMembers() - teamMembersid3);
                    }
                    if (contestid.getContestid() == 4) {
                        contestid.setMembers(contestid.getMembers() - teamMembersid4);
                    }

                }
                candidate.getTeams().add(team);

            }
        }

        return candidatelist;
    }

    public List<Candidate> addTeamByArea(List<Candidate> candidatelist, Team team, String locationid) {
        //得分
        String starterea = team.getName().split("(?<=區)")[0];
        String endarea = schoolrepository.findBySchoolid(locationid).getSchoolname().split("(?<=區)")[0];
        String id = String.format("%s%s", starterea, endarea);
        Areascore area = areascoreRepository.findById(id).get();
        team.setScores(area.getScores());

        //members
        Integer teamMembersid1 = null;
        Integer teamMembersid2 = null;
        Integer teamMembersid3 = null;
        Integer teamMembersid4 = null;

        for (Contestid contestid : team.getContestids()) {
            if (contestid.getContestid() == 1) {
                teamMembersid1 = contestid.getMembers();
            }
            if (contestid.getContestid() == 2) {
                teamMembersid2 = contestid.getMembers();
            }
            if (contestid.getContestid() == 3) {
                teamMembersid3 = contestid.getMembers();
            }
            if (contestid.getContestid() == 4) {
                teamMembersid4 = contestid.getMembers();
            }
        }

        for (Candidate candidate : candidatelist) {
            if (candidate.getLocation().getSchoolid().equals(locationid)) {

                for (Contestid contestid : candidate.getLocation().getContestids()) {
                    if (contestid.getContestid() == 1) {
                        contestid.setMembers(contestid.getMembers() - teamMembersid1);
                    }
                    if (contestid.getContestid() == 2) {
                        contestid.setMembers(contestid.getMembers() - teamMembersid2);

                    }
                    if (contestid.getContestid() == 3) {
                        contestid.setMembers(contestid.getMembers() - teamMembersid3);
                    }
                    if (contestid.getContestid() == 4) {
                        contestid.setMembers(contestid.getMembers() - teamMembersid4);
                    }

                }
                candidate.getTeams().add(team);

            }
        }
        return candidatelist;
    }


    public Areascore findACommonLocationByArea(List<String> capableLocations, Team team) {
        //startarea, endarea回傳是schoolid
        Areascore area = new Areascore();
        area.setStartarea(team.getSchoolid());
        area.setScores(999999.00);
        String startarea = team.getName().split("(?<=區)")[0];
        for (String location : capableLocations) {

            String endarea = schoolrepository.findBySchoolid(location).getSchoolname().split("(?<=區)")[0];

            String id = String.format("%s%s", startarea, endarea);
            Areascore tmp = areascoreRepository.findById(id).get();

            if (tmp.getScores() < area.getScores()) {
                area.setEndarea(location);
                area.setScores(tmp.getScores());
            }
        }

        return area;
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


}
