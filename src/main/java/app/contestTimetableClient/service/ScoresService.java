package app.contestTimetableClient.service;

import app.contestTimetableClient.model.Candidate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ScoresService {

//    http://fecbob.pixnet.net/blog/post/38076871-java-%E5%B7%B2%E7%9F%A5%E5%85%A9%E5%80%8B%E5%9C%B0%E9%BB%9E%E7%B6%93%E7%B7%AF%E5%BA%A6%E7%AE%97%E8%B7%9D%E9%9B%A2%EF%BC%88%E9%9D%9E%E5%B8%B8%E7%B2%BE%E7%A2%BA%EF%BC%89

    private static final double EARTH_RADIUS = 6378.137;



    public Double getTotalScores(List<Candidate> candidatelist, HashMap<Integer, Integer> weightsSettings) {
        AtomicReference<Double> totalscores = new AtomicReference<>(0.0);

        candidatelist.forEach(candidate -> {
            candidate.getTeams().forEach(team -> {
                weightsSettings.forEach((scores, weights) -> {
                    if (team.getScores() == Double.valueOf(scores)) {
//                        System.out.println("需加權:" + scores + ":" + weights);
                        totalscores.updateAndGet(v -> new Double((double) (v + (team.getMembers() * team.getScores() * weights))));
                    } else {
                        totalscores.updateAndGet(v -> new Double((double) (v + (team.getMembers() * team.getScores()))));

                    }

                });


            });
        });
//        System.out.println("總共distance："+totaldistance.get());
        return totalscores.get();
    }

    public String getScoresFrequency(List<Candidate> candidatelist) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String scoresfrequency = "";
        HashMap<Integer, Integer> hashMap = new HashMap<>();


        candidatelist.forEach(candidate -> {
            candidate.getTeams().forEach(team -> {
                hashMap.computeIfPresent((int) Math.round(team.getScores()), (k, v) -> v + 1);
                //  computeIfPresent 要先判斷, 不可對調,  如果沒有, 則起始值為1
                hashMap.computeIfAbsent((int) Math.round(team.getScores()), v -> v = 1);
            });
        });
//        System.out.println("scores frequency:"+mapper.writeValueAsString(hashMap));
        return mapper.writeValueAsString(hashMap);
    }


    public Double getTotalDistance(List<Candidate> candidatelist) {
        AtomicReference<Double> totaldistance = new AtomicReference<>(0.0);

        candidatelist.forEach(candidate -> {
            candidate.getTeams().forEach(team -> {
                totaldistance.updateAndGet(v -> new Double((double) (v + team.getScores())));
            });
        });
//        System.out.println("總共distance："+totaldistance.get());
        return totaldistance.get();
    }


}
