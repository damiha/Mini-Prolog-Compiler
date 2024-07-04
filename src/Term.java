import java.util.List;
import compiler.GenerationMode;

abstract class Term {

    abstract <T> T accept(Visitor<T> visitor, GenerationMode mode);

    static class Atom extends Term{

        String atomName;

        public Atom(String atomName){
            this.atomName = atomName;
        }

        public String toString(){
            return atomName;
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

        public String toString(){
            return "_";
        }
    }

    static class Var extends Term{
        String varName;

        public Var(String varName){
            this.varName = varName;
        }

        public String toString(){
            return varName;
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

        public String toString(){
            return refName;
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

        public String toString(){
            StringBuilder res = new StringBuilder(structName);

            if(!terms.isEmpty()){
                res.append("(");

                for(int i = 0; i < terms.size(); i++){
                    res.append(terms.get(i).toString()).append(i < terms.size() - 1 ? ", " : "");
                }

                res.append(")");
            }
            return res.toString();
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
