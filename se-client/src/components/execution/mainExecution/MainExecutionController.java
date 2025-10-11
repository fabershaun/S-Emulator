package components.execution.mainExecution;

import components.UIUtils.AlertUtils;
import components.UIUtils.ToastUtil;
import components.execution.chainInstructionsTable.ChainInstructionsTableController;
import components.execution.debuggerExecutionMenu.DebuggerExecutionMenuController;
import components.execution.mainInstructionsTable.MainInstructionsTableController;
import components.execution.summaryLineOfMainInstructionsTable.SummaryLineController;
import components.execution.topToolBar.TopToolBarController;
import components.mainAppV3.MainAppController;
import dto.v2.InstructionDTO;
import dto.v2.ProgramDTO;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
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
    private ProgramDTO currentProgram;

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


    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void setProperty(StringProperty currentUserName, LongProperty totalCreditsAmount) {
    }

    public void setupAfterMainAppInit(String programSelectedName) {
        String finalUrl = HttpUrl
                .parse(CURRENT_PROGRAM_DATA)
                .newBuilder()
                .addQueryParameter(PROGRAM_NAME_QUERY_PARAM, programSelectedName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        AlertUtils.showError("Failed trying get the current chosen program", e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (response.code() != 200) {
                    try {
                        String errorMessage = GSON_INSTANCE.fromJson(responseBody, String.class);

                        Platform.runLater(() -> {
                            if (response.code() == 400) {
                                // 400 = business logic issue, e.g. duplicate file
                                ToastUtil.showToast(
                                        rootStackPane,
                                        errorMessage,
                                        false
                                );
                            } else {
                                // 500 = internal error
                                AlertUtils.showError("Server Error", errorMessage);
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(() ->
                                AlertUtils.showError("Program data load failed", "Server returned " + response.code() + ": " + responseBody)
                        );
                    }
                    return;
                }

                currentProgram = GSON_INSTANCE.fromJson(responseBody, ProgramDTO.class);
            }
        });
    }
}
