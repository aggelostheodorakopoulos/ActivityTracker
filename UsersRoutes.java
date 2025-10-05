import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;


//Αυτή η κλάση χρησιμοποιείται απο τον Μaster για να κρατάει τις διαδρομές των κάθε χρηστών
//Κρατάμε ένα Map , που σαν key έχει το όνομα του user και σαν value , έχει ένα ArrayList απο Maps των διαδρομώνσ
public class UsersRoutes implements Serializable {
    public Map<String, ArrayList<Map<Integer,Float>>> UsersRoutes;

    UsersRoutes(Map<String, ArrayList<Map<Integer,Float>>> UsersRoutes){
        this.UsersRoutes=UsersRoutes;
    }

    public void setCreator(String creator){

        ArrayList<Map<Integer, Float>> NewRoute =new ArrayList<>();
        if(!UsersRoutes.containsKey(creator)){
            this.UsersRoutes.put(creator,NewRoute);
        }
        else{return;}
    }

    public void AddRoute(String creator,Map<Integer,Float> Reduced){
        ArrayList<Map<Integer, Float>> NewRoute = UsersRoutes.get(creator);
        NewRoute.add(Reduced);
        UsersRoutes.put(creator,NewRoute);
    }
}
