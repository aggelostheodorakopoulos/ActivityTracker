import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null ;


    public static void main(String[] args) {
        new Client().startClient();
    }

    public void startClient() {
        Scanner sc = new Scanner(System.in);
        Socket requestSocket = null;         // πρίζα αιτήματος
        System.out.println("Client start ");

        try {
            // Create a socket to connect to the server
            requestSocket = new Socket("localhost", 4321);
            dataOutputStream = new DataOutputStream(requestSocket.getOutputStream());
            dataInputStream = new DataInputStream(requestSocket.getInputStream());


            while(true) {
                int bytes = 0;
                System.out.println("Give the name of file ");
                String msg = sc.nextLine();
                //dataOutputStream.writeUTF(msg);
                File file = new File(msg);
                // Create a file input stream to read the file to send
                FileInputStream fileInputStream = new FileInputStream(file);
                dataOutputStream.writeLong(file.length());
                byte[] buffer = new byte[4 * 1024];
                while ((bytes = fileInputStream.read(buffer)) != -1) {
                    // Send the file to Server Socket
                    dataOutputStream.write(buffer, 0, bytes);
                    dataOutputStream.flush();
                }
                System.out.println("File sent: " + msg);
                ObjectInputStream objectInputStream = new ObjectInputStream(dataInputStream);
                Map<Integer,Float> Results=(Map<Integer,Float>) objectInputStream.readObject();

                System.out.println("The metrics for the specific route you provided are: ");
                System.out.println("---------------------------------------------------");
                System.out.println("Total Distance: " +Results.get(0)+" km");
                System.out.println("Total Elevation: " +Results.get(1)+" m");
                System.out.println("Average speed of Route: " +Results.get(2)+" km/h");
                System.out.println("Total time: " +Results.get(3)/60+" minutes");

                System.out.println();
                System.out.println("The statistics for you and the other users after this route are:");
                System.out.println("---------------------------------------------------");

                Statistics stats=(Statistics) objectInputStream.readObject();
                for(Map.Entry<String,Results> entry:stats.getStats().entrySet()){
                    System.out.println(entry.getKey().toUpperCase()+": Average distance: "+entry.getValue().getTotalDistance()+" km , Average time: "+entry.getValue().getTime()/60+" min ,Average speed: "+entry.getValue().getSpeed()+" km/h , Average elevation: "+entry.getValue().getEle()+" meters");

                }



                if (msg.equalsIgnoreCase("bye")){
                    break;
                }

            }



            dataInputStream.close();
            requestSocket.close();
            sc.close();

        } catch (Exception expc) {
            System.err.println("data received in unknown format");
        }


    }

}