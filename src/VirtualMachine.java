
import java.util.Map;
import java.util.Scanner;

public class VirtualMachine {

    Instr[] codeStore;
    int[] stack;

    int maxCodeSize = 1000;
    int maxStackSize = 1000;
    int maxHeapSize = 1000;
    int maxTStackSize = 1000;

    int programCounter;
    int stackPointer;
    int framePointer;
    int heapPointer;
    int trailPointer;
    int backtrackPoint;

    Instr instructionRegister;

    HeapElement[] heap;

    Map<String, Integer> jumpTable;
    Map<String, Integer> predicateTable;

    // for remembering the order of variable bindings?
    int[] tStack;

    boolean isRunning;

    // the 'Enter' behavior of the prolog repl
    boolean waitingForUserResponse = true;

    boolean debugPrint = false;

    public VirtualMachine(){
        stack = new int[maxStackSize];
        tStack = new int[maxTStackSize];
        codeStore = new Instr[maxCodeSize];
        heap = new HeapElement[maxHeapSize];
    }

    private void setDebugPrint(){
        debugPrint = true;
    }

    private void loadCode(Code code){
        jumpTable = code.jumpTable;
        predicateTable = code.predicateTable;

        // copy the instructions
        for(int i = 0; i < code.instructions.size(); i++){
            codeStore[i] = code.instructions.get(i);
        }

        programCounter = 0;
        stackPointer = 0;
        heapPointer = 0;

        framePointer = -1;
        backtrackPoint = -1;
        trailPointer = -1;
    }

    private void debug(String message){
        if(debugPrint){
            System.out.println(String.format("[DEBUG]: %s", message));
        }
    }

