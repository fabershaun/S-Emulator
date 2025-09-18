package program;

import java.util.*;


public class ProgramImpl extends AbstractProgram {

    private final Map<String, FunctionImpl> nameToFunction;

    public ProgramImpl(String programOfFunctionName) {
        super(programOfFunctionName);
        nameToFunction = new HashMap<>();
    }

    public void addInnerFunction(String functionName, FunctionImpl function) {
        this.nameToFunction.put(functionName, function);
    }

    public FunctionImpl getFunctionByName(String name) {
        return this.nameToFunction.get(name);
    }

    public Collection<FunctionImpl> getFunctions() {
        return nameToFunction.values();
    }

    // TODO: עכשיו לכתוב את המתודה שבונה את רשימת תתי התוכניות


}
