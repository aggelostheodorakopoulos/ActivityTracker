import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


//Ουσιαστικά αυτή η κλάση παίρνει τις διαδρομές για όλους τους users και κρατάει τα συνολικά στατιστικά αποτελέσματα
//Με την προσθήκη νέας διαδρομής την καλούμε στα Thread του Master με τα αντίστοιχα sychronization και γράφουμε τα στατιστικά.
public class Statistics implements Serializable {
    private Map<String,Results> StatisticsForUsers;

    Map<String,ArrayList<Map<Integer,Float>>> UnhadledS;

    Statistics(){

    }

    Statistics(Map<String,ArrayList<Map<Integer,Float>>>StatisticsForUsers){
        this.UnhadledS=StatisticsForUsers;
    }

    public void setStats(Map<String, ArrayList<Map<Integer,Float>>> Map){
        this.StatisticsForUsers=new HashMap<>();


        for (Map.Entry<String,ArrayList<Map<Integer,Float>>> entry:Map.entrySet()){
            float av_time=0.0f;
            float av_distance=0.0f;
            float av_ele=0.0f;
            float av_speed=0.0f;
            String key=entry.getKey();
            ArrayList<Map<Integer,Float>> stat=entry.getValue();
            for(int i=0;i<stat.size();i++){
                av_time+=stat.get(i).get(3);
                av_distance+=stat.get(i).get(0);
                av_ele+=stat.get(i).get(1);
                av_speed+=stat.get(i).get(2);
            }
            Results r=new Results();
            r.setEle(av_ele/stat.size());
            r.setTime(av_time/stat.size());
            r.setTotalDistance(av_distance/stat.size());
            r.setSpeed(av_speed/stat.size());
            r.setCreator(key);
            this.StatisticsForUsers.put(key,r);
        }

    }

    public Map<String, Results> getStats(){
        return this.StatisticsForUsers;
    }
}
