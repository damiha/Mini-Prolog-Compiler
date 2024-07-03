import java.util.List;
import compiler.GenerationMode;

abstract class Term {

    abstract <T> T accept(Visitor<T> visitor, GenerationMode mode);

    static class Atom extends Term{

        String atomName;

        public Atom(String atomName){
            this.atomName = atomName;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitAtom(this, mode);
        }
    }

    static class Anon extends Term{

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitAnon(this, mode);
        }
    }

    static class Var extends Term{
        String varName;

        public Var(String refName){
            this.varName = refName;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitVar(this, mode);
        }
    }

    static class Ref extends Term{
        String refName;

        public Ref(String refName){
            this.refName = refName;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitRef(this, mode);
        }
    }

    // X = f(Y, Z)
    // this is a unification so the right hand side is a term

    // if it was X :- f(Y, Z)
    // this would make the right hand side a goal, and we wouldn't use codeA but codeG

    // if we have bigger(elephant, horse)
    // we really have bigger(X, Y) :- X = elephant, Y = horse
    // so in this case, bigger is also a predicate
    static class Struct extends Term {
        String structName;

        List<Term> terms;

        public Struct(String structName, List<Term> terms){
            this.structName = structName;
            this.terms = terms;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitStruct(this,  mode);
        }
    }

    interface Visitor<T>{
        T visitAtom(Atom atom, GenerationMode mode);
        T visitVar(Var var, GenerationMode mode);
        T visitAnon(Anon anon, GenerationMode mode);
        T visitRef(Ref ref, GenerationMode mode);
        T visitStruct(Struct struct, GenerationMode mode);
    }
}
