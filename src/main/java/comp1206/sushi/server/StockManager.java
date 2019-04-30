package comp1206.sushi.server;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;
import comp1206.sushi.common.Staff;
import comp1206.sushi.common.TooMuchException;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StockManager {

    ServerInterface server;
    ThreadPoolExecutor pool;

    public StockManager(ServerInterface server){
        this.server = server;
        end=false;
        this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
//        pool.setKeepAliveTime(70,TimeUnit.SECONDS);
        //        init1();
    }

    void kill(){
        pauseDishChecking=true;
        end=true;
        readyDishes=new ArrayList<>();
        pool.shutdownNow();
//        System.out.println("Shutting down stock manager");
        while(true){
//            try {
//                Thread.sleep(100);
//            System.out.println(pool.getActiveCount());
                if (pool.isTerminated()) {
                    System.out.println("Threads should be dead: " + pool.getActiveCount());
                    break;
                }
//            }catch(InterruptedException e){
////                try again lol
//            }

        }
    }
    boolean end = false;

    public void dishCheck(){if (!end){
        for (Dish dish: server.getDishes()) {
//            System.out.println(dish.getName());
            //Added an extra check so multiple staff members can restock at the same time.
            boolean yes;
            if (dish.getStockPotential()<dish.getRestockThreshold().intValue()){
                dish.startRestocking();
//                System.out.println("Dish restocking");
//                System.out.println(dish.getName());
                pool.submit(new Runnable() {
//                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
                            if (!server.getDishes().contains(dish)) {
                                throw new NullPointerException();
                            }
                            restockDishBatch(dish);

//                            dish.endRestocking();
                        } catch (NullPointerException e) {
//                            e.printStackTrace();
//                            dish.endRestocking();
//                            keepRunning=false;
                        }
                    }
                });
//                t.start();
            }
        }
    }}

    /**
     * surely either all restocking should be done in simultaneous batches or there should be no restocking
     * amount in the dishes class because it doesnt make sense to have staff restocking till a certain number
     * above the threshold is met, especially as that number is determined by the number of dishes below that
     * number it was on before you began restocking
     * Thats actually stupid seeing as each staff member can only create 1 dish at a time
     */



    public boolean pauseDishChecking =true;
    void init1(){
        Thread stockChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){if (!end){
                    try {
                        Thread.sleep(100);
                        synchronized ((Object) pauseDishChecking) {
                            if (!pauseDishChecking) {
                                ingredientCheck();
                                dishCheck();
                                tryNotify();
//                                System.out.println("RUNNNN");
//                                dishChecker2();
                            }else{
//                                System.out.println("Checking paused");
                            }
                        }
                    }catch(Exception e){
                        System.out.println("Hopefully just a new config...");
                    }
                }}
            }
        });
        stockChecker.start();
    }


    public ArrayList<Ingredient> ingNeedRestock = new ArrayList<>();
    public void ingredientCheck(){if (!end){
//        System.out.println("Checking ingredients");
        for (Ingredient ingredient : server.getIngredients()) {
            if (ingredient.getStockPotential()<= ingredient.getRestockThreshold().intValue()) {
                restockIngredient(ingredient);
            }
        }
    }}

    private void restockIngredient(Ingredient ingredient){if(!end){
//            System.out.println("Restocking "+ingredient.getName()+" from "+ingredient.getStock());
//        ingredient.restockIncrement();
        if (ingredient.getStockPotential()<ingredient.getRestockThreshold().intValue()) {
            System.out.println("Restock "+ingredient.getName());
            ingNeedRestock.add(ingredient);
            ingredient.startRestocking();
            tryNotify();
        }

    }}

    private Staff getWorker(){
        Staff worker= null;
        for (Staff staff: server.getStaff()){
            if (staff.getStatus().equals("IDLE")) {
                worker = staff;
                break;
            }
        }
        //If a worker is found, completing the task
        return worker;
    }


    public ArrayList<Dish> readyDishes= new ArrayList<>();
    //restocks dish by the amount rather than one-by-one
    private void restockDishBatch(Dish dish){if (!end){
//        System.out.println("Restocking "+dish.getName());
        //Taking the ingredients for this dish
        //This should keep trying to take ingredients till the right ones have been taken
        for (Map.Entry<Ingredient, Number> ingredient : dish.getRecipe().entrySet()) {
            boolean keepTrying = true;
            try{
                while (keepTrying) {
                    try {
                        Thread.sleep(100);
                        ingredient.getKey().takePositiveStock(ingredient.getValue().doubleValue() * dish.getRestockAmount().doubleValue());
                        tryNotify();
                        keepTrying = false;
    //                    System.out.println("Used " + ingredient.getKey().getName()+"  "+ ingredient.getKey().getStock());
                    } catch (TooMuchException e) {
                        restockIngredient(ingredient.getKey());
                        tryNotify();
    //                    System.out.println("Had to restock "+ingredient.getKey().getName());
    //                    server.notifyUpdate();
                    }
                }
            }catch(InterruptedException e){
                    break;
            }
        }

//        tryNotify();
        //Now the dish is ready, so I will tell the staff threads that one of them should pick it up.
        readyDishes.add(dish);
        System.out.println(dish.getName()+" Will hopefully be restocked at some point.");

    }}

    public void tryNotify(){
        try{
            server.notifyUpdate();
        }catch (Exception e){
//            System.out.println("Failed notify");
        }
    }


}
