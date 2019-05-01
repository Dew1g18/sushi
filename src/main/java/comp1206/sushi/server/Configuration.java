package comp1206.sushi.server;

import comp1206.sushi.common.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class Configuration {
    private ServerInterface server;
    private String filename;
    private BufferedReader reader;
    private Restaurant restaurant;
    private GenericHelp gh = new GenericHelp();
    public ArrayList<User> users = new ArrayList<User>();
    public ArrayList<Order> orders = new ArrayList<Order>();

    public Configuration(ServerInterface server, String filename){
        this.server = server;
        this.filename = filename;
    }

    public Restaurant loadConfig(){
//        BufferedReader reader = new BufferedReader();
        try {
            FileReader fileReader = new FileReader(filename);
            this.reader = new BufferedReader(fileReader);
            return readWholeFile();
        }catch(FileNotFoundException e){
            System.out.println("File not found, feel like that shouldn't be a problem given the filename was given by a filechecker.");
            return server.getRestaurant();
        }
    }

    private String getLine(){
        try{
            return reader.readLine();
        }catch(Exception e){
            System.out.println(e);
            return null;
        }
    }

    private boolean addLineToServer(){
        String currentLine = getLine();
        if (currentLine==null){
            return false;
        }
        System.out.println(currentLine);
        String[] fullLine = currentLine.split(":");
        switch(fullLine[0]){
            case "POSTCODE":
                this.server.addPostcode(fullLine[1]);
                break;

            case "RESTAURANT":
                this.restaurant = new Restaurant(fullLine[1], gh.ifInList(server.getPostcodes(), fullLine[2]));
                break;

            case "SUPPLIER":
                server.addSupplier(fullLine[1], gh.ifInList(server.getPostcodes(), fullLine[2]));
                break;

            case "INGREDIENT":
                Number restockThreshold = Integer.parseInt(fullLine[4]);
                Number restockAmount = Integer.parseInt(fullLine[5]);
                Number weight = Double.parseDouble(fullLine[6]);
                server.addIngredient(fullLine[1], fullLine[2], gh.ifInList(server.getSuppliers(),fullLine[3]),restockThreshold, restockAmount,weight);
                break;

            case "DISH":
                Number price = Double.parseDouble(fullLine[3]);
                Number dishRestockThreshold = Integer.parseInt(fullLine[4]);
                Number dishRestockAmount = Integer.parseInt(fullLine[5]);
                Dish dish = server.addDish(fullLine[1],fullLine[2],price, dishRestockThreshold,dishRestockAmount);
                String[] recipe = fullLine[6].split(",");
                for (String ingredient : recipe){
                    String[] components = ingredient.split(" \\* ");
                    Number quantitly = Integer.parseInt(components[0]);
                    server.addIngredientToDish(dish, gh.ifInList(server.getIngredients(),components[1]),quantitly);
                }break;

            case "USER":
                users.add(new User(fullLine[1], fullLine[2],fullLine[3],gh.ifInList(server.getPostcodes(), fullLine[4])));
                break;
            case "ORDER":
                Order order = new Order();
                for (String dishAndValue : fullLine[2].split(",")){
                    String[] dishAndValList = dishAndValue.split(" \\* ");
                    Dish dish1 = gh.ifInList(server.getDishes(),dishAndValList[1]);
                    Integer val = Integer.parseInt(dishAndValList[0]);
                    order.setStatus("Received by server");
                    order.addDishToOrder(dish1,val);
                }
                orders.add(order);
                break;
            case "STOCK":
                setStockOfThing(fullLine[1], fullLine[2]);
                break;
            case "STAFF":
                server.addStaff(fullLine[1]);
                break;
            case "DRONE":
                server.addDrone(Double.parseDouble(fullLine[1]));
                break;
        }return true;
    }

    private void setStockOfThing(String thing, String number){
        int stock = Integer.parseInt(number);
        Ingredient ingredient = gh.ifInList(server.getIngredients(), thing);
        Dish dish = gh.ifInList(server.getDishes(), thing);
        if (dish!=null){
            server.setStock(dish, stock);
        }
        if (ingredient!=null){
            server.setStock(ingredient,stock);
        }
    }

    public Restaurant readWholeFile() {
        try {
            //Make a method to repeat this till the config file is empty
            boolean keepRunning = true;
            while (keepRunning) {
                keepRunning = addLineToServer();
            }
            System.out.println("Whole file read succesful!");
            return this.restaurant;
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Really hoping this means the whole file was read and I can return some meaningful things");
            return this.restaurant;
        }
    }

}

