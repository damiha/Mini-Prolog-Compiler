
import static compiler.GenerationMode.*;
import compiler.GenerationMode;

public class Compiler implements Term.Visitor<Code>, Goal.Visitor<Code> {

    // if true, uses codeU for the right hand side of the unification
    boolean isUnificationOptimized = false;

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

    public Code codeG(Goal goal){
        return goal.accept(this, G);
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

    @Override
    public Code visitPredicateCall(Goal.PredicateCall predicateCall, GenerationMode mode) {

        checkGenMode(mode, G);

        return codeG(predicateCall.struct);
    }

    @Override
    public Code visitUnification(Goal.Unification unification, GenerationMode mode) {

        checkGenMode(mode, G);

        Code code = new Code();

        if(unification.leftHandSide instanceof Term.Var){
            // unbound variable, we need to bind
            if(isContainedIn((Term.Var) unification.leftHandSide, unification.rightHandSide, 0)){
                code.addInstruction(new Instr.Fail());
            }
            else{
                code.addCode(codeA(unification.leftHandSide));
                code.addCode(codeA(unification.rightHandSide));

                // binds the unbound variable to the right hand side
                code.addInstruction(new Instr.Bind());
            }
        }
        else if(unification.leftHandSide instanceof Term.Ref){
            // bound variable, we need to unify
            if(isUnificationOptimized){
                throw new RuntimeException("Not implemented yet.");
            }
            else{
                code.addCode(codeA(unification.leftHandSide));
                code.addCode(codeA(unification.rightHandSide));
                code.addInstruction(new Instr.Unify());
            }
        }
        return code;
    }

    // layer = 0 (top layer), no problem
    // X = X
    private boolean isContainedIn(Term.Var var, Term rightHandSide, int layer){
        // X = X or X = Y is allowed
        // X = f(X) is not allowed
        if(rightHandSide instanceof Term.Struct){

            for(Term term : ((Term.Struct) rightHandSide).terms){
                if(isContainedIn(var, term, layer + 1)){
                    return true;
                }
            }
            return false;
        }
        else if(layer > 0 && rightHandSide instanceof Term.Var){
            return var.varName.equals(((Term.Var) rightHandSide).varName);
        }
        else if(layer > 0 && rightHandSide instanceof Term.Ref){
            return var.varName.equals(((Term.Ref) rightHandSide).refName);
        }
        else{
            return false;
        }
    }
}
