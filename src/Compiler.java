
import static compiler.GenerationMode.*;
import compiler.GenerationMode;

public class Compiler implements Term.Visitor<Code> {

    Environment env;
    public Compiler(){

        // this environment may change
        env = new Environment();
    }

    public Code codeA(Term term){
        return term.accept(this, A);
    }

    public Code codeG(Term term){
        return term.accept(this, G);
    }


    @Override
    public Code visitAtom(Term.Atom atom, GenerationMode mode) {

        checkGenMode(mode, A);

        Code code = new Code();
        code.addInstruction(new Instr.PutAtom(atom.atomName));
        return code;
    }

    @Override
    public Code visitVar(Term.Var var, GenerationMode mode) {

        checkGenMode(mode, A);

        Code code = new Code();

        if(!env.has(var.varName)){
            env.put(var.varName);
        }

        code.addInstruction(new Instr.PutVar(env.get(var.varName)));

        return code;
    }

    @Override
    public Code visitAnon(Term.Anon anon, GenerationMode mode) {

        checkGenMode(mode, A);

        Code code = new Code();
        code.addInstruction(new Instr.PutAnon());
        return code;
    }

    @Override
    public Code visitRef(Term.Ref ref, GenerationMode mode) {

        checkGenMode(mode, A);

        Code code = new Code();

        // for a ref, the variable name must already be in the environment
        code.addInstruction(new Instr.PutRef(env.get(ref.refName)));

        return code;
    }

    @Override
    public Code visitStruct(Term.Struct struct, GenerationMode mode) {

        checkGenMode(mode, A, G);

        Code code = new Code();

        if(mode == A) {

            for (Term term : struct.terms) {
                code.addCode(codeA(term));
            }

            int arity = struct.terms.size();

            code.addInstruction(new Instr.PutStruct(struct.structName, arity));
        }
        else if(mode == G){

            String continueAfterCallLabel = code.getNewJumpLabel();

            code.addInstruction(new Instr.Mark(continueAfterCallLabel));

            for (Term term : struct.terms) {
                code.addCode(codeA(term));
            }

            int arity = struct.terms.size();

            code.addInstruction(new Instr.Call(struct.structName, arity));

            code.setJumpLabelAtEnd(continueAfterCallLabel);
        }
        return code;
    }

    private void checkGenMode(GenerationMode mode, GenerationMode... allowedModes){
        for(GenerationMode allowedMode : allowedModes){
            if(allowedMode == mode){
                return;
            }
        }
        throw new RuntimeException(String.format("GenerationMode %s is not allowed.", mode));
    }
}
