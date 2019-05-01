package comp1206.sushi.server;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Order;
import comp1206.sushi.common.TooMuchException;
import comp1206.sushi.common.User;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class OrderHandler implements Runnable {
    ServerInterface serverInterface;
    GenericHelp gh = new GenericHelp();

    public OrderHandler(ServerInterface serverInterface){
        this.serverInterface=serverInterface;

    }


    @Override
    public void run() {
        try{
            while (true) {
                Thread.sleep(3000);
                for (Order order : serverInterface.getOrders()){
                    if (order.getStatus().equals("Received by server")||order.getStatus().equals("Processing dishes")){
                        order.setStatus("Processing dishes");
                        workOnOrder(order);
                    }
                }
            }
        }catch (InterruptedException e){
            System.out.println("Broken for new config or something hopefully!");
        }
    }

   public void workOnOrder(Order order){
//       System.out.println("Working on order");
        if (enoughDishes(order)){
            System.out.println("enough dishes!");
            for (Map.Entry<Dish, Number> entry : order.getOrder().entrySet()) {
                Dish dishFromServer = gh.ifInList(serverInterface.getDishes(), entry.getKey().getName());
                try {
                    dishFromServer.takePositiveStock(entry.getValue().intValue());
                    System.out.println("Got "+ dishFromServer.getName());
                } catch (TooMuchException e) {
                    System.out.println("Something has gone drastically wrong (OrderHandler)");
                }
            }
            order.setStatus("Dishes ready");
        }
   }

   public boolean enoughDishes(Order order){
       for (Map.Entry<Dish, Number> entry : order.getOrder().entrySet()){
           Dish dishFromServer = gh.ifInList(serverInterface.getDishes(), entry.getKey().getName());
           if (dishFromServer.getStock().intValue()<entry.getValue().intValue()){
               return false;
           }
       }return true;
   }

    public void cancelOrder(Order order){
        User userFromServer = gh.ifInList(serverInterface.getUsers(),order.getUser().getName());
        Order orderFromUser = gh.ifInList(userFromServer.getOrderHistory(), order.getName());
        userFromServer.getOrderHistory().remove(orderFromUser);
    }
}
