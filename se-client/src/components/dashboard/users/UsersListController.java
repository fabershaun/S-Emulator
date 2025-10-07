package components.dashboard.users;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
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
    private final StringProperty selectedUserProperty = new SimpleStringProperty();

    @FXML private Label usersLabel;
    @FXML private ListView<String> usersListView;


    public UsersListController() {
        totalUsers = new SimpleIntegerProperty();
    }

    @FXML
    public void initialize() {
        usersLabel.textProperty().bind(Bindings.concat("Available users: (", totalUsers.asString(), ")"));

        usersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUserSelected) -> {
            selectedUserProperty.set(newUserSelected); // can be null if cleared
        });
    }

    private void updateUsersList(List<String> usersList) {
        Platform.runLater(() -> {
            String currentSelection = usersListView.getSelectionModel().getSelectedItem();

            // Update list items
            ObservableList<String> items = usersListView.getItems();
            items.setAll(usersList);

            // Restore selection if still exists in the new list
            if (currentSelection != null && usersList.contains(currentSelection)) {
                usersListView.getSelectionModel().select(currentSelection);
            }

            // Update count
            totalUsers.set(usersList.size());
        });
    }

    public void startListRefresher() {
        listRefresher = new UserListRefresher(this::updateUsersList);
        timer = new Timer();
        timer.schedule(listRefresher, 0, REFRESH_RATE);
    }

    @FXML void onUnselectUserClicked() {
        usersListView.getSelectionModel().clearSelection();
    }

    // Getter for other components to bind to
    public StringProperty selectedUserProperty() {
        return selectedUserProperty;
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
