import java.io.Serializable;
import java.util.ArrayList;
public class Gpx implements Serializable {
    ArrayList<waypoint> GpxList= new ArrayList<>();
    private String creator;

    public void add(waypoint wp){
        GpxList.add(wp);
    }
    public int size(){
        return GpxList.size();
    }
    public waypoint get(int i){
        return GpxList.get(i);


    }

    public ArrayList<waypoint> GetWaypointList(){
        return this.GpxList;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }



    public void printGpx(){
        System.out.println("Size of Gpx = " + GpxList.size());
        for (int i = 0; i <GpxList.size();i++)
        {
            System.out.println("Creator "+ this.getCreator() + " Lat "+ GpxList.get(i).getlat() + " Lon " + GpxList.get(i).getlon() + " ele " + GpxList.get(i).getele() + " time " + GpxList.get(i).gettime()    );
        }

    }

}