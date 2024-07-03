import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    @Test
    public void testParser1(){
        String source = """
                mortal(X) :- human(X).
                human(socrates).
                ?mortal(socrates).
                """;

        Lexer lexer = new Lexer(source);

        Parser parser = new Parser(lexer.getTokens());

        Program program = parser.parse();

        // parsed result looks good
    }

    @Test
    public void testParser2(){
        String source = """
                is_cooler(tony_hawk, X).
                ?
                is_cooler(tony_hawk, me).
                """;

        Lexer lexer = new Lexer(source);

        Parser parser = new Parser(lexer.getTokens());

        Program program = parser.parse();

        // parsed result looks good
        System.out.println();
    }

    @Test
    public void testParser3(){
        String source = """
                is_equal(one, one).
                ?
                is_equal(X, X).
                """;

        Lexer lexer = new Lexer(source);

        Parser parser = new Parser(lexer.getTokens());

        Program program = parser.parse();

        // parsed result looks good
        System.out.println();
    }

    @Test
    public void testParserWholeProgram(){
        String source = """
                t(X) :- X = b.
                p(X) :- q(X), t(X).
                q(X) :- s(X).
                s(X) :- t(X).
                s(X) :- X = a.
                ?p.
                """;

        Lexer lexer = new Lexer(source);

        Parser parser = new Parser(lexer.getTokens());

        Program program = parser.parse();

        // parsed result looks good
        System.out.println();
    }
}