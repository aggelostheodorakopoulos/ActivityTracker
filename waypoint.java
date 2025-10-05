import java.io.Serializable;

public class waypoint implements Serializable {
    private double lat;
    private double lon;
    private double ele;
    private String time;
    private String user;

    waypoint(double lat, double lon , double ele,String time,String user){
        this.user=user;
        this.lat= lat;
        this.lon= lon;
        this.ele= ele;
        this.time= time;
    }
    public double getlat(){
        return lat;
    }
    public double getlon(){
        return lon;
    }
    public double getele(){
        return ele;
    }
    public String gettime(){
        return time;
    }

    public String GetUser(){
        return user;
    }
}

