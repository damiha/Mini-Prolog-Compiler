public class Instr {

    static class PutAtom{

        String atomName;

        public PutAtom(String atomName){
            this.atomName = atomName;
        }

        public String toString(){
            return String.format("PutAtom %s", atomName);
        }
    }

    static class PutVar{

        // this is an address relative to the current frame pointer
        int relativeAddress;

        public PutVar(int relativeAddress){
            this.relativeAddress = relativeAddress;
        }

        public String toString(){
            return String.format("PutVar %d", relativeAddress);
        }
    }

    static class PutRef{

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
    static class PutAnon{

        public PutAnon(){}

        public String toString(){
            return "PutAnon";
        }
    }

    static class PutStruct{

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

}
