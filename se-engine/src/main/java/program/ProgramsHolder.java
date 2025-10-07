package program;

import java.io.Serializable;
import java.util.*;

public class ProgramsHolder implements Serializable {


    private final Map<String, Program> nameToFunction = new HashMap<>();
    private final Map<String, Program> nameToMainProgram = new HashMap<>();
    private final Map<String, String> userStringToName = new HashMap<>();


    public void addMainProgram(String programName, String UserString, Program mainProgram) {
        this.nameToMainProgram.put(programName.toUpperCase(Locale.ROOT), mainProgram);
        this.userStringToName.put(UserString.toUpperCase(Locale.ROOT), programName.toUpperCase(Locale.ROOT));
    }

    public Program getMainProgramByName(String name) {    // To UPPER
        String nameToUpper = name.toUpperCase(Locale.ROOT);
        return this.nameToMainProgram.get(nameToUpper);
    }

    public void addFunction(String functionName, String UserString, Program function) {
        this.nameToFunction.put(functionName.toUpperCase(Locale.ROOT), function);
        this.userStringToName.put(UserString.toUpperCase(Locale.ROOT), functionName.toUpperCase(Locale.ROOT));
    }

    public Program getFunctionByName(String name) {    // To UPPER
        String nameToUpper = name.toUpperCase(Locale.ROOT);
        return this.nameToFunction.get(nameToUpper);
    }

    public String getNameByUserString(String UserString) {    // To UPPER
        String UserStringToUpper = UserString.toUpperCase(Locale.ROOT);
       return userStringToName.get(UserStringToUpper);
    }

    public Collection<Program> getFunctions() {
        return nameToFunction.values();
    }

    public Collection<Program> getMainPrograms() {
        return nameToMainProgram.values();
    }

    public Program getMainProgramForConsoleModuleOnly() {
        return getMainPrograms().stream().findFirst().orElse(null);
    }
}
