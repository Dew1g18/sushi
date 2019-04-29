package comp1206.sushi.server;

import comp1206.sushi.common.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class GenericHelp implements Serializable{

    public <T extends Model> T ifInList(List<T> list, String name) {
        for (T thing : list) {
            if (thing.getName().equals(name)) {
                return thing;
            }
        }
        return null;
    }

    public <T extends Model> boolean isInSet(Set<T> list, String name) {
        for (T thing : list) {
            if (thing.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIngredientUsed(List<Dish> dishes, Ingredient ingredient){
        for (Dish dish : dishes){
            if (isInSet(dish.getRecipe().keySet(),ingredient.getName())){
                return true;
            }
        }
        return false;
    }

    public boolean isDishUsed(List<Order> orders, Dish dish){
        for (Order order : orders){
            if(isInSet(order.getOrder().keySet(),dish.getName())){
                return true;
            }
        }
        if (dish.inHand){
            return true;
        }else {
            return false;
        }
    }

    public boolean isSupplierUsed(List<Ingredient> ingredients, Supplier supplier){
        for (Ingredient ingredient : ingredients){
            if (ingredient.getSupplier().getName()==supplier.getName()){
                return true;
            }
        }
        return false;
    }


    public boolean isUsernameUsed(List<User> users, String username){
        for (User user : users){
            if (user.getName().equals(username)){
                return true;
            }
        }
        return false;
    }

    public boolean isPostcodeUsed(ServerInterface server, Postcode postcode){
        String postName = postcode.getName();
        if (server.getRestaurant().getLocation().getName()==postName){
            return true;
        }
        if (isPostcodeUsedBySupplier(server.getSuppliers(),postName)){
            return true;
        }
        if (isPostcodeUsedByUser(server.getUsers(), postName)){
            return true;
        }
        return false;
    }

    public boolean isPostcodeUsedBySupplier(List<Supplier> suppliers, String postcode){
        for (Supplier supplier: suppliers){
            if(supplier.getPostcode().getName()==postcode){
                return true;
            }
        }
        return false;
    }

    public boolean isPostcodeUsedByUser(List<User> users, String postcode){
        for(User user: users){
            if (user.getPostcode().getName()==postcode){
                return true;
            }
        }
        return false;
    }

    public boolean isUserUsed(List<Order> orders, User user){
        for (Order order: orders){
            if(order.getUser().getName().equals(user.getName())){
                return true;
            }
        }
        return false;
    }
}
