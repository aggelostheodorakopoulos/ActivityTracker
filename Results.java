import java.io.Serializable;

public class Results implements Serializable {
    private double TotalDistance;
    private double ele;
    private double speed;
    private double time;

    private String creator;

    Results(){

    }

    Results(double TotalDistance ,double ele, double speed, double time){
        this.TotalDistance=TotalDistance;
        this.ele=ele;
        this.speed=speed;
        this.time=time;
    }

    Results(double TotalDistance ,double ele, double speed, double time,String creator){
        this.TotalDistance=TotalDistance;
        this.ele=ele;
        this.speed=speed;
        this.time=time;
        this.creator=creator;
    }
    public double getTotalDistance(){
        return TotalDistance;
    }
    public double getEle(){
        return ele;
    }
    public double getSpeed(){
        return speed;
    }
    public double getTime(){
        return time;
    }

    public void setCreator(String creator){
        this.creator=creator;
    }

    public void setEle(double ele) {
        this.ele = ele;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public void setTotalDistance(double totalDistance) {
        TotalDistance = totalDistance;
    }

    public String getCreator(){
        return creator;
    }
}