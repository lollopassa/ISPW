package com.biteme.app.boundary;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.controller.OrdinazioneController;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.controller.ArchivioController;
import com.biteme.app.bean.OrdineBean;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.util.SceneLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrdinazioneBoundary {

    private static final String VALIDATION_ERROR = "Errore di Validazione";
    private static final String SUCCESS = "Successo";
    private static final String ERROR = "Errore";

    @FXML
    private TextField nomeClienteField;
    @FXML
    private ComboBox<String> tipoOrdineComboBox;
    @FXML
    private TextField orarioField;
    @FXML
    private TextField copertiField;
    @FXML
    private TextField tavoloField;
    @FXML
    private TableView<OrdinazioneBean> ordinazioniTableView;
    @FXML
    private TableColumn<OrdinazioneBean, Integer> idColumn;
    @FXML
    private TableColumn<OrdinazioneBean, String> nomeColumn;
    @FXML
    private TableColumn<OrdinazioneBean, String> tipoOrdineColumn;
    @FXML
    private TableColumn<OrdinazioneBean, String> orarioColumn;
    @FXML
    private TableColumn<OrdinazioneBean, String> copertiColumn;
    @FXML
    private TableColumn<OrdinazioneBean, String> infoTavoloColumn;
    @FXML
    private TableColumn<OrdinazioneBean, String> statoOrdineColumn;
    @FXML
    private Button modificaButton;
    @FXML
    private Button eliminaButton;
    @FXML
    private Button archiviaButton;

    private final OrdineController ordineController = new OrdineController();
    private final OrdinazioneController ordinazioneController = new OrdinazioneController();
    private final ArchivioController archivioController = new ArchivioController();

    private static OrdinazioneBean ordineSelezionato;

    @FXML
    public void initialize() {
        configureComboBox();
        initTableColumns();
        refreshTable();

        modificaButton.setDisable(true);
        eliminaButton.setDisable(true);
        archiviaButton.setDisable(true);

        ordinazioniTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isSelected = (newValue != null);
            modificaButton.setDisable(!isSelected);
            eliminaButton.setDisable(!isSelected);
            archiviaButton.setDisable(!isSelected);
        });
    }

    private void configureComboBox() {
        tipoOrdineComboBox.setItems(FXCollections.observableArrayList());
        tipoOrdineComboBox.getItems().add(null);
        tipoOrdineComboBox.getItems().addAll("Al Tavolo", "Asporto");
        tipoOrdineComboBox.setPromptText("Al Tavolo o Asporto?");
        tipoOrdineComboBox.setValue(null);
        tipoOrdineComboBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return (object == null) ? "Al Tavolo o Asporto?" : object;
            }
            @Override
            public String fromString(String string) {
                return string;
            }
        });
    }

    private void initTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tipoOrdineColumn.setCellValueFactory(new PropertyValueFactory<>("tipoOrdine"));
        orarioColumn.setCellValueFactory(new PropertyValueFactory<>("orarioCreazione"));
        copertiColumn.setCellValueFactory(new PropertyValueFactory<>("numeroClienti"));
        infoTavoloColumn.setCellValueFactory(new PropertyValueFactory<>("infoTavolo"));
        statoOrdineColumn.setCellValueFactory(new PropertyValueFactory<>("statoOrdine"));
    }

    /**
     * Il metodo createOrdine delega al controller la validazione e la creazione dell'OrdinazioneBean.
     */
    @FXML
    public void createOrdine() {
        try {
            OrdinazioneBean ordinazioneBean = ordinazioneController.processOrdineCreation(
                    nomeClienteField.getText().trim(),
                    tipoOrdineComboBox.getValue(),
                    orarioField.getText().trim(),
                    copertiField.getText().trim(),
                    tavoloField.getText().trim()
            );
            // Se la validazione ha avuto successo, prosegui con la creazione dell'ordine
            ordinazioneController.creaOrdine(ordinazioneBean);
            onSuccess(ordinazioneBean.getNome());
        } catch (OrdinazioneException e) {
            // In caso di errori di validazione o business, oltre a mostrare l'alert,
            // riposizioniamo il focus sul campo "Nome Cliente" per agevolare la correzione
            handleFormError(e, VALIDATION_ERROR, Alert.AlertType.WARNING);
        } catch (Exception e) {
            handleFormError(e, ERROR, Alert.AlertType.ERROR);
        }
    }

    private void onSuccess(String nome) {
        showAlert(SUCCESS, "Ordine creato con successo per il cliente: " + nome, Alert.AlertType.INFORMATION);
        clearFields();
        refreshTable();
    }

    public static OrdinazioneBean getOrdineSelezionato() {
        return ordineSelezionato;
    }

    public static void setOrdineSelezionato(OrdinazioneBean ordine) {
        ordineSelezionato = ordine;
    }

    @FXML
    private void modificaOrdine() {
        OrdinazioneBean ordinazione = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazione == null) {
            showAlert(ERROR, "Seleziona un'ordinazione da modificare.", Alert.AlertType.WARNING);
            return;
        }
        OrdinazioneBean ordine = new OrdinazioneBean();
        ordine.setId(ordinazione.getId());
        ordine.setNome(ordinazione.getNome());
        ordine.setNumeroClienti(ordinazione.getNumeroClienti());
        ordine.setTipoOrdine(ordinazione.getTipoOrdine());
        ordine.setInfoTavolo(ordinazione.getInfoTavolo());
        ordine.setStatoOrdine(ordinazione.getStatoOrdine());
        ordine.setOrarioCreazione(ordinazione.getOrarioCreazione());

        setOrdineSelezionato(ordine);
        SceneLoader.getInstance().loadScene("/com/biteme/app/ordine.fxml", "Modifica Ordine");
    }

    @FXML
    private void eliminaOrdine() {
        OrdinazioneBean ordinazioneSelezionata = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazioneSelezionata == null) {
            showAlert(ERROR, "Seleziona un ordine da eliminare.", Alert.AlertType.WARNING);
            return;
        }
        try {
            ordinazioneController.eliminaOrdinazione(ordinazioneSelezionata.getId());
            showAlert(SUCCESS, "Ordine eliminato con successo.", Alert.AlertType.INFORMATION);
            // Reset selezione e aggiorna la tabella
            ordinazioniTableView.getSelectionModel().clearSelection();
            refreshTable();
        } catch (Exception e) {
            handleTableError(e, ERROR, Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void archiviaOrdine() {
        OrdinazioneBean ordinazioneSelezionata = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazioneSelezionata == null) {
            showAlert(ERROR, "Seleziona un ordine da archiviare.", Alert.AlertType.WARNING);
            return;
        }
        try {
            OrdineBean ordineBean = ordineController.getOrdineById(ordinazioneSelezionata.getId());
            BigDecimal totale = calcolaTotaleOrdine(ordineBean);

            ArchivioBean archivioBean = new ArchivioBean();
            archivioBean.setIdOrdine(ordineBean.getId());
            archivioBean.setProdotti(ordineBean.getProdotti());
            archivioBean.setQuantita(ordineBean.getQuantita());
            archivioBean.setTotale(totale);
            archivioBean.setDataArchiviazione(LocalDateTime.now());

            archivioController.archiviaOrdine(archivioBean);
            ordinazioneController.eliminaOrdinazione(ordinazioneSelezionata.getId());

            showAlert(SUCCESS, "Ordine archiviato con successo.", Alert.AlertType.INFORMATION);
            // Reset selezione e aggiorna la tabella
            ordinazioniTableView.getSelectionModel().clearSelection();
            refreshTable();
        } catch (Exception e) {
            handleTableError(e, ERROR, Alert.AlertType.ERROR);
        }
    }


    private BigDecimal calcolaTotaleOrdine(OrdineBean ordineBean) {
        BigDecimal totale = BigDecimal.ZERO;
        if (ordineBean != null) {
            List<String> prodotti = ordineBean.getProdotti();
            List<Integer> quantita = ordineBean.getQuantita();
            if (prodotti != null && quantita != null && prodotti.size() == quantita.size()) {
                for (int i = 0; i < prodotti.size(); i++) {
                    // Prezzo fisso di esempio: 10 per ogni unitÃ
                    BigDecimal prezzoUnitario = BigDecimal.valueOf(10);
                    totale = totale.add(prezzoUnitario.multiply(BigDecimal.valueOf(quantita.get(i))));
                }
            }
        }
        return totale;
    }

    private void refreshTable() {
        List<OrdinazioneBean> ordini = ordinazioneController.getOrdini();
        ordinazioniTableView.getItems().setAll(ordini);
    }

    private void clearFields() {
        nomeClienteField.clear();
        orarioField.clear();
        copertiField.clear();
        tavoloField.clear();
        tipoOrdineComboBox.getSelectionModel().clearSelection();
        tipoOrdineComboBox.setValue(null);
    }


    private void handleFormError(Exception e, String title, Alert.AlertType alertType) {
        showAlert(title, e.getMessage(), alertType);
        clearFields();
        nomeClienteField.requestFocus();
    }


    private void handleTableError(Exception e, String title, Alert.AlertType alertType) {
        showAlert(title, e.getMessage(), alertType);
        ordinazioniTableView.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}