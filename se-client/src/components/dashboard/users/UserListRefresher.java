package components.dashboard.users;

import dto.v3.UserDTO;
import javafx.application.Platform;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

import static components.dashboard.mainDashboard.DashboardController.showError;
import static utils.Constants.GSON_INSTANCE;
import static utils.Constants.USERS_LIST_PAGE;

public class UserListRefresher extends TimerTask {

    private final Consumer<List<UserDTO>> usersListConsumer;

    public UserListRefresher(Consumer<List<UserDTO>> usersListConsumer) {
        this.usersListConsumer = usersListConsumer;
    }

    @Override
    public void run() {
        HttpClientUtil.runAsync(USERS_LIST_PAGE, null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    showError("Server Error", "Failed to load users list" + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Platform.runLater(() -> showError("Load failed", errorBody));
                    return;
                }

                String jsonArrayOfUsers = response.body().string();
                UserDTO[] usersArray = GSON_INSTANCE.fromJson(jsonArrayOfUsers, UserDTO[].class);

                // Handle null / empty response safely
                if (usersArray == null) {
                    usersListConsumer.accept(List.of());
                    return;
                }

                usersListConsumer.accept(Arrays.asList(usersArray));
            }
        });
    }
}
