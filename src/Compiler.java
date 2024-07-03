
import static compiler.GenerationMode.*;
import compiler.GenerationMode;

import java.util.HashSet;
import java.util.Set;

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

    public Code codeU(Term term){ return term.accept(this, U); }

    public Code codeG(Term term){
        return term.accept(this, G);
    }

    public Code codeG(Goal goal){
        return goal.accept(this, G);
    }

    public Code codeP(Predicate predicate){

        int n = predicate.clauses.size();

        if(n == 1){
            return codeC(predicate.clauses.get(0));
        }

        Code code = new Code();

        code.addInstruction(new Instr.SetBackTrackPoint());

        String[] jumpLabels = new String[n];

        for(int i = 0; i < n; i++){
            jumpLabels[i] = code.getNewJumpLabel();
        }

        // generate the try instructions
        for(int i = 0; i < n - 1; i++){
            code.addInstruction(new Instr.Try(jumpLabels[i]));
        }

        code.addInstruction(new Instr.DeleteBackTrackPoint());

        code.addInstruction(new Instr.Jump(jumpLabels[n - 1]));

        for(int i = 0; i < n; i++){
            Clause clause = predicate.clauses.get(i);

            code.addCode(codeC(clause), jumpLabels[i]);
        }

        return code;
    }

    public Code codeC(Clause clause){

        Code code = new Code();

        // a new environment for every clause
        env = new Environment();

        // add all the variables to the environment
        addVarTermsToEnv(clause.clauseHead);

        for(Goal goal : clause.goals){
            addVarTermsToEnv(goal);
        }

        code.addInstruction(new Instr.PushEnv(env.size()));

        for(Goal goal : clause.goals){
            code.addCode(codeG(goal));
        }

        code.addInstruction(new Instr.PopEnv());

        return code;
    }

    public Code code(Program program){
        Code code = new Code();

        String labelGoalFailed = code.getNewJumpLabel();

        code.addInstruction(new Instr.Init(labelGoalFailed));

        // d is the free parameters in the goal
        // we want to print those
        int d = getFreeVars(program.query);

        code.addInstruction(new Instr.PushEnv(d));

        code.addCode(codeG(program.query));

        code.addInstruction(new Instr.Halt(d));

        code.addInstruction(new Instr.No(), labelGoalFailed);

        // now translate all the predicates
        for(Predicate predicate : program.predicates){
            code.addCode(codeP(predicate));
        }

        return code;
    }

    private int getFreeVars(Goal goal){
        env = new Environment();

        addVarTermsToEnv(goal);

        return env.size();
    }

    private void addVarTermsToEnv(Term term){
        if(term instanceof Term.Var){
            env.put(((Term.Var) term).varName);
        }
        else if(term instanceof Term.Struct){
            for(Term subTerm : ((Term.Struct) term).terms){
                addVarTermsToEnv(subTerm);
            }
        }
    }

    private void addVarTermsToEnv(Goal goal){
        if(goal instanceof Goal.PredicateCall){
            addVarTermsToEnv(((Goal.PredicateCall) goal).struct);
        }
        else if(goal instanceof Goal.Unification){
            addVarTermsToEnv(((Goal.Unification) goal).leftHandSide);
            addVarTermsToEnv(((Goal.Unification) goal).rightHandSide);
        }
    }

    @Override
    public Code visitAtom(Term.Atom atom, GenerationMode mode) {

        checkGenMode(mode, A, U);

        Code code = new Code();

        if(mode == A) {
            code.addInstruction(new Instr.PutAtom(atom.atomName));
        }
        else if(mode == U){
            code.addInstruction(new Instr.UAtom(atom.atomName));
        }
        return code;
    }

    @Override
    public Code visitVar(Term.Var var, GenerationMode mode) {

        checkGenMode(mode, A, U);

        Code code = new Code();

        /*
        if(!env.has(var.varName)){
            env.put(var.varName);
        }
        */

        if(mode == A) {
            code.addInstruction(new Instr.PutVar(env.get(var.varName)));
        }
        else if(mode == U){
            code.addInstruction(new Instr.UVar(env.get(var.varName)));
        }

        return code;
    }

    @Override
    public Code visitAnon(Term.Anon anon, GenerationMode mode) {

        checkGenMode(mode, A, U);

        Code code = new Code();

        if(mode == A) {
            code.addInstruction(new Instr.PutAnon());
        }
        else if(mode == U){
            code.addInstruction(new Instr.Pop());
        }
        return code;
    }

    @Override
    public Code visitRef(Term.Ref ref, GenerationMode mode) {

        checkGenMode(mode, A, U);

        Code code = new Code();

        // for a ref, the variable name must already be in the environment
        if(mode == A) {
            code.addInstruction(new Instr.PutRef(env.get(ref.refName)));
        }
        else if(mode == U){
            code.addInstruction(new Instr.URef(env.get(ref.refName)));
        }

        return code;
    }

    @Override
    public Code visitStruct(Term.Struct struct, GenerationMode mode) {

        checkGenMode(mode, A, G, U);

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
        else if(mode == U){

            String jumpToCreation = code.getNewJumpLabel();
            String jumpOverCreation = code.getNewJumpLabel();

            int arity = struct.terms.size();

            code.addInstruction(new Instr.UStruct(struct.structName, arity, jumpToCreation));

            for(int i = 0; i  < arity; i++){
                code.addInstruction(new Instr.Son(i + 1));
                code.addCode(codeU(struct.terms.get(i)));
            }

            code.addInstruction(new Instr.Up(jumpOverCreation));
            code.setJumpLabelAtEnd(jumpToCreation);

            // now, the creation part begins
            check(code, initVars(struct));

            code.addCode(codeA(struct));

            code.addInstruction(new Instr.Bind());

            code.setJumpLabelAtEnd(jumpOverCreation);
        }
        return code;
    }

    // generates check instructions for all refs
    private void check(Code code, Set<Term.Ref> refs){
        for(Term.Ref ref : refs){
            code.addInstruction(new Instr.Check(env.get(ref.refName)));
        }
    }

    private Set<Term.Ref> initVars(Term.Struct struct){
        Set<Term.Ref> initializedVars = new HashSet<>();

        for(Term subTerm : struct.terms){
            if(subTerm instanceof Term.Ref){
                initializedVars.add((Term.Ref) subTerm);
            }
            else if(subTerm instanceof Term.Struct){
                initializedVars.addAll(initVars((Term.Struct) subTerm));
            }
        }
        return initializedVars;
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
                code.addCode(codeA(unification.leftHandSide));

                // this is the optimized stuff
                code.addCode(codeU(unification.rightHandSide));
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
