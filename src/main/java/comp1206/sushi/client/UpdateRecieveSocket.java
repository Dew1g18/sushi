package comp1206.sushi.client;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.Restaurant;
import comp1206.sushi.common.User;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UpdateRecieveSocket extends Thread{
    ClientInterface client;
    Socket clientSocket;
    public Restaurant restaurant;
    public List<Dish> dishes;
    public List<User> users;
    public List<Postcode> postcodes;
    public boolean ready = false;
    ObjectOutputStream out;
    ObjectInputStream in;

    public UpdateRecieveSocket(ClientInterface client){
        this.client = client;
        System.out.println("Connecting to server");
    }

    public void run(){
        System.out.println("running");
        while (!ready) {
            try {
                this.clientSocket = new Socket("localhost", 6969);
                System.out.println("Connected to the server!");
                clientSocket.setReceiveBufferSize(4);
                this.out = new ObjectOutputStream(clientSocket.getOutputStream());
//                out.flush();
                InputStream inputStream = clientSocket.getInputStream();//new BufferedInputStream(clientSocket.getInputStream());
                this.in = new ObjectInputStream(inputStream);
                System.out.println("inputstream got");
                this.restaurant = ((Restaurant) in.readObject());
                this.dishes = ((List<Dish>) in.readObject());
                this.users = ((List<User>) in.readObject());
                this.postcodes = ((List<Postcode>) in.readObject());
                System.out.println("info loaded");
                ready = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void getUpdate(){
        boolean trying = true;
        while(trying) {
            try {
                this.join();
//                System.out.println("Getting update");
//                List<Dish> oldDishes = this.dishes;
                this.restaurant = ((Restaurant) in.readObject());
                this.dishes = ((List<Dish>) in.readObject());
//                System.out.println(((List<Dish>) in.readObject()));
                this.users = ((List<User>) in.readObject());
                this.postcodes = ((List<Postcode>) in.readObject());
//                System.out.println(this.dishes);
//                System.out.println("Object read");
                trying=false;
//                System.out.println("Update got");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void dishChanges(Dish localDish, Dish updatedDish){
        localDish.setPrice(updatedDish.getPrice());
        localDish.setDescription(updatedDish.getDescription());
    }




}
