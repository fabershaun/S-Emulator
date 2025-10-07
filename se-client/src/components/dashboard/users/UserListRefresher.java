package components.dashboard.users;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import static utils.Constants.GSON_INSTANCE;
import static utils.Constants.USERS_LIST;

public class UserListRefresher extends TimerTask {

    private final Consumer<List<String>> usersListConsumer;

    public UserListRefresher(Consumer<List<String>> usersListConsumer) {
        this.usersListConsumer = usersListConsumer;
    }

    @Override
    public void run() {
        HttpClientUtil.runAsync(USERS_LIST, null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // todo: write
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String jsonArrayOfUsersNames = response.body().string();
                String[] usersNames = GSON_INSTANCE.fromJson(jsonArrayOfUsersNames, String[].class);
                usersListConsumer.accept(Arrays.asList(usersNames));
            }
        });
    }
}
