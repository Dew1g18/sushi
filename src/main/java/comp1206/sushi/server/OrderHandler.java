package comp1206.sushi.server;

import comp1206.sushi.common.Order;
import comp1206.sushi.common.User;

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
                Thread.sleep(100);
//                for (Order order : )
            }
        }catch (InterruptedException e){
            System.out.println("Broken for new config or something hopefully!");
        }
    }

    public void workOnOrder(Order order){

    }

    public void cancelOrder(Order order){
        User userFromServer = gh.ifInList(serverInterface.getUsers(),order.getUser().getName());
        Order orderFromUser = gh.ifInList(userFromServer.getOrderHistory(), order.getName());
        userFromServer.getOrderHistory().remove(orderFromUser);
    }
}
