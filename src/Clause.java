import java.util.List;

public class Clause {

    Term.Struct clauseHead;

    List<Goal> goals;

    public Clause(Term.Struct clauseHead, List<Goal> goals){
        this.clauseHead = clauseHead;
        this.goals = goals;
    }

    public String predicateName(){
        return clauseHead.structName;
    }

    public int arity(){
        return clauseHead.terms.size();
    }
}