    // prints 'true', 'false' or the variable assignments on the terminal
    public void run(Code code){

        loadCode(code);
        isRunning = true;

        while(isRunning){

            instructionRegister = codeStore[programCounter++];

            if(instructionRegister instanceof Instr.No){
                isRunning = false;
            }
            else if(instructionRegister instanceof Instr.PutAtom){
                // a new atom is created in the heap
                // its heap address is returned on the stack
                heap[heapPointer] = new HeapElement.Atom(((Instr.PutAtom) instructionRegister).atomName);

                // put atom increases the stack pointer by 1 (doesn't overwrite the top)
                stack[++stackPointer] = heapPointer++;
            }

            else if(instructionRegister instanceof Instr.PutVar){
                // a new unbound variable is created on the heap
                // at address relative to frame pointer, value points to new heap object
                // points to itself initially (that's why it is unbound)
                heap[heapPointer] = new HeapElement.Variable(heapPointer);

                // additionally to the FP + i cell getting the heap address
                // it is also returned on the stack
                stack[++stackPointer] = heapPointer;

                stack[framePointer + ((Instr.PutVar) instructionRegister).relativeAddress] = heapPointer++;
            }

            else if(instructionRegister instanceof Instr.PutAnon){

                // a new variable is created on the heap, address returned to the stack
                // but no variable relative to frame pointer also receives the new heap element
                heap[heapPointer] = new HeapElement.Variable(heapPointer);
                stack[++stackPointer] = heapPointer++;
            }

            else if(instructionRegister instanceof Instr.PutRef){

                // we put a heap address on the stack

                // the heap address comes from FP + i stack element (we maximally dereference it)

                // nothing is created on the heap

                // make space for the new heap address
                stackPointer++;

                stack[stackPointer] = deref(stack[framePointer + ((Instr.PutRef) instructionRegister).relativeAddress]);
            }

            else if(instructionRegister instanceof Instr.PutStruct){

                int n = ((Instr.PutStruct) instructionRegister).arity;

                // the n elements lie on top of the stack
                HeapElement.StructHead structHead = new HeapElement.StructHead(
                        ((Instr.PutStruct) instructionRegister).structName, n);

                stackPointer = stackPointer - n + 1;

                heap[heapPointer] = structHead;

                // + 1 because at the current heap pointer, there's the struct head
                for(int i = 1; i <= n; i++){
                    heap[heapPointer + i] = new HeapElement.StructCell(stack[stackPointer + i - 1]);
                }

                // point to the struct head
                stack[stackPointer] = heapPointer;

                heapPointer = heapPointer + n + 1;
            }

            else if(instructionRegister instanceof Instr.Mark){
                // the mark instruction prepares the organizational cells
                // the topmost organizational cell stores the pc where to continue
                // if the goal succeeds (the positive continuation)
                stackPointer = stackPointer + 6;
                stack[stackPointer] = jumpTable.get(((Instr.Mark) instructionRegister).jumpLabel);
                stack[stackPointer - 1] = framePointer;

                // the second address from the top stores the old frame pointer
                // the frame pointer is set to a different value at call!
            }

            else if(instructionRegister instanceof Instr.Call call){

                String predicateName = ((Instr.Call) instructionRegister).PredicateName;
                int n = ((Instr.Call) instructionRegister).arity;

                debug(String.format("Calling %s/%d", predicateName, n));

                String predicateLabel = String.format("%s/%d", predicateName, n);

                if(predicateTable.containsKey(predicateLabel)){

                    // tries to achieve the goal

                    // the frame pointer points to the topmost organizational cell
                    // the stack might have grown since the mark instruction
                    // exactly by n values (n heap addresses for n arguments)
                    framePointer = stackPointer - n;
                    programCounter = predicateTable.get(predicateLabel);
                }
                else{
                    fail();
                }
            }

            else if(instructionRegister instanceof Instr.Bind){
                // the value below the top most stack value should contain an unbound variable
                // the top most value should contain a heap address that we can bind to
                HeapElement.Variable unboundVar = (HeapElement.Variable) heap[stack[stackPointer - 1]];

                unboundVar.pointsToHeapAddress = stack[stackPointer];

                // the heap address at stackPointer - 1 contained an unbound var
                // the var just got bound
                // so record that the heap address is now bound
                trail(stack[stackPointer - 1]);

                // both get consumed after the binding
                stackPointer -= 2;
            }

            else if(instructionRegister instanceof Instr.Unify){
                // the top most value contains something we want to bind to
                // the value one down contains a reference (something that is already bound)

                debug(String.format("Unifying %s = %s", heap[stack[stackPointer - 1]], heap[stack[stackPointer]]));

                // now we can get into unification problems
                unify(stack[stackPointer - 1], stack[stackPointer]);

                // if the unification worked, both heap addresses are consumed
                stackPointer -= 2;
            }

            else if(instructionRegister instanceof Instr.Check check){
                throw new RuntimeException("Instructions of codeU are added later.");
            }

            else if(instructionRegister instanceof Instr.Son son){
                throw new RuntimeException("Instructions of codeU are added later.");
            }

            else if(instructionRegister instanceof Instr.Up up){
                throw new RuntimeException("Instructions of codeU are added later.");
            }

            else if(instructionRegister instanceof Instr.UAtom uatom){
                throw new RuntimeException("Instructions of codeU are added later.");
            }

            else if(instructionRegister instanceof Instr.UVar uvar){
                throw new RuntimeException("Instructions of codeU are added later.");
            }

            else if(instructionRegister instanceof Instr.URef uref){
                throw new RuntimeException("Instructions of codeU are added later.");
            }

            // that's the 'UAnon'
            else if(instructionRegister instanceof Instr.Pop){
                throw new RuntimeException("Instructions of codeU are added later.");
            }

            else if(instructionRegister instanceof Instr.UStruct ustruct){
                throw new RuntimeException("Instructions of codeU are added later.");
            }

            else if(instructionRegister instanceof Instr.PushEnv pushEnv){
                stackPointer = framePointer + pushEnv.m;
            }

            else if(instructionRegister instanceof Instr.SetBackTrackPoint setBackTrackPoint){
                setHpOld(heapPointer);
                setTpOld(trailPointer);
                setBpOld(backtrackPoint);

                // at last, set the frame pointer
                // can't be set earlier because decides where hpOld etc. is

                // setting the backtrack point means
                // restore this frame when the goals fail
                backtrackPoint = framePointer;
            }

            else if(instructionRegister instanceof Instr.Try tryInstr){

                debug(String.format("Trying clause:  %s", tryInstr.clauseDebugInfo));

                // if we have s(X) :- t(X)
                // and s(X) :- X = a

                // when goal t(X) fails, we move to X = a and try to unify

                // the pc has already moved one further
                // to the next clause of the predicate
                setNegCont(programCounter);

                programCounter = jumpTable.get(tryInstr.jumpLabel);
            }

            else if(instructionRegister instanceof Instr.DeleteBackTrackPoint deleteBackTrackPoint){
                // we move one backtrack point back
                // once this fails, we don't backtrack inside the predicate (like trying s(X) :- X = a
                // after we failed to show t(X)

                // but we go back one level
                backtrackPoint = bpOld();
            }

            else if(instructionRegister instanceof Instr.PopEnv){
                // When was PushEnv used?

                // when we tried to show a clause
                // and the clause had m free variables (k of those where in the clause head)

                // Why this?
                if(framePointer > backtrackPoint){
                    stackPointer = framePointer - 6;
                }

                // pop env means we have proved all goals successfully
                programCounter = posCont();

                // return to calling stack frame
                framePointer = fpOld();
            }

            else if(instructionRegister instanceof Instr.Jump jump){
                debug(String.format("Jumping to %s", jump.jumpDebugInfo));

                programCounter = jumpTable.get(jump.jumpLabel);
            }

            else if(instructionRegister instanceof Instr.Init init){


                // the init instruction stores a jump address
                // the jump address says where to return once the goal (the query) fails
                // that should be the negative continuation address?

                // why 5?
                // stackPointer = -1 and then + 6 for a new stack frame (6 organizational cells 0...5)
                backtrackPoint = framePointer = stackPointer = 5;


                // the bottom most organization cell stores the negative continuation address
                stack[0] = jumpTable.get(init.jumpLabel);

                // the bp old
                stack[1] = -1;

                // the tp old
                stack[2] = -1;

                // the hp old
                stack[3] = 0;

                // this frame gets restored when the goal fails (and the negative continuation address is loaded into the programCounter)
                backtrackPoint = framePointer;
            }

            else if(instructionRegister instanceof Instr.Halt halt){

                // the variables can be accessed via the frame pointer
                if(halt.d > 0){

                    Scanner scanner = new Scanner(System.in);

                    StringBuilder res = new StringBuilder();

                    // the local variables start with 1...n
                    for(int i = 1; i <= halt.d; i++){

                        int finalHeapAddress = deref(stack[framePointer + i]);

                        HeapElement finalHeapElement = heap[finalHeapAddress];

                        res.append(String.format("%s = %s", halt.varNames[i - 1], finalHeapElement.getOutputRepresentation()));
                    }

                    log(res.toString());

                    // waiting on user input
                    if(waitingForUserResponse) {
                        scanner.nextLine();
                    }
                    // when the user presses enter, backtracking occurs
                    // to see if another variable assignment exists
                    backtrack();
                }
                else{
                    log("true");
                }
            }
        }

        log("false");
    }

