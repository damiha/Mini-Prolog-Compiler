import java.util.HashMap;
import java.util.Map;

public class Environment {

    // we get the position of the variable (needed for put ref)
    public Map<String, Integer> data;

    public Environment(){
        this.data = new HashMap<>();
    }

    public boolean has(String varName){
        return data.containsKey(varName);
    }

    public void put(String varName){
        data.put(varName, data.size() + 1);
    }
}
