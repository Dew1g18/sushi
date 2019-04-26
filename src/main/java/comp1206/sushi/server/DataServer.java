package comp1206.sushi.server;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Restaurant;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DataServer extends Thread{

    ServerInterface server;
    Map<Socket, ObjectOutputStream> clientSockets;
    ServerSocket listener;
//    ObjectOutputStream out;
    ObjectInputStream in;
    boolean thereAreClients = false;

    public DataServer(ServerInterface server){
        this.server=server;
        try  {
            clientSockets = new HashMap<>();
            System.out.println("The server is running...");
            listener  = new ServerSocket(6969);
            periodicUpdateThread();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void run(){
        while(true){
            try{
                System.out.println("running");
                Socket newClient = listener.accept();
                newClient.setTcpNoDelay(true);
                //BufferedOutputStream outputStream = new BufferedOutputStream(newClient.getOutputStream());
                OutputStream outputStream = newClient.getOutputStream();
                ObjectOutputStream output = new ObjectOutputStream(outputStream);
                clientSockets.put(newClient, output);
                this.in = new ObjectInputStream(newClient.getInputStream());
                sendAll(newClient);
                thereAreClients=true;
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }


    public void sendAll(Socket newClient){
//        while (true) {
            try {
//            in.readObject();
                ObjectOutputStream output = clientSockets.get(newClient);
                output.writeUnshared(server.getRestaurant());
                output.writeUnshared(server.getDishes());
                output.writeUnshared(server.getUsers());
                output.writeUnshared(server.getPostcodes());
                output.flush();
                output.reset();
//                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
//        }
    }


    public void periodicUpdateThread(){
        Thread p_updater = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try{
                        Thread.sleep(1000);
                    if(thereAreClients) {
                        for (Socket client : clientSockets.keySet()) {
                            sendAll(client);
                        }
                    }}catch (InterruptedException e){
                        e.printStackTrace();
                    }

                }
            }
        });
        p_updater.start();
    }


    /**
     * Info needed by the client:
     *  -Dishes (Name, Price, Description)
     *  -Orders (All)
     *  -Users (Orders,
     */


//    private static class DataSender implements Runnable {
//        private Socket socket;
//        private ArrayList<Object> queue;
//
//        DataSender(Socket socket) {
//            this.socket = socket;
//        }
//
//        @Override
//        public void run() {
//            System.out.println("Connected: " + socket);
//            try {
//                Scanner in = new Scanner(socket.getInputStream());
//                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
////                out.writeObject();
//            } catch (Exception e) {
//                System.out.println("Error:" + socket);
//            } finally {
//                try { socket.close(); } catch (IOException e) {}
//                System.out.println("Closed: " + socket);
//            }
//        }
//    }
}


/**Bibliography
 *
 *  -For this class I needed a lot of help to learn how to use sockets, I got most of my help from here
 *      -http://cs.lmu.edu/~ray/notes/javanetexamples/#capitalize
 *      -https://www.tutorialspoint.com/java/java_networking.htm
 */
