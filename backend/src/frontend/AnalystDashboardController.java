package frontend;

import audit.LogEntry;
import custody.QueueNode;
import evidence.EvidenceNode;
import evidence.STATUS;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;

public class AnalystDashboardController {
    @FXML private Label userLabel;
    @FXML private Label queueCountLabel;
    @FXML private Label underAnalysisLabel;
    @FXML private Label analyzedLabel;
    @FXML private Label reportCountLabel;
    @FXML private TableView<QueueNode> queueTable;
    @FXML private TableColumn<QueueNode, String> evidenceColumn;
    @FXML private TableColumn<QueueNode, String> caseColumn;
    @FXML private TableColumn<QueueNode, String> submittedByColumn;
    @FXML private TableColumn<QueueNode, String> priorityColumn;
    @FXML private Label selectedEvidenceLabel;
    @FXML private Label selectedCaseLabel;
    @FXML private Label selectedStatusLabel;
    @FXML private TextArea findingsArea;
    @FXML private TextArea remarksArea;
    @FXML private ComboBox<STATUS> finalStatusCombo;
    @FXML private ListView<String> auditList;
    @FXML private ListView<String> statusHistoryList;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        userLabel.setText(FrontendState.getCurrentUser() == null ? "Analyst" : FrontendState.getCurrentUser().getUsername());
        finalStatusCombo.getItems().setAll(STATUS.UNDER_ANALYSIS, STATUS.ANALYZED, STATUS.CLOSED);
        finalStatusCombo.setValue(STATUS.UNDER_ANALYSIS);
        evidenceColumn.setCellValueFactory(new PropertyValueFactory<>("evidenceId"));
        caseColumn.setCellValueFactory(new PropertyValueFactory<>("caseId"));
        submittedByColumn.setCellValueFactory(new PropertyValueFactory<>("submittedBy"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        queueTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, item) -> showSelected(item));
        refresh();
    }

    @FXML
    private void handleTakeNext() {
        QueueNode item = FrontendState.custodyQueue.dequeue();
        if (item == null) {
            messageLabel.setText("Queue is empty.");
            return;
        }
        int evidenceNum = Integer.parseInt(item.getEvidenceId().replace("EV-", ""));
        FrontendState.evidenceList.changeStatus(evidenceNum, STATUS.UNDER_ANALYSIS);
        FrontendState.auditStack.push(user(), "Started analysis on " + item.getEvidenceId());
        showSelected(item);
        messageLabel.setText("Evidence moved under analysis.");
        refresh();
    }

    @FXML
    private void handleSaveReport() {
        messageLabel.setText("Report notes saved in this UI draft.");
    }

    @FXML
    private void handleMarkAnalyzed() {
        String id = selectedEvidenceLabel.getText();
        if (id == null || id.isBlank() || id.equals("-")) {
            messageLabel.setText("Select or take an evidence item first.");
            return;
        }
        int evidenceNum = Integer.parseInt(id.replace("EV-", ""));
        FrontendState.evidenceList.changeStatus(evidenceNum, finalStatusCombo.getValue());
        FrontendState.auditStack.push(user(), "Changed " + id + " to " + finalStatusCombo.getValue());
        selectedStatusLabel.setText(finalStatusCombo.getValue().name());
        messageLabel.setText("Evidence status updated.");
        refresh();
    }

    @FXML
    private void handleLogout() throws IOException {
        FrontendApp.showLogin();
    }

    private void showSelected(QueueNode item) {
        if (item == null) {
            selectedEvidenceLabel.setText("-");
            selectedCaseLabel.setText("-");
            selectedStatusLabel.setText("-");
            return;
        }
        selectedEvidenceLabel.setText(item.getEvidenceId());
        selectedCaseLabel.setText(item.getCaseId());
        EvidenceNode evidence = FrontendState.evidenceList.searchById(Integer.parseInt(item.getEvidenceId().replace("EV-", "")));
        selectedStatusLabel.setText(evidence == null ? "IN_QUEUE" : evidence.getStatus().name());
    }

    private void refresh() {
        queueTable.setItems(FXCollections.observableArrayList(FrontendState.custodyQueue.displayQueue()));
        queueCountLabel.setText(String.format("%02d", FrontendState.custodyQueue.displayQueue().size()));
        underAnalysisLabel.setText(String.valueOf(FrontendState.evidenceList.displayForward().stream()
                .filter(e -> e.getStatus() == STATUS.UNDER_ANALYSIS).count()));
        analyzedLabel.setText(String.valueOf(FrontendState.evidenceList.displayForward().stream()
                .filter(e -> e.getStatus() == STATUS.ANALYZED).count()));
        reportCountLabel.setText(String.valueOf(FrontendState.auditStack.viewLog().size()));
        auditList.getItems().setAll(FrontendState.auditStack.recentlog(5).stream().map(LogEntry::toString).toList());
        statusHistoryList.getItems().setAll(FrontendState.evidenceList.displayForward().stream().limit(5)
                .map(e -> e.getEvidenceId() + " - " + e.getStatus()).toList());
    }

    private String user() {
        return FrontendState.getCurrentUser() == null ? "Analyst" : FrontendState.getCurrentUser().getUsername();
    }
}
