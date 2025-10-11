package components.execution.mainExecution;

import components.UIUtils.AlertUtils;
import components.UIUtils.ToastUtil;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.Objects;

import static utils.Constants.*;
import static utils.Constants.GSON_INSTANCE;

public class MainExecutionController {

    private MainAppController mainAppController;
    private LongProperty totalCreditsAmount;
    private final ObjectProperty<ProgramDTO> currentProgramProperty = new SimpleObjectProperty<>();

    private final ExpansionCollapseModelV3 degreeModel = new ExpansionCollapseModelV3();
    private final HighlightSelectionModelV3 highlightSelectionModel = new HighlightSelectionModelV3();

    @FXML private StackPane rootStackPane;
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
        }
    }

    private void initToolBarController() {
        topToolBarController.setExecutionController(this);
        topToolBarController.setModels(degreeModel, highlightSelectionModel);
    }

    private void initMainInstructionsTableController() {

    }

    private void initSummaryLineController() {
    }

    private void initChainInstructionTableController() {

    }

    private void initDebuggerExecutionMenuController() {

    }


    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void setProperty(StringProperty currentUserName, LongProperty totalCreditsAmount) {
    }

    public void setupAfterMainAppInit(String programSelectedName) {
        String url = buildProgramDataUrl(programSelectedName);
        fetchProgramDataAsync(url);
    }

    private String buildProgramDataUrl(String programName) {
        return HttpUrl.parse(CURRENT_PROGRAM_DATA)
                .newBuilder()
                .addQueryParameter(PROGRAM_NAME_QUERY_PARAM, programName)
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
            handleErrorResponse(response.code(), responseBody);
            return;
        }

        // Parse the JSON into a ProgramDTO object
        ProgramDTO program = GSON_INSTANCE.fromJson(responseBody, ProgramDTO.class);

        // Update UI (must be done on JavaFX thread)
        Platform.runLater(() -> {
            currentProgramProperty.set(program); // triggers listeners automatically

            // Optional success notification
            ToastUtil.showToast(rootStackPane, "Program loaded successfully", true);
        });
    }

    private void handleErrorResponse(int statusCode, String responseBody) {
        try {
            String errorMessage = GSON_INSTANCE.fromJson(responseBody, String.class);

            Platform.runLater(() -> {
                switch (statusCode) {
                    case 400 -> {
                        // 400 = business logic issue (user-level error)
                        ToastUtil.showToast(rootStackPane, errorMessage, false);
                    }
                    case 401 -> {
                        // 401 = not logged in / session expired
                        AlertUtils.showError("Unauthorized", "You are not logged in or your session has expired.");
                    }
                    case 404 -> {
                        // 404 = program not found
                        AlertUtils.showError("Program Not Found", errorMessage);
                    }
                    case 500 -> {
                        // 500 = internal server error
                        AlertUtils.showError("Server Error", errorMessage);
                    }
                    default -> {
                        // Any other unexpected code
                        AlertUtils.showError("Unexpected Error",
                                "Server returned code " + statusCode + ": " + errorMessage);
                    }
                }
            });
        } catch (Exception e) {
            Platform.runLater(() ->
                    AlertUtils.showError("Program Data Load Failed",
                            "Server returned " + statusCode + " with invalid JSON:\n" + responseBody)
            );
        }
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
}
