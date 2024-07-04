public class Runner {

    boolean debugPrint = false;

    public void activateDebugPrint(){
        debugPrint = true;
    }

    public void run(String source){

        Lexer lexer = new Lexer(source);

        Parser parser = new Parser(lexer.getTokens());

        Program program = parser.parse();

        Compiler compiler = new Compiler();
        compiler.isUnificationOptimized = true;

        Code compiled = compiler.code(program);

        // default setting: no debug print
        // backtrack on 'ENTER'
        VirtualMachine vm = new VirtualMachine();

        if(debugPrint){
            vm.debugPrint = true;
        }

        vm.run(compiled);
    }
}
