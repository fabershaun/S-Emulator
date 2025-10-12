package components.execution.mainExecution;

import components.UIUtils.AlertUtils;
import components.execution.chainInstructionsTable.ChainInstructionsTableController;
import components.execution.debuggerExecutionMenu.DebuggerExecutionMenuController;
import components.execution.mainInstructionsTable.MainInstructionsTableController;
import components.execution.summaryLineOfMainInstructionsTable.SummaryLineController;
import components.execution.topToolBar.ExpansionCollapseModelV3;
import components.execution.topToolBar.HighlightSelectionModelV3;
import components.execution.topToolBar.TopToolBarController;
import components.mainAppV3.MainAppController;
import dto.v2.InstructionDTO;
import dto.v2.ProgramDTO;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.HttpClientUtil;
import utils.HttpResponseHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static utils.Constants.*;
import static utils.Constants.GSON_INSTANCE;
import static utils.HttpResponseHandler.handleErrorResponse;

public class MainExecutionController {

    private MainAppController mainAppController;
    private LongProperty totalCreditsAmount;
    private final ObjectProperty<ProgramDTO> selectedProgramProperty = new SimpleObjectProperty<>();

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
            initChainInstructionTableController();
            initDebuggerExecutionMenuController();

            initializeListeners();
        }
    }

    private void initializeListeners() {
        selectedProgramProperty.addListener((obs, oldProg, newProgram) -> { // todo: remove
            if (newProgram != null) {
                topToolBarController.setProgramCurrentName(selectedProgramProperty.get().getProgramName());
            }
        });
    }

    private void initToolBarController() {
        topToolBarController.setExecutionController(this);
        topToolBarController.setModels(degreeModel, highlightSelectionModel);
    }

    private void initMainInstructionsTableController() {
        mainInstructionsTableController.setExecutionController(this);
        mainInstructionsTableController.setModels(highlightSelectionModel);
        mainInstructionsTableController.setProperty(selectedProgramProperty);

    }

    private void initSummaryLineController() {
    }

    private void initChainInstructionTableController() {

    }

    private void initDebuggerExecutionMenuController() {

    }

    private void initDegreeModel() {
        if (selectedProgramProperty.get() == null) return;

        degreeModel.setCurrentDegree(0);
        degreeModel.setProgram(selectedProgramProperty.get());

        String programSelectedName = selectedProgramProperty.get().getProgramName();
        String maxDegreeUrl = buildUrlWithQueryParam(MAX_DEGREE_PATH, PROGRAM_NAME_QUERY_PARAM, programSelectedName);
        fetchMaxDegreeAsync(maxDegreeUrl)     // Get the max degree of the program: from the server
                .thenAccept(maxDegree ->
                        Platform.runLater(() -> degreeModel.setMaxDegree(maxDegree))
                )
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtils.showError("Error", "Failed to fetch max degree: " + ex.getMessage())
                    );
                    return null;
                });
    }

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void setProperty(StringProperty currentUserName, LongProperty totalCreditsAmount) {
    }

    public void setupAfterMainAppInit(String programSelectedName) {
        String url = buildUrlWithQueryParam(CURRENT_PROGRAM_DATA_PATH, PROGRAM_NAME_QUERY_PARAM, programSelectedName);
        fetchProgramDataAsync(url);
    }

    private String buildUrlWithQueryParam(String path, String queryParameterName, String queryParameter) {
        return HttpUrl.parse(path)
                .newBuilder()
                .addQueryParameter(queryParameterName, queryParameter)
                .build()
                .toString();
    }

    private void fetchProgramDataAsync(String url) {
        HttpClientUtil.runAsync(url, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> AlertUtils.showError("Network Error", e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                handleProgramDataResponse(response);
            }
        });
    }

    private void handleProgramDataResponse(Response response) throws IOException {
        // Read the response body safely
        String responseBody = HttpClientUtil.readResponseBodySafely(response);

        // Check for non-200 HTTP codes
        if (response.code() != 200) {
            handleErrorResponse(response.code(), responseBody, "Loading program data");
            return;
        }

        // Parse the JSON into a ProgramDTO object
        ProgramDTO program = GSON_INSTANCE.fromJson(responseBody, ProgramDTO.class);

        // Update UI and Models (must be done on JavaFX thread after the program was loaded)
        Platform.runLater(() -> {
            selectedProgramProperty.set(program); // triggers listeners automatically
            initDegreeModel();
        });
    }

    public void jumpToDegree(int target) {
//        int maxDegree = engine.getMaxDegree(selectedProgramProperty.get().getProgramName());
//        int safeTargetDegree = Math.max(0, Math.min(target, maxDegree));          // Clamp the requested degree to a valid range [0, maxDegree]
//        String activeProgramName = getActiveProgramName();
//        ExpandProgramTask expansionTask = new ExpandProgramTask(activeProgramName, engine, safeTargetDegree);
//
//        expansionTask.setOnSucceeded(ev -> {
//            ProgramDTO programByDegree = expansionTask.getValue();
//            selectedProgramProperty.set(programByDegree);
//            degreeModel.setMaxDegree(maxDegree);
//            degreeModel.setCurrentDegree(safeTargetDegree);
//        });
//
//        expansionTask.setOnFailed(ev -> handleTaskFailure(expansionTask, "Expand failed"));
//
//        new Thread(expansionTask, "expand-thread").start();
    }

    private CompletableFuture<Integer> fetchMaxDegreeAsync(String url) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        HttpClientUtil.runAsync(url, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> AlertUtils.showError("Network Error", e.getMessage()));
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (response.code() != 200) {
                    HttpResponseHandler.handleErrorResponse(response.code(), responseBody, "Getting max degree");
                    future.completeExceptionally(new RuntimeException("Bad response: " + response.code()));
                    return;
                }

                try {
                    int maxDegree = GSON_INSTANCE.fromJson(responseBody, Integer.class);
                    future.complete(maxDegree);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    public void onInstructionSelected(InstructionDTO selectedInstruction) {
        int instructionNumber = selectedInstruction.getInstructionNumber();
        List<InstructionDTO> selectedInstructionChain = selectedProgramProperty.get().getExpandedProgram().get(instructionNumber - 1); // -1 because we started the count from 0
        chainInstructionTableController.fillTable(selectedInstructionChain);
    }

    public void onInstructionDeselected() {
        chainInstructionTableController.clearChainTable();
    }

}