    private void fail(){

        // TODO: is this the right thing to do?
        backtrack();
    }

    public void setNoWaitForUser(){
        waitingForUserResponse = false;
    }

    private boolean unify(int heapU, int heapV){

        // both objects are already the same (as indicated by same position in the heap)
        if(heapU == heapV){
            return true;
        }

        if(heap[heapU] instanceof HeapElement.Variable){
            if(heap[heapV] instanceof HeapElement.Variable){
                // the younger address (higher) heap address should point to the older (lower) heap address
                if(heapU < heapV){
                    // heapV is younger
                    HeapElement.Variable vVar = (HeapElement.Variable) heap[heapV];

                    vVar.pointsToHeapAddress = heapU;

                    // the object at address heapV just got bound
                    // we want to record that
                    trail(heapV);

                    return true;
                }
                else{
                    // it must be heapU > heapV (because heapU != heapV)
                    HeapElement.Variable uVar = (HeapElement.Variable) heap[heapU];

                    // heapU is younger, so it gets bound
                    uVar.pointsToHeapAddress = heapV;

                    // u just got bound so record that
                    trail(heapU);

                    return true;
                }
            }
            else{
                // both are maximally dereferenced
                // if heapV is not a variable, it is either an atom or a struct
                // if it's an atom, no problem. We can bind the var to the atom
                // we need the occur check for the struct
                if(check(heapU, heapV)){

                    // u gets bound to v (because u is the only variable)
                    HeapElement.Variable uVar = (HeapElement.Variable) heap[heapU];

                    uVar.pointsToHeapAddress = heapV;

                    // we just bound u (the variable) to v (can be an atom or a struct)
                    trail(heapU);

                    return true;
                }
                else{
                    // heapV must have been an address to a struct that contains X
                    backtrack();

                    return false;
                }
            }
        }
        else if(heap[heapV] instanceof HeapElement.Variable){
            // heapU is either a struct or an atom,
            // so maybe we can bind v to it
            if(check(heapV, heapU)){
                HeapElement.Variable vVar = (HeapElement.Variable) heap[heapV];

                vVar.pointsToHeapAddress = heapU;

                // record the binding
                trail(heapV);

                return true;
            }
            else{
                backtrack();
                return false;
            }
        }

        if(heap[heapU] instanceof HeapElement.Atom uAtom && heap[heapV] instanceof HeapElement.Atom vAtom){

            if(uAtom.atomName.equals(vAtom.atomName)){
                return true;
            }
            else{
                backtrack();
                return false;
            }
        }

        if(heap[heapU] instanceof HeapElement.StructHead uStruct && heap[heapV] instanceof HeapElement.StructHead vStruct){

            if(uStruct.structName.equals(vStruct.structName) && uStruct.arity == vStruct.arity){

                // outer part works, now we have to unify all the children
                int n = uStruct.arity;

                for(int i = 1; i <= n; i++){

                    // struct addresses might not be maximally dereferenced
                    // and that we must always guarantee

                    // the n elements after the struct head are sure to contain integers so we can do the cast
                    if(!unify(deref(((HeapElement.StructCell)heap[heapU + i]).i), deref(((HeapElement.StructCell)heap[heapV + i]).i))){
                        backtrack();
                        return false;
                    }
                }
                // everything matches
                return true;
            }
        }

        // in all other cases, unification is not possible
        backtrack();
        return false;
    }

