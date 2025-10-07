package components.dashboard.usersHistory;

import components.dashboard.mainDashboard.DashboardController;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.HttpClientUtil;

import java.io.IOException;

import static utils.Constants.GSON_INSTANCE;
import static utils.Constants.USER_HISTORY_LIST_PAGE;

public class UsersHistoryController {

    @FXML private Label userHistoryLabel;
    @FXML private ListView<String> historyListView;

    private DashboardController dashboardController;
    private StringProperty selectedUserProperty;
    private StringProperty currentUserLoginProperty;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setProperty(StringProperty selectedUserProperty, StringProperty currentUserLoginProperty) {
        this.selectedUserProperty = selectedUserProperty;
        this.currentUserLoginProperty = currentUserLoginProperty;
    }

    public void initializeListeners() {
        selectedUserProperty.addListener((obs, oldUser, newUser) -> {
                String targetUser = (newUser == null || newUser.isEmpty())
                        ? currentUserLoginProperty.get()   // fallback: current user
                        : newUser;

                userHistoryLabel.setText("History of: " + targetUser);
                loadUserHistory(targetUser);
                });
    }

    private void loadUserHistory(String username) {
        String finalUrl = HttpUrl
                .parse(USER_HISTORY_LIST_PAGE)
                .newBuilder()
                .addQueryParameter("username", username)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        historyListView.getItems().setAll("Failed to load history: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String json = response.body().string();
                String[] historyItems = GSON_INSTANCE.fromJson(json, String[].class);
                System.out.println("DEBUG - Server returned: " + json);

                Platform.runLater(() -> {
                    if (historyItems == null || historyItems.length == 0) {
                        historyListView.getItems().clear();
                    } else {
                        historyListView.getItems().setAll(historyItems);
                    }
                });
            }
        });
    }
}
