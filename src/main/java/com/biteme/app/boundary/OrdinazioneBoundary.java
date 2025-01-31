package com.biteme.app.boundary;

import com.biteme.app.controller.OrdinazioneController;
import com.biteme.app.entity.Ordinazione;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.util.SceneLoader;

import java.util.List;

import java.time.LocalTime;
import com.biteme.app.entity.TipoOrdine;


public class OrdinazioneBoundary {

    // Campi UI
    @FXML
    private TextField nomeClienteField;

    @FXML
    private ComboBox<TipoOrdine> tipoOrdineComboBox;


    @FXML
    private TextField orarioField;

    @FXML
    private TextField copertiField;

    @FXML
    private TextField tavoloField;

    @FXML
    private TableView<Ordinazione> ordinazioniTableView;

    @FXML
    private TableColumn<Ordinazione, Integer> idColumn;

    @FXML
    private TableColumn<Ordinazione, String> nomeColumn;

    @FXML
    private TableColumn<Ordinazione, String> tipoOrdineColumn;

    @FXML
    private TableColumn<Ordinazione, String> orarioColumn;

    @FXML
    private TableColumn<Ordinazione, String> copertiColumn;

    @FXML
    private TableColumn<Ordinazione, String> infoTavoloColumn;

    @FXML
    private TableColumn<Ordinazione, String> statoOrdineColumn;

    @FXML
    private Button modificaButton; // Pulsante per modificare l'ordine

    @FXML
    private Button eliminaButton; // Pulsante per eliminare l'ordine

    private static OrdinazioneBean ordineSelezionato; // Variabile statica per mantenere l'ordine selezionato
    private final OrdinazioneController ordinazioneController = new OrdinazioneController();