    private void trail(int heapAddressToRecordBinding){

        // TODO: why this condition?
        if(heapAddressToRecordBinding < hpOld()) {
            trailPointer++;

            // we just record, which heap address has been bound (order is most important for us)
            tStack[trailPointer] = heapAddressToRecordBinding;
        }
    }

    // check = true means heapU is not contained in heapV
    // the order of the arguments matters

    // check = false initiates the backtracking
    private boolean check(int heapU, int heapV){

        // this wouldn't get triggered by a unification "X = X" because
        // check wouldn't even be invoked (look at first case of the unify function)
        if(heapU == heapV){
            return false;
        }

        if(heap[heapV] instanceof HeapElement.StructHead vStruct){

            // can't be contained in any arguments of v
            int n = vStruct.arity;

            for(int i = 0; i < n; i++){

                // not the element at heapV + i is important
                // but where it points
                if(!check(heapU, deref(((HeapElement.StructCell)heap[heapV + i]).i))){
                    return false;
                }
            }
        }

        return true;
    }

    private void backtrack(){

        debug("backtracking...");

        // the backtrack point is the frame pointer in the case stuff fails
        // there's also fp old
        // that's for the frame we want to return to once the goal as succeeded
        framePointer = backtrackPoint;

        heapPointer = hpOld();

        // unification has failed,
        // so we have to discard this setting of the variables
        reset(tpOld(), trailPointer);
        trailPointer = tpOld();

        // the goal has failed (unification has failed)
        // load the negative continuation address
        programCounter = negCont();
    }

    private void reset(int heapX, int heapY){

        debug("Resetting...");

        // unbind variables

        // the variables are on the heap
        // that they were bound is recorded in the trail
        for(int u = heapY; u > heapX; u--){

            // the trail saves the addresses that where bound
            // if we set them back to the values in t stack
            // stuff points to itself (unbound vars again)
            heap[tStack[u]] = new HeapElement.Variable(tStack[u]);
        }
    }

    // helper functions for the six organizational cells
    private int posCont(){
        return stack[framePointer];
    }

    private void setPosCont(int value){
        stack[framePointer] = value;
    }

    private int fpOld(){
        return stack[framePointer - 1];
    }

    private void setFpOld(int value){
        stack[framePointer - 1] = value;
    }

    private int hpOld(){
        return stack[framePointer - 2];
    }

    private void setHpOld(int value){
        stack[framePointer - 2] = value;
    }

    // old trail pointer (when we want to restore the old variable binding at backtracking)
    private int tpOld(){
        return stack[framePointer - 3];
    }

    private void setTpOld(int value){
        stack[framePointer - 3] = value;
    }

    private int bpOld(){
        return stack[framePointer - 4];
    }

    private void setBpOld(int value){
        stack[framePointer - 4] = value;
    }

    // the negative continuation address, once the goal fails
    private int negCont(){
        return stack[framePointer - 5];
    }

    private void setNegCont(int value){
        stack[framePointer - 5] = value;
    }

    private int deref(int heapAddress){

        // if it points to something else, we have to follow the target
        HeapElement atAddress = heap[heapAddress];

        if(atAddress instanceof HeapElement.Variable){
            int pointsToAddress = ((HeapElement.Variable) atAddress).pointsToHeapAddress;

            // follow
            if(pointsToAddress != heapAddress){
                return deref(pointsToAddress);
            }
        }
        // either follows itself (unbound variable or anonymous variable) or is not a variable (atom)
        return heapAddress;
    }

    private void log(String message){
        System.out.printf("VM: %s%n", message);
    }
}
