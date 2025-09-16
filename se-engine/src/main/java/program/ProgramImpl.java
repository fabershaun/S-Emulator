package program;

import java.util.*;


public class ProgramImpl extends AbstractProgram {

    private final List<Program> functions;

    public ProgramImpl(String programOfFunctionName) {
        super(programOfFunctionName);
        this.functions = new ArrayList<>();
    }


    public List<Program> getFunctions() {
        return this.functions;
    }

    // TODO: עכשיו לכתוב את המתודה שבונה את רשימת תתי התוכניות
}
