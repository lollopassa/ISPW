package com.biteme.app.boundary;

import com.biteme.app.bean.PrenotazioniBean;
import com.biteme.app.controller.PrenotazioniController;
import com.biteme.app.entity.Prenotazione;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;

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
    private static final String DELETE_BUTTON_STYLE = "-fx-background-color: #E0218A; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 12px;";
    private static final String DAY_LABEL_DEFAULT_STYLE = "-fx-font-size: 14; -fx-alignment: center; -fx-border-color: lightgray; "
            + "-fx-border-width: 0.5; -fx-background-color: transparent; "
            + "-fx-text-fill: black; -fx-cursor: hand;";
    private static final String DAY_LABEL_SELECTED_STYLE = "-fx-font-size: 14; -fx-alignment: center; -fx-border-color: lightgray; "
            + "-fx-border-width: 0.5; -fx-background-color: #E0218A; "
            + "-fx-text-fill: white; -fx-cursor: hand;";

    // Campi UI
    @FXML
    private TextField nomeClienteField;

    @FXML
    private TextField orarioField;

    @FXML
    private TextField noteField;

    @FXML
    private TextField telefonoField;

    @FXML
    private TextField copertiField;

    @FXML
    private TableView<Prenotazione> prenotazioniTableView;

    @FXML
    private TableColumn<Prenotazione, Integer> idColumn;

    @FXML
    private TableColumn<Prenotazione, String> nomeColumn;

    @FXML
    private TableColumn<Prenotazione, LocalDate> dataColumn;

    @FXML
    private TableColumn<Prenotazione, String> orarioColumn;

    @FXML
    private TableColumn<Prenotazione, String> telefonoColumn;

    @FXML
    private TableColumn<Prenotazione, String> noteColumn;

    @FXML
    private TableColumn<Prenotazione, Integer> copertiColumn;

    @FXML
    private TableColumn<Prenotazione, Void> azioniColumn;

    @FXML
    private Label meseLabel;

    @FXML
    private GridPane calendarioGrid;

    @FXML
    private Label frecciaIndietro;

    @FXML
    private Label frecciaAvanti;

    private YearMonth meseCorrente;
    private Node casellaSelezionata;

    private LocalDate giornoSelezionato;
    private boolean isGiornoSelezionato = false;

    private final PrenotazioniController prenotazioniController = new PrenotazioniController();

    @FXML
    private void initialize() {
        // Configura colonne della tabella
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        dataColumn.setCellValueFactory(new PropertyValueFactory<>("data"));
        orarioColumn.setCellValueFactory(new PropertyValueFactory<>("orario"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        copertiColumn.setCellValueFactory(new PropertyValueFactory<>("coperti"));

        // Configura calendario
        meseCorrente = YearMonth.now();
        giornoSelezionato = LocalDate.now();
        aggiornaCalendario();

        // Carica dati iniziali nella tabella
        refreshTable(LocalDate.now());

        frecciaIndietro.setOnMouseClicked(event -> mesePrecedente());
        frecciaAvanti.setOnMouseClicked(event -> meseSuccessivo());

        addDeleteButtonToTable();
    }

    @FXML
    private void createBooking() {
        if (!isGiornoSelezionato) {
            showAlert(ERROR_TITLE, "Devi selezionare un giorno per creare una prenotazione.", Alert.AlertType.ERROR);
            return;
        }

        PrenotazioniBean prenotazioniBean = new PrenotazioniBean();
        prenotazioniBean.setNomeCliente(nomeClienteField.getText());

        try {
            prenotazioniBean.setOrario(LocalTime.parse(orarioField.getText()));
            prenotazioniBean.setData(giornoSelezionato);
        } catch (Exception e) {
            showAlert(ERROR_TITLE, "Inserisci un orario valido (esempio: 14:00)", Alert.AlertType.ERROR);
            return;
        }

        String telefono = telefonoField.getText();
        if (!telefono.isBlank() && !telefono.matches("\\d+")) {
            showAlert(ERROR_TITLE, "Il numero di telefono deve contenere solo cifre se fornito.", Alert.AlertType.ERROR);
            return;
        }
        prenotazioniBean.setTelefono(telefono.isBlank() ? null : telefono);

        prenotazioniBean.setNote(noteField.getText());

        try {
            int coperti = Integer.parseInt(copertiField.getText());
            if (coperti <= 0) {
                throw new NumberFormatException();
            }
            prenotazioniBean.setCoperti(coperti);
        } catch (NumberFormatException e) {
            showAlert(ERROR_TITLE, "Inserisci un numero valido per i coperti.", Alert.AlertType.ERROR);
            return;
        }

        if (prenotazioniBean.getNomeCliente().isBlank()) {
            showAlert(ERROR_TITLE, "Il nome del cliente è obbligatorio.", Alert.AlertType.WARNING);
            return;
        }

        prenotazioniController.creaPrenotazione(prenotazioniBean);

        showAlert(SUCCESS_TITLE, "Prenotazione aggiunta correttamente!", Alert.AlertType.INFORMATION);

        resetForm();
        refreshTable(giornoSelezionato);
    }

    @FXML
    private void deleteBooking() {
        Prenotazione selected = prenotazioniTableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(ERROR_TITLE, "Seleziona una prenotazione da eliminare.", Alert.AlertType.ERROR);
            return;
        }

        prenotazioniController.eliminaPrenotazione(selected.getId());
        showAlert(SUCCESS_TITLE, "Prenotazione eliminata correttamente.", Alert.AlertType.INFORMATION);

        refreshTable(giornoSelezionato);
    }

    private void refreshTable(LocalDate data) {
        List<Prenotazione> prenotazioni = prenotazioniController.getPrenotazioniByData(data);
        prenotazioniTableView.getItems().setAll(prenotazioni);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
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
        calendarioGrid.getChildren().clear();

        LocalDate primoGiornoDelMese = mese.atDay(1);
        int giornoInizioSettimana = primoGiornoDelMese.getDayOfWeek().getValue();
        int giorniDelMese = mese.lengthOfMonth();
        int giornoCorrente = 1;

        for (int riga = 0; riga < 6; riga++) {
            for (int colonna = 0; colonna < 7; colonna++) {
                if (riga == 0 && colonna < giornoInizioSettimana - 1) {
                    calendarioGrid.add(new Label(""), colonna, riga);
                } else if (giornoCorrente > giorniDelMese) {
                    break;
                } else {
                    int giornoFinale = giornoCorrente;
                    Label giornoLabel = new Label(String.valueOf(giornoFinale));

                    giornoLabel.setUserData(giornoFinale);
                    giornoLabel.setOnMouseClicked(event -> gestisciClickGiorno(LocalDate.of(mese.getYear(), mese.getMonth(), giornoFinale)));
                    giornoLabel.setMinSize(40.0, 40.0);
                    giornoLabel.setStyle(DAY_LABEL_DEFAULT_STYLE);
                    calendarioGrid.add(giornoLabel, colonna, riga);

                    giornoCorrente++;
                }
            }
        }
    }

    private void gestisciClickGiorno(LocalDate data) {
        giornoSelezionato = data;
        isGiornoSelezionato = true;
        if (casellaSelezionata instanceof Label labelPrecedente) {
            labelPrecedente.setStyle(DAY_LABEL_DEFAULT_STYLE);
        }

        for (Node node : calendarioGrid.getChildren()) {
            if (node instanceof Label giornoLabel && giornoLabel.getUserData() != null && giornoLabel.getUserData().equals(data.getDayOfMonth())) {
                giornoLabel.setStyle(DAY_LABEL_SELECTED_STYLE);
                casellaSelezionata = giornoLabel;
                break;
            }
        }
        refreshTable(data);
    }

    private void resetForm() {
        nomeClienteField.setText("");
        orarioField.setText("");
        telefonoField.setText("");
        noteField.setText("");
        copertiField.setText("");
    }

    private void addDeleteButtonToTable() {
        azioniColumn.setCellFactory(param -> new DeleteButtonCell());
    }

    // Classe interna per gestire le celle con pulsante "Elimina"
    private class DeleteButtonCell extends TableCell<Prenotazione, Void> {
        private final Button deleteButton;

        public DeleteButtonCell() {
            deleteButton = new Button("Elimina");
            deleteButton.setStyle(DELETE_BUTTON_STYLE);

            deleteButton.setOnAction(event -> {
                Prenotazione prenotazione = getTableView().getItems().get(getIndex());
                deletePrenotazione(prenotazione);
            });
        }

        private void deletePrenotazione(Prenotazione prenotazione) {
            if (prenotazione != null) {
                prenotazioniController.eliminaPrenotazione(prenotazione.getId());
                getTableView().getItems().remove(prenotazione);
                showAlert(SUCCESS_TITLE, "La prenotazione è stata eliminata correttamente.", Alert.AlertType.INFORMATION);
            } else {
                showAlert(ERROR_TITLE, "Impossibile eliminare la prenotazione selezionata.", Alert.AlertType.ERROR);
            }
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getIndex() >= getTableView().getItems().size()) {
                setGraphic(null);
            } else {
                setGraphic(deleteButton);
            }
        }
    }
}