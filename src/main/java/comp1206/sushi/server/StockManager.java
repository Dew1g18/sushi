package comp1206.sushi.server;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;
import comp1206.sushi.common.Staff;

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
        this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
//        pool.setKeepAliveTime(70,TimeUnit.SECONDS);
        //        init1();
    }

    void kill(){
        pauseDishChecking=true;
        end=true;
        pool.shutdownNow();
        while(true){
//            try{
//            Thread.sleep(100);
            if (pool.isTerminated()) {
                System.out.println("Threads should be dead: " + pool.getActiveCount());
                break;
//            }
//            }catch(InterruptedException e){
                //try again lol
            }

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
//                System.out.println(dish.getName());
                pool.submit(new Runnable() {
//                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
//                            for (int i = 0; i < dish.getRestockAmount().intValue(); i++) {
//                                if (!server.getDishes().contains(dish)){
//                                    throw new NullPointerException();
//                                }
//                                restockDish(dish);
//                            }
                            //TODO --uncomment the above and comment out the below if it turns out we restock
                            //TODO --one dish at a time instead of batches
                            //Read the spec again and staff are meant to do it in the whole batch size
                            if (!server.getDishes().contains(dish)) {
                                throw new NullPointerException();
                            }
                            restockDishBatch(dish);

                            dish.endRestocking();
                        } catch (NullPointerException e) {
//                            e.printStackTrace();
                            dish.endRestocking();
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

    public void ingredientCheck(){if (!end){
        for (Ingredient ingredient : server.getIngredients()) {
            if (ingredient.getStock().intValue() < ingredient.getRestockThreshold().intValue()) {
                restockIngredient(ingredient);
            }
        }
    }}

    //restocks dish once
//    private void restockDish(Dish dish){if (!end){
//        //Taking the ingredients for this dish
//        //This should keep trying to take ingredients till the right ones have been taken
//        for (Map.Entry<Ingredient, Number> ingredient : dish.getRecipe().entrySet()){
//            boolean keepTrying =true;
//            while (keepTrying){
//                try {
//                    Thread.sleep(100);
//                    ingredient.getKey().takePositiveStock(ingredient.getValue().doubleValue());
//                    keepTrying=false;
//                } catch (Exception e) {
//                    restockIngredient(ingredient.getKey());
////                    System.out.println("Had to restock an ingredient");
////                    server.notifyUpdate();
//                }
//            }
//        }
//        //Finding a staff member to complete the task
//        boolean notGotWorker = true;
//        while (notGotWorker) {
//            try {
//                Thread.sleep(100);
//                Staff worker = getWorker();
//                synchronized (worker) {
//                    //If a worker is found, completing the task
//                    if (worker != null) {
//                        notGotWorker = false;
////                        worker.setStatus("WORKING on "+dish.getName());
//                        try {
//                            server.notifyUpdate();
//                        } catch (NullPointerException e) {
////                        System.out.println("Can I just ignore the ones of these which will pop up on boot?");
//                        }
//                        worker.restockItem(dish);
//                    }
//                }
//            }catch (NullPointerException e){
////                System.out.println("lacking in workers");
//                //Ignoring this, its just the thread has to sit here an wait for a worker to be idle so it can
//                //restock this dish
//            }catch (InterruptedException e){
//                //Just trying to reduce cpu consumption with these sleeps
//            }
//        }
//    }}

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

    private void restockIngredient(Ingredient ingredient){if(!end){
//            System.out.println("Restocking "+ingredient.getName()+" from "+ingredient.getStock());
            ingredient.restockIncrement();

    }}

    //restocks dish by the ammount rather than one-by-one
    private void restockDishBatch(Dish dish){if (!end){
//        System.out.println("Restocking "+dish.getName());
        //Taking the ingredients for this dish
        //This should keep trying to take ingredients till the right ones have been taken
        for (Map.Entry<Ingredient, Number> ingredient : dish.getRecipe().entrySet()){
            boolean keepTrying =true;
            while (keepTrying){
                try {
                    Thread.sleep(100);
                    ingredient.getKey().takePositiveStock(ingredient.getValue().doubleValue()*dish.getRestockAmount().doubleValue());
                    tryNotify();
                    keepTrying=false;
                    System.out.println("Used " + ingredient.getKey().getName()+"  "+ ingredient.getKey().getStock());
                } catch (Exception e) {
                    restockIngredient(ingredient.getKey());
//                    System.out.println("Had to restock "+ingredient.getKey().getName());
//                    server.notifyUpdate();
                }
            }
        }
        //Finding a staff member to complete the task
        boolean notGotWorker = true;
        while (notGotWorker) {
            try {
                Thread.sleep(100);
                if (server.getDishes().contains(dish)){
                    Staff worker = getWorker();
                    synchronized (worker) {
                        //If a worker is found, completing the task
                        if (worker != null) {
                            notGotWorker = false;
                            worker.setStatus("WORKING on "+dish.getName());
                            System.out.println(worker.getName()+" "+worker.getStatus());
                            try {
                                server.notifyUpdate();
                            } catch (NullPointerException e) {
    //                        System.out.println("Can I just ignore the ones of these which will pop up on boot?");
                            }
                            worker.restockBatch(dish);
                        }
                    }
                }
            }catch (NullPointerException e){
//                System.out.println("lacking in workers");
                //Ignoring this, its just the thread has to sit here an wait for a worker to be idle so it can
                //restock this dish
            }catch(InterruptedException e){
                //catch and release for the good of cpu health
//                e.printStackTrace();
                //In this case I needed to get out of the loop here if the interrupt was thrown becasue that was the
                //Threadpool trying to shut down and I was not allowing it because I would just catch that exception and move on
                break;
            }
        }
    }}

    public void tryNotify(){
        try{
            server.notifyUpdate();
        }catch (Exception e){
            System.out.println("Failed notify");
        }
    }


}
