import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import static lexer.TokenType.*;

public class LexerTest {

    @Test
    public void testLexer1(){

        String source = """
                mortal(X) :- human(X).
                human(socrates).
                ?mortal(socrates).
                """;

        Lexer lexer = new Lexer(source);

        List<Token> parsedTokens = lexer.getTokens();

        List<Token> expectedTokens = new ArrayList(List.of(
                new Token(0, IDENTIFIER, "mortal", null),
                new Token(0, LEFT_PAREN, "(", null),
                new Token(0, IDENTIFIER, "X", null),
                new Token(0, RIGHT_PAREN, ")", null),
                new Token(0, IMPLIED_BY, ":-", null),
                new Token(0, IDENTIFIER, "human", null),
                new Token(0, LEFT_PAREN, "(", null),
                new Token(0, IDENTIFIER, "X", null),
                new Token(0, RIGHT_PAREN, ")", null),
                new Token(0, DOT, ".", null),
                new Token(0, IDENTIFIER, "human", null),
                new Token(0, LEFT_PAREN, "(", null),
                new Token(0, IDENTIFIER, "socrates", null),
                new Token(0, RIGHT_PAREN, ")", null),
                new Token(0, DOT, ".", null),
                new Token(0, QUESTION_MARK, "?", null),
                new Token(0, IDENTIFIER, "mortal", null),
                new Token(0, LEFT_PAREN, "(", null),
                new Token(0, IDENTIFIER, "socrates", null),
                new Token(0, RIGHT_PAREN, ")", null),
                new Token(0, DOT, ".", null),
                new Token(0, EOF, "", null)
        ));

        assertEquals(expectedTokens, parsedTokens);
    }
}