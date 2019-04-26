package comp1206.sushi.server;

import comp1206.sushi.common.Model;

import java.util.List;

public class GenericHelp {

    public <T extends Model> T ifInList(List<T> list, String name) {
        for (T thing : list) {
            if (thing.getName().equals(name)) {
                return thing;
            }
        }
        return null;
    }
}
