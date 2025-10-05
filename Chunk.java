import java.io.Serializable;
import java.util.ArrayList;

public class Chunk implements Serializable {
    private int NoChunk;
    private ArrayList<waypoint> waypoints;

    public Chunk(int i){
        this.NoChunk=i;
    }

    public Chunk(int NoChunk , ArrayList<waypoint> waypoints){
        this.NoChunk=NoChunk;
        this.waypoints=waypoints;

    }

    public int getChunkNumber(){
        return NoChunk;
    }

    public ArrayList<waypoint> getWaypoints(){
        return waypoints;
    }



    public void setWaypoints(ArrayList<waypoint>way){
        this.waypoints=way;
    }

    public void setNoChunk(int i){
        this.NoChunk=i;
    }


}
