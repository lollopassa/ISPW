package com.biteme.app.ui;

import com.biteme.app.boundary.OrdinazioneBoundary;
import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.util.SceneLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.List;

/**
 * Controller grafico FXML per le Ordinazioni.
 */
public class OrdinazioneUI {
    private static final String VALIDATION_ERROR = "Errore di Validazione";
    private static final String SUCCESS = "Successo";
    private static final String ERROR = "Errore";

    @FXML private TextField nomeClienteField;
    @FXML private ComboBox<String> tipoOrdineComboBox;
    @FXML private TextField orarioField;
    @FXML private TextField copertiField;
    @FXML private TextField tavoloField;
    @FXML private TableView<OrdinazioneBean> ordinazioniTableView;
    @FXML private TableColumn<OrdinazioneBean, Integer> idColumn;
    @FXML private TableColumn<OrdinazioneBean, String> nomeColumn;
    @FXML private TableColumn<OrdinazioneBean, String> tipoOrdineColumn;
    @FXML private TableColumn<OrdinazioneBean, String> orarioColumn;
    @FXML private TableColumn<OrdinazioneBean, String> copertiColumn;
    @FXML private TableColumn<OrdinazioneBean, String> infoTavoloColumn;
    @FXML private TableColumn<OrdinazioneBean, String> statoOrdineColumn;
    @FXML private Button modificaButton;
    @FXML private Button eliminaButton;
    @FXML private Button archiviaButton;

    private final OrdinazioneBoundary boundary = new OrdinazioneBoundary();

    @FXML
    public void initialize() {
        tipoOrdineComboBox.setItems(FXCollections.observableArrayList("Al Tavolo", "Asporto"));
        tipoOrdineComboBox.getItems().add(0, null);
        tipoOrdineComboBox.setPromptText("Al Tavolo o Asporto?");
        tipoOrdineComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(String obj) { return obj == null? "Al Tavolo o Asporto?": obj; }
            @Override public String fromString(String str) { return str; }
        });

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tipoOrdineColumn.setCellValueFactory(new PropertyValueFactory<>("tipoOrdine"));
        orarioColumn.setCellValueFactory(new PropertyValueFactory<>("orarioCreazione"));
        copertiColumn.setCellValueFactory(new PropertyValueFactory<>("numeroClienti"));
        infoTavoloColumn.setCellValueFactory(new PropertyValueFactory<>("infoTavolo"));
        statoOrdineColumn.setCellValueFactory(new PropertyValueFactory<>("statoOrdine"));

        refreshTable();
        modificaButton.setDisable(true);
        eliminaButton.setDisable(true);
        archiviaButton.setDisable(true);

        ordinazioniTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean sel = newV != null;
            modificaButton.setDisable(!sel);
            eliminaButton.setDisable(!sel);
            archiviaButton.setDisable(!sel);
        });
    }

    @FXML
    private void createOrdine() {
        try {
            OrdinazioneBean bean = boundary.createOrdine(
                    nomeClienteField.getText().trim(),
                    tipoOrdineComboBox.getValue(),
                    orarioField.getText().trim(),
                    copertiField.getText().trim(),
                    tavoloField.getText().trim()
            );
            showAlert(SUCCESS, "Ordine creato per " + bean.getNome(), Alert.AlertType.INFORMATION);
            clearFields();
            refreshTable();
        } catch (Exception e) {
            showAlert(VALIDATION_ERROR, e.getMessage(), Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void modificaOrdine() {
        OrdinazioneBean sel = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert(ERROR, "Seleziona un'ordinazione da modificare.", Alert.AlertType.WARNING); return; }
        OrdinazioneBoundary.setOrdineSelezionato(sel);
        SceneLoader.getInstance().loadScene("/com/biteme/app/ordine.fxml", "Modifica Ordine");
    }

    @FXML
    private void eliminaOrdine() {
        OrdinazioneBean sel = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert(ERROR, "Seleziona un ordine da eliminare.", Alert.AlertType.WARNING); return; }
        try {
            boundary.eliminaOrdine(sel.getId());
            showAlert(SUCCESS, "Ordine eliminato.", Alert.AlertType.INFORMATION);
            refreshTable();
        } catch (Exception e) {
            showAlert(ERROR, e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void archiviaOrdine() {
        OrdinazioneBean sel = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert(ERROR, "Seleziona un ordine da archiviare.", Alert.AlertType.WARNING); return; }
        try {
            boundary.archiviaOrdine(sel);
            showAlert(SUCCESS, "Ordine archiviato.", Alert.AlertType.INFORMATION);
            refreshTable();
        } catch (Exception e) {
            showAlert(ERROR, e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refreshTable() {
        List<OrdinazioneBean> list = boundary.getOrdini();
        ordinazioniTableView.setItems(FXCollections.observableArrayList(list));
    }

    private void clearFields() {
        nomeClienteField.clear();
        tipoOrdineComboBox.setValue(null);
        orarioField.clear();
        copertiField.clear();
        tavoloField.clear();
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}