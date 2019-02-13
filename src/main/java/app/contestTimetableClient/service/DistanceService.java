package app.contestTimetableClient.service;

import app.contestTimetableClient.model.Candidate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DistanceService {

//    http://fecbob.pixnet.net/blog/post/38076871-java-%E5%B7%B2%E7%9F%A5%E5%85%A9%E5%80%8B%E5%9C%B0%E9%BB%9E%E7%B6%93%E7%B7%AF%E5%BA%A6%E7%AE%97%E8%B7%9D%E9%9B%A2%EF%BC%88%E9%9D%9E%E5%B8%B8%E7%B2%BE%E7%A2%BA%EF%BC%89

    private static final double EARTH_RADIUS = 6378.137;
//    private static double rad(double d)
//    {
//        return d * Math.PI / 180.0;
//    }


    //經度longitude, 緯度latitude
    //台灣位於東經120度至122度，北緯22度至25度。
    public double getDistance(double latitude1, double longitude1, double latitude2,
                              double longitude2) {

        // 纬度
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        // 经度
        double lng1 = Math.toRadians(longitude1);
        double lng2 = Math.toRadians(longitude2);
        // 纬度之差
        double a = lat1 - lat2;
        // 经度之差
        double b = lng1 - lng2;
        // 计算两点距离的公式
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
        // 弧长乘地球半径, 返回单位: 千米
        s =  s * EARTH_RADIUS;
        return s*1000;

    }

    public Double getTotalDistance(ArrayList<Candidate> candidatelist){
        AtomicReference<Double> totaldistance = new AtomicReference<>(0.0);

        candidatelist.forEach(candidate -> {
            candidate.getTeams().forEach(team -> {
                totaldistance.updateAndGet(v -> new Double((double) (v + team.getDistance())));
            });
        });
//        System.out.println("總共distance："+totaldistance.get());
        return totaldistance.get();
    }


}
