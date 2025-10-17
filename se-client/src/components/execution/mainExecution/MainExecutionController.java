package components.execution.mainExecution;

import com.google.gson.JsonObject;
import services.ProgramPollingService;
import services.AppService;
import utils.ui.AlertUtils;
import components.execution.chainInstructionsTable.ChainInstructionsTableController;
import components.execution.debuggerExecutionMenu.DebuggerExecutionMenuController;
import components.execution.mainInstructionsTable.MainInstructionsTableController;
import components.execution.summaryLineOfMainInstructionsTable.SummaryLineController;
import components.execution.topToolBar.ExpansionCollapseModelV3;
import components.execution.topToolBar.HighlightSelectionModelV3;
import components.execution.topToolBar.TopToolBarController;
import components.mainAppV3.MainAppController;
import dto.v2.DebugDTO;
import dto.v2.InstructionDTO;
import dto.v2.ProgramDTO;
import dto.v2.ProgramExecutorDTO;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import okhttp3.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static utils.Constants.*;
import static utils.Constants.GSON_INSTANCE;

public class MainExecutionController {

    private MainAppController mainAppController;
    private static final ExecutorService CLIENT_EXECUTOR = Executors.newFixedThreadPool(2);

    private AppService appService;
    private ProgramPollingService programPollingService;

    private LongProperty totalCreditsAmount;
    private StringProperty currentUserName;
    private final ObjectProperty<ProgramDTO> selectedProgramProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty = new SimpleObjectProperty<>(null);
    private final StringProperty chosenArchitectureProperty = new SimpleStringProperty("");
    private final IntegerProperty architectureRankProperty = new SimpleIntegerProperty();
    private final ExpansionCollapseModelV3 degreeModel = new ExpansionCollapseModelV3();
    private final HighlightSelectionModelV3 highlightSelectionModel = new HighlightSelectionModelV3();

    @FXML private HBox topToolBar;
    @FXML private TopToolBarController topToolBarController;    // must: field name = fx:id + "Controller"
    @FXML private TableView<InstructionDTO> mainInstructionsTable;
    @FXML private MainInstructionsTableController mainInstructionsTableController;          // must: field name = fx:id + "Controller"
    @FXML private HBox summaryLine;
    @FXML private SummaryLineController summaryLineController;
    @FXML private TableView<InstructionDTO> chainInstructionTable;
    @FXML private ChainInstructionsTableController chainInstructionTableController;    // must: field name = fx:id + "Controller"
    @FXML private VBox debuggerExecutionMenu;
    @FXML private DebuggerExecutionMenuController debuggerExecutionMenuController;  // must: field name = fx:id + "Controller"


    @FXML
    public void initialize() {
        if (
            topToolBarController != null &&
            mainInstructionsTableController != null &&
            summaryLineController != null &&
            chainInstructionTableController != null &&
            debuggerExecutionMenuController != null
        ) {
            initToolBarController();
            initMainInstructionsTableController();
            initSummaryLineController();
            initDebuggerExecutionMenuController();

            initializeListeners();
        }
    }

    private void initializeListeners() {
        selectedProgramProperty.addListener((obs, oldProg, newProgram) -> {
            if (newProgram != null) {
                topToolBarController.setProgramCurrentName(selectedProgramProperty.get().getProgramName());
            } else {
                selectedProgramProperty.set(null);
            }
        });
    }

    private void initToolBarController() {
        topToolBarController.setExecutionController(this);
        topToolBarController.setModels(degreeModel, highlightSelectionModel);
    }

    private void initMainInstructionsTableController() {
        mainInstructionsTableController.setExecutionController(this);
        mainInstructionsTableController.setProperty(selectedProgramProperty, chosenArchitectureProperty, architectureRankProperty);
        mainInstructionsTableController.setModels(highlightSelectionModel);
        mainInstructionsTableController.initializeListeners();
    }

    private void initSummaryLineController() {
        summaryLineController.setProperty(selectedProgramProperty);
        summaryLineController.initializeBindings();
    }

