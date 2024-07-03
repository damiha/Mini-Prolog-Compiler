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
}