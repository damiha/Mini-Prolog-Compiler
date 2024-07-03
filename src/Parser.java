import lexer.TokenType;

import java.util.*;

import static lexer.TokenType.*;

public class Parser {

    List<Token> tokens;

    int current = 0;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

    private boolean isAtEnd(){
        return current >= tokens.size() || peek().type == EOF;
    }

    private boolean check(TokenType type){
        return !isAtEnd() && peek().type == type;
    }

    // check = checkAtK with k = 0
    // realizes a lookahead
    private boolean checkAtK(TokenType type, int k){
        return (current + k < tokens.size()) && ((tokens.get(current + k)).type == type);
    }

    private boolean checkSequence( int start, TokenType... types){
        for(int i = 0; i < types.length; i++){
            if(!checkAtK(types[i], start + i)){
                return false;
            }
        }
        return true;
    }

    private boolean checkTypes(TokenType... types){
        for(TokenType t : types){
            if(check(t)){
                return true;
            }
        }
        return false;
    }

    private void advance(){
        current++;
    }

    private boolean match(TokenType type){

        if(check(type)){
            advance();
            return true;
        }

        return false;
    }

    private Token consume(TokenType type, String message){
        if(match(type)){
            return previous();
        }
        error(message);
        return null;
    }

    public Program parse(){

        List<Clause> clauses = new ArrayList<>();

        while(!match(QUESTION_MARK)){
            clauses.add(clause());
            // every clause ends with a dot
            consume(DOT, "Dot expected at end of clause");
        }

        // question mark has been consumed
        Goal query = goal(new Environment());

        Predicate predicate = new Predicate(clauses);

        return new Program(List.of(predicate), query);
    }

    private void error(String message){
        throw new RuntimeException(String.format("[%d] %s", peek().line, message));
    }

    private Clause clause(){

        // each clause has its own variable environment?
        // so we know when to use var and when to use ref

        Environment clauseEnv = new Environment();

        Term.Struct clauseHead = clauseHead(clauseEnv);

        // clause in 'normal form' so we need to parse the goals
        if(match(IMPLIED_BY)){
            List<Goal> goals = goals(clauseEnv);

            return new Clause(clauseHead, goals);
        }
        else{
            // is_bigger(elephant, horse).
            // needs to be transformed to is_bigger(X, Y) :- X = elephant, Y = horse
            return normalizeSimpleClause(clauseHead, clauseEnv);
        }
    }

    private Term.Struct clauseHead(Environment clauseEnv){

        Term term = term(clauseEnv);

        if(term instanceof Term.Struct){
            return (Term.Struct) term;
        }
        throw new RuntimeException("Expected a clause head of the form p(X_1, ..., X_n)");
    }

    // parse as many goals as you can before the dot comes
    private List<Goal> goals(Environment clauseEnv){
        List<Goal> goals = new ArrayList<>();

        do{
            goals.add(goal(clauseEnv));
        }while(match(COMMA));

        return goals;
    }

    private Goal goal(Environment clauseEnv){
        // either a unification or a predicate call
        Term first = term(clauseEnv);

        // X = t (unification)
        if(match(EQUAL)){
            Term second = term(clauseEnv);

            return new Goal.Unification(first, second);
        }
        else{
            if(first instanceof Term.Struct){
                return new Goal.PredicateCall((Term.Struct) first);
            }
            else{
                throw new RuntimeException("Predicate call must be of the form p(t_1, ..., t_n)");
            }
        }
    }

    private Term term(Environment clauseEnv){

        String name = consume(IDENTIFIER, "Identifier expected.").lexeme;

        if(match(LEFT_PAREN)){

            List<Term> terms = new ArrayList<>();

            do{
                terms.add(term(clauseEnv));
            }while(match(COMMA));

            consume(RIGHT_PAREN, "Missing closing parentheses");

            return new Term.Struct(name, terms);
        }
        else {
            if (name.equals("_")) {
                return new Term.Anon();
            } else if (startsWithLowerCaseLetter(name)) {
                return new Term.Atom(name);
            } else {
                if (clauseEnv.has(name)) {
                    return new Term.Ref(name);
                } else {
                    clauseEnv.put(name);
                    return new Term.Var(name);
                }
            }
        }
    }

    private boolean startsWithLowerCaseLetter(String name){
        char c = name.charAt(0);
        return 'a' <= c && c <= 'z';
    }

    private Clause normalizeSimpleClause(Term.Struct clauseHead, Environment clauseEnv){

        List<Goal> unifications = new ArrayList<>();
        int auxiliaryVars = 0;

        Map<String, String> atomsToVars = new HashMap<>();

        for(int i = 0; i < clauseHead.terms.size(); i++){
            Term term = clauseHead.terms.get(i);

            // Make a unification out of this
            // human(socrates)
            // human(X) :- X = socrates
            if(term instanceof Term.Atom){

                // invent a new variable
                String varName;
                boolean isNewVar = false;
                if(atomsToVars.containsKey(((Term.Atom) term).atomName)){
                    varName = atomsToVars.get(((Term.Atom) term).atomName);
                }
                else{
                    varName = String.format("A_%d", ++auxiliaryVars);
                    isNewVar = true;
                    atomsToVars.put(((Term.Atom) term).atomName, varName);
                }

                // substitute variable into clause head
                if(isNewVar){
                    clauseHead.terms.set(i, new Term.Var(varName));

                    // create new unification
                    unifications.add(new Goal.Unification(new Term.Ref(varName), new Term.Atom(((Term.Atom) term).atomName)));
                }

                else{
                    // no new unification is needed
                    clauseHead.terms.set(i, new Term.Ref(varName));
                }
            }
        }

        // the clause head is changed now
        return new Clause(clauseHead, unifications);
    }
}
