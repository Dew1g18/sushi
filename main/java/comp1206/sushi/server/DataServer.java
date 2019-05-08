package comp1206.sushi.server;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Order;
import comp1206.sushi.common.Restaurant;
import comp1206.sushi.common.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class DataServer extends Thread{

    ServerInterface server;
    Map<Socket, ObjectOutputStream> clientToOutputMap;
    Map<Socket, ObjectInputStream> clientToInputStream;
    ServerSocket listener;
    boolean thereAreClients = false;

    public DataServer(ServerInterface server){
        this.gotOrders = new ArrayList<>();
        this.server=server;
        try  {
            clientToOutputMap = new HashMap<>();
            clientToInputStream = new HashMap<>();
            listener  = new ServerSocket(6969);
            periodicUpdateThread();
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("The server is running...");
    }

    public void run(){
        while(true){
            try{
                System.out.println("looking for clients");
                Socket newClient = listener.accept();
                newClient.setSoTimeout(1000);
                System.out.println("accepted a client");
                newClient.setTcpNoDelay(true);
                //BufferedOutputStream outputStream = new BufferedOutputStream(newClient.getOutputStream());

                OutputStream outputStream = newClient.getOutputStream();
                ObjectOutputStream output = new ObjectOutputStream(outputStream);

//                System.out.println("Streams created");

                clientToOutputMap.put(newClient, output);
                clientToInputStream.put(newClient,new ObjectInputStream(newClient.getInputStream()));

                sendAll(newClient);

//                System.out.println("Sent init data");
                thereAreClients=true;
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }


    public void sendAll(Socket client){
//        while (true) {
            try {
//            in.readObject();
                ObjectOutputStream output = clientToOutputMap.get(client);
                output.writeUnshared(server.getRestaurant());
                output.writeUnshared(server.getDishes());
                output.writeUnshared(server.getUsers());
                output.writeUnshared(server.getPostcodes());
                output.writeUnshared(server.getOrders());
                output.flush();
                output.reset();
//                break;
            } catch (IOException e) {
                clientToInputStream.remove(client);
                clientToOutputMap.remove(client);
                if (clientToOutputMap.isEmpty()){
                    thereAreClients=false;
                }
                e.printStackTrace();
                System.out.println("That just told you that a socket disconnected");
            }
//        }
    }

    public void readAll(Socket client){
        try{
//            System.out.println("Attempting socket read");
            ObjectInputStream in = clientToInputStream.get(client);
            Order downloadedOrder = ((Order) in.readObject());
            System.out.println(downloadedOrder.getOrder());
            gotOrders.add(downloadedOrder);
//            return downloadedOrder;
        }catch(IOException e){
//            e.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }catch(NullPointerException e){

        }
    }


    public void periodicUpdateThread(){
        Thread p_updater = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try{
                        Thread.sleep(1000);
                    if(thereAreClients) {
                        for (Socket client : clientToOutputMap.keySet()) {
                            readAll(client);
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
    public List<Order> getGotOrders(){
        return gotOrders;
    }


    public void clearOrders(){
        gotOrders= new ArrayList<>();
    }

    private ArrayList<Order> gotOrders;




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
