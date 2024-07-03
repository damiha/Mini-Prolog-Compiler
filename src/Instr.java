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

    static class UAtom extends Instr{
        String atomName;

        public UAtom(String atomName){
            this.atomName = atomName;
        }

        public String toString(){
            return String.format("UAtom %s", atomName);
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

    static class UVar extends Instr{

        // this is an address relative to the current frame pointer
        int relativeAddress;

        public UVar(int relativeAddress){
            this.relativeAddress = relativeAddress;
        }

        public String toString(){
            return String.format("UVar %d", relativeAddress);
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

    static class URef extends Instr{

        // this is an address relative to the current frame pointer
        int relativeAddress;

        public URef(int relativeAddress){
            this.relativeAddress = relativeAddress;
        }

        public String toString(){
            return String.format("URef %s", relativeAddress);
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

    static class Pop extends Instr{

        public Pop(){}

        public String toString(){
            return "Pop";
        }
    }

    static class Up extends Instr{

        String jumpLabel;

        public Up(String jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        public String toString(){
            return String.format("Up %s", jumpLabel);
        }
    }

    static class PushEnv extends Instr{
        int m;

        public PushEnv(int m){
            this.m = m;
        }

        public String toString(){
            return String.format("PushEnv %d", m);
        }
    }

    static class PopEnv extends Instr{

        public String toString(){
            return "PopEnv";
        }
    }

    static class SetBackTrackPoint extends Instr{

        public String toString(){
            return "SetBackTrackPoint";
        }
    }

    static class DeleteBackTrackPoint extends Instr{

        public String toString(){
            return "DeleteBackTrackPoint";
        }
    }

    static class Try extends Instr{
        String jumpLabel;

        public Try(String jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        public String toString(){
            return String.format("Try %s", jumpLabel);
        }
    }

    static class Init extends Instr{
        String jumpLabel;

        public Init(String jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        public String toString(){
            return String.format("Init %s", jumpLabel);
        }
    }

    static class Halt extends Instr{
        int d;

        public Halt(int d){
            this.d = d;
        }

        public String toString(){
            return String.format("Halt %d", d);
        }
    }

    static class No extends Instr{

        public String toString(){
            return "No";
        }
    }

    static class Jump extends Instr{
        String jumpLabel;

        public Jump(String jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        public String toString(){
            return String.format("Jump %s", jumpLabel);
        }
    }

    static class Son extends Instr{

        int i;

        public Son(int i){
            this.i = i;
        }

        public String toString(){
            return String.format("Son %d", i);
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

    static class UStruct extends Instr{

        String functionName;
        int arity;
        String jumpLabel;

        public UStruct(String functionName, int arity, String jumpLabel){
            this.functionName = functionName;
            this.arity = arity;
            this.jumpLabel = jumpLabel;
        }

        public String toString(){
            return String.format("UStruct %s/%d %s", functionName, arity, jumpLabel);
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

    static class Bind extends Instr{

        @Override
        public String toString(){
            return "Bind";
        }
    }

    static class Check extends Instr{

        int i;

        public Check(int i){
            this.i = i;
        }

        @Override
        public String toString(){
            return String.format("Check %d", i);
        }
    }

    static class Fail extends Instr{

        @Override
        public String toString(){
            return "Fail";
        }
    }

    static class Unify extends Instr{
        @Override
        public String toString(){
            return "Unify";
        }
    }
}