    private void initDebuggerExecutionMenuController() {
        debuggerExecutionMenuController.setExecutionController(this);
        debuggerExecutionMenuController.setProperty(selectedProgramProperty, programAfterExecuteProperty, chosenArchitectureProperty, architectureRankProperty);
        debuggerExecutionMenuController.initializeListeners();
    }

    private void initDegreeModel() {
        if (selectedProgramProperty.get() == null) return;

        degreeModel.setCurrentDegree(0);
        degreeModel.setProgram(selectedProgramProperty.get());

        String programSelectedName = requireCurrentProgramName();
        String maxDegreeUrl = buildUrlWithQueryParam(MAX_DEGREE_PATH, PROGRAM_NAME_QUERY_PARAM, programSelectedName);

        appService.fetchMaxDegreeAsync(
                maxDegreeUrl,
                maxDegree -> Platform.runLater(() -> degreeModel.setMaxDegree(maxDegree)),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Error", "Failed to fetch max degree: " + errorMsg)
                )
        );
    }

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void setProgramPollingService(ProgramPollingService programPollingService) {
        this.programPollingService = programPollingService;
    }

    public void setProgramService(AppService appService) {
        this.appService = appService;
    }

    public void setProperty(StringProperty currentUserName, LongProperty totalCreditsAmount) {
        this.currentUserName = currentUserName;
        this.totalCreditsAmount = totalCreditsAmount;
    }

    public void setupAfterMainAppInit(String programSelectedName) {
        String url = buildUrlWithQueryParam(CURRENT_PROGRAM_DATA_PATH, PROGRAM_NAME_QUERY_PARAM, programSelectedName);

        appService.fetchProgramDataAsync(
                url,
                program -> Platform.runLater(() -> {
                    selectedProgramProperty.set(program);
                    initDegreeModel();
                }),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Error", "Failed to fetch program: " + errorMsg)
                )
        );
    }

    public void clearArchitectureColors() {
        if (mainInstructionsTableController != null) {
            mainInstructionsTableController.clearArchitectureColors();
        }
    }

    private String buildUrlWithQueryParam(String path, String queryParameterName, String queryParameter) {
        return HttpUrl.parse(path)
                .newBuilder()
                .addQueryParameter(queryParameterName, queryParameter)
                .build()
                .toString();
    }

    public void jumpToDegree(int targetDegree) {
        String programName = requireCurrentProgramName();
        String jumpToDegreeUrl = HttpUrl.parse(JUMP_TO_DEGREE_PATH)
                .newBuilder()
                .addQueryParameter(PROGRAM_NAME_QUERY_PARAM, programName)
                .addQueryParameter(TARGET_DEGREE_QUERY_PARAM, String.valueOf(targetDegree))
                .build()
                .toString();

        appService.fetchJumpDegreeAsync(
                jumpToDegreeUrl,
                expandedProgram -> Platform.runLater(() -> {
                    selectedProgramProperty.set(expandedProgram);
                    degreeModel.setCurrentDegree(targetDegree);
                }),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Network Error", errorMsg)
                )
        );
    }

    public void onInstructionSelected(InstructionDTO selectedInstruction) {
        int instructionNumber = selectedInstruction.getInstructionNumber();
        List<InstructionDTO> selectedInstructionChain = selectedProgramProperty.get().getExpandedProgram().get(instructionNumber - 1); // -1 because we started the count from 0
        chainInstructionTableController.fillTable(selectedInstructionChain);
    }

    public void onInstructionDeselected() {
        chainInstructionTableController.clearChainTable();
    }

    private ProgramDTO requireCurrentProgram() {
        ProgramDTO program = selectedProgramProperty.get();
        if (program == null) {
            AlertUtils.showError("Unexpected Error", "No active program loaded in execution view.");
            return null;
        }
        return program;
    }

    private String requireCurrentProgramName() {
        ProgramDTO currentProgram = requireCurrentProgram();
        if (currentProgram == null) {
            return null;
        }

        String currentProgramName = currentProgram.getProgramName();
        if (currentProgramName == null) {
            AlertUtils.showError("Unexpected Error", "Program has no name (unexpected state).");
            return null;
        }

        return currentProgramName;
    }

    public void disableToolBarComponents(boolean disable) {
        topToolBarController.setComponentsDisabled(disable);
    }

    public void EnterDebugMode() {
        mainInstructionsTableController.highlightLineDebugMode(0);  // Highlight the first line on table instructions
        disableToolBarComponents(true);
    }

    public void loadArchitectureTypes() {
        appService.fetchArchitectureTypesAsync(
                ARCHITECTURE_TYPES_PATH,
                architectureList -> Platform.runLater(() -> {
                    debuggerExecutionMenuController.getArchitectureComboBox()
                            .getItems()
                            .setAll(architectureList);
                }),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Error", "Failed to load architecture types: " + errorMsg)
                )
        );
    }

    public boolean checkIfHasEnoughCreditsToPlay() {
        String programName = requireCurrentProgramName();
        String architecture = chosenArchitectureProperty.get();

        // Build JSON body
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty(PROGRAM_NAME_QUERY_PARAM, programName);
        jsonBody.addProperty(CHOSEN_ARCHITECTURE_STR_QUERY_PARAM, architecture);
        RequestBody requestBody = RequestBody.create(GSON_INSTANCE.toJson(jsonBody), MEDIA_TYPE_JSON);

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        appService.fetchUserHasEnoughCredits(
                CREDIT_CHECK_PATH,
                requestBody,
                future::complete,
                errorMsg -> future.complete(false)
        );

        try {
            boolean isEnoughCredits = future.get(2, TimeUnit.SECONDS);
            if (!isEnoughCredits) {
                Platform.runLater(() -> mainAppController.popUpToastMessage("Not enough credits to run program", false));
            }

            return isEnoughCredits;
        } catch (Exception e) {
            Platform.runLater(() -> AlertUtils.showError("Credits Check Failed", e.getMessage()));
            return false;
        }
    }

    public void runProgram(List<Long> inputValues) {

        RequestBody requestBody = buildRunProgramRequestBody(inputValues);
        appService.fetchRunProgramAsync(
                RUN_PROGRAM_PATH,
                requestBody,
                runId -> Platform.runLater(() -> {
                    programPollingService.startPolling(() -> checkProgramStatus(runId));
                }),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Run Failed", errorMsg)
                )
        );

        // Update credit amount
        appService.fetchUserCreditsAsync(
                FETCH_CREDITS_PATH,
                null,
                credits -> Platform.runLater(() -> totalCreditsAmount.set(credits)),
                errorMsg -> Platform.runLater(() -> AlertUtils.showError("Error", errorMsg))
        );
    }

    // Checks the current execution status of the program on the server
    private void checkProgramStatus(String runId) {
        try {
            String url = buildUrlWithQueryParam(PROGRAM_STATUS_PATH, RUN_ID_QUERY_PARAM, runId);

            appService.fetchProgramStatusAsync(
                    url,
                    state -> handleProgramState(runId, state), // "DONE", "FAILED", etc.
                    errorMsg -> Platform.runLater(() ->
                            AlertUtils.showError("Program Execution Failed", errorMsg)
                    )
            );

        } catch (Exception e) {
            System.err.println("Polling failed: " + e.getMessage());
        }
    }

    private RequestBody buildRunProgramRequestBody(List<Long> inputs) {
        // Collect all required data from the current state
        String programName = requireCurrentProgramName();
        String architecture = chosenArchitectureProperty.get();
        int degree = degreeModel.currentDegreeProperty().get();

        // Build JSON body
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty(PROGRAM_NAME_QUERY_PARAM, programName);
        jsonBody.addProperty(CHOSEN_ARCHITECTURE_STR_QUERY_PARAM, architecture);
        jsonBody.addProperty(DEGREE_QUERY_PARAM, degree);
        jsonBody.add(INPUTS_VALUES_QUERY_PARAM, GSON_INSTANCE.toJsonTree(inputs));

        // Convert to RequestBody
        return RequestBody.create(GSON_INSTANCE.toJson(jsonBody), MEDIA_TYPE_JSON);
    }

    // Handles the state returned from the server ("DONE", "FAILED", etc.)
    private void handleProgramState(String runId, String state) {
        if (state.startsWith("FAILED")) {
            handleProgramFailed(state);
        } else if ("DONE".equals(state)) {
            handleProgramDone(runId);
        }
    }

    // Called when the program finishes successfully
    private void handleProgramDone(String runId) {
        programPollingService.stopPolling();

        String url = buildUrlWithQueryParam(PROGRAM_AFTER_RUN_PATH, RUN_ID_QUERY_PARAM, runId);

        appService.fetchProgramAfterRunAsync(
                url,
                result -> Platform.runLater(() -> {
                    programAfterExecuteProperty.set(result);
                }),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Fetch Failed", errorMsg)
                )
        );
    }

    // Called when the program fails on the server
    private void handleProgramFailed(String state) {
        programPollingService.stopPolling();
        String errorMsg = state.contains(":") ? state.split(":", 2)[1] : "Program execution failed.";
        Platform.runLater(() -> AlertUtils.showError("Program Execution Failed", errorMsg));
    }

    public void initializeDebugger(List<Long> inputValues) {
        RequestBody requestBody = buildRunProgramRequestBody(inputValues);

        appService.initializeDebugger(
            INITIALIZE_DEBUGGER_PATH,
            requestBody,
            () -> Platform.runLater(() -> {
                debuggerExecutionMenuController.enterDebugging();
                debuggerExecutionMenuController.onResume();
            }),
            errorMsg -> Platform.runLater(() -> {
                AlertUtils.showError("Initialized debugger Failed", errorMsg);
                debuggerExecutionMenuController.enterNewRunPressed();
            })
        );
    }

    public void debugResume(Consumer<DebugDTO> onComplete) {
        List<Boolean> breakPoints = mainInstructionsTableController.getBreakPoints();

        appService.debugResumeAsync(
                RESUME_DEBUGGER_PATH,
                breakPoints,
                debugStep -> Platform.runLater(() -> {
                    if (debugStep == null) {
                        AlertUtils.showError("Debug Resume", "Server returned no data.");
                        return;
                    }

                    if (debugStep.hasMoreInstructions()) {
                        updateControllerAfterDebugStep(debugStep);
                    }
                    onComplete.accept(debugStep);
                }),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Resume Failed", errorMsg)
                )
        );
    }

    public void debugStepOver(Consumer<DebugDTO> onComplete) {
        appService.debugStepOverAsync(
                STEP_OVER_DEBUGGER_PATH,
                debugStep -> Platform.runLater(() -> {
                    if (debugStep == null) {
                        AlertUtils.showError("Debug Resume", "Server returned no data.");
                        return;
                    }

                    if (debugStep.hasMoreInstructions()) {
                        updateControllerAfterDebugStep(debugStep);
                    }

                    updateControllerAfterDebugStep(debugStep);
                    onComplete.accept(debugStep);
                }),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Step Over Failed", errorMsg)
                )
        );
    }

    private void updateControllerAfterDebugStep(DebugDTO debugStep) {
        mainInstructionsTableController.highlightLineDebugMode(debugStep.getNextInstructionNumber());  // Highlight line on table instructions
        topToolBarController.setComponentsDisabled(true);
    }

    public void finishDebug() {
        mainInstructionsTableController.turnOffHighlighting();
        topToolBarController.setComponentsDisabled(false);
    }


    public void debugStop() {
        appService.debugStopAsync(
                STOP_DEBUGGER_PATH,
                () -> Platform.runLater(() -> {
                    finishDebug();
                    mainAppController.popUpToastMessage("Debugger stopped successfully", true);
                }),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Stop Failed", errorMsg)
                )
        );
    }

    public void debugStepBack(Consumer<DebugDTO> onComplete) {
        appService.debugStepBackAsync(
                STEP_BACK_DEBUGGER_PATH,
                debugStep -> Platform.runLater(() -> {
                    if (debugStep == null) {
                        AlertUtils.showError("Debug Step Back", "Server returned no data.");
                        return;
                    }

                    updateControllerAfterDebugStep(debugStep);
                    onComplete.accept(debugStep);
                }),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Step Back Failed", errorMsg)
                )
        );
    }
}
