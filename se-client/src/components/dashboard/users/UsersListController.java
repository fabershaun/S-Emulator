package components.dashboard.users;

import dto.v3.UserDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.Closeable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static utils.Constants.REFRESH_RATE;

public class UsersListController implements Closeable {

    private Timer timer;
    private TimerTask listRefresher;
    private final IntegerProperty totalUsers;
    private final ObjectProperty<UserDTO> selectedUserProperty = new SimpleObjectProperty<>();

    @FXML private Label usersLabel;
    @FXML private TableView<UserDTO> usersTableView;
    @FXML private TableColumn<UserDTO, String> colUserName;
    @FXML private TableColumn<UserDTO, Number> colCurrentCredits;
    @FXML private TableColumn<UserDTO, Number> colExecutionsCount;
    @FXML private TableColumn<UserDTO, Number> colMainProgramsCount;
    @FXML private TableColumn<UserDTO, Number> colSubFunctionsCount;
    @FXML private TableColumn<UserDTO, Number> colUsedCredits;


    public UsersListController() {
        totalUsers = new SimpleIntegerProperty();
    }

    @FXML
    public void initialize() {
        usersLabel.textProperty().bind(Bindings.concat("Available users: (", totalUsers.asString(), ")"));

        colUserName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));
        colMainProgramsCount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getMainProgramsCount()));
        colSubFunctionsCount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getSubFunctionsCount()));
        colCurrentCredits.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getCurrentCredits()));
        colUsedCredits.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getUsedCredits()));
        colExecutionsCount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getExecutionsCount()));

        usersTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUserSelected) -> {
            selectedUserProperty.set(newUserSelected);
        });
    }

    private void updateUsersList(List<UserDTO> usersList) {
        Platform.runLater(() -> {
            UserDTO currentSelection = usersTableView.getSelectionModel().getSelectedItem();

            // Update list items
            ObservableList<UserDTO> items = usersTableView.getItems();
            items.setAll(usersList);

            // Restore selection if still exists in the new list
            if (currentSelection != null) {
                String selectedName = currentSelection.getUserName();

                usersList.stream()
                        .filter(p -> p.getUserName().equals(selectedName))
                        .findFirst()
                        .ifPresent(p -> usersTableView.getSelectionModel().select(p));
            }

            // Restore selection if still exists in the new list
            if (currentSelection != null && usersList.contains(currentSelection)) {
                usersTableView.getSelectionModel().select(currentSelection);
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

    public ObjectProperty<UserDTO> selectedUserProperty() {
        return selectedUserProperty;
    }

    @FXML void onUnselectUserClicked() {
        usersTableView.getSelectionModel().clearSelection();
    }

    @Override
    public void close() {
        usersTableView.getItems().clear();
        totalUsers.setValue(0);
        if (listRefresher != null && timer != null) {
            listRefresher.cancel();
            timer.cancel();
        }
    }
}
