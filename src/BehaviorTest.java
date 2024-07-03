import org.junit.Test;

public class BehaviorTest {

    @Test
    public void testExampleUnoptimized1(){
        String source = """
                t(X) :- X = b.
                p :- q(X), t(X).
                q(X) :- s(X).
                s(X) :- t(X).
                s(X) :- X = a.
                ?p.
                """;

        Lexer lexer = new Lexer(source);

        Parser parser = new Parser(lexer.getTokens());

        Program program = parser.parse();

        Compiler compiler = new Compiler();
        compiler.isUnificationOptimized = false;

        Code compiled = compiler.code(program);

        VirtualMachine vm = new VirtualMachine();

        vm.run(compiled);

    }

    @Test
    public void testExampleUnoptimized2(){

        // who is mortal?
        String source = """
                mortal(X) :- human(X).
                human(socrates).
                human(aristotle).
                ?mortal(X).
                """;

        Lexer lexer = new Lexer(source);

        Parser parser = new Parser(lexer.getTokens());

        Program program = parser.parse();

        Compiler compiler = new Compiler();
        compiler.isUnificationOptimized = false;

        Code compiled = compiler.code(program);

        VirtualMachine vm = new VirtualMachine();
        vm.setNoWaitForUser();

        vm.run(compiled);


    }
}
