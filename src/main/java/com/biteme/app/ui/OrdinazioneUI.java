package com.biteme.app.ui;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.boundary.OrdinazioneBoundary;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.util.SceneLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.List;

public class OrdinazioneUI {

    private static final String TIPO_AL_TAVOLO = "Al Tavolo";

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

    @FXML private Button creaButton;
    @FXML private Button modificaButton;
    @FXML private Button eliminaButton;
    @FXML private Button archiviaButton;

    private final OrdinazioneBoundary boundary = new OrdinazioneBoundary();

    @FXML
    public void initialize() {
        tipoOrdineComboBox.setItems(FXCollections.observableArrayList(null, TIPO_AL_TAVOLO, "Asporto"));
        tipoOrdineComboBox.setPromptText("Al Tavolo o Asporto?");
        tipoOrdineComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(String object) {
                return (object == null) ? "Al Tavolo o Asporto?" : object;
            }
            @Override public String fromString(String string) { return string; }
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
        ordinazioniTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean sel = newSel != null;
            modificaButton.setDisable(!sel);
            eliminaButton.setDisable(!sel);
            archiviaButton.setDisable(!sel);
        });
    }

    @FXML
    private void createOrdine() {
        String nome      = nomeClienteField.getText().trim();
        String tipo      = tipoOrdineComboBox.getValue();
        String orario    = orarioField.getText().trim();
        String coperti   = copertiField.getText().trim();
        String tavolo    = tavoloField.getText().trim();

        try {
            boundary.createOrdinazione(nome, tipo, orario, coperti, tavolo);
            showInfo("Ordine creato per “" + nome + "”");
            clearForm();
            refreshTable();
        } catch (OrdinazioneException ex) {
            showWarning(ex.getMessage());
            return;
        }

        List<OrdinazioneBean> esistenti = boundary.getAll();

        boolean dupNome = esistenti.stream()
                .anyMatch(o -> o.getNome().equalsIgnoreCase(nome));
        if (dupNome) {
            showWarning("Esiste già un'ordinazione per il cliente “" + nome + "”.");
            nomeClienteField.requestFocus();
            return;
        }

        if (TIPO_AL_TAVOLO.equals(tipo)) {
            boolean dupTavolo = esistenti.stream()
                    .filter(o -> TIPO_AL_TAVOLO.equals(o.getTipoOrdine()))
                    .anyMatch(o -> o.getInfoTavolo().equalsIgnoreCase(tavolo));
            if (dupTavolo) {
                showWarning("Esiste già un'ordinazione al tavolo “" + tavolo + "”.");
                tavoloField.requestFocus();
                return;
            }
        } else {
            boolean dupOrario = esistenti.stream()
                    .filter(o -> !TIPO_AL_TAVOLO.equals(o.getTipoOrdine()))
                    .anyMatch(o -> o.getOrarioCreazione().equals(orario));
            if (dupOrario) {
                showWarning("Esiste già un'asporto per l'orario “" + orario + "”.");
                orarioField.requestFocus();
                return;
            }
        }

        try {
            boundary.createOrdinazione(nome, tipo, orario, coperti, tavolo);
            showInfo("Ordine creato per " + nome);
            clearForm();
            refreshTable();
        } catch (OrdinazioneException ex) {
            showWarning(ex.getMessage());
        }
    }



    @FXML
    private void eliminaOrdine() {
        try {
            var sel = ordinazioniTableView.getSelectionModel().getSelectedItem();
            boundary.delete(sel.getId());
            refreshTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void archiviaOrdine() {
        try {
            var sel = ordinazioniTableView.getSelectionModel().getSelectedItem();
            boundary.archive(sel);
            refreshTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void modificaOrdine() {
        var sel = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showWarning("Seleziona un'ordinazione da modificare.");
            return;
        }

        try {
            OrdinazioneBoundary.setSelected(sel);
            SceneLoader.getInstance().loadScene("/com/biteme/app/ordine.fxml", "Modifica Ordine");
        } catch (Exception e) {
            showError("Impossibile aprire la scena di modifica: " + e.getMessage());
        }
    }


    private void refreshTable() {
        List<OrdinazioneBean> list = boundary.getAll();
        ordinazioniTableView.setItems(FXCollections.observableArrayList(list));
    }

    private void clearForm() {
        nomeClienteField.clear();
        orarioField.clear();
        copertiField.clear();
        tavoloField.clear();
        tipoOrdineComboBox.getSelectionModel().clearSelection();
    }

    private void showInfo(String msg)    { new Alert(Alert.AlertType.INFORMATION, msg,    ButtonType.OK).showAndWait(); }
    private void showWarning(String msg) { new Alert(Alert.AlertType.WARNING,     msg,    ButtonType.OK).showAndWait(); }
    private void showError(String msg)   { new Alert(Alert.AlertType.ERROR,       msg,    ButtonType.OK).showAndWait(); }
}