import lexer.TokenType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Lexer {

    String source;
    int current = 0;
    int start = 0;
    int line = 0;

    List<Token> tokens;

    Map<String, TokenType> keywordToToken;

    public Lexer(String source){
        this.source = source;
        tokens = new ArrayList<>();

        // so far, no keywords
        keywordToToken = new HashMap<>();
    }

    public List<Token> getTokens(){

        while(!isAtEnd()){

            char c = advance();

            boolean cAlreadyMatched = false;

            switch(c){
                case ' ':
                    break;
                case '\n':
                    line++;
                    break;
                case '%':
                    // a prolog comment is initiated by a slash
                    comment();
                    break;
                case '(':
                    addToken(TokenType.LEFT_PAREN);
                    break;
                case ')':
                    addToken(TokenType.RIGHT_PAREN);
                    break;
                case ';':
                    addToken(TokenType.SEMICOLON);
                    break;
                case ',':
                    addToken(TokenType.COMMA);
                    break;
                case '.':
                    addToken(TokenType.DOT);
                    break;
                case ':':
                    if(match('-')){
                        addToken(TokenType.IMPLIED_BY);
                    }
                    else{
                        throw new RuntimeException(":- expected.");
                    }
                    break;
                case '\\':

                    // \+ is prolog syntax for not
                    if(match('+')){
                        addToken(TokenType.NOT);
                    }
                    else if(match('=')){
                        addToken(TokenType.NOT_EQUAL);
                    }
                    else{
                        throw new RuntimeException("\\ expects either + or =.");
                    }
                    break;
                case '?':
                    addToken(TokenType.QUESTION_MARK);
                    break;
                case '=':
                    addToken(TokenType.EQUAL);
                    break;

            }

            // for identifies (can be keywords)
            if (isAlpha(c)) {

                // either keyword or variable
                identifier();
            } else if (isNumeric(c)) {
                number();
            }
        }

        tokens.add(new Token(line, TokenType.EOF, "", null));

        return tokens;
    }

    private void number(){
        while(isNumeric(peek())){
            advance();
        }

        addToken(TokenType.NUMBER);
    }

    private void identifier(){
        while(isAlphaNumeric(peek())){
            advance();
        }

        addToken(keywordToToken.getOrDefault(getLexeme(), TokenType.IDENTIFIER));
    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isNumeric(c);
    }

    private boolean isNumeric(char c){
        return '0' <= c && c <= '9';
    }

    private void comment(){
        while(!isAtEnd() && peek() != '\n'){
            advance();
        }
        start = current;
    }

    private boolean isAlpha(char c){
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || (c == '_');
    }

    private String getLexeme(){
        return source.substring(start, current).strip();
    }

    private void addToken(TokenType type){

        String lexeme = getLexeme();

        Object value = null;

        if(type == TokenType.NUMBER){
            value = Integer.parseInt(lexeme);
        }

        tokens.add(new Token(line, type, lexeme, value));

        // we successfully added a token
        start = current;
    }

    private char advance(){
        return source.charAt(current++);
    }

    private char peek(){
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private boolean match(char c){
        if(peek() == c){
            advance();
            return true;
        }
        return false;
    }

    private boolean isAtEnd(){
        return current >= source.length();
    }
}
