package program;

import java.util.*;


public class ProgramImpl extends SubProgramImpl {

    private final List<Program> subPrograms;    // maybe need to chane to <SubProgramImpl>

    public ProgramImpl(String name) {
        super(name);
        this.subPrograms = new ArrayList<>();
    }


    public List<Program> getSubPrograms() {
        return this.subPrograms;
    }

    // TODO: עכשיו לכתוב את המתודה שבונה את רשימת תתי התוכניות
}
