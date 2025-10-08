package engine;

import dto.v2.*;
import engine.logic.execution.debugMode.Debug;
import engine.logic.execution.debugMode.DebugImpl;
import engine.logic.exceptions.EngineLoadException;
import engine.logic.execution.runMode.ProgramExecutorImpl;
import engine.logic.execution.runMode.ProgramExecutor;
import engine.logic.programData.instruction.InstructionData;
import engine.logic.saveToXml.XmlProgramSaver;
import engine.logic.programData.program.ProgramsHolder;
import engine.logic.programData.program.Program;
import engine.logic.loadFromXml.XmlProgramLoader;
import engine.logic.programData.variable.Variable;
import dto.v3.UserDTO;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class EngineImpl implements Engine, Serializable {
    private final ProgramsHolder programsHolder = new ProgramsHolder();
    private final Map<String, List<ProgramExecutor>> programToExecutionHistory = new HashMap<>();
    private final Map<String, Map<Integer, Program>> nameAndDegreeToProgram = new HashMap<>();
    private final Map<String, Debug> usernameToDebug = new HashMap<>();
    //Debug debug;

    private final Map<String, UserDTO> usernameToUserDTO = new HashMap<>();

    public EngineImpl() {
        // Create default user with empty name (For version 2)
        UserDTO defaultUser = new UserDTO(UserDTO.DEFAULT_NAME);
        usernameToUserDTO.put(UserDTO.DEFAULT_NAME, defaultUser);
    }

    @Override
    public synchronized void addUser(String username) {
        if (usernameToUserDTO.containsKey(username)) {
            throw new IllegalArgumentException("In EngineImpl, when try to add a new user: user '" + username + "' already exists");
        }
        usernameToUserDTO.put(username, new UserDTO(username));
    }

    @Override
    public synchronized void removeUser(String username) {
        if (!usernameToUserDTO.containsKey(username)) {
            throw new NoSuchElementException("In EngineImpl, when try to remove the user: " + username + "' not found");
        }
        usernameToUserDTO.remove(username);
    }

    @Override
    public synchronized Set<String> getUsers() {
        return Collections.unmodifiableSet(usernameToUserDTO.keySet());
    }

    @Override
    public boolean isUserExists(String username) {
        return usernameToUserDTO.containsKey(username);
    }

    @Override
    public UserDTO getUserDTO(String username) {
        UserDTO userDTO = usernameToUserDTO.get(username);
        if (userDTO == null) {
            throw new NoSuchElementException("In EngineImpl, when try to get the user: user '" + username + "' not found");
        }
        return userDTO;
    }

    @Override
    public String loadProgramFromStream(InputStream xmlStream, String sourceName, String uploaderName) throws EngineLoadException {
        XmlProgramLoader loader = new XmlProgramLoader();
        UserDTO userDTO = getUserDTO(uploaderName);
        Program program = loader.loadFromStream(xmlStream, sourceName, this.programsHolder, userDTO, uploaderName);
        finalizeProgramLoading(program, userDTO);

        return program.getName();
    }

    @Override
    public String loadProgramFromFile(Path xmlPath, String uploaderName) throws EngineLoadException {
        XmlProgramLoader loader = new XmlProgramLoader();
        UserDTO userDTO = getUserDTO(uploaderName);
        Program program = loader.loadFromFile(xmlPath, this.programsHolder, userDTO, uploaderName);
        finalizeProgramLoading(program, userDTO);

        return program.getName();
    }

    private void finalizeProgramLoading(Program program, UserDTO userDTO) throws EngineLoadException {
        program.validateProgram();
        program.initialize();
        programsHolder.addMainProgram(program.getName(), program.getName(), program);
        userDTO.addOneToMainProgramsCount();

        calculateExpansionForAllLoadedPrograms(program.getName());
    }

    @Override
    public void runProgram(String programName, int degree, String uploaderName, Long... inputs) {

        Program workingProgram = getExpandedProgram(programName, degree);
        ProgramExecutor programExecutor = new ProgramExecutorImpl(workingProgram);

        UserDTO userDTO = getUserDTO(uploaderName);
        userDTO.addOneToExecutionsCount();

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
    public void initializeDebugger(String programName, int degree, List<Long> inputs, String uploaderName) {
        Program workingProgram = getExpandedProgram(programName, degree);

        // In any case, overwrite the previous value
        usernameToDebug.put(uploaderName, new DebugImpl(workingProgram, degree, inputs, uploaderName));
    }

    private Debug getDebugSystemByUsername(String username) {
        Debug debug = usernameToDebug.get(username);
        if (debug == null) {
            throw new IllegalArgumentException("In EngineImpl, when try getting Debug system from map: username not found: '" + username + "'");
        }
        return debug;
    }

    @Override
    public DebugDTO getProgramAfterStepOver(String uploaderName) {
        Debug debug = getDebugSystemByUsername(uploaderName);

        DebugDTO debugDTO = debug.stepOver();    // Step Over

        if (!debugDTO.hasMoreInstructions()) {  // Add debug program executor to history map
            addDebugResultToHistoryMap(debugDTO, uploaderName);
        }

        return debugDTO;
    }

    @Override
    public DebugDTO getProgramAfterResume(List<Boolean> breakPoints, String uploaderName) throws InterruptedException {
        Debug debug = getDebugSystemByUsername(uploaderName);

        DebugDTO debugDTO = debug.resume(breakPoints);  // Resume

        if (!debugDTO.hasMoreInstructions()) {      // Add to history
            addDebugResultToHistoryMap(debugDTO, uploaderName);
        }

        return debugDTO;
    }

    @Override
    public DebugDTO getProgramAfterStepBack(String uploaderName) {
        Debug debug = getDebugSystemByUsername(uploaderName);
        return debug.stepBack();
    }

    @Override
    public void stopDebugPress(String uploaderName) {
        Debug debug = getDebugSystemByUsername(uploaderName);
        DebugDTO debugDTO = debug.stop();
        addDebugResultToHistoryMap(debugDTO, uploaderName);
    }

    private void addDebugResultToHistoryMap(DebugDTO debugDTO, String uploaderName) {
        Debug debug = getDebugSystemByUsername(uploaderName);

        String programName = debugDTO.getProgramName();
        List<ProgramExecutor> executionHistory = programToExecutionHistory.computeIfAbsent(programName, k -> new ArrayList<>());    // Get the history list per program (if not exist create empty list
        executionHistory.add(debug.getDebugProgramExecutor());

        UserDTO userDTO = this.usernameToUserDTO.get(uploaderName);
        userDTO.addOneToExecutionsCount();
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
