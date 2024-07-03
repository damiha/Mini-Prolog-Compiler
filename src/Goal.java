public class Goal {

    static class PredicateCall extends Goal{

        Term.Struct struct;

        public PredicateCall(Term.Struct struct){
            this.struct = struct;
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
    }
}
