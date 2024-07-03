import java.util.List;

public class Clause {

    Term.Struct clauseHead;

    List<Goal> goals;

    public Clause(Term.Struct clauseHead, List<Goal> goals){
        this.clauseHead = clauseHead;
        this.goals = goals;
    }
}
