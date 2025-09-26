package engine;

import debug.Debug;
import debug.DebugImpl;
import dto.DebugDTO;
import dto.InstructionsDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;
import execution.ProgramExecutorImpl;
import execution.ProgramExecutor;
import program.Program;
import loader.XmlProgramLoader;
import variable.Variable;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class EngineImpl implements Engine, Serializable {
    private transient Path xmlPath;
    private Program mainProgram;
    private final Map<String, List<ProgramExecutor>> programToExecutionHistory = new HashMap<>();
    private Debug debug;
    private Map<String, Map<Integer, Program>> nameAndDegreeToProgram;

    @Override
    public void loadProgram(Path xmlPath) throws EngineLoadException {
        this.xmlPath = xmlPath;
        Program newProgram;

        XmlProgramLoader loader = new XmlProgramLoader();
        newProgram = loader.load(xmlPath);
        newProgram.validateProgram();
        newProgram.initialize();

        mainProgram = newProgram;

        calculateExpansionForAllPrograms(); // Initialize expansion
    }

    @Override
    public void runProgram(String programName, int degree, Long... inputs) {

        Program workingProgram = getExpandedProgram(programName, degree);
        ProgramExecutor programExecutor = new ProgramExecutorImpl(workingProgram);
        programExecutor.run(degree, inputs);

        List<ProgramExecutor> executionHistory = programToExecutionHistory.computeIfAbsent(programName, k -> new ArrayList<>());    // Get the history list per program (if not exist create empty list
        executionHistory.add(programExecutor);
    }

    private Program getProgramByName(String programName) {
        if (mainProgram.getName().equals(programName)) {
            return mainProgram;
        }
        Program resProgram = mainProgram.getFunctionsHolder().getFunctionByName(programName);

        if (resProgram == null) {
            throw new IllegalStateException("Program " + programName + " does not exist");
        }

        return resProgram;
    }

    @Override
    public ProgramDTO getProgramDTOByName(String programName) {
        return  buildProgramDTO(getProgramByName(programName));
    }

    @Override
    public ProgramDTO getProgramDTOByUserString(String userString) {
        if (mainProgram.getUserString().equals(userString)) {
            return buildProgramDTO(mainProgram);
        }
        Program resProgram = mainProgram.getFunctionsHolder().getFunctionByUserString(userString);

        if (resProgram == null) {
            throw new IllegalStateException("Program " + userString + " does not exist");
        }

        return buildProgramDTO(resProgram);
    }

    @Override
    public ProgramDTO getMainProgram() {
        return buildProgramDTO(mainProgram);
    }

    @Override
    public ProgramExecutorDTO getProgramAfterRun(String programName) {
        ProgramDTO programDTO = buildProgramDTO(getProgramByName(programName));
        ProgramExecutor programExecutor = programToExecutionHistory.get(programName).getLast();

        return new ProgramExecutorDTO(programDTO,
                programExecutor.getVariablesToValuesSorted(),
                programExecutor.getVariableValue(Variable.RESULT),
                programExecutor.getTotalCyclesOfProgram(),
                programExecutor.getRunDegree(),
                programExecutor.getInputsValuesOfUser()
        );
    }

    @Override
    public List<ProgramExecutorDTO> getHistoryPerProgram(String programName) {
        List<ProgramExecutor> programExecutors = programToExecutionHistory.get(programName);

        if (programExecutors == null || programExecutors.isEmpty()) {
            return List.of();
        }

        ProgramDTO programDTO = buildProgramDTO(programExecutors.getFirst().getProgram());

        List<ProgramExecutorDTO> res = new ArrayList<>();
        for(ProgramExecutor programExecutorItem : programExecutors) {
            ProgramExecutorDTO programExecutorDTO = buildProgramExecutorDTO(programDTO, programExecutorItem);
            res.add(programExecutorDTO);
        }

        return res;
    }

    @Override
    public List<ProgramDTO> getAllPrograms() {
        List<ProgramDTO> result = new ArrayList<>();

        result.add(buildProgramDTO(mainProgram));   // Add the main program

        for(Program function : mainProgram.getFunctionsHolder().getFunctions()) {
            result.add(buildProgramDTO(function));      // Add all the functions
        }

        return result;
    }

    @Override
    public int getMaxDegree(String programName) {
        return this.nameAndDegreeToProgram.get(programName).size();  // The size of the map is the max degree
    }

    @Override
    public ProgramDTO getExpandedProgramDTO(String programName, int degree) {
        return buildProgramDTO(getExpandedProgram(programName, degree));
    }

    private Program getExpandedProgram(String programName, int degree) {
        Map<Integer, Program> degreeMap = this.nameAndDegreeToProgram.get(programName);
        if (degreeMap == null) {
            throw new IllegalArgumentException("Program not found: " + programName);
        }

        Program expandedProgram = degreeMap.get(degree);
        if (expandedProgram == null) {
            throw new IllegalArgumentException("Degree " + degree + " not found for program: " + programName);
        }

        return expandedProgram;
    }

    @Override
    public void calculateExpansionForAllPrograms() {
        this.nameAndDegreeToProgram.put(
                mainProgram.getName(),
                mainProgram.calculateDegreeToProgram()
        );

        List<Program> functions = mainProgram.getFunctionsHolder().getFunctions().stream().toList();
        for (Program function : functions) {
            this.nameAndDegreeToProgram.put(
                    function.getName(),
                    function.calculateDegreeToProgram()
            );
        }
    }

    public static ProgramDTO buildProgramDTO(Program program) {
        InstructionsDTO instructionsDTO = new InstructionsDTO(program.getInstructionDtoList());

        return new ProgramDTO(
                program.getName(),
                program.getUserString(),
                program.getOrderedLabelsExitLastStr(),
                program.getInputVariablesSortedStr(),
                program.getWorkVariablesSortedStr(),
                instructionsDTO,
                program.getExpandedProgram()
        );
    }

    public static ProgramExecutorDTO buildProgramExecutorDTO(ProgramDTO programDTO, ProgramExecutor programExecutor) {
        return new ProgramExecutorDTO(
                programDTO,
                programExecutor.getVariablesToValuesSorted(),
                programExecutor.getVariableValue(Variable.RESULT),
                programExecutor.getTotalCyclesOfProgram(),
                programExecutor.getRunDegree(),
                programExecutor.getInputsValuesOfUser()
        );
    }

    @Override
    public void initializeDebugger(String programName, int degree, List<Long> inputs) {
        Program program = getProgramByName(programName);

        Program deepCopyOfProgram = program.deepClone();
        deepCopyOfProgram.expandProgram(degree);

        this.debug = new DebugImpl(deepCopyOfProgram, degree, inputs);
    }

    @Override
    public DebugDTO getProgramAfterStepOver() {
        DebugDTO debugDTO = debug.stepOver();    // Step Over

        if (!debugDTO.hasMoreInstructions()) {  // Add debug program executor to history map
            addDebugResultToHistoryMap(debugDTO);
        }

        return debugDTO;
    }

    @Override
    public DebugDTO getProgramAfterResume(List<Boolean> breakPoints) {
        DebugDTO debugDTO = debug.resume(breakPoints);  // Resume

        if (!debugDTO.hasMoreInstructions()) {      // Add to history
            addDebugResultToHistoryMap(debugDTO);
        }

        return debugDTO;
    }

    @Override
    public DebugDTO getProgramAfterStepBack() {
        return debug.stepBack();
    }

    @Override
    public void stopDebugPress() {
        DebugDTO debugDTO =  debug.stop();
        addDebugResultToHistoryMap(debugDTO);
    }

    private void addDebugResultToHistoryMap(DebugDTO debugDTO) {
        String programName = debugDTO.getDebugProgramExecutorDTO().getProgramDTO().getProgramName();
        List<ProgramExecutor> executionHistory = programToExecutionHistory.computeIfAbsent(programName, k -> new ArrayList<>());    // Get the history list per program (if not exist create empty list
        executionHistory.add(debug.getDebugProgramExecutor());
    }

    @Override
    public void saveState(Path path) throws EngineLoadException {
        try {
            EngineIO.save(this, path);
        } catch (IOException e) {
            throw new EngineLoadException("Failed to save engine state: " + e.getMessage(), e);
        }
    }

    @Override
    public void loadState(Path path) throws EngineLoadException {
        try {
            EngineImpl loaded = EngineIO.load(path);

            this.xmlPath = loaded.xmlPath;
            this.mainProgram = loaded.mainProgram;
            //this.executionHistory = loaded.executionHistory;

        } catch (IOException | ClassNotFoundException e) {
            throw new EngineLoadException("Failed to load engine state: " + e.getMessage(), e);
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(xmlPath != null ? xmlPath.toString() : null);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String pathStr = (String) in.readObject();
        this.xmlPath = pathStr != null ? Path.of(pathStr) : null;
    }
}
