package program;

import variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class FunctionImpl extends AbstractProgram {
    private final String userString;
//    private final List<Variable> functionArgumentsList = new ArrayList<>();

    public FunctionImpl(String name, String userString) {
        super(name);
        this.userString = userString;
    }

    public String getUserString() {
        return userString;
    }

//    public void addFunctionArgumentToList(Variable functionArgument) {
//        functionArgumentsList.add(functionArgument);
//    }
//
//    public List<Variable> getFunctionArgumentsList() {
//        return functionArgumentsList;
//    }
}
