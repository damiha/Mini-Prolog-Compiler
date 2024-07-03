
import java.util.List;

public class Program {

    List<Predicate> predicates;
    Goal query;

    public Program(List<Predicate> predicates,  Goal query){
        this.predicates = predicates;
        this.query = query;
    }
}
