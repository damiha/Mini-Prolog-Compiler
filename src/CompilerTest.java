import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CompilerTest {

    @Test
    public void testTermCompile1 (){
        Compiler compiler = new Compiler();
        compiler.env.put("X");
        compiler.env.put("Y");
        compiler.env.put("Z");

        Term struct = new Term.Struct("f", List.of(
                new Term.Struct("g", List.of(new Term.Ref("X"), new Term.Var("Y"))),
                new Term.Atom("a"),
                new Term.Var("Z")
        ));

        Code compiled = compiler.codeA(struct);

        Code expected = new Code();
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.PutVar(2));
        expected.addInstruction(new Instr.PutStruct("g", 2));
        expected.addInstruction(new Instr.PutAtom("a"));
        expected.addInstruction(new Instr.PutVar(3));
        expected.addInstruction(new Instr.PutStruct("f",3));

        assertEquals(expected.toString(), compiled.toString());
    }

    @Test
    public void testTermCompile2(){
        Compiler compiler = new Compiler();
        compiler.env.put("X");
        compiler.env.put("Y");

        Term struct = new Term.Struct("p", List.of(
                new Term.Atom("a"),
                new Term.Var("X"),
                new Term.Struct("g", List.of(new Term.Ref("X"), new Term.Var("Y")))
        ));

        Code compiled = compiler.codeG(struct);

        Code expected = new Code();
        expected.addInstruction(new Instr.Mark("_0"));
        expected.addInstruction(new Instr.PutAtom("a"));
        expected.addInstruction(new Instr.PutVar(1));
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.PutVar(2));
        expected.addInstruction(new Instr.PutStruct("g",2));
        expected.addInstruction(new Instr.Call("p",3));
        expected.setJumpLabelAtEnd("_0");

        assertEquals(expected.toString(), compiled.toString());
    }



    @Test
    public void testUnoptUnification1(){
        Compiler compiler = new Compiler();
        compiler.env.put("X");
        compiler.env.put("Y");
        compiler.env.put("Z");
        compiler.env.put("U");

        Goal.Unification unification = new Goal.Unification(
                new Term.Ref("U"),
                new Term.Struct("f", List.of(
                        new Term.Struct("g", List.of(new Term.Ref("X"), new Term.Var("Y"))),
                        new Term.Atom("a"),
                        new Term.Var("Z")
                ))
        );

        Code compiled = compiler.codeG(unification);

        Code expected = new Code();
        expected.addInstruction(new Instr.PutRef(4));
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.PutVar(2));
        expected.addInstruction(new Instr.PutStruct("g", 2));
        expected.addInstruction(new Instr.PutAtom("a"));
        expected.addInstruction(new Instr.PutVar(3));
        expected.addInstruction(new Instr.PutStruct("f", 3));
        expected.addInstruction(new Instr.Unify());

        assertEquals(expected.toString(), compiled.toString());
    }

    @Test
    public void testUnoptUnify2(){
        Compiler compiler = new Compiler();
        compiler.env.put("X");
        compiler.env.put("Y");
        compiler.env.put("Z");
        compiler.env.put("U");

        Goal.Unification unification = new Goal.Unification(
                new Term.Var("X"),
                new Term.Struct("f", List.of(
                        new Term.Struct("g", List.of(new Term.Ref("X"), new Term.Var("Y"))),
                        new Term.Atom("a"),
                        new Term.Var("Z")
                ))
        );

        Code compiled = compiler.codeG(unification);

        Code expected = new Code();
        expected.addInstruction(new Instr.Fail());

        assertEquals(expected.toString(), compiled.toString());
    }

    @Test
    public void testUnoptUnify3(){
        Compiler compiler = new Compiler();
        compiler.env.put("X");
        compiler.env.put("Y");
        compiler.env.put("Z");
        compiler.env.put("U");

        Goal.Unification unification = new Goal.Unification(
                new Term.Var("X"),
                new Term.Struct("f", List.of(
                        new Term.Ref("Y")
                ))
        );

        Code compiled = compiler.codeG(unification);

        Code expected = new Code();
        expected.addInstruction(new Instr.PutVar(1));
        expected.addInstruction(new Instr.PutRef(2));
        expected.addInstruction(new Instr.PutStruct("f", 1));
        expected.addInstruction(new Instr.Bind());

        assertEquals(expected.toString(), compiled.toString());
    }

    @Test
    public void testOptStructUnify(){
        Compiler compiler = new Compiler();
        compiler.env.put("X");
        compiler.env.put("Y");
        compiler.env.put("Z");

        Term.Struct struct = new Term.Struct("f", List.of(
                new Term.Struct("g", List.of(new Term.Ref("X"), new Term.Var("Y"))),
                new Term.Atom("a"),
                new Term.Var("Z")
        ));

        Code compiled = compiler.codeU(struct);

        Code expected = new Code();
        expected.addInstruction(new Instr.UStruct("f", 3, "_0"));
        expected.addInstruction(new Instr.Son(1));
        expected.addInstruction(new Instr.UStruct("g", 2, "_2"));
        expected.addInstruction(new Instr.Son(1));
        expected.addInstruction(new Instr.URef(1));
        expected.addInstruction(new Instr.Son(2));
        expected.addInstruction(new Instr.UVar(2));
        expected.addInstruction(new Instr.Up("_3"));

        expected.addInstruction(new Instr.Check(1), "_2");
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.PutVar(2));
        expected.addInstruction(new Instr.PutStruct("g", 2));
        expected.addInstruction(new Instr.Bind());

        expected.addInstruction(new Instr.Son(2), "_3");
        expected.addInstruction(new Instr.UAtom("a"));
        expected.addInstruction(new Instr.Son(3));
        expected.addInstruction(new Instr.UVar(3));
        expected.addInstruction(new Instr.Up("_1"));

        expected.addInstruction(new Instr.Check(1), "_0");
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.PutVar(2));
        expected.addInstruction(new Instr.PutStruct("g", 2));
        expected.addInstruction(new Instr.PutAtom("a"));
        expected.addInstruction(new Instr.PutVar(3));
        expected.addInstruction(new Instr.PutStruct("f", 3));
        expected.addInstruction(new Instr.Bind());

        expected.setJumpLabelAtEnd("_1");

        assertEquals(expected.toString(), compiled.toString());
    }

    @Test
    public void testCompileClause1(){

        // a(X, Y) <- f(X, X1), a(X1, Y)
        Clause clause = new Clause(
                new Term.Struct("a", List.of(new Term.Var("X"), new Term.Var("Y"))),
                List.of(
                        new Goal.PredicateCall(new Term.Struct("f", List.of(new Term.Ref("X"), new Term.Var("X1")))),
                        new Goal.PredicateCall(new Term.Struct("a", List.of(new Term.Ref("X1"), new Term.Ref("Y"))))
                )
        );

        Compiler compiler = new Compiler();

        Code compiled = compiler.codeC(clause);

        Code expected = new Code();

        expected.addInstruction(new Instr.PushEnv(3));
        expected.addInstruction(new Instr.Mark("_0"));
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.PutVar(3));
        expected.addInstruction(new Instr.Call("f", 2));
        expected.setJumpLabelAtEnd("_0");

        expected.addInstruction(new Instr.Mark("_1"));
        expected.addInstruction(new Instr.PutRef(3));
        expected.addInstruction(new Instr.PutRef(2));
        expected.addInstruction(new Instr.Call("a", 2));
        expected.setJumpLabelAtEnd("_1");

        expected.addInstruction(new Instr.PopEnv());

        assertEquals(expected.toString(), compiled.toString());
    }

    @Test
    public void testCompilePredicate1(){
        // s(X) :- t(X)
        // s(X) :- X = a

        Predicate predicate = new Predicate(List.of(
                new Clause(new Term.Struct("s", List.of(new Term.Var("X"))), List.of(new Goal.PredicateCall(new Term.Struct("t", List.of(new Term.Ref("X")))))),
                new Clause(new Term.Struct("s", List.of(new Term.Var("X"))), List.of(new Goal.Unification(new Term.Ref("X"), new Term.Atom("a"))))
        ));

        Compiler compiler = new Compiler();
        compiler.isUnificationOptimized = true;

        Code compiled = compiler.codeP(predicate);

        Code expected = new Code();

        expected.setPredicateLabelAtEnd("s/1");

        expected.addInstruction(new Instr.SetBackTrackPoint());
        expected.addInstruction(new Instr.Try("_0"));
        expected.addInstruction(new Instr.DeleteBackTrackPoint());
        expected.addInstruction(new Instr.Jump("_1"));

        expected.addInstruction(new Instr.PushEnv(1), "_0");
        expected.addInstruction(new Instr.Mark("_2"));
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.Call("t", 1));
        expected.addInstruction(new Instr.PopEnv(), "_2");

        expected.addInstruction(new Instr.PushEnv(1), "_1");
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.UAtom("a"));
        expected.addInstruction(new Instr.PopEnv());

        assertEquals(expected.toString(), compiled.toString());
    }

    @Test
    public void testCompileProgram1(){

        // t(X) :- X = b
        // p :- q(X), t(X)
        // q(X) :- s(X)
        // s(X) :- t(X)
        // s(X) :- X = a
        // ? p

        // expected behavior: true (X = b and rule s(X) :- t(X) has to be used)
        Program program = new Program(
                List.of(new Predicate(List.of(
                            new Clause(new Term.Struct("t", List.of(new Term.Var("X"))), List.of(new Goal.Unification(new Term.Ref("X"), new Term.Atom("b")))))),
                        new Predicate(List.of(new Clause(new Term.Struct("p", List.of()), List.of(
                                new Goal.PredicateCall(new Term.Struct("q", List.of(new Term.Var("X")))),
                                new Goal.PredicateCall(new Term.Struct("t", List.of(new Term.Ref("X"))))
                                )))),
                        new Predicate(List.of(
                                new Clause(new Term.Struct("q", List.of(new Term.Var("X"))), List.of(new Goal.PredicateCall(new Term.Struct("s", List.of(new Term.Ref("X"))))))
                )), new Predicate(List.of(
                        new Clause(new Term.Struct("s", List.of(new Term.Var("X"))), List.of(new Goal.PredicateCall(new Term.Struct("t", List.of(new Term.Ref("X")))))),
                        new Clause(new Term.Struct("s", List.of(new Term.Var("X"))), List.of(new Goal.Unification(new Term.Ref("X"), new Term.Atom("a"))))
                )
                )),
                new Goal.PredicateCall(new Term.Struct("p", List.of()))
        );

        Compiler compiler = new Compiler();
        compiler.isUnificationOptimized = true;

        Code compiled = compiler.code(program);

        Code expected = new Code();
        expected.addInstruction(new Instr.Init("_0"));
        expected.addInstruction(new Instr.PushEnv(0));

        // codeG query
        expected.addInstruction(new Instr.Mark("_1"));
        expected.addInstruction(new Instr.Call("p", 0));

        expected.addInstruction(new Instr.Halt(0, new String[0]), "_1");
        expected.addInstruction(new Instr.No(), "_0");

        expected.setPredicateLabelAtEnd("t/1");
        expected.addInstruction(new Instr.PushEnv(1));
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.UAtom("b"));
        expected.addInstruction(new Instr.PopEnv());

        expected.setPredicateLabelAtEnd("p/0");

        expected.addInstruction(new Instr.PushEnv(1));
        expected.addInstruction(new Instr.Mark("_2"));
        expected.addInstruction(new Instr.PutVar(1));
        expected.addInstruction(new Instr.Call("q", 1));

        expected.addInstruction(new Instr.Mark("_3"), "_2");
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.Call("t", 1));
        expected.addInstruction(new Instr.PopEnv(), "_3");

        expected.setPredicateLabelAtEnd("q/1");

        expected.addInstruction(new Instr.PushEnv(1));
        expected.addInstruction(new Instr.Mark("_4"));
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.Call("s", 1));

        expected.addInstruction(new Instr.PopEnv(), "_4");

        expected.setPredicateLabelAtEnd("s/1");

        expected.addInstruction(new Instr.SetBackTrackPoint());
        expected.addInstruction(new Instr.Try("_5"));
        expected.addInstruction(new Instr.DeleteBackTrackPoint());
        expected.addInstruction(new Instr.Jump("_6"));

        expected.addInstruction(new Instr.PushEnv(1), "_5");
        expected.addInstruction(new Instr.Mark("_7"));
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.Call("t", 1));

        expected.addInstruction(new Instr.PopEnv(), "_7");
        expected.addInstruction(new Instr.PushEnv(1), "_6");

        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.UAtom("a"));
        expected.addInstruction(new Instr.PopEnv());

        assertEquals(expected.toString(), compiled.toString());
    }

    @Test
    public void testCompileProgram2(){

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

        Code expected = new Code();
        expected.addInstruction(new Instr.Init("_0"));
        expected.addInstruction(new Instr.PushEnv(0));

        // codeG query
        expected.addInstruction(new Instr.Mark("_1"));
        expected.addInstruction(new Instr.Call("p", 0));

        expected.addInstruction(new Instr.Halt(0, new String[0]), "_1");
        expected.addInstruction(new Instr.No(), "_0");

        expected.setPredicateLabelAtEnd("t/1");
        expected.addInstruction(new Instr.PushEnv(1));
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.UAtom("b"));
        expected.addInstruction(new Instr.PopEnv());

        expected.setPredicateLabelAtEnd("p/0");

        expected.addInstruction(new Instr.PushEnv(1));
        expected.addInstruction(new Instr.Mark("_2"));
        expected.addInstruction(new Instr.PutVar(1));
        expected.addInstruction(new Instr.Call("q", 1));

        expected.addInstruction(new Instr.Mark("_3"), "_2");
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.Call("t", 1));
        expected.addInstruction(new Instr.PopEnv(), "_3");

        expected.setPredicateLabelAtEnd("q/1");

        expected.addInstruction(new Instr.PushEnv(1));
        expected.addInstruction(new Instr.Mark("_4"));
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.Call("s", 1));

        expected.addInstruction(new Instr.PopEnv(), "_4");

        expected.setPredicateLabelAtEnd("s/1");

        expected.addInstruction(new Instr.SetBackTrackPoint());
        expected.addInstruction(new Instr.Try("_5"));
        expected.addInstruction(new Instr.DeleteBackTrackPoint());
        expected.addInstruction(new Instr.Jump("_6"));

        expected.addInstruction(new Instr.PushEnv(1), "_5");
        expected.addInstruction(new Instr.Mark("_7"));
        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.Call("t", 1));

        expected.addInstruction(new Instr.PopEnv(), "_7");
        expected.addInstruction(new Instr.PushEnv(1), "_6");

        expected.addInstruction(new Instr.PutRef(1));
        expected.addInstruction(new Instr.UAtom("a"));
        expected.addInstruction(new Instr.PopEnv());

        assertEquals(expected.toString(), compiled.toString());
    }
}