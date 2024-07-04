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

    public String toString(){
        StringBuilder res = new StringBuilder(clauseHead.toString() + " :- ");

        int i = 0;
        for(Goal goal : goals){
            res.append(goal.toString()).append(i < arity() - 1 ? ", " : "");
        }

        return res.toString();
    }
}
