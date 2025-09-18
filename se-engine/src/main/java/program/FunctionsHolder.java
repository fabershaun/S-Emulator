package program;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class FunctionsHolder implements Serializable {
    private final Map<String, Program> nameToFunction = new HashMap<>();;

    public void addFunction(String functionName, Program function) {
        this.nameToFunction.put(functionName.toUpperCase(Locale.ROOT), function);
    }

    public Program getFunctionByName(String name) {
        return this.nameToFunction.get(name.toUpperCase(Locale.ROOT));
    }

    public Collection<Program> getFunctions() {
        return nameToFunction.values();
    }

    public Set<String> getFunctionNamesUpperCase() {
        return getFunctions()
                .stream()
                .map(func -> func.getName().toUpperCase(Locale.ROOT)) // normalize to uppercase
                .collect(Collectors.toSet());
    }
}
