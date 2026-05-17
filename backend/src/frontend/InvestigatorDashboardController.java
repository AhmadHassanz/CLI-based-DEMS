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
    @FXML private TableView<EvidenceNode> selectedCaseEvidenceTable;
    @FXML private TableColumn<EvidenceNode, String> evidenceIdColumn;
    @FXML private TableColumn<EvidenceNode, String> evidenceCaseIdColumn;
    @FXML private TableColumn<EvidenceNode, String> evidenceDescriptionColumn;
    @FXML private TableColumn<EvidenceNode, String> evidenceStatusColumn;
    @FXML private TableColumn<EvidenceNode, String> evidencePriorityColumn;
    @FXML private TableColumn<EvidenceNode, String> evidenceSubmittedByColumn;
    @FXML private TableColumn<EvidenceNode, LocalDate> evidenceDateColumn;
    @FXML private ListView<String> queueList;
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
        evidenceIdColumn.setCellValueFactory(new PropertyValueFactory<>("evidenceId"));
        evidenceCaseIdColumn.setCellValueFactory(new PropertyValueFactory<>("caseId"));
        evidenceDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        evidenceStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        evidencePriorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        evidenceSubmittedByColumn.setCellValueFactory(new PropertyValueFactory<>("submittedBy"));
        evidenceDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
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
        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        } catch (Exception e) {
            messageLabel.setText("Could not add evidence: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendToQueue() {
        try {
            EvidenceNode selectedEvidence = selectedCaseEvidenceTable.getSelectionModel().getSelectedItem();
            if (selectedEvidence == null) {
                messageLabel.setText("Select evidence from the selected case first.");
                return;
            }

            int id = parseNumber(selectedEvidence.getEvidenceId(), "EV-");
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
        showEvidenceForCase(casesTable.getSelectionModel().getSelectedItem());
    }

    private void showEvidenceForCase(CaseNode selectedCase) {
        selectedCaseEvidenceTable.getItems().clear();
        if (selectedCase == null) {
            selectedCaseLabel.setText("Select a case to view evidence");
            return;
        }

        selectedCaseLabel.setText("Evidence for " + selectedCase.getId());
        int caseNum = parseNumber(selectedCase.getId(), "C-");
        var evidenceForCase = FrontendState.evidenceList.searchByCaseId(caseNum);
        selectedCaseEvidenceTable.setItems(FXCollections.observableArrayList(evidenceForCase));
    }

    private String currentUsername() {
        return FrontendState.getCurrentUser() == null ? "Investigator" : FrontendState.getCurrentUser().getUsername();
    }

    private int parseNumber(String value, String prefix) {
        return Integer.parseInt(value.trim().toUpperCase().replace(prefix, ""));
    }
}
