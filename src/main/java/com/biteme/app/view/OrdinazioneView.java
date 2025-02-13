package com.biteme.app.view;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.controller.OrdinazioneController;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.controller.ArchivioController;
import com.biteme.app.bean.OrdineBean;
import com.biteme.app.util.SceneLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrdinazioneView {

    // Definizione delle costanti per evitare duplicazioni delle stringhe
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

    @FXML
    public void createOrdine() {
        String nome = nomeClienteField.getText().trim();
        String tipoOrdine = tipoOrdineComboBox.getValue();
        String orario = orarioField.getText().trim();
        String coperti = copertiField.getText().trim();
        String tavolo = tavoloField.getText().trim();

        // Validazione dei campi
        if (!validateFields(nome, tipoOrdine, orario, coperti, tavolo)) {
            return;
        }

        // Gestione degli ordini "Al Tavolo" e "Asporto"
        if ("Asporto".equals(tipoOrdine)) {
            coperti = "";
            tavolo = "";
        } else {
            orario = java.time.LocalTime.now().toString().substring(0, 5); // Imposta l'orario locale
        }

        // Crea il bean con i dati validati
        OrdinazioneBean ordinazioneBean = createOrdinazione(nome, tipoOrdine, orario, coperti, tavolo);

        try {
            // Chiama il controller per creare l'ordine
            ordinazioneController.creaOrdine(ordinazioneBean);
            onSuccess(nome);
        } catch (Exception e) {
            showAlert(ERROR, "Si è verificato un errore durante la creazione dell'ordine: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Metodo per validare tutti i campi
    private boolean validateFields(String nome, String tipoOrdine, String orario, String coperti, String tavolo) {
        if (nome.isEmpty()) {
            showAlert(VALIDATION_ERROR, "Il campo Nome Cliente deve essere compilato.", Alert.AlertType.WARNING);
            return false;
        }
        if (tipoOrdine == null || tipoOrdine.isEmpty()) {
            showAlert(VALIDATION_ERROR, "Seleziona un tipo di ordine: 'Al Tavolo' o 'Asporto'.", Alert.AlertType.WARNING);
            return false;
        }

        if ("Al Tavolo".equals(tipoOrdine)) {
            return validateAlTavoloFields(coperti, tavolo);
        } else {
            return validateAsportoFields(orario);
        }
    }

    // Metodo specifico per validare i campi "Al Tavolo"
    private boolean validateAlTavoloFields(String coperti, String tavolo) {
        if (coperti.isEmpty()) {
            showAlert(VALIDATION_ERROR, "Il numero di coperti è obbligatorio per gli ordini 'Al Tavolo'.", Alert.AlertType.WARNING);
            return false;
        }
        if (!coperti.matches("\\d+")) { // Solo numeri interi
            showAlert(VALIDATION_ERROR, "Il campo 'Numero di Coperti' deve contenere solo numeri interi.", Alert.AlertType.WARNING);
            return false;
        }
        if (tavolo.isEmpty()) {
            showAlert(VALIDATION_ERROR, "Il numero del tavolo è obbligatorio per gli ordini 'Al Tavolo'.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    // Metodo specifico per validare i campi "Asporto"
    private boolean validateAsportoFields(String orario) {
        if (orario.isEmpty()) {
            showAlert(VALIDATION_ERROR, "Il campo Orario deve essere compilato per Asporto.", Alert.AlertType.WARNING);
            return false;
        }
        if (!ordinazioneController.isValidTime(orario)) {
            showAlert(VALIDATION_ERROR, "Il campo 'Orario' deve essere nel formato HH:mm (es. '12:20').", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    // Metodo per creare il bean OrdinazioneBean
    private OrdinazioneBean createOrdinazione(String nome, String tipoOrdine, String orario, String coperti, String tavolo) {
        OrdinazioneBean ordinazioneBean = new OrdinazioneBean();
        ordinazioneBean.setNome(nome);
        ordinazioneBean.setTipoOrdine(tipoOrdine);
        ordinazioneBean.setOrarioCreazione(orario);
        ordinazioneBean.setNumeroClienti(coperti);
        ordinazioneBean.setInfoTavolo(tavolo);
        return ordinazioneBean;
    }

    // Metodo per gestire il successo della creazione dell'ordine
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
            showAlert(ERROR, "Seleziona un ordinazione da modificare.", Alert.AlertType.WARNING);
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
        SceneLoader.loadScene("/com/biteme/app/ordine.fxml", "Modifica Ordine");
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
            refreshTable();
        } catch (Exception e) {
            showAlert(ERROR, "Si è verificato un errore durante l'eliminazione dell'ordine: " + e.getMessage(), Alert.AlertType.ERROR);
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
            // Recupera i dettagli completi dell'ordine tramite OrdineController
            OrdineBean ordineBean = ordineController.getOrdineById(ordinazioneSelezionata.getId());
            BigDecimal totale = calcolaTotaleOrdine(ordineBean);

            ArchivioBean archivioBean = new ArchivioBean();
            archivioBean.setIdOrdine(ordineBean.getId());
            archivioBean.setProdotti(ordineBean.getProdotti());
            archivioBean.setQuantita(ordineBean.getQuantita());
            archivioBean.setTotale(totale);
            archivioBean.setDataArchiviazione(LocalDateTime.now());

            archivioController.archiviaOrdine(archivioBean);
            // Rimuove l'ordine archiviato
            ordinazioneController.eliminaOrdinazione(ordinazioneSelezionata.getId());
            showAlert(SUCCESS, "Ordine archiviato con successo.", Alert.AlertType.INFORMATION);
            refreshTable();
        } catch (Exception e) {
            showAlert(ERROR, "Si è verificato un errore durante l'archiviazione: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private BigDecimal calcolaTotaleOrdine(OrdineBean ordineBean) {
        BigDecimal totale = BigDecimal.ZERO;
        if (ordineBean != null) {
            List<String> prodotti = ordineBean.getProdotti();
            List<Integer> quantita = ordineBean.getQuantita();
            if (prodotti != null && quantita != null && prodotti.size() == quantita.size()) {
                // Esempio di calcolo: sostituisci con la logica di calcolo reale
                for (int i = 0; i < prodotti.size(); i++) {
                    totale = totale.add(BigDecimal.valueOf(10).multiply(BigDecimal.valueOf(quantita.get(i))));
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

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}