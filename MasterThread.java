
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class MasterThread extends Thread{

    private final int workerNUM;

    public Statistics stats;
    private Socket connectionSocket ;
    private final List<Socket> workerSockets ; // List to store worker sockets
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    public UsersRoutes UserTotals;



    public MasterThread(Socket socket ,List<Socket> workerSockets,int workerNum,UsersRoutes UserTotals,Statistics stats){
        this.connectionSocket = socket;
        this.workerSockets=workerSockets;
        this.workerNUM=workerNum;
        this.UserTotals=UserTotals;
        this.stats=stats;

    }

    public void run(){

        try {
            while(true) {
                System.err.println("MasterThread "+getName()+" started");
                //Ξεκινώντας τα threads λαμβάνουμε το input
                //Xρησιμοποιώντας τις κλάσεις gpx , xmlread κάνουμε parse το αρχείο με σκοπό να το
                //επεξεργαστούμε
                try {
                    dataInputStream = new DataInputStream(connectionSocket.getInputStream());
                    dataOutputStream = new DataOutputStream(connectionSocket.getOutputStream());
                }catch(IOException e){e.printStackTrace();}

                String creator;

                Gpx gpx=new Gpx();
                xmlread xm=new xmlread(getName());
                String NameOfFile=xm.FileName;
                System.err.println(NameOfFile);
                File file=new File("C:\\Users\\user\\IdeaProjects\\DistributedSystems\\%s.txt",NameOfFile);
                receiveFile("file",dataInputStream);
                synchronized (xm){
                    gpx = xm.Readxml("file");
                    creator = gpx.getCreator();
                    System.out.println(creator);
                    xm.notifyAll();
                }

                ArrayList<waypoint> ToChunk;
                ToChunk = gpx.GetWaypointList();

                int chunkSize = 3;

                //αυθαίρετο chunk size , στην προκειμένη περίπτωση 3 ώστε να έχουμε πολλά chunks προς επεξεργασία

                ArrayList<Chunk> Chunked = new ArrayList<>();

                //Αρχίζουμε το chunking
                //Για κάθε επόμενο sublist εισάγουμε και στην αρχή του το τελευταίο waypoint της προηγούμενης λίστας.
                waypoint last;
                last = ToChunk.get(0);
                for (int i = 0; i < ToChunk.size(); i += chunkSize) {
                    ArrayList<waypoint> temp = new ArrayList<>(ToChunk.subList(i, Math.min(i + chunkSize, ToChunk.size())));
                    if (i == 0) {
                        Chunk chunk = new Chunk(i, temp);
                        Chunked.add(chunk);
                    } else {
                        //Προσθέτουμε το τελευταίο του προηγούμενο chunk στην αρχή αυτουνού
                        temp.add(0, last);
                        Chunk chunk = new Chunk(i, temp);
                        Chunked.add(chunk);
                    }
                    last = temp.get(temp.size() - 1);
                }

                ArrayList<Map<Integer, Float>> Results = new ArrayList<>();
                int WorkIndex = 0;
                //Ξεκινάμε να στέλνουμε στους Workers με RR
                //Μόλις ανέβει το WorkIndex στον αριθμό των workers ξεκινάμε πάλι απο τον 1 Worker
                for (int counter = 0; counter < Chunked.size(); counter++) {

                    if (WorkIndex == workerNUM) {
                        WorkIndex = 0;
                    }
                    OutputStream outputStream = workerSockets.get(WorkIndex).getOutputStream(); //new OutputStream(workerSockets.get(Windex).getOutputStream());
                    InputStream inputStream = workerSockets.get(WorkIndex).getInputStream();


                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

                    synchronized (objectOutputStream){
                        Chunk ch = Chunked.get(counter);
                        objectOutputStream.writeObject(ch);
                        objectOutputStream.flush();
                        objectOutputStream.notifyAll();
                    }

                    synchronized (inputStream){
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                        Map<Integer, Float> Intermediate = (Map<Integer, Float>) objectInputStream.readObject();
                        Results.add(Intermediate);
                        inputStream.notifyAll();

                    }

                    WorkIndex++;
                }
                //Έχουμε λάβει τα intermediate results αυτού του gpx και προχωράμε στο Reduce

                Map<Integer, Float> Reduced = Reduce(Results);

                //Προσθέτουμε την διαδρομή που κάναμε reduce στο UserTotals , για να κρατήσουμε στατιστικά
                synchronized (UserTotals) {
                    UserTotals.setCreator(creator);
                    UserTotals.AddRoute(creator,Reduced);
                    UserTotals.notifyAll();
                }


                //Στέλνουμε πίσω στον Client τα αποτελέσματα για την διαδρομή που έστειλε
                //και έπειτα στέλνουμε και τα στατιστικά
                //εφόσον πρώτα τα κάνουμε set , και με τα καινούργια δεδομένα
                synchronized (UserTotals){
                    OutputStream out = connectionSocket.getOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
                    objectOutputStream.writeObject(Reduced);
                    objectOutputStream.flush();
                    UserTotals.notifyAll();

                    synchronized (stats){

                        stats.setStats(UserTotals.UsersRoutes);
                        objectOutputStream.writeObject(stats);
                        objectOutputStream.flush();
                        stats.notifyAll();
                    }

                }




            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }





    private static void receiveFile(String fileName,DataInputStream dataInputStream)throws Exception {

        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        long size = dataInputStream.readLong(); // read file size
        byte[] buffer = new byte[4 * 1024];


        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size


        }
        // Here we received file
        System.out.println("File is Received");



    }


    //Η μέθοδος Reduce λαμβάνει ένα ArrayList απο τα map που παρήγαγαν οι worker
    //και αθροίζει τα αποτελέσματα τους
    //επειδή θέλουμε τη μέση ταχύτητα , την υπολογίζουμε εκτός της επανάληψης
    //έπειτα γυρνάει τον map που αντιστοιχεί σε όλα τα chunk
    public static Map<Integer,Float> Reduce (ArrayList<Map<Integer,Float>> TotalResults) {

        HashMap<Integer,Float> Total=new HashMap<>();

        for(Map<Integer,Float> map:TotalResults){
            for(Map.Entry<Integer,Float> entry:map.entrySet()){
                Integer key=entry.getKey();
                Float value= entry.getValue();
                if(Total.containsKey(key)){
                    Total.put(key,Total.get(key)+value);
                }else{
                    Total.put(key,value);
                }
            }
        }
        float AverageSpeed=Total.get(2)/TotalResults.size();
        Total.put(2,AverageSpeed);

        return Total;
    }

}