    @FXML
    public void initialize() {

        configureComboBox();
        // Inizializza le colonne della tabella
        initTableColumns();

        // Carica i dati nella tabella
        refreshTable();

        // Disabilita i pulsanti finché non viene selezionata una riga
        modificaButton.setDisable(true);
        eliminaButton.setDisable(true);

        // Aggiunge un listener per abilitare/disabilitare i pulsanti in base alla selezione
        ordinazioniTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isSelected = (newValue != null);
            modificaButton.setDisable(!isSelected);
            eliminaButton.setDisable(!isSelected);
        });
    }

    private void configureComboBox() {
        // Configuriamo il ComboBox per la selezione della categoria
        tipoOrdineComboBox.setItems(FXCollections.observableArrayList(TipoOrdine.values()));
        tipoOrdineComboBox.setPromptText("Al Tavolo o Asporto?"); // Mostra l'indicazione iniziale
    }

    private void initTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        tipoOrdineColumn.setCellValueFactory(new PropertyValueFactory<>("tipoOrdine"));
        orarioColumn.setCellValueFactory(new PropertyValueFactory<>("orarioCreazione"));
        copertiColumn.setCellValueFactory(new PropertyValueFactory<>("numeroClienti"));
        infoTavoloColumn.setCellValueFactory(new PropertyValueFactory<>("infoTavolo"));
        statoOrdineColumn.setCellValueFactory(new PropertyValueFactory<>("statoOrdine"));
    }

    @FXML
    public void createOrdine() {
        // Estrai i dati dai campi di input
        String nomeCliente = nomeClienteField.getText();
        TipoOrdine tipoOrdine = tipoOrdineComboBox.getValue();
        String orario = orarioField.getText();
        String coperti = copertiField.getText();
        String tavolo = tavoloField.getText();

        // Controlla che il tipo ordine sia stato selezionato
        if (tipoOrdine == null) {
            showAlert("Tipo Ordine Mancante",
                    "Seleziona un tipo di ordine: 'Al Tavolo' o 'Asporto'.",
                    Alert.AlertType.WARNING);
            return;
        }

        // Gestisci i casi specifici per il tipo di ordine
        if (tipoOrdine == TipoOrdine.AL_TAVOLO) {
            // Assegna l'orario corrente se il tipo è "Al Tavolo"
            orario = LocalTime.now().toString().substring(0, 5); // HH:mm
        } else if (tipoOrdine == TipoOrdine.ASPORTO) {
            // Imposta campi specifici come null per "Asporto"
            coperti = null;
            tavolo = null;

            // Valida l'orario per l'Asporto
            if (orario.isEmpty()) {
                showAlert("Campi Mancanti",
                        "Il campo Orario deve essere compilato in caso di Asporto.",
                        Alert.AlertType.WARNING);
                return;
            }

            if (!ordinazioneController.isValidTime(orario)) {
                showAlert("Formato Orario Non Valido",
                        "Il campo 'Orario' deve essere nel formato HH:mm, ad esempio '12:20'.",
                        Alert.AlertType.ERROR);
                return;
            }
        }

        // Controllo che il ComboBox TipoOrdine abbia una selezione valida
        if (tipoOrdineComboBox.getValue() == null) {
            showAlert("Errore", "Seleziona un tipo di ordine valido (Al Tavolo o Asporto)!", Alert.AlertType.ERROR);
            return;
        }

        // Esegui la validazione degli altri campi obbligatori
        if (nomeCliente.isEmpty()) {
            showAlert("Campi Mancanti", "Il campo Nome Cliente deve essere compilato.", Alert.AlertType.WARNING);
            return;
        }

        // Crea un bean con i dati raccolti
        OrdinazioneBean ordinazioneBean = new OrdinazioneBean();
        ordinazioneBean.setNomeCliente(nomeCliente);
        ordinazioneBean.setTipoOrdine(tipoOrdine);
        ordinazioneBean.setOrarioCreazione(orario);  // Usa l'ora attuale o quella fornita
        ordinazioneBean.setNumeroClienti(coperti);  // null se "Asporto"
        ordinazioneBean.setInfoTavolo(tavolo);      // null se "Asporto"

        // Passa il bean al controller per creare un nuovo ordine
        ordinazioneController.creaOrdine(ordinazioneBean);

        // Aggiorna la tabella dopo la creazione
        refreshTable();
    }

    // Metodo Getter per ottenere l'oggetto statico OrdineSelezionato
    public static OrdinazioneBean getOrdineSelezionato() {
        return ordineSelezionato;
    }

    @FXML
    private void modificaOrdine() {
        Ordinazione ordinazione = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazione == null) {
            showAlert("Errore", "Seleziona un ordinazione da modificare.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Inizializza OrdinazioneBean con i dettagli dell'ordine selezionato
            ordineSelezionato = new OrdinazioneBean();
            ordineSelezionato.setId(ordinazione.getId());
            ordineSelezionato.setNomeCliente(ordinazione.getNomeCliente());
            ordineSelezionato.setNumeroClienti(ordinazione.getNumeroClienti());

            // Utilizza il valore TipoOrdine correttamente
            TipoOrdine tipoOrdine = ordinazione.getTipoOrdine(); // Assume che sia un enum
            if (tipoOrdine == null) {
                throw new IllegalArgumentException("Tipo ordine nullo!");
            }
            ordineSelezionato.setTipoOrdine(tipoOrdine);

            // Gestione campo InfoTavolo basata sul tipo ordine
            ordineSelezionato.setInfoTavolo(
                    ordineSelezionato.getTipoOrdine() == TipoOrdine.ASPORTO
                            ? "Asporto"
                            : ordinazione.getInfoTavolo()
            );

            ordineSelezionato.setStatoOrdine(ordinazione.getStatoOrdine());
            ordineSelezionato.setOrarioCreazione(ordinazione.getOrarioCreazione());

            // Carica la scena con SceneLoader
            SceneLoader.loadScene("/com/biteme/app/ordine.fxml", "Modifica Ordine");

        } catch (IllegalArgumentException e) {
            showAlert("Errore", "Il tipo ordine selezionato non è valido: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Errore", "Errore durante il caricamento della schermata di modifica.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Metodo per eliminare l'ordine selezionato
    @FXML
    private void eliminaOrdine() {
        Ordinazione ordinazioneSelezionato = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazioneSelezionato == null) {
            showAlert("Errore", "Seleziona un ordine da eliminare.", Alert.AlertType.ERROR);
            return;
        }

        boolean conferma = mostraDialogConferma("Sei sicuro di voler eliminare l'ordine " + ordinazioneSelezionato.getId() + "?");
        if (conferma) {
            try {
                ordinazioneController.eliminaOrdine(ordinazioneSelezionato.getId());
                refreshTable();
                showAlert("Successo", "Ordine eliminato con successo.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Errore", "Errore durante l'eliminazione dell'ordine.", Alert.AlertType.ERROR);
            }
        }
    }

    private void refreshTable() {
        // Recupera la lista aggiornata di ordini dal controller
        List<Ordinazione> ordini = ordinazioneController.getOrdini();

        // Aggiorna gli elementi nella TableView
        ordinazioniTableView.getItems().setAll(ordini);
    }

    private boolean mostraDialogConferma(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma");
        alert.setContentText(messaggio);
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}