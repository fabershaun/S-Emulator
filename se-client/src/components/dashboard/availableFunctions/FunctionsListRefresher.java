package components.dashboard.availableFunctions;

import com.google.gson.reflect.TypeToken;
import dto.v3.FunctionDTO;
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

import static components.dashboard.mainDashboard.DashboardController.showError;
import static utils.Constants.*;


public class FunctionsListRefresher extends TimerTask {

    private final Consumer<List<FunctionDTO>> functionsListConsumer;

    public FunctionsListRefresher(Consumer<List<FunctionDTO>> functionsListConsumer) {
        this.functionsListConsumer = functionsListConsumer;
    }

    @Override
    public void run() {
        HttpClientUtil.runAsync(AVAILABLE_FUNCTIONS_LIST_PAGE, null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    showError("Server Error", "Failed to load available programs" + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String jsonResponse  = response.body().string();
                // Define the generic list type for deserialization
                Type listType = new TypeToken<List<FunctionDTO>>() {}.getType();

                // Parse JSON into a list of AvailableProgramDTO objects
                List<FunctionDTO> programs = GSON_INSTANCE.fromJson(jsonResponse, listType);

                // Update UI on JavaFX thread
                functionsListConsumer.accept(programs);
            }
        });
    }
}
