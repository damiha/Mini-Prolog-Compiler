public abstract class HeapElement {

    abstract String getOutputRepresentation();

    static class Atom extends HeapElement{

        String atomName;

        public Atom(String atomName){
            this.atomName = atomName;
        }

        public String toString(){
            return String.format("(A, %s)", atomName);
        }

        @Override
        String getOutputRepresentation() {
            return atomName;
        }
    }

    static class Variable extends HeapElement{
        int pointsToHeapAddress;

        public Variable(int pointsToHeapAddress){
            this.pointsToHeapAddress = pointsToHeapAddress;
        }

        public String toString(){
            return String.format("(R, points_to: %d", pointsToHeapAddress);
        }

        @Override
        String getOutputRepresentation() {

            // TODO: change this
            return toString();
        }
    }

    static class StructHead extends HeapElement{
        int arity;
        String structName;

        public StructHead(String structName, int arity){
            this.structName = structName;
            this.arity = arity;
        }

        public String toString(){
            return String.format("(S, %s/%d)", structName, arity);
        }

        @Override
        String getOutputRepresentation() {
            // TODO: change this
            return toString();
        }
    }

    static class StructCell extends HeapElement{

        int i;
        public StructCell(int i){
            this.i = i;
        }

        public String toString(){
            return "" + i;
        }

        @Override
        String getOutputRepresentation() {
            return "";
        }
    }
}
