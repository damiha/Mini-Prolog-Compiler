import java.util.List;

public class ClauseHead {

    String headName;
    List<Term> headVars;

    public ClauseHead(String headName, List<Term> headVars){
        this.headName = headName;
        this.headVars = headVars;
    }
}
