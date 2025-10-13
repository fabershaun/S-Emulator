package components.dashboard.availablePrograms;

import com.google.gson.reflect.TypeToken;
import utils.ui.AlertUtils;
import dto.v3.MainProgramDTO;
import javafx.application.Platform;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.HttpClientUtil;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

import static utils.Constants.*;


public class ProgramsListRefresher extends TimerTask {

    private final Consumer<List<MainProgramDTO>> programsListConsumer;

    public ProgramsListRefresher(Consumer<List<MainProgramDTO>> programsListConsumer) {
        this.programsListConsumer = programsListConsumer;
    }

    @Override
    public void run() {
        HttpClientUtil.runAsync(AVAILABLE_PROGRAMS_LIST_PAGE, null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> AlertUtils.showError("Server Error", "Failed to load available programs" + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                if (response.code() != 200) {
                    Platform.runLater(() -> AlertUtils.showError("Server Error", "Trying to load main programs list, the server returned: " + response.code()));
                    return;
                }

                // Read body only after confirming 200 OK
                String jsonResponse = HttpClientUtil.readResponseBodySafely(response);

                // Define the generic list type for deserialization
                Type listType = new TypeToken<List<MainProgramDTO>>() {}.getType();

                // Parse JSON into a list of AvailableProgramDTO objects
                List<MainProgramDTO> programs = GSON_INSTANCE.fromJson(jsonResponse, listType);

                // Update UI safely on JavaFX thread
                Platform.runLater(() -> programsListConsumer.accept(programs));
            }
        });
    }
}
