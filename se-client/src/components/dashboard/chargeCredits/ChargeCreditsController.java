package components.dashboard.chargeCredits;

import components.dashboard.mainDashboard.DashboardController;
import javafx.beans.property.LongProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ChargeCreditsController {

    private DashboardController dashboardController;
    private LongProperty totalCreditsAmount;

    @FXML private TextField chargeCreditsTextField;


    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setProperty(LongProperty totalCreditsAmount) {
        this.totalCreditsAmount = totalCreditsAmount;
    }

    @FXML
    public void initialize() {
        // Allow only integer input
        chargeCreditsTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            // Keep only digits
            String digitsOnly = newValue.replaceAll("\\D", "");

            // Limit to 15 digits
            if (digitsOnly.length() > 12) {
                digitsOnly = digitsOnly.substring(0, 12);
            }

            // Update only if a change is actually needed
            if (!digitsOnly.equals(newValue)) {
                chargeCreditsTextField.setText(digitsOnly);
            }
        });
    }

    @FXML
    public void onChargeCreditsButton() {
        String text = chargeCreditsTextField.getText().trim();
        if (text.isEmpty()) {
            return; // No input, nothing to add
        }

        // Parse the integer and add to the total
        long  amountToAdd = Long.parseLong(text);

        totalCreditsAmount.set(totalCreditsAmount.get() + amountToAdd);

        // Clear field after successful charge
        chargeCreditsTextField.clear();

        // Update in engine
        dashboardController.addCreditsToUser(amountToAdd);
    }
}
