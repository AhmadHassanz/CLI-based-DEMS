package frontend;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import users.User;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        roleCombo.getItems().setAll("Admin", "Investigator", "Analyst");
    }

    @FXML
    private void handleLogin() throws IOException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            messageLabel.setText("Enter username, password, and role.");
            return;
        }

        User user = FrontendState.userService.login(username, password);
        if (user == null || !user.getRole().equalsIgnoreCase(role)) {
            FrontendState.auditStack.push(username.isEmpty() ? "UNKNOWN" : username, "Failed JavaFX login attempt as " + role);
            messageLabel.setText("Invalid credentials or role mismatch.");
            return;
        }

        FrontendState.setCurrentUser(user);
        FrontendState.auditStack.push(user.getUsername(), "Logged into JavaFX dashboard");

        switch (user.getRole().toLowerCase()) {
            case "admin" -> FrontendApp.showAdminDashboard();
            case "investigator" -> FrontendApp.showInvestigatorDashboard();
            case "analyst" -> FrontendApp.showAnalystDashboard();
            default -> messageLabel.setText("Unsupported role.");
        }
    }

    @FXML
    private void handleClear() {
        usernameField.clear();
        passwordField.clear();
        roleCombo.setValue(null);
        messageLabel.setText("");
    }
}
