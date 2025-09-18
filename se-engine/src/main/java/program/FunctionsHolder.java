package program;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FunctionsHolder implements Serializable {
    private final Map<String, Program> nameToFunction = new HashMap<>();;

    public void addFunction(String functionName, Program function) {
        this.nameToFunction.put(functionName, function);
    }

    public Program getFunctionByName(String name) {
        return this.nameToFunction.get(name);
    }

    public Collection<Program> getFunctions() {
        return nameToFunction.values();
    }
}
