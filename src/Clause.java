import java.util.List;

public class Clause {

    ClauseHead clauseHead;

    List<Goal> goals;

    public Clause(ClauseHead clauseHead, List<Goal> goals){
        this.clauseHead = clauseHead;
        this.goals = goals;
    }
}
