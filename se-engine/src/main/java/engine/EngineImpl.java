package engine;

import dto.v2.*;
import engine.logic.debug.Debug;
import engine.logic.debug.DebugImpl;
import engine.logic.exceptions.EngineLoadException;
import engine.logic.execution.ProgramExecutorImpl;
import engine.logic.execution.ProgramExecutor;
import engine.logic.instruction.InstructionData;
import engine.logic.saveToXml.XmlProgramSaver;
import engine.logic.program.ProgramsHolder;
import engine.logic.program.Program;
import engine.logic.loader.XmlProgramLoader;
import engine.logic.variable.Variable;
import dto.v3.UserDTO;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class EngineImpl implements Engine, Serializable {
    private final ProgramsHolder programsHolder = new ProgramsHolder();
    private final Map<String, List<ProgramExecutor>> programToExecutionHistory = new HashMap<>();
    private final Map<String, Map<Integer, Program>> nameAndDegreeToProgram = new HashMap<>();
    private Debug debug;

    private final Map<String, UserDTO> usernameToUserDTO = new HashMap<>();


    public Program getMainProgram(String programName) {
        return programsHolder.getMainProgramByName(programName);
    }

    @Override
    public String loadProgramFromStream(InputStream xmlStream, String sourceName) throws EngineLoadException {
        XmlProgramLoader loader = new XmlProgramLoader();
        Program program = loader.loadFromStream(xmlStream, sourceName, this.programsHolder);
        finalizeProgramLoading(program);

        return program.getName();
    }

    @Override
    public String loadProgramFromFile(Path xmlPath) throws EngineLoadException {
        XmlProgramLoader loader = new XmlProgramLoader();
        Program program = loader.loadFromFile(xmlPath, this.programsHolder);
        finalizeProgramLoading(program);

        return program.getName();
    }

    private void finalizeProgramLoading(Program program) throws EngineLoadException {
        program.validateProgram();
        program.initialize();
        programsHolder.addMainProgram(program.getName(), program.getName(), program);
        calculateExpansionForAllLoadedPrograms(program.getName());
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

        if (programsHolder.getMainProgramByName(programName) != null) {   // If program is main program
            return programsHolder.getMainProgramByName(programName);
        }

        Program resProgram = programsHolder.getFunctionByName(programName);    // else - program is a function

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
    public String getProgramNameByUserString(String userString) {
        return programsHolder.getNameByUserString(userString);
    }

    @Override
    public ProgramDTO getProgramDTOByUserString(String userString) {
        String programName = getProgramNameByUserString(userString);
        return getProgramDTOByName(programName);
    }

    @Override
    public ProgramExecutorDTO getProgramAfterRun(String programName) {
        ProgramDTO programDTO = buildProgramDTO(getProgramByName(programName));
        ProgramExecutor programExecutor = programToExecutionHistory.get(programName).getLast();

        return new ProgramExecutorDTO(programDTO,
                programExecutor.getVariablesToValuesSorted(),
                programExecutor.getVariableValue(Variable.RESULT),
                programExecutor.getTotalCycles(),
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

        for (Program mainProgram : programsHolder.getMainPrograms()) {
            result.add(buildProgramDTO(mainProgram));   // Add all the main programs
        }

        for(Program function : programsHolder.getFunctions()) {
            result.add(buildProgramDTO(function));      // Add all the functions
        }

        return result;
    }

    @Override
    public Set<String> getMainProgramsSetStr() {
        return programsHolder.getMainPrograms()
                .stream()
                .map(Program::getUserString)         // Extract the program name
                .collect(Collectors.toSet());  // Collect unique names into a Set
    }

    @Override
    public Set<String> getFunctionsSetStr() {
        return programsHolder.getFunctions()
                .stream()
                .map(Program::getUserString)         // Extract the program name
                .collect(Collectors.toSet());  // Collect unique names into a Set
    }

    @Override
    public int getMaxDegree(String programName) {
        return this.nameAndDegreeToProgram.get(programName).size() - 1;  // The size of the map is the max degree
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
    public void calculateExpansionForAllLoadedPrograms(String mainProgramName) {
        Program mainProgram = programsHolder.getMainProgramByName(mainProgramName);

        this.nameAndDegreeToProgram.put(
                mainProgramName,
                mainProgram.calculateDegreeToProgram()
        );

        List<Program> functions = programsHolder.getFunctions().stream().toList();
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
                programExecutor.getTotalCycles(),
                programExecutor.getRunDegree(),
                programExecutor.getInputsValuesOfUser()
        );
    }

    @Override
    public void initializeDebugger(String programName, int degree, List<Long> inputs) {
        Program workingProgram = getExpandedProgram(programName, degree);

        this.debug = new DebugImpl(workingProgram, degree, inputs);
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
    public DebugDTO getProgramAfterResume(List<Boolean> breakPoints) throws InterruptedException {
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
        DebugDTO debugDTO = debug.stop();
        addDebugResultToHistoryMap(debugDTO);
    }

    private void addDebugResultToHistoryMap(DebugDTO debugDTO) {
        String programName = debugDTO.getProgramName();
        List<ProgramExecutor> executionHistory = programToExecutionHistory.computeIfAbsent(programName, k -> new ArrayList<>());    // Get the history list per program (if not exist create empty list
        executionHistory.add(debug.getDebugProgramExecutor());
    }

    @Override
    public InstructionDTO createOriginalInstruction() {
        // Create and return a new Origin instruction
        return new InstructionDTO(InstructionData.ORIGIN.getName(), 0, InstructionData.ORIGIN.getCycles(), "B", null, null, null, null, 0,  "", null );
    }

    @Override
    public void exportToXml(File file, String programName, List<InstructionDTO> instructions) {
        File target = ensureXml(file);
        try {
            XmlProgramSaver saver = new XmlProgramSaver();
            saver.save(target, programName, instructions);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export program to XML: " + e.getMessage(), e);
        }
    }   // For 'הכתבן החרוץ'

    private File ensureXml(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".xml")) return file;
        return new File(file.getParentFile(), file.getName() + ".xml");
    }

    @Override
    public ProgramDTO getMainProgramToConsoleModule() {
        Program mainProgram = this.programsHolder.getMainProgramForConsoleModuleOnly();
        return buildProgramDTO(mainProgram);
    }
}
