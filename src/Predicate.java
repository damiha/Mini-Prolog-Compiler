import java.util.List;

public class Predicate {

    List<Clause> clauses;

    // a predicate must have at least one clause!
    public Predicate(List<Clause> clauses){
        this.clauses = clauses;
    }

    public String getPredicateName(){
        return clauses.get(0).predicateName();
    }

    public int getArity(){
        return clauses.get(0).arity();
    }

    public String getPredicateLabel(){
        return String.format("%s/%d", getPredicateName(), getArity());
    }
}
