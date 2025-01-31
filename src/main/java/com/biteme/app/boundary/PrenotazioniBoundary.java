package com.biteme.app.boundary;

import com.biteme.app.bean.PrenotazioniBean;
import com.biteme.app.controller.PrenotazioniController;
import com.biteme.app.entity.Prenotazione;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class PrenotazioniBoundary {

    // Costanti
    private static final String SUCCESS_TITLE = "Successo";
    private static final String ERROR_TITLE = "Errore";
    private static final String DAY_LABEL_DEFAULT_STYLE = "-fx-font-size: 14; -fx-alignment: center; -fx-border-color: lightgray; -fx-border-width: 0.5;" +
            "-fx-background-color: transparent; -fx-text-fill: black; -fx-cursor: hand;";
    private static final String DAY_LABEL_SELECTED_STYLE = "-fx-font-size: 14; -fx-alignment: center; -fx-border-color: lightgray; -fx-border-width: 0.5;" +
            "-fx-background-color: #E0218A; -fx-text-fill: white; -fx-cursor: hand;";

    // Campi FXML
    @FXML
    private Label meseLabel;
    @FXML
    private Label frecciaIndietro;
    @FXML
    private Label frecciaAvanti;
    @FXML
    private GridPane calendarioGrid;

    @FXML
    private TextField nomeClienteField;
    @FXML
    private TextField orarioField;
    @FXML
    private TextField copertiField;
    @FXML
    private TextField noteField;
    @FXML
    private TextField telefonoField;

    @FXML
    private TableView<Prenotazione> prenotazioniTableView;
    @FXML
    private TableColumn<Prenotazione, Integer> idColumn;
    @FXML
    private TableColumn<Prenotazione, String> nomeColumn;
    @FXML
    private TableColumn<Prenotazione, LocalDate> dataColumn;
    @FXML
    private TableColumn<Prenotazione, LocalTime> orarioColumn;
    @FXML
    private TableColumn<Prenotazione, Integer> copertiColumn;
    @FXML
    private TableColumn<Prenotazione, String> telefonoColumn;
    @FXML
    private TableColumn<Prenotazione, String> noteColumn;

    @FXML
    private Button modificaButton;
    @FXML
    private Button eliminaButton;

    // Variabili
    private final PrenotazioniController prenotazioniController = new PrenotazioniController();
    private YearMonth meseCorrente;
    private Node casellaSelezionata;
    private LocalDate giornoSelezionato;

    @FXML
    private void initialize() {
        configureTableColumns();
        // Configurazione calendario
        meseCorrente = YearMonth.now();
        giornoSelezionato = LocalDate.now();
        aggiornaCalendario();

        // Caricamento iniziale dati
        refreshTable(LocalDate.now());

        modificaButton.setDisable(true);
        eliminaButton.setDisable(true);

        prenotazioniTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean rigaSelezionata = newValue != null;
            modificaButton.setDisable(!rigaSelezionata); // Abilita/disabilita il pulsante "Modifica"
            eliminaButton.setDisable(!rigaSelezionata);  // Abilita/disabilita il pulsante "Elimina"
        });

        // Gestione eventi per il cambio mese
        frecciaIndietro.setOnMouseClicked(event -> mesePrecedente());
        frecciaAvanti.setOnMouseClicked(event -> meseSuccessivo());
    }
    private void configureTableColumns() {
        // Configurazione colonne della tabella
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        dataColumn.setCellValueFactory(new PropertyValueFactory<>("data"));
        orarioColumn.setCellValueFactory(new PropertyValueFactory<>("orario"));
        copertiColumn.setCellValueFactory(new PropertyValueFactory<>("coperti"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
    }
    @FXML
    private void createBooking() {
        if (giornoSelezionato == null) {
            showAlert(ERROR_TITLE, "Seleziona un giorno dal calendario.", Alert.AlertType.ERROR);
            return;
        }

        PrenotazioniBean bean = new PrenotazioniBean();
        bean.setNomeCliente(nomeClienteField.getText().trim());

        // Validazione orario
        try {
            bean.setOrario(LocalTime.parse(orarioField.getText().trim()));
        } catch (Exception e) {
            showAlert(ERROR_TITLE, "Inserisci un orario valido (hh:mm).", Alert.AlertType.ERROR);
            return;
        }
        bean.setData(giornoSelezionato);

        // Validazione telefono
        String telefono = telefonoField.getText().trim();
        if (!telefono.isBlank() && !telefono.matches("\\d+")) {
            showAlert(ERROR_TITLE, "Inserisci un numero di telefono valido.", Alert.AlertType.ERROR);
            return;
        }
        bean.setTelefono(telefono);

        bean.setNote(noteField.getText().trim());

        // Validazione coperti
        try {
            int coperti = Integer.parseInt(copertiField.getText().trim());
            if (coperti <= 0) throw new NumberFormatException();
            bean.setCoperti(coperti);
        } catch (NumberFormatException e) {
            showAlert(ERROR_TITLE, "Inserisci un numero valido per i coperti.", Alert.AlertType.ERROR);
            return;
        }

        // Creazione prenotazione tramite controller
        prenotazioniController.creaPrenotazione(bean);

        showAlert(SUCCESS_TITLE, "Prenotazione creata con successo!", Alert.AlertType.INFORMATION);
        resetForm();
        refreshTable(giornoSelezionato);
    }

    @FXML
    private void eliminaPrenotazione() {
        Prenotazione selected = prenotazioniTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(ERROR_TITLE, "Seleziona una prenotazione dalla tabella.", Alert.AlertType.ERROR);
            return;
        }

        prenotazioniController.eliminaPrenotazione(selected.getId());
        showAlert(SUCCESS_TITLE, "Prenotazione eliminata con successo.", Alert.AlertType.INFORMATION);
        refreshTable(giornoSelezionato);
    }

    private void mostraDialogModifica(Prenotazione prenotazione) {
        Dialog<Prenotazione> dialog = new Dialog<>();
        dialog.setTitle("Modifica Prenotazione");
        dialog.setHeaderText("Modifica i dati della prenotazione");

        // Configurazione dei campi di input per la modifica
        TextField nomeField = new TextField(prenotazione.getNomeCliente());
        DatePicker dataPicker = new DatePicker(prenotazione.getData());
        TextField orarioField = new TextField(prenotazione.getOrario().toString());
        TextField copertiField = new TextField(String.valueOf(prenotazione.getCoperti()));
        TextField telefonoField = new TextField(prenotazione.getTelefono());
        TextField noteField = new TextField(prenotazione.getNote());

        // Layout del dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nome cliente:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Data:"), 0, 1);
        grid.add(dataPicker, 1, 1);
        grid.add(new Label("Orario (hh:mm):"), 0, 2);
        grid.add(orarioField, 1, 2);
        grid.add(new Label("Coperti:"), 0, 3);
        grid.add(copertiField, 1, 3);
        grid.add(new Label("Telefono:"), 0, 4);
        grid.add(telefonoField, 1, 4);
        grid.add(new Label("Note:"), 0, 5);
        grid.add(noteField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Pulsante di salvataggio
        ButtonType salvaButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvaButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == salvaButtonType) {
                try {
                    // Validazione dei dati inseriti
                    String nomeCliente = nomeField.getText().trim();
                    if (nomeCliente.isEmpty()) {
                        showAlert(ERROR_TITLE, "Il nome del cliente non può essere vuoto.", Alert.AlertType.ERROR);
                        return null;
                    }

                    LocalDate data = dataPicker.getValue();
                    if (data == null) {
                        showAlert(ERROR_TITLE, "Devi selezionare una data.", Alert.AlertType.ERROR);
                        return null;
                    }

                    LocalTime orario;
                    try {
                        orario = LocalTime.parse(orarioField.getText().trim());
                    } catch (Exception e) {
                        showAlert(ERROR_TITLE, "Inserisci un orario valido (hh:mm).", Alert.AlertType.ERROR);
                        return null;
                    }

                    int coperti;
                    try {
                        coperti = Integer.parseInt(copertiField.getText().trim());
                        if (coperti <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        showAlert(ERROR_TITLE, "Inserisci un numero valido per i coperti.", Alert.AlertType.ERROR);
                        return null;
                    }

                    String telefono = telefonoField.getText().trim();
                    if (!telefono.isEmpty() && !telefono.matches("\\d{10}")) {
                        showAlert(ERROR_TITLE, "Il numero di telefono deve essere composto da 10 cifre.", Alert.AlertType.ERROR);
                        return null;
                    }

                    String note = noteField.getText().trim();

                    return new Prenotazione(
                            prenotazione.getId(), // Manteniamo l'ID originale
                            nomeCliente,
                            orario,
                            data,
                            note,
                            telefono,
                            coperti
                    );
                } catch (Exception e) {
                    showAlert(ERROR_TITLE, "Errore durante la modifica della prenotazione.", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(prenotazioneAggiornata -> {
            // Controllo se la data è stata modificata
            if (!prenotazione.getData().equals(prenotazioneAggiornata.getData())) {
                // Rimuovi la prenotazione dalla vecchia data e aggiorna anche il calendario del giorno nuovo
                refreshTable(prenotazione.getData());
                refreshTable(prenotazioneAggiornata.getData());
            } else {
                // Solo aggiorna senza spostamenti
                refreshTable(prenotazioneAggiornata.getData());
            }

            // Salva la prenotazione modificata nel database
            prenotazioniController.modificaPrenotazione(prenotazioneAggiornata);
            showAlert(SUCCESS_TITLE, "Prenotazione aggiornata correttamente!", Alert.AlertType.INFORMATION);
        });
    }

    @FXML
    private void modificaPrenotazione() {
        // Ottieni la prenotazione selezionata
        Prenotazione prenotazioneSelezionata = prenotazioniTableView.getSelectionModel().getSelectedItem();
        if (prenotazioneSelezionata == null) {
            showAlert(ERROR_TITLE, "Seleziona una prenotazione da modificare.", Alert.AlertType.ERROR);
            return;
        }

        // Mostra il dialog di modifica per la prenotazione
        mostraDialogModifica(prenotazioneSelezionata);
    }

    private void refreshTable(LocalDate data) {
        List<Prenotazione> prenotazioni = prenotazioniController.getPrenotazioniByData(data);
        prenotazioniTableView.getItems().setAll(prenotazioni);
    }

    private void aggiornaCalendario() {
        String nomeMese = meseCorrente.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        meseLabel.setText(nomeMese + " " + meseCorrente.getYear());
        popolaCalendario(meseCorrente);
    }

    private void mesePrecedente() {
        meseCorrente = meseCorrente.minusMonths(1);
        aggiornaCalendario();
    }

    private void meseSuccessivo() {
        meseCorrente = meseCorrente.plusMonths(1);
        aggiornaCalendario();
    }

    private void popolaCalendario(YearMonth mese) {
        // Rimuove i giorni esistenti (ma SENZA toccare l'intestazione dei giorni della settimana)
        calendarioGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        // Calcolo della distribuzione dei giorni
        LocalDate primoGiorno = mese.atDay(1);
        int primoGiornoSettimana = primoGiorno.getDayOfWeek().getValue() % 7; // Con giorno Lunedì come primo giorno della settimana
        int giorniNelMese = mese.lengthOfMonth();

        // Aggiunge i giorni del mese al calendario
        int giornoCorrente = 1;
        for (int riga = 1; giornoCorrente <= giorniNelMese; riga++) {
            for (int colonna = 0; colonna < 7; colonna++) {
                if (riga == 1 && colonna < primoGiornoSettimana) continue; // Salta le celle prima del primo giorno del mese
                if (giornoCorrente > giorniNelMese) break; // Fine del mese

                LocalDate data = mese.atDay(giornoCorrente); // Data attuale del ciclo
                Label giorno = new Label(String.valueOf(giornoCorrente));
                giorno.setStyle(DAY_LABEL_DEFAULT_STYLE); // Stile per i giorni non evidenziati

                // Aggiungi un callback per selezionare il giorno
                giorno.setOnMouseClicked(event -> selezionaGiorno(data, giorno));

                // Imposta dimensioni uniformi per i giorni del mese
                giorno.setPrefSize(40, 40);

                // Aggiunge il giorno alla griglia
                calendarioGrid.add(giorno, colonna, riga);

                giornoCorrente++;
            }
        }
    }

    private void selezionaGiorno(LocalDate data, Label giorno) {
        if (casellaSelezionata instanceof Label) {
            (casellaSelezionata).setStyle(DAY_LABEL_DEFAULT_STYLE);
        }
        giorno.setStyle(DAY_LABEL_SELECTED_STYLE);
        casellaSelezionata = giorno;
        giornoSelezionato = data;

        refreshTable(data);
    }

    private void resetForm() {
        nomeClienteField.clear();
        orarioField.clear();
        copertiField.clear();
        noteField.clear();
        telefonoField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}