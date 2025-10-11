package components.dashboard.chargeCredits;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import static components.UIUtils.ToastUtil.showToast;

public class ChargeCreditsController {

    private LongProperty totalCreditsAmount;

    @FXML private Button chargeCreditsButton;
    @FXML private TextField chargeCreditsTextField;

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
            String digitsOnly = newValue.replaceAll("[^\\d]", "");

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

//        // Optional: show a confirmation toast
//        showToast("Charged " + amountToAdd + " credits successfully", true);
    }
}
