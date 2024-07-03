public class Instr {

    static class PutAtom extends Instr{

        String atomName;

        public PutAtom(String atomName){
            this.atomName = atomName;
        }

        public String toString(){
            return String.format("PutAtom %s", atomName);
        }
    }

    static class PutVar extends Instr{

        // this is an address relative to the current frame pointer
        int relativeAddress;

        public PutVar(int relativeAddress){
            this.relativeAddress = relativeAddress;
        }

        public String toString(){
            return String.format("PutVar %d", relativeAddress);
        }
    }

    static class PutRef extends Instr{

        // this is an address relative to the current frame pointer
        int relativeAddress;

        public PutRef(int relativeAddress){
            this.relativeAddress = relativeAddress;
        }

        public String toString(){
            return String.format("PutRef %s", relativeAddress);
        }
    }

    // you might have
    // mortal(sokrates)
    // and you want to query mortal(_) just to see that the predicate is satisfiable
    static class PutAnon extends Instr{

        public PutAnon(){}

        public String toString(){
            return "PutAnon";
        }
    }

    static class PutStruct extends Instr{

        String functionName;
        int arity;

        public PutStruct(String functionName, int arity){
            this.functionName = functionName;
            this.arity = arity;
        }

        public String toString(){
            return String.format("PutStruct %s/%d", functionName, arity);
        }
    }

    static class Mark extends Instr{
        String jumpLabel;

        public Mark(String jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        public String toString(){
            return String.format("Mark %s", jumpLabel);
        }
    }

    static class Call extends Instr{
        String functionName;
        int arity;

        public Call(String functionName, int arity){
            this.functionName = functionName;
            this.arity = arity;
        }

        public String toString(){
            return String.format("Call %s/%d", functionName, arity);
        }
    }
}
