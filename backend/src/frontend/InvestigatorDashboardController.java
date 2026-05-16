package frontend;

import cases.CaseNode;
import custody.QueueNode;
import evidence.EvidenceNode;
import evidence.PRIORITY;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.time.LocalDate;

public class InvestigatorDashboardController {
    @FXML private Label userLabel;
    @FXML private Label activeCasesLabel;
    @FXML private Label pendingEvidenceLabel;
    @FXML private Label queueLabel;
    @FXML private Label closedCasesLabel;
    @FXML private TableView<CaseNode> casesTable;
    @FXML private TableColumn<CaseNode, String> caseIdColumn;
    @FXML private TableColumn<CaseNode, String> titleColumn;
    @FXML private TableColumn<CaseNode, String> investigatorColumn;
    @FXML private TableColumn<CaseNode, String> statusColumn;
    @FXML private TableColumn<CaseNode, LocalDate> dateColumn;
    @FXML private TextField evidenceIdField;
    @FXML private TextField caseIdField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<PRIORITY> priorityCombo;
    @FXML private DatePicker evidenceDatePicker;
    @FXML private Label selectedCaseLabel;
    @FXML private ListView<String> selectedCaseEvidenceList;
    @FXML private ListView<String> queueList;
    @FXML private ListView<String> auditList;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        userLabel.setText(currentUsername());
        priorityCombo.getItems().setAll(PRIORITY.HIGH, PRIORITY.MEDIUM, PRIORITY.LOW);
        priorityCombo.setValue(PRIORITY.MEDIUM);
        evidenceDatePicker.setValue(LocalDate.now());
        caseIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        investigatorColumn.setCellValueFactory(new PropertyValueFactory<>("investigator"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateOpened"));
        casesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldCase, selectedCase) -> {
            showEvidenceForCase(selectedCase);
            if (selectedCase != null) {
                caseIdField.setText(selectedCase.getId());
            }
        });
        refresh();
    }

    @FXML
    private void handleAddEvidence() {
        try {
            int evidenceNum = parseNumber(evidenceIdField.getText(), "EV-");
            int caseNum = parseNumber(caseIdField.getText(), "C-");
            if (descriptionField.getText().trim().isEmpty()) {
                messageLabel.setText("Description is required.");
                return;
            }
            if (FrontendState.caseBST.SearchById(FrontendState.caseBST.getRoot(), caseNum) == null) {
                messageLabel.setText("Case not found. Ask admin to create it first.");
                return;
            }
            FrontendState.evidenceList.addEvidence(evidenceNum, caseNum, descriptionField.getText().trim(),
                    priorityCombo.getValue(), currentUsername(), evidenceDatePicker.getValue());
            FrontendState.auditStack.push(currentUsername(), "Added evidence EV-" + String.format("%03d", evidenceNum));
            messageLabel.setText("Evidence added.");
            refresh();
            showEvidenceForCase(casesTable.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            messageLabel.setText("Could not add evidence: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendToQueue() {
        try {
            String selectedEvidence = selectedCaseEvidenceList.getSelectionModel().getSelectedItem();
            if (selectedEvidence == null || selectedEvidence.startsWith("No evidence")) {
                messageLabel.setText("Select evidence from the selected case first.");
                return;
            }

            int id = parseNumber(selectedEvidence.split(" ")[0], "EV-");
            EvidenceNode node = FrontendState.evidenceList.searchById(id);
            if (node == null) {
                messageLabel.setText("Selected evidence was not found.");
                return;
            }
            int caseNum = parseNumber(node.getCaseId(), "C-");
            FrontendState.custodyQueue.enqueue(id, caseNum, currentUsername(), node.getPriority());
            FrontendState.auditStack.push(currentUsername(), "Sent " + node.getEvidenceId() + " to custody queue");
            messageLabel.setText("Evidence sent to queue.");
            refresh();
            showEvidenceForCase(casesTable.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            messageLabel.setText("Could not queue evidence: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        FrontendApp.showLogin();
    }

    private void refresh() {
        casesTable.setItems(FXCollections.observableArrayList(FrontendState.caseBST.SortCases()));
        activeCasesLabel.setText(String.valueOf(FrontendState.caseBST.SortCases().size()));
        pendingEvidenceLabel.setText(String.valueOf(FrontendState.evidenceList.displayForward().size()));
        queueLabel.setText(String.format("%02d", FrontendState.custodyQueue.displayQueue().size()));
        closedCasesLabel.setText(String.valueOf(FrontendState.caseBST.SortCases().stream()
                .filter(c -> "CLOSED".equals(c.getStatus().name())).count()));
        queueList.getItems().setAll(FrontendState.custodyQueue.displayQueue().stream().map(QueueNode::toString).toList());
        auditList.getItems().setAll(FrontendState.auditStack.recentlog(5).stream().map(Object::toString).toList());
        showEvidenceForCase(casesTable.getSelectionModel().getSelectedItem());
    }

    private void showEvidenceForCase(CaseNode selectedCase) {
        selectedCaseEvidenceList.getItems().clear();
        if (selectedCase == null) {
            selectedCaseLabel.setText("Select a case to view evidence");
            selectedCaseEvidenceList.getItems().add("No case selected.");
            return;
        }

        selectedCaseLabel.setText("Evidence for " + selectedCase.getId());
        int caseNum = parseNumber(selectedCase.getId(), "C-");
        var evidenceForCase = FrontendState.evidenceList.searchByCaseId(caseNum);
        if (evidenceForCase.isEmpty()) {
            selectedCaseEvidenceList.getItems().add("No evidence found for this case.");
            return;
        }

        selectedCaseEvidenceList.getItems().setAll(evidenceForCase.stream()
                .map(e -> e.getEvidenceId() + " | " + e.getStatus() + " | " + e.getPriority() + " | " + e.getDescription())
                .toList());
    }

    private String currentUsername() {
        return FrontendState.getCurrentUser() == null ? "Investigator" : FrontendState.getCurrentUser().getUsername();
    }

    private int parseNumber(String value, String prefix) {
        return Integer.parseInt(value.trim().toUpperCase().replace(prefix, ""));
    }
}
