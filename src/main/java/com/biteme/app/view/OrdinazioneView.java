package com.biteme.app.view;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.ArchivioController;
import com.biteme.app.controller.OrdinazioneController;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.controller.ProdottoController;
import com.biteme.app.model.Ordinazione;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.util.SceneLoader;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import java.time.LocalTime;
import java.util.logging.Logger;

import com.biteme.app.model.TipoOrdine;


public class OrdinazioneView {

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

    private final OrdineController ordineController = new OrdineController();
    private final ArchivioController archivioController = new ArchivioController();
    private final ProdottoController prodottoController = new ProdottoController();

    private static OrdinazioneBean ordineSelezionato;
    private final OrdinazioneController ordinazioneController = new OrdinazioneController();
    private static final String ERROR_TITLE = "Errore";

    @FXML
    private Button modificaButton; // Pulsante per modificare l'ordine

    @FXML
    private Button eliminaButton; // Pulsante per eliminare l'ordine

    @FXML
    private Button archiviaButton; // Pulsante per archiviare l'ordine






    @FXML
    public void initialize() {
        configureComboBox();
        initTableColumns();
        refreshTable();

        modificaButton.setDisable(true);
        eliminaButton.setDisable(true);
        archiviaButton.setDisable(true); // Disabilita inizialmente anche il pulsante archivia

        ordinazioniTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isSelected = (newValue != null);
            modificaButton.setDisable(!isSelected);
            eliminaButton.setDisable(!isSelected);
            archiviaButton.setDisable(!isSelected); // Abilita o disabilita il pulsante archivia
        });
    }

    private void configureComboBox() {
        tipoOrdineComboBox.setItems(FXCollections.observableArrayList());
        tipoOrdineComboBox.getItems().add(null);
        tipoOrdineComboBox.getItems().addAll(TipoOrdine.values());
        tipoOrdineComboBox.setPromptText("Al Tavolo o Asporto?");
        tipoOrdineComboBox.setValue(null);
        tipoOrdineComboBox.setCellFactory(lv -> createOrderCell());
        tipoOrdineComboBox.setButtonCell(createOrderCell());
    }

    // Metodo per creare una ListCell personalizzata
    private ListCell<TipoOrdine> createOrderCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(TipoOrdine item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "Al Tavolo o Asporto?" : item.name().replace("_", " "));
            }
        };
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
        String nomeCliente = nomeClienteField.getText();
        TipoOrdine tipoOrdine = tipoOrdineComboBox.getValue();
        String orario = orarioField.getText();
        String coperti = copertiField.getText();
        String tavolo = tavoloField.getText();

        if (tipoOrdine == null) {
            showAlert("Tipo Ordine Mancante",
                    "Seleziona un tipo di ordine: 'Al Tavolo' o 'Asporto'.",
                    Alert.AlertType.WARNING);
            return;
        }

        if (tipoOrdine == TipoOrdine.AL_TAVOLO) {
            orario = LocalTime.now().toString().substring(0, 5);
        } else if (tipoOrdine == TipoOrdine.ASPORTO) {
            coperti = null;
            tavolo = null;

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

        if (nomeCliente.isEmpty()) {
            showAlert("Campi Mancanti", "Il campo Nome Cliente deve essere compilato.", Alert.AlertType.WARNING);
            return;
        }

        OrdinazioneBean ordinazioneBean = new OrdinazioneBean();
        ordinazioneBean.setNome(nomeCliente);
        ordinazioneBean.setTipoOrdine(tipoOrdine);
        ordinazioneBean.setOrarioCreazione(orario);
        ordinazioneBean.setNumeroClienti(coperti);
        ordinazioneBean.setInfoTavolo(tavolo);

        ordinazioneController.creaOrdine(ordinazioneBean);

        showAlert("Ordine Creato",
                "L'ordine è stato creato con successo per il cliente: " + nomeCliente,
                Alert.AlertType.INFORMATION);

        clearFields();
        refreshTable();
    }

    // Metodo Getter per ottenere l'oggetto statico OrdineSelezionato
    public static OrdinazioneBean getOrdineSelezionato() {
        return ordineSelezionato;
    }

    public static void setOrdineSelezionato(OrdinazioneBean ordine) {
        ordineSelezionato = ordine;
    }

    @FXML
    private void modificaOrdine() {
        Ordinazione ordinazione = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazione == null) {
            showAlert(ERROR_TITLE, "Seleziona un ordine da modificare.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Creiamo un nuovo OrdinazioneBean e usiamo il setter statico per impostarlo
            OrdinazioneBean ordine = new OrdinazioneBean();
            ordine.setId(ordinazione.getId());
            ordine.setNome(ordinazione.getNomeCliente());
            ordine.setNumeroClienti(ordinazione.getNumeroClienti());
            ordine.setTipoOrdine(ordinazione.getTipoOrdine());
            ordine.setInfoTavolo(ordinazione.getInfoTavolo());
            ordine.setStatoOrdine(ordinazione.getStatoOrdine());
            ordine.setOrarioCreazione(ordinazione.getOrarioCreazione());

            // Utilizzo del metodo statico per impostare l'ordine selezionato
            setOrdineSelezionato(ordine);

            // Carichiamo la nuova scena
            SceneLoader.loadScene("/com/biteme/app/ordine.fxml", "Modifica Ordine");

        } catch (Exception e) {
            showAlert(ERROR_TITLE, "Errore durante il caricamento della schermata di modifica.", Alert.AlertType.ERROR);
        }
    }


    // Metodo per eliminare l'ordine selezionato
    @FXML
    private void eliminaOrdine() {
        Ordinazione ordinazioneSelezionato = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazioneSelezionato == null) {
            showAlert(ERROR_TITLE, "Seleziona un ordine da eliminare.", Alert.AlertType.ERROR);
            return;
        }

        boolean conferma = mostraDialogConferma("Sei sicuro di voler eliminare l'ordine " + ordinazioneSelezionato.getId() + "?");
        if (conferma) {
            try {
                ordinazioneController.eliminaOrdine(ordinazioneSelezionato.getId());
                refreshTable();
                showAlert("Successo", "Ordine eliminato con successo.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert(ERROR_TITLE, "Errore durante l'eliminazione dell'ordine.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void archiviaOrdine(ActionEvent actionEvent) {
        Ordinazione ordinazioneSelezionata = ordinazioniTableView.getSelectionModel().getSelectedItem();

        if (ordinazioneSelezionata == null) {
            showAlert(ERROR_TITLE, "Seleziona un ordine da archiviare.", Alert.AlertType.ERROR);
            return;
        }

        boolean conferma = mostraDialogConferma("Sei sicuro di voler archiviare l'ordine " + ordinazioneSelezionata.getId() + "?");
        if (conferma) {
            try {
                // Step 1: Recupera l'OrdineBean corrispondente
                OrdineBean ordineBean = ordineController.getOrdineById(ordinazioneSelezionata.getId());

                // Step 2: Calcola il totale
                BigDecimal totale = calcolaTotaleOrdine(ordineBean);

                // Step 3: Inizializza ArchivioBean
                ArchivioBean archivioBean = new ArchivioBean();
                archivioBean.setIdOrdine(ordineBean.getId());
                archivioBean.setProdotti(ordineBean.getProdotti());
                archivioBean.setQuantita(ordineBean.getQuantita());
                archivioBean.setTotale(totale);
                archivioBean.setDataArchiviazione(LocalDateTime.now());

                // Passa i dettagli al controller per gestire l'archiviazione effettiva
                archivioController.archiviaOrdine(archivioBean);

                // Step 4: Elimina l'ordine dalla lista attiva
                ordinazioneController.eliminaOrdine(ordinazioneSelezionata.getId());

                refreshTable();
                showAlert("Successo", "Ordine archiviato con successo.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert(ERROR_TITLE, "Errore durante l'archiviazione: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private BigDecimal calcolaTotaleOrdine(OrdineBean ordineBean) {
        BigDecimal totale = BigDecimal.ZERO;

        if (ordineBean == null) {
            throw new IllegalArgumentException("OrdineBean non valido: null");
        }

        List<String> prodotti = ordineBean.getProdotti();
        List<Integer> quantita = ordineBean.getQuantita();

        // Controllo se prodotti o quantità sono null o vuoti
        if (prodotti == null || quantita == null || prodotti.isEmpty() || quantita.isEmpty()) {
            Logger.getLogger(this.getClass().getName())
                    .warning("Lista prodotti o quantità vuota. Totale ordine: 0");
            return totale; // Ritorna 0 come totale
        }

        // Verifica che la lunghezza delle liste prodotti e quantita sia congruente
        if (prodotti.size() != quantita.size()) {
            throw new IllegalStateException("Dimensioni di prodotti e quantità non corrispondenti");
        }

        for (int i = 0; i < prodotti.size(); i++) {
            String nomeProdotto = prodotti.get(i);
            ProdottoBean prodotto = prodottoController.getProdottoByNome(nomeProdotto);

            if (prodotto == null) {
                throw new RuntimeException("Prodotto non trovato: " + nomeProdotto);
            }

            BigDecimal prezzo = prodotto.getPrezzo();
            totale = totale.add(prezzo.multiply(BigDecimal.valueOf(quantita.get(i))));
        }

        return totale;
    }


    private void refreshTable() {
        List<Ordinazione> ordini = ordinazioneController.getOrdini();
        ordinazioniTableView.getItems().setAll(ordini);
    }

    private void clearFields() {
        // Ripulisci i campi
        nomeClienteField.clear();
        orarioField.clear();
        copertiField.clear();
        tavoloField.clear();

        // Resetta la ComboBox al placeholder
        tipoOrdineComboBox.getSelectionModel().clearSelection();
        tipoOrdineComboBox.setValue(null);
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