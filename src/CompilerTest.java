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
}