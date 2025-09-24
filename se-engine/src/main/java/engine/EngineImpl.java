package engine;

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

    @Override
    public void loadProgram(Path xmlPath) throws EngineLoadException {
        this.xmlPath = xmlPath;
        Program newProgram;

        XmlProgramLoader loader = new XmlProgramLoader();
        newProgram = loader.load(xmlPath);
        newProgram.validateProgram();
        newProgram.initialize();

        mainProgram = newProgram;
        //executionHistory = new ExecutionHistoryImpl();
    }

    @Override
    public void runProgram(String programName, int degree, Long... inputs) {
        Program program = getProgramByName(programName);

        Program deepCopyOfProgram = program.deepClone();
        deepCopyOfProgram.expandProgram(degree);

        ProgramExecutor programExecutor = new ProgramExecutorImpl(deepCopyOfProgram);

        programExecutor.run(degree, inputs);

        List<ProgramExecutor> executionHistory = programToExecutionHistory.computeIfAbsent(programName, k -> new ArrayList<>());
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
        Program program = getProgramByName(programName);

        return program.calculateProgramMaxDegree();
    }

    @Override
    public ProgramDTO getExpandedProgram(String programName, int degree) {
        Program program = getProgramByName(programName);

        Program deepCopyOfProgram = program.deepClone();
        deepCopyOfProgram.expandProgram(degree);

        return buildProgramDTO(deepCopyOfProgram);
    }

    private ProgramDTO buildProgramDTO(Program program) {
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

    private ProgramExecutorDTO buildProgramExecutorDTO(ProgramDTO programDTO, ProgramExecutor programExecutor) {
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
