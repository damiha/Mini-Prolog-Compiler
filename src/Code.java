import java.util.*;

public class Code {

    List<Instr> instructions;
    Map<String, Integer> jumpTable;

    Map<String, Integer> predicateTable;

    int jumpLabelsIssued = 0;

    public Code(){
        instructions = new ArrayList<>();
        jumpTable = new HashMap<>();
        predicateTable = new HashMap<>();
    }

    public void addInstruction(Instr instruction, String jumpLabel){

        if(jumpLabel != null) {
            jumpTable.put(jumpLabel, instructions.size());
        }

        instructions.add(instruction);
    }

    public void addInstruction(Instr instruction){
        addInstruction(instruction, null);
    }

    public String getNewJumpLabel(){
        return String.format("_%d", jumpLabelsIssued++);
    }

    private void mergeJumpTables(Code other){

        int offset = jumpLabelsIssued;
        int codeOffset = instructions.size();

        Map<String, String> oldToNewJumpLabels = new HashMap<>();

        for(int i = 0; i < other.jumpLabelsIssued; i++){
            String oldLabel = String.format("_%d", i);
            String newLabel = String.format("_%d", offset + i);

            // for changing the jump instructions to the new labels
            oldToNewJumpLabels.put(oldLabel, newLabel);

            jumpTable.put(newLabel, codeOffset + other.jumpTable.get(oldLabel));
        }

        // change the jump instructions

        // super important, common error source
        for(Instr instr : other.instructions){

            if(instr instanceof Instr.Mark){
                String oldLabel = ((Instr.Mark) instr).jumpLabel;
                ((Instr.Mark) instr).jumpLabel = oldToNewJumpLabels.get(oldLabel);
            }

            else if(instr instanceof Instr.Up){
                String oldLabel = ((Instr.Up) instr).jumpLabel;
                ((Instr.Up) instr).jumpLabel = oldToNewJumpLabels.get(oldLabel);
            }

            else if(instr instanceof Instr.UStruct){
                String oldLabel = ((Instr.UStruct) instr).jumpLabel;
                ((Instr.UStruct) instr).jumpLabel = oldToNewJumpLabels.get(oldLabel);
            }

            else if(instr instanceof Instr.Jump){
                String oldLabel = ((Instr.Jump) instr).jumpLabel;
                ((Instr.Jump) instr).jumpLabel = oldToNewJumpLabels.get(oldLabel);
            }

            else if(instr instanceof Instr.Try){
                String oldLabel = ((Instr.Try) instr).jumpLabel;
                ((Instr.Try) instr).jumpLabel = oldToNewJumpLabels.get(oldLabel);
            }
        }

        jumpLabelsIssued += other.jumpLabelsIssued;
    }

    private void mergePredicateTables(Code other){
        int offset = instructions.size();

        // no call instructions have to be changed
        for(String predicateLabel : other.predicateTable.keySet()){

            int newCodePosition = other.predicateTable.get(predicateLabel) + offset;
            predicateTable.put(predicateLabel, newCodePosition);
        }
    }

    public void addCode(Code other, String jumpLabel){

        if(jumpLabel != null) {
            jumpTable.put(jumpLabel, instructions.size());
        }

        mergeJumpTables(other);
        mergePredicateTables(other);

        instructions.addAll(other.instructions);
    }

    public void setJumpLabelAtEnd(String jumpLabel){
        jumpTable.put(jumpLabel, instructions.size());
    }

    public void setPredicateLabelAtEnd(String predicateLabel){
        predicateTable.put(predicateLabel, instructions.size());
    }

    public void addCode(Code other){
        addCode(other, null);
    }

    private String[] getJumpLabelPrefix(){
        String[] jumpLabelPrefix = new String[instructions.size() + 1];

        for(String jumpLabel : jumpTable.keySet()){

            int lineNumber = jumpTable.get(jumpLabel);

            String currentPrefix = jumpLabelPrefix[lineNumber];
            currentPrefix = currentPrefix != null ? currentPrefix : "";

            currentPrefix += String.format("%s: ", jumpLabel);

            jumpLabelPrefix[lineNumber] = currentPrefix;
        }

        return jumpLabelPrefix;
    }

    private String[] getPredicateTablePrefix(){
        String[] predicateTablePrefix = new String[instructions.size() + 1];

        for(String jumpLabel : predicateTable.keySet()){

            int lineNumber = predicateTable.get(jumpLabel);

            String currentPrefix = predicateTablePrefix[lineNumber];
            currentPrefix = currentPrefix != null ? currentPrefix : "";

            currentPrefix += String.format("%s: ", jumpLabel);

            predicateTablePrefix[lineNumber] = currentPrefix;
        }

        return predicateTablePrefix;
    }

    public String toString(){
        List<String> instructionStrings = new ArrayList<>();

        String[] jumpLabelPrefix = getJumpLabelPrefix();
        String[] predicateTablePrefix = getPredicateTablePrefix();

        for(int i = 0; i < instructions.size(); i++){

            String prefix = jumpLabelPrefix[i];
            prefix = prefix != null ? prefix : "";

            String predicatePrefix = predicateTablePrefix[i];
            predicatePrefix = (predicatePrefix != null ? predicatePrefix : "");

            instructionStrings.add(String.format("%s%s%s", predicatePrefix, prefix, instructions.get(i)));
        }

        if(jumpLabelPrefix[instructions.size()] != null || predicateTablePrefix[instructions.size()] != null){

            String jumpTableString = jumpLabelPrefix[instructions.size()] != null ? String.format("%s: ", jumpLabelPrefix[instructions.size()]) : "";
            String predicateTableString = predicateTablePrefix[instructions.size()] != null ? String.format("%s: ", predicateTablePrefix[instructions.size()]) : "";

            instructionStrings.add(String.format("%s%s", jumpTableString, predicateTableString));
        }

        return String.join("\n", instructionStrings);
    }
}
