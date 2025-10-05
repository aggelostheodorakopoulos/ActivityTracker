import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Ο Worker , λαμβάνει σαν όρισμα απο το όνομα του Host , port και ένα ΙD
//Δημιουργεί ένα socket με αυτά , και λαμβάνει απο το stream το αντίστοιχο αντικείμενο chunk

public class WorkerNode {
    private final String masterHost;
    private final int masterPort;

    private final Object lock =new Object();

    private int numThreads=0;


    public WorkerNode(String masterHost, int masterPort, int workerId) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;

    }

    public void start() {
        try {
            // Σύνδεση με τον Master

            Socket socket = new Socket(masterHost, masterPort);
            System.out.println("Connected to master node at " + masterHost + ":" + masterPort);


            //Εδώ περιμένουμε για το Chunk
            //Μόλις το λάβουμε ξεκινάμε ένα καινούρφιο thread για κάθε chunk.
            while (true) {


                try{
                    Chunk chunk;
                    synchronized (lock){
                        ObjectInputStream in =new ObjectInputStream(socket.getInputStream());
                        chunk= (Chunk)in.readObject();
                        lock.notifyAll();}

                    System.err.println("Data received");

                    WorkerThread workerThread = new WorkerThread(socket,chunk);
                    workerThread.start();
                    numThreads++;
                }catch (ClassNotFoundException e){
                    throw new IOException("Σφάλμα λήψης");
                }

            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //Λαμβάνει ως όρισμα το socket(Για να γνωρίζει που να στείλει το output
    //Καθώς και το chunk προς επεξεργασία.
    private class WorkerThread extends Thread {
        private Socket socket;
        private Chunk chunk;

        private ObjectOutputStream out = null;
        public WorkerThread(Socket socket, Chunk chunk) {
            this.socket = socket;
            this.chunk = chunk;

        }

        @Override
        public void run() {

            try {
                System.err.println("Thread "+getName()+" started");
                //Καλούμε την συνάρτηση Mapper που ουσιαστικά , με τους κατάλληλους υπολογισμούς
                //Αναθέτει σε κάθε ζητούμενο ένα key 0,1,2,3 για απόσταση , ανάβαση , ταχύτητα , χρόνος αντίστοιχα





                //Μόλις έρθει η ώρα να στείλουμε πίσω το αποτέλεσμα , κλειδώνουμε το socket ώστε
                //να μην υπάρξει σύγχυση καθώς πολλά thread θα προσπαθήσουν να γράψουν.
                synchronized (socket) {
                    Map<Integer, Float> results = Mapper(chunk);
                    try {

                        out = new ObjectOutputStream(socket.getOutputStream());

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    for (Map.Entry<Integer,Float> entry : results.entrySet()) {

                        System.out.println( getName()+" Key: " + entry.getKey() + ", Value: " + entry.getValue());
                    }

                    out.writeObject(results);
                    out.flush();

                }}
            catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                synchronized (lock){
                    System.err.println("Clear to send ");
                    numThreads--;
                    lock.notifyAll();
                }
            }




        }
        //Ουσιαστικά το chunk είναι ένα ArrayList απο waypoints
        //πηγαίνει σειριακά και κάνει τους κατάλληλους υπολογισμούς
        //καλώντας τις κατάλληλες συναρτήσεις , και αναθέτουμε τα κλειδιά
        private Map<Integer,Float> Mapper(Chunk chunk) {


            List<waypoint> Calculate=chunk.getWaypoints();
            float TotalDistance = 0.0F;
            float ele = 000.0F;
            float speed = 000.0F;
            float totalTime = 0;

            for (int i=1;i<Calculate.size();i++) {
                waypoint first =Calculate.get(i-1);
                waypoint second = Calculate.get(i);
                float d = distance(first, second);
                TotalDistance += d;
                ele += elevation(first, second);
                totalTime += time(first, second);
            }


            speed += speed(TotalDistance,totalTime);


            Map<Integer, Float> Results = new HashMap<>();
            Results.put(0,TotalDistance);
            Results.put(1,ele);
            Results.put(2,speed);
            Results.put(3,totalTime);

            return Results;

        }
    }

    private float distance(waypoint first, waypoint second) {
        final int R=6371;//Η ακτίνα της γης σε χιλιόμετρα
        double latDistance=Math.toRadians(second.getlat()- first.getlat());
        double lonDistance=Math.toRadians(second.getlon()- first.getlon());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(first.getlat())) * Math.cos(Math.toRadians(second.getlat()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (float) (R*c);
    }

    private float elevation(waypoint first, waypoint second) {
        float elevation = 0.0F;
        if (first.getele() < second.getele()) {
            elevation += (second.getele() - first.getele());
        }
        return elevation;
    }


    private float time(waypoint first, waypoint second) {
        String time1 = first.gettime();
        String time2 = second.gettime();


        Instant instance1=Instant.parse(time1);
        Instant instance2=Instant.parse(time2);

        Duration duration = Duration.between(instance1,instance2);

        return duration.toSeconds();

    }

    private float speed(float distance,float time){


        return (float)(distance/(time/3600.0));

    }

    public static void main(String[] args) {
        // Initialize the worker with the master node host and port and a worker id
        WorkerNode worker = new WorkerNode("localhost", 8080, 1);
        worker.start();
    }


}


