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

    private static OrdinazioneBean ordineSelezionato;
    private final OrdinazioneController ordinazioneController = new OrdinazioneController();
    private static final String ERROR_TITLE = "Errore";

    @FXML
    private Button modificaButton; // Pulsante per modificare l'ordine

    @FXML
    private Button eliminaButton; // Pulsante per eliminare l'ordine





    @FXML
    public void initialize() {
        configureComboBox();
        initTableColumns();
        refreshTable();

        modificaButton.setDisable(true);
        eliminaButton.setDisable(true);

        ordinazioniTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isSelected = (newValue != null);
            modificaButton.setDisable(!isSelected);
            eliminaButton.setDisable(!isSelected);
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
                "L'ordine Ã¨ stato creato con successo per il cliente: " + nomeCliente,
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