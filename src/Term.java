import java.util.List;

public class Term {

    static class Atom extends Term{

        String atomName;

        public Atom(String atomName){
            this.atomName = atomName;
        }
    }

    static class Anon extends Term{

    }

    static class Ref extends Term{
        String refName;

        public Ref(String refName){
            this.refName = refName;
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
    }
}
