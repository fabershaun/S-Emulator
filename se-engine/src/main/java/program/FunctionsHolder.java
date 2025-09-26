package program;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class FunctionsHolder implements Serializable {
    private final Map<String, Program> nameToFunction = new HashMap<>();
    private final Map<String, Program> userStringToFunction = new HashMap<>();

    public void addFunction(String functionName, String UserString, Program function) {
        this.nameToFunction.put(functionName.toUpperCase(Locale.ROOT), function);
        this.userStringToFunction.put(UserString, function);
    }

    public String getUserStringByName(String name) {    // To UPPER
        String nameToUpper = name.toUpperCase(Locale.ROOT);
       return this.nameToFunction.get(nameToUpper).getUserString();
    }

    public Program getFunctionByName(String name) {    // To UPPER
        String nameToUpper = name.toUpperCase(Locale.ROOT);
        return this.nameToFunction.get(nameToUpper);
    }

    public Program getFunctionByUserString(String userString) {
        return this.userStringToFunction.get(userString);
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

    public String getFunctionUserStringByName(String functionName) {
        return this.nameToFunction.get(functionName.toUpperCase(Locale.ROOT)).getUserString();
    }
}
