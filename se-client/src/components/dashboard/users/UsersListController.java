package components.dashboard.users;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.Closeable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static utils.Constants.REFRESH_RATE;

public class UsersListController implements Closeable {

    private Timer timer;
    private TimerTask listRefresher;
    private final IntegerProperty totalUsers;

    @FXML private Label usersLabel;
    @FXML private ListView<String> usersListView;

    public UsersListController() {
        totalUsers = new SimpleIntegerProperty();
    }

    @FXML
    public void initialize() {
        usersLabel.textProperty().bind(Bindings.concat("Available users: (", totalUsers.asString(), ")"));
    }

    private void updateUsersList(List<String> usersList) {
        Platform.runLater(() -> {
            ObservableList<String> items = usersListView.getItems();
            items.clear();
            items.addAll(usersList);
            totalUsers.setValue(usersList.size());
        });
    }

    public void startListRefresher() {
        listRefresher = new UserListRefresher(this::updateUsersList);
        timer = new Timer();
        timer.schedule(listRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    @FXML void onUnselectUserClicked() {
    }

    @Override
    public void close() {
        usersListView.getItems().clear();
        totalUsers.setValue(0);
        if (listRefresher != null && timer != null) {
            listRefresher.cancel();
            timer.cancel();
        }
    }
}
