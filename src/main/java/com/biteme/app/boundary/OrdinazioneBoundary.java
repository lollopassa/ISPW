package com.biteme.app.boundary;

import com.biteme.app.controller.OrdinazioneController;
import com.biteme.app.entity.Ordine;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.biteme.app.bean.OrdinazioneBean;

import java.util.List;

public class OrdinazioneBoundary {

    // Campi UI
    @FXML
    private TextField nomeClienteField;

    @FXML
    private TextField tipoOrdineField;

    @FXML
    private TextField orarioField;

    @FXML
    private TextField copertiField;

    @FXML
    private TextField tavoloField;

    @FXML
    private TableView<Ordine> ordinazioniTableView;

    @FXML
    private TableColumn<Ordine, Integer> idColumn;

    @FXML
    private TableColumn<Ordine, String> nomeColumn;

    @FXML
    private TableColumn<Ordine, String> tipoOrdineColumn;

    @FXML
    private TableColumn<Ordine, String> orarioColumn;

    @FXML
    private TableColumn<Ordine, String> copertiColumn;

    @FXML
    private TableColumn<Ordine, String> infoTavoloColumn;

    @FXML
    private TableColumn<Ordine, String> statoOrdineColumn;

    @FXML
    private TableColumn<Ordine, Void> azioniColumn;

    private final OrdinazioneController ordinazioneController = new OrdinazioneController();

    @FXML
    public void initialize() {
        // Inizializza le colonne della tabella
        initTableColumns();

        // Carica i dati nella tabella
        refreshTable();
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
        String tipoOrdine = tipoOrdineField.getText();
        String orario = orarioField.getText();
        String coperti = copertiField.getText();
        String tavolo = tavoloField.getText();

        // Controlla che tipoOrdine sia valido
        if (!tipoOrdine.equalsIgnoreCase("Al Tavolo") && !tipoOrdine.equalsIgnoreCase("Asporto")) {
            showAlert("Tipo Ordine Invalido",
                    "Il campo 'Tipo Ordine' deve essere solo 'Al Tavolo' o 'Asporto'.",
                    Alert.AlertType.WARNING);
            return;
        }

        // Imposta automaticamente l'orario se il tipo ordine è "Al Tavolo"
        if (tipoOrdine.equalsIgnoreCase("Al Tavolo")) {
            orario = java.time.LocalTime.now().toString().substring(0, 5); // Prende l'ora attuale nel formato HH:mm
        }

        // Imposta campi a null se il tipo ordine è "Asporto"
        if (tipoOrdine.equalsIgnoreCase("Asporto")) {
            coperti = null;
            tavolo = null;

            // Esegui la validazione dell'orario di input
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

        // Esegui la validazione dei campi obbligatori
        if (nomeCliente.isEmpty() || tipoOrdine.isEmpty()) {
            showAlert("Campi Mancanti",
                    "I campi Nome Cliente e Tipo Ordine devono essere compilati.",
                    Alert.AlertType.WARNING);
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


    private void refreshTable() {
        // Recupera la lista aggiornata di ordini dal controller
        List<Ordine> ordini = ordinazioneController.getOrdini();

        // Aggiorna gli elementi nella TableView
        ordinazioniTableView.getItems().setAll(ordini);
    }


    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}