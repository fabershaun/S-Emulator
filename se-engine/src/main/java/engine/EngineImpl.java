package engine;

import dto.v2.*;
import dto.v3.AvailableFunctionsDTO;
import dto.v3.AvailableProgramsDTO;
import dto.v3.HistoryRowV3DTO;
import engine.logic.execution.debugMode.Debug;
import engine.logic.execution.debugMode.DebugImpl;
import engine.logic.exceptions.EngineLoadException;
import engine.logic.execution.runMode.ProgramExecutorImpl;
import engine.logic.execution.runMode.ProgramExecutor;
import engine.logic.programData.architecture.ArchitectureType;
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

    private final Map<String, Map<Integer, Program>> nameAndDegreeToProgram = new HashMap<>();      // Program name : ( Degree : Program )
    private final Map<String, Debug> usernameToDebug = new HashMap<>();                             // Username : Debug
    private final Map<String, UserDTO> usernameToUserDTO = new HashMap<>();                         // Username : UserDTO
    private final Map<String, List<ProgramExecutor>> programToExecutionHistory = new HashMap<>();   // Program name : Execution history
    private final Map<String, List<ProgramExecutor>> usernameToExecutionHistory = new HashMap<>();  // Username : Execution history

    public EngineImpl() {
        // Create default user with empty name (For version 2)
        UserDTO defaultUser = new UserDTO(UserDTO.DEFAULT_NAME);
        usernameToUserDTO.put(UserDTO.DEFAULT_NAME, defaultUser);
        getUserDTO(UserDTO.DEFAULT_NAME).addToCurrentCredits(Integer.MAX_VALUE);
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
    public synchronized Set<UserDTO> getAllUsers() {
        Set<UserDTO> filteredUsers = usernameToUserDTO.values().stream()
                .filter(user -> !user.getUserName().equals(UserDTO.DEFAULT_NAME))          // Remove default user (from version 2)
                .collect(Collectors.toSet());

        return Collections.unmodifiableSet(filteredUsers);
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
    public void runProgram(String programName, String architectureTypeRepresentation, int degree, String uploaderName, Long... inputs) {

        Program workingProgram = getExpandedProgram(programName, degree);  // Get the relevant program from the map
        workingProgram.incrementExecutionsCount();  // Add one to execution count of the program;
        ProgramExecutor programExecutor = new ProgramExecutorImpl(workingProgram);
        ArchitectureType architectureTypeSelected = ArchitectureType.fromRepresentation(architectureTypeRepresentation);
        UserDTO userDTO = getUserDTO(uploaderName);
        userDTO.addOneToExecutionsCount();

        programExecutor.run(userDTO, architectureTypeSelected, degree, inputs); // The important method

        // For Version 2
        List<ProgramExecutor> executionV2History = programToExecutionHistory.computeIfAbsent(programName, k -> new ArrayList<>());  // Get the history list per program (if not exist create empty list) and add it to the list
        executionV2History.add(programExecutor);

        // For Version 3
        List<ProgramExecutor> executionV3History = usernameToExecutionHistory.computeIfAbsent(programName, k -> new ArrayList<>());
        executionV3History.add(programExecutor);
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
    public ProgramExecutorDTO getProgramAfterRunV2(String programName) {
        ProgramDTO programDTO = buildProgramDTO(getProgramByName(programName));
        ProgramExecutor programExecutor = programToExecutionHistory.get(programName).getLast();

        return new ProgramExecutorDTO(programDTO,
                programExecutor.getVariablesToValuesSorted(),
                programExecutor.getVariableValue(Variable.RESULT),
                programExecutor.getTotalCycles(),
                programExecutor.getRunDegree(),
                programExecutor.getInputsValuesOfUser(),
                programExecutor.getArchitectureTypeSelected().getArchitectureRepresentation()
        );
    }

    @Override
    public ProgramExecutorDTO getProgramAfterRunV3(String programName) {
        ProgramDTO programDTO = buildProgramDTO(getProgramByName(programName));
        ProgramExecutor programExecutor = usernameToExecutionHistory.get(programName).getLast();

        return new ProgramExecutorDTO(programDTO,
                programExecutor.getVariablesToValuesSorted(),
                programExecutor.getVariableValue(Variable.RESULT),
                programExecutor.getTotalCycles(),
                programExecutor.getRunDegree(),
                programExecutor.getInputsValuesOfUser(),
                programExecutor.getArchitectureTypeSelected().getArchitectureRepresentation()
        );
    }

    @Override
    public List<ProgramExecutorDTO> getHistoryV2PerProgram(String programName) {
        List<ProgramExecutor> programExecutors = programToExecutionHistory.get(programName);
        return buildExecutorDTOList(programExecutors);
    }

    @Override
    public List<HistoryRowV3DTO> getHistoryV3PerProgram(String programName) {
        List<ProgramExecutor> programExecutors = usernameToExecutionHistory.get(programName);
        List<ProgramExecutorDTO> programExecutorDTOList = buildExecutorDTOList(programExecutors);
        return buildHistoryRowsFromExecutors(programExecutorDTOList);
    }

    private List<ProgramExecutorDTO> buildExecutorDTOList(List<ProgramExecutor> programExecutors) {
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

    private List<HistoryRowV3DTO> buildHistoryRowsFromExecutors(List<ProgramExecutorDTO> executors) {
        if (executors == null || executors.isEmpty()) {
            return List.of();
        }

        List<HistoryRowV3DTO> historyRows = new ArrayList<>();

        for (ProgramExecutorDTO executor : executors) {
            ProgramDTO programDTO = executor.getProgramDTO();

            HistoryRowV3DTO row = new HistoryRowV3DTO(
                    programDTO.getProgramType(),
                    programDTO.getProgramUserString(),
                    executor.getArchitectureTypeSelected(),
                    executor.getDegree(),
                    executor.getResult(),
                    executor.getTotalCycles(),
                    executor.getVariablesToValuesSorted(),
                    executor.getInputsValuesOfUser()
            );

            historyRows.add(row);
        }

        return historyRows;
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
    public List<AvailableProgramsDTO> getAvailableMainProgramsDTOsList() {
        List<AvailableProgramsDTO> availableProgramsDTOsList = new ArrayList<>();

        for(Program mainProgram : programsHolder.getMainPrograms()) {
            availableProgramsDTOsList.add(buildAvailableProgramDTO(mainProgram));
        }

        return availableProgramsDTOsList;
    }

    @Override
    public List<AvailableFunctionsDTO> getAvailableFunctionsDTOsList() {
        List<AvailableFunctionsDTO> availableFunctionsDTOsList = new ArrayList<>();

        for(Program function : programsHolder.getMainPrograms()) {
            availableFunctionsDTOsList.add(buildAvailableFunctionDTO(function));
        }

        return availableFunctionsDTOsList;    }


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
                program.getProgramType().getType(),
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
                programExecutor.getInputsValuesOfUser(),
                programExecutor.getArchitectureTypeSelected().getArchitectureRepresentation()
        );
    }

    public AvailableProgramsDTO buildAvailableProgramDTO(Program program) {
        return new AvailableProgramsDTO(
                program.getName(),
                program.getUploaderName(),
                program.getInstructionsList().size(),
                getMaxDegree(program.getName()),
                program.getExecutionsCount(),
                program.getAverageCreditCost()
        );
    }

    public AvailableFunctionsDTO buildAvailableFunctionDTO(Program function) {
        return new AvailableFunctionsDTO(
                function.getName(),
                function.getMainProgramNameOfThisProgram(),
                function.getUploaderName(),
                function.getInstructionsList().size(),
                getMaxDegree(function.getName())
        );
    }

    @Override
    public void initializeDebugger(String programName, int degree, List<Long> inputs, String uploaderName) {
        Program workingProgram = getExpandedProgram(programName, degree);
        UserDTO userDTO = getUserDTO(uploaderName);

        // In any case, overwrite the previous value
        usernameToDebug.put(uploaderName, new DebugImpl(workingProgram, degree, inputs, uploaderName, userDTO));
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
        UserDTO userDTO = getUserDTO(uploaderName);

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
