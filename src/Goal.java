
import compiler.GenerationMode;

public abstract class Goal {

    abstract <T> T accept(Visitor<T> visitor, GenerationMode mode);

    static class PredicateCall extends Goal{

        Term.Struct struct;

        public PredicateCall(Term.Struct struct){
            this.struct = struct;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitPredicateCall(this, mode);
        }
    }

    static class Unification extends Goal{
        Term leftHandSide;
        Term rightHandSide;

        // X = t
        public Unification(Term leftHandSide, Term rightHandSide){
            this.leftHandSide = leftHandSide;
            this.rightHandSide = rightHandSide;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitUnification(this, mode);
        }
    }

    interface Visitor<T> {
        T visitPredicateCall(PredicateCall predicateCall, GenerationMode mode);
        T visitUnification(Unification unification, GenerationMode mode);
    }
}
