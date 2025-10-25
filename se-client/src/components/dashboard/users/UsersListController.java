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
    private ObjectProperty<UserDTO> selectedUserProperty;

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

        usersTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUserSelected) -> selectedUserProperty.set(newUserSelected));
    }

    public void setProperty(ObjectProperty<UserDTO> selectedUserProperty) {
        this.selectedUserProperty = selectedUserProperty;
    }

    private void updateUsersList(List<UserDTO> newUsersList) {
        Platform.runLater(() -> {
            ObservableList<UserDTO> currentItems = usersTableView.getItems();

            // Save current selection (if any)
            UserDTO selected = usersTableView.getSelectionModel().getSelectedItem();
            String selectedName = selected != null ? selected.getUserName() : null;

            // Add new users
            for (UserDTO newUser : newUsersList) {
                boolean exists = currentItems.stream()
                        .anyMatch(u -> u.getUserName().equals(newUser.getUserName()));
                if (!exists) {
                    currentItems.add(newUser);
                }
            }

            // Update existing users' data (fields that may change)
            for (UserDTO existing : currentItems) {
                newUsersList.stream()
                        .filter(u -> u.getUserName().equals(existing.getUserName()))
                        .findFirst()
                        .ifPresent(updated -> {
                            // Update all relevant fields shown in the table
                            existing.setMainProgramsCount(updated.getMainProgramsCount());
                            existing.setSubFunctionsCount(updated.getSubFunctionsCount());
                            existing.setCurrentCredits(updated.getCurrentCredits());
                            existing.setUsedCredits(updated.getUsedCredits());
                            existing.setExecutionsCount(updated.getExecutionsCount());
                        });
            }

            // Restore selection if possible
            if (selectedName != null) {
                currentItems.stream()
                        .filter(u -> u.getUserName().equals(selectedName))
                        .findFirst()
                        .ifPresent(u -> usersTableView.getSelectionModel().select(u));
            }

            // Update label
            totalUsers.set(currentItems.size());

            usersTableView.refresh();
        });
    }

    public void startListRefresher() {
        listRefresher = new UserListRefresher(this::updateUsersList);
        timer = new Timer();
        timer.schedule(listRefresher, 0, REFRESH_RATE);
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
