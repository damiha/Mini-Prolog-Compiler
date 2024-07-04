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
    public void testExampleOptimized1(){
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
        compiler.isUnificationOptimized = true;

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

    @Test
    public void testExampleOptimized2(){

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
        compiler.isUnificationOptimized = true;

        Code compiled = compiler.code(program);

        VirtualMachine vm = new VirtualMachine();
        vm.setNoWaitForUser();

        vm.run(compiled);
    }

    @Test
    public void testExampleUnoptimized3(){

        // who is mortal?
        String source = """
                sibling(mary, john).
                sibling(X, Y) :- sibling(Y, X).
                ?sibling(john, mary).
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

    @Test
    public void testExampleOptimized3(){

        // who is mortal?
        String source = """
                sibling(mary, john).
                sibling(X, Y) :- sibling(Y, X).
                ?sibling(john, mary).
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

    @Test
    public void testExampleUnoptimized4(){

        // who is mortal?
        String source = """
                father(john, jack).
                mother(mary, jack).
                father(jack, james).
                parent(X, Y) :- father(X, Y).
                parent(X, Y) :- mother(X, Y).
                ancestor(X, Y) :- parent(X, Y).
                ancestor(X, Y) :- parent(X, Z), ancestor(Z, Y).
                ?ancestor(john, X).
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

    @Test
    public void testExampleOptimized4(){

        // who is mortal?
        String source = """
                father(john, jack).
                mother(mary, jack).
                father(jack, james).
                parent(X, Y) :- father(X, Y).
                parent(X, Y) :- mother(X, Y).
                ancestor(X, Y) :- parent(X, Y).
                ancestor(X, Y) :- parent(X, Z), ancestor(Z, Y).
                ?ancestor(X, james).
                """;

        Lexer lexer = new Lexer(source);

        Parser parser = new Parser(lexer.getTokens());

        Program program = parser.parse();

        Compiler compiler = new Compiler();
        compiler.isUnificationOptimized = true;

        Code compiled = compiler.code(program);

        VirtualMachine vm = new VirtualMachine();
        vm.setNoWaitForUser();

        vm.run(compiled);
    }
}
