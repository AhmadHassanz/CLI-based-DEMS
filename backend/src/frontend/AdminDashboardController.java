package frontend;

import audit.LogEntry;
import cases.CaseNode;
import cases.CaseStatus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import users.User;

import java.io.IOException;
import java.time.LocalDate;

public class AdminDashboardController {
    @FXML private Label userLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalCasesLabel;
    @FXML private Label openCasesLabel;
    @FXML private Label auditEventsLabel;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<String> newRoleCombo;
    @FXML private TextField oldUsernameField;
    @FXML private TextField updateUsernameField;
    @FXML private PasswordField updatePasswordField;
    @FXML private TableView<CaseNode> casesTable;
    @FXML private TableColumn<CaseNode, String> caseIdColumn;
    @FXML private TableColumn<CaseNode, String> caseTitleColumn;
    @FXML private TableColumn<CaseNode, String> investigatorColumn;
    @FXML private TableColumn<CaseNode, String> caseStatusColumn;
    @FXML private TextField caseNumberField;
    @FXML private TextField caseTitleField;
    @FXML private TextField caseInvestigatorField;
    @FXML private DatePicker caseDatePicker;
    @FXML private ComboBox<CaseStatus> caseStatusCombo;
    @FXML private ListView<String> auditList;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        userLabel.setText(user());
        newRoleCombo.getItems().setAll("Admin", "Investigator", "Analyst");
        newRoleCombo.setValue("Investigator");
        caseStatusCombo.getItems().setAll(CaseStatus.OPENED, CaseStatus.CLOSED);
        caseStatusCombo.setValue(CaseStatus.OPENED);
        caseDatePicker.setValue(LocalDate.now());
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        caseIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        caseTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        investigatorColumn.setCellValueFactory(new PropertyValueFactory<>("investigator"));
        caseStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        refresh();
    }

    @FXML
    private void handleCreateUser() {
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password are required.");
            return;
        }
        if (FrontendState.userService.getUser(username) != null) {
            messageLabel.setText("Username already exists.");
            return;
        }
        FrontendState.userService.addUser(new User(username, password, newRoleCombo.getValue()));
        FrontendState.auditStack.push(user(), "Created user account: " + username);
        messageLabel.setText("User created.");
        refresh();
    }

    @FXML
    private void handleUpdateUser() {
        String oldUsername = oldUsernameField.getText().trim();
        String newUsername = updateUsernameField.getText().trim();
        String newPassword = updatePasswordField.getText().trim();
        if (oldUsername.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()) {
            messageLabel.setText("Current username, new username, and password are required.");
            return;
        }
        FrontendState.userService.updateUser(oldUsername, newUsername, newPassword);
        FrontendState.auditStack.push(user(), "Updated user account: " + oldUsername);
        messageLabel.setText("User updated.");
        refresh();
    }

    @FXML
    private void handleDeleteUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a user first.");
            return;
        }
        if (selected.getUsername().equalsIgnoreCase(user())) {
            messageLabel.setText("You cannot delete yourself.");
            return;
        }
        FrontendState.userService.deleteUser(selected.getUsername());
        FrontendState.auditStack.push(user(), "Deleted user account: " + selected.getUsername());
        messageLabel.setText("User deleted.");
        refresh();
    }

    @FXML
    private void handleAddCase() {
        try {
            int id = parseNumber(caseNumberField.getText(), "C-");
            FrontendState.caseBST.addCase(id, caseTitleField.getText().trim(), caseInvestigatorField.getText().trim(), caseDatePicker.getValue());
            FrontendState.caseBST.updateStatus(id, caseStatusCombo.getValue());
            FrontendState.auditStack.push(user(), "Added case C-" + String.format("%03d", id));
            messageLabel.setText("Case added.");
            clearCaseFields();
            refresh();
        } catch (Exception e) {
            messageLabel.setText("Could not add case: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearAuditStack() {
        FrontendState.auditStack.clear();
        messageLabel.setText("Audit stack cleared.");
        refresh();
    }

    @FXML
    private void handleUpdateCaseStatus() {
        CaseNode selected = casesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a case first.");
            return;
        }
        int id = parseNumber(selected.getId(), "C-");
        FrontendState.caseBST.updateStatus(id, caseStatusCombo.getValue());
        FrontendState.auditStack.push(user(), "Updated " + selected.getId() + " to " + caseStatusCombo.getValue());
        messageLabel.setText("Case status updated.");
        refresh();
    }

    @FXML
    private void handleDeleteCase() {
        CaseNode selected = casesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a case first.");
            return;
        }
        int id = parseNumber(selected.getId(), "C-");
        FrontendState.caseBST.deleteCase(id);
        FrontendState.auditStack.push(user(), "Deleted " + selected.getId());
        messageLabel.setText("Case deleted.");
        refresh();
    }

    @FXML
    private void handleLogout() throws IOException {
        FrontendApp.showLogin();
    }

    private void refresh() {
        usersTable.setItems(FXCollections.observableArrayList(FrontendState.userService.getAllUsers()));
        casesTable.setItems(FXCollections.observableArrayList(FrontendState.caseBST.SortCases()));
        auditList.getItems().setAll(FrontendState.auditStack.viewLog().stream().map(LogEntry::toString).toList());
        totalUsersLabel.setText(String.valueOf(FrontendState.userService.getAllUsers().size()));
        totalCasesLabel.setText(String.valueOf(FrontendState.caseBST.SortCases().size()));
        openCasesLabel.setText(String.valueOf(FrontendState.caseBST.SortCases().stream()
                .filter(c -> c.getStatus() == CaseStatus.OPENED).count()));
        auditEventsLabel.setText(String.valueOf(FrontendState.auditStack.viewLog().size()));
    }

    private String user() {
        return FrontendState.getCurrentUser() == null ? "Admin" : FrontendState.getCurrentUser().getUsername();
    }

    private void clearCaseFields() {
        caseNumberField.clear();
        caseTitleField.clear();
        caseInvestigatorField.clear();
        caseDatePicker.setValue(LocalDate.now());
        caseStatusCombo.setValue(CaseStatus.OPENED);
    }

    private int parseNumber(String value, String prefix) {
        return Integer.parseInt(value.trim().toUpperCase().replace(prefix, ""));
    }
}
