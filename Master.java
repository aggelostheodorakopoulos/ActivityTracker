import java.io.*;
import java.net.*;
import java.util.*;

public class Master {
    private final List<Socket> workerSockets = new ArrayList<>(); // List to store worker sockets
    ServerSocket serverSocketW = null;
    Socket connectionSocketW;
    static int workerNUM;
    //private List<Worker> workers;


    public  int PORT = 8080;
    public static void main(String[] args) throws IOException {
        System.out.println("Give number of workers");
        Scanner scanner=new Scanner(System.in);
        String input = scanner.nextLine();
        workerNUM = Integer.parseInt(input);
        new Master().openServer();
    }

    public void openServer() throws IOException {

        try  {



            serverSocketW = new ServerSocket(8080);
            int j=0;
            while (j<workerNUM) {
                System.out.println("Server started and listening on port  " + PORT);
                System.out.println("Waiting for worker no."+(j+1));
                connectionSocketW = serverSocketW.accept();
                System.out.println("Worker no."+(j+1)+" connected");
                synchronized (workerSockets) {
                    workerSockets.add(connectionSocketW);
                }


                j++;

            }
        } catch (IOException e) {
            System.err.println("Error while running the server: " + e.getMessage());
        }
        ServerSocket serverSocket ;
        Socket connectionSocket ;
        // String message = null;
        serverSocket = new ServerSocket(4321);
        Map<String,ArrayList<Map<Integer,Float>>> Total=new HashMap<>();
        UsersRoutes TotalRoutes=new UsersRoutes(Total);
        Statistics stats=new Statistics();
        try {
            while (true) {
                System.out.println("Waiting for Clients  ");
                connectionSocket = serverSocket.accept();
                System.out.println("Client Connected");
                MasterThread thread = new MasterThread(connectionSocket,workerSockets,workerNUM,TotalRoutes,stats);
                thread.start();
            }



        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


}