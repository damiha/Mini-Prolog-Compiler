import lexer.TokenType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        }

        // question mark has been consumed
        Goal query = predicateCall();

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

        ClauseHead clauseHead = clauseHead(clauseEnv);

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

    private ClauseHead clauseHead(Environment clauseEnv){

        Token headName = consume(IDENTIFIER, "A clause must start with a clause head (needs name).");

        // if this is empty, no unification necessary and the statement is simply true
        // good_weather. would just be a synonym for true
        List<Term> headVars = new ArrayList<>();


        if(match(LEFT_PAREN)){

            // has to  have minimum one parameter
            // we don't allow good_weather().

            do{
                // in the clause head, they can't be nested
                Token name = consume(IDENTIFIER, "clause head needs parameter. Cannot be ()");


            }while(match(COMMA));

            consume(RIGHT_PAREN, "Missing closing ).");
        }

        return new ClauseHead(headName.lexeme, headVars);
    }

    // parse as many goals as you can before the dot comes
    private List<Goal> goals(Environment clauseEnv){
        return null;
    }

    private boolean startsWithLowerCaseLetter(String name){
        return false;
    }

    private Clause normalizeSimpleClause(ClauseHead clauseHead, Environment clauseEnv){
        return null;
    }

    private Goal predicateCall(){
        return null;
    }
}
