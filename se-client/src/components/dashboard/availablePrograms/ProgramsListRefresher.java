package components.dashboard.availablePrograms;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

import static utils.Constants.*;

public class ProgramsListRefresher extends TimerTask {

    private final Consumer<List<?>> programsListConsumer;

    public ProgramsListRefresher(Consumer<List<?>> programsListConsumer) {
        this.programsListConsumer = programsListConsumer;
    }

    @Override
    public void run() {
        HttpClientUtil.runAsync(AVAILABLE_PROGRAMS_LIST_PAGE, null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        programsListConsumer.accept(List.of("Failed to load programs: " + e.getMessage()))
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String jsonResponse  = response.body().string();
                // Define the generic list type for deserialization
                Type listType = new TypeToken<List<?>>() {}.getType();

                // Parse JSON into a list of AvailableProgramDTO objects
                List<?> programs = GSON_INSTANCE.fromJson(jsonResponse, listType);

                // Update UI on JavaFX thread
                Platform.runLater(() -> programsListConsumer.accept(programs));
            }
        });
    }
}     // TODO: fix ? -> type
