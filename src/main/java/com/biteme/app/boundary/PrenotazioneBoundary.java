package com.biteme.app.boundary;

import com.biteme.app.bean.EmailBean;
import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.controller.EmailController;
import com.biteme.app.controller.PrenotazioneController;
import com.biteme.app.exception.PrenotationValidationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class PrenotazioneBoundary {

    private static final String SUCCESS_TITLE = "Successo";
    private static final String ERROR_TITLE = "Errore";
    private static final String DAY_LABEL_DEFAULT_STYLE = "-fx-font-size: 14; -fx-alignment: center; -fx-border-color: lightgray; -fx-border-width: 0.5;" +
            "-fx-background-color: transparent; -fx-text-fill: black; -fx-cursor: hand;";
    private static final String DAY_LABEL_SELECTED_STYLE = "-fx-font-size: 14; -fx-alignment: center; -fx-border-color: lightgray; -fx-border-width: 0.5;" +
            "-fx-background-color: #E0218A; -fx-text-fill: white; -fx-cursor: hand;";

    @FXML private Label meseLabel;
    @FXML private Label frecciaIndietro;
    @FXML private Label frecciaAvanti;
    @FXML private GridPane calendarioGrid;

    @FXML private TextField nomeClienteField;
    @FXML private TextField orarioField;
    @FXML private TextField copertiField;
    @FXML private TextField noteField;
    @FXML private TextField emailField;

    @FXML private TableView<PrenotazioneBean> prenotazioniTableView;
    @FXML private TableColumn<PrenotazioneBean, Integer> idColumn;
    @FXML private TableColumn<PrenotazioneBean, String> nomeColumn;
    @FXML private TableColumn<PrenotazioneBean, LocalDate> dataColumn;
    @FXML private TableColumn<PrenotazioneBean, LocalTime> orarioColumn;
    @FXML private TableColumn<PrenotazioneBean, Integer> copertiColumn;
    @FXML private TableColumn<PrenotazioneBean, String> emailColumn;
    @FXML private TableColumn<PrenotazioneBean, String> noteColumn;

    @FXML private Button modificaButton;
    @FXML private Button eliminaButton;
    @FXML private Button emailButton;  

    private final PrenotazioneController prenotazioneController = new PrenotazioneController();
    private final EmailController emailController = new EmailController();
    private YearMonth meseCorrente;
    private Node casellaSelezionata;
    private LocalDate giornoSelezionato;

    @FXML
    private void initialize() {
        configureTableColumns();
        meseCorrente = YearMonth.now();
        giornoSelezionato = null;
        aggiornaCalendario();

        prenotazioniTableView.getItems().clear();

        modificaButton.setDisable(true);
        eliminaButton.setDisable(true);
        emailButton.setDisable(true);

        modificaButton.setCursor(javafx.scene.Cursor.HAND);
        eliminaButton.setCursor(javafx.scene.Cursor.HAND);
        emailButton.setCursor(javafx.scene.Cursor.HAND);

        frecciaIndietro.setCursor(javafx.scene.Cursor.HAND);
        frecciaAvanti.setCursor(javafx.scene.Cursor.HAND);

        prenotazioniTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean rigaSelezionata = newValue != null;
            modificaButton.setDisable(!rigaSelezionata);
            eliminaButton.setDisable(!rigaSelezionata);
            emailButton.setDisable(!rigaSelezionata);
        });

        frecciaIndietro.setOnMouseClicked(event -> mesePrecedente());
        frecciaAvanti.setOnMouseClicked(event -> meseSuccessivo());
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        dataColumn.setCellValueFactory(new PropertyValueFactory<>("data"));
        orarioColumn.setCellValueFactory(new PropertyValueFactory<>("orario"));
        copertiColumn.setCellValueFactory(new PropertyValueFactory<>("coperti"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
    }

    /**
     * Crea una nuova prenotazione partendo dai dati inseriti dall'utente.
     */
    @FXML
    private void creaPrenotazione() {
        try {
            PrenotazioneBean bean = new PrenotazioneBean();
            bean.setNomeCliente(nomeClienteField.getText().trim());
            bean.setOrarioStr(orarioField.getText().trim());
            bean.setData(giornoSelezionato);
            bean.setEmail(emailField.getText().trim());
            bean.setNote(noteField.getText().trim());
            bean.setCopertiStr(copertiField.getText().trim());

            prenotazioneController.creaPrenotazione(bean);

            showAlert(SUCCESS_TITLE, "Prenotazione creata con successo!", Alert.AlertType.INFORMATION);
            resetForm();
            refreshTable(giornoSelezionato);
        } catch (PrenotationValidationException e) {
            showAlert(ERROR_TITLE, e.getMessage(), Alert.AlertType.ERROR);
            resetForm();
        }
    }

    @FXML
    private void eliminaPrenotazione() {
        PrenotazioneBean selected = prenotazioniTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(ERROR_TITLE, "Seleziona una prenotazione dalla tabella.", Alert.AlertType.ERROR);
            return;
        }

        prenotazioneController.eliminaPrenotazione(selected.getId());
        showAlert(SUCCESS_TITLE, "Prenotazione eliminata con successo.", Alert.AlertType.INFORMATION);
        refreshTable(giornoSelezionato);
    }

    @FXML
    private void modificaPrenotazione() {
        PrenotazioneBean selected = prenotazioniTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        mostraDialogModifica(selected);
    }

    private void mostraDialogModifica(PrenotazioneBean prenotazione) {
        Dialog<PrenotazioneBean> dialog = new Dialog<>();
        dialog.setTitle("Modifica Prenotazione");
        dialog.setHeaderText("Modifica i dati della prenotazione");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField nomeField = new TextField(prenotazione.getNomeCliente());
        DatePicker dataPicker = new DatePicker(prenotazione.getData());
        
        TextField orarioInput = new TextField(prenotazione.getOrario() != null ? prenotazione.getOrario().toString() : "");
        TextField copertiInput = new TextField(prenotazione.getCoperti() != 0 ? String.valueOf(prenotazione.getCoperti()) : "");
        TextField emailInput = new TextField(prenotazione.getEmail());
        TextField noteInput = new TextField(prenotazione.getNote());

        grid.add(new Label("Nome cliente:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Data:"), 0, 1);
        grid.add(dataPicker, 1, 1);
        grid.add(new Label("Orario (hh:mm):"), 0, 2);
        grid.add(orarioInput, 1, 2);
        grid.add(new Label("Coperti:"), 0, 3);
        grid.add(copertiInput, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailInput, 1, 4);
        grid.add(new Label("Note:"), 0, 5);
        grid.add(noteInput, 1, 5);

        dialog.getDialogPane().setContent(grid);

        ButtonType salvaButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvaButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == salvaButtonType) {
                try {
                    PrenotazioneBean bean = new PrenotazioneBean();
                    bean.setId(prenotazione.getId());
                    bean.setNomeCliente(nomeField.getText().trim());
                    bean.setData(dataPicker.getValue());
                    bean.setOrarioStr(orarioInput.getText().trim());
                    bean.setCopertiStr(copertiInput.getText().trim());
                    bean.setEmail(emailInput.getText().trim());
                    bean.setNote(noteInput.getText().trim());
                    return prenotazioneController.modificaPrenotazione(bean);
                } catch (PrenotationValidationException e) {
                    showAlert(ERROR_TITLE, e.getMessage(), Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(result -> {
            if (result != null) {
                showAlert(SUCCESS_TITLE, "Prenotazione modificata con successo!", Alert.AlertType.INFORMATION);
                refreshTable(giornoSelezionato);
            }
        });
    }

    @FXML
    private void inviaEmail() {
        PrenotazioneBean selected = prenotazioniTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(ERROR_TITLE, "Seleziona una prenotazione dalla tabella.", Alert.AlertType.ERROR);
            return;
        }
        
        PrenotazioneBean bean = new PrenotazioneBean();
        bean.setNomeCliente(selected.getNomeCliente());
        bean.setData(selected.getData());
        bean.setOrario(selected.getOrario());
        bean.setCoperti(selected.getCoperti());
        bean.setNote(selected.getNote());
        bean.setEmail(selected.getEmail());

        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Invia Prenotazione via Email");
        dialog.setHeaderText("Inserisci l'indirizzo email del cliente");
        ButtonType inviaButtonType = new ButtonType("Invia", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(inviaButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField newemailField = new TextField();
        newemailField.setPromptText("Email del cliente");
        grid.add(new Label("Email:"), 0, 0);
        grid.add(newemailField, 1, 0);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == inviaButtonType) {
                return newemailField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(email -> {
            if (email == null || email.isEmpty()) {
                showAlert(ERROR_TITLE, "L'indirizzo email non può essere vuoto.", Alert.AlertType.ERROR);
                return;
            }
            
            EmailBean emailBean = emailController.composeEmailFromPrenotazione(bean);
            emailBean.setDestinatario(email);
            try {
                emailController.sendEmail(emailBean);
                showAlert(SUCCESS_TITLE, "Email inviata correttamente a " + email, Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert(ERROR_TITLE, "Errore durante l'invio dell'email: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void refreshTable(LocalDate data) {
        if (data != null) {
            List<PrenotazioneBean> prenotazioni = prenotazioneController.getPrenotazioniByData(data);
            prenotazioniTableView.setItems(FXCollections.observableArrayList(prenotazioni));
            prenotazioniTableView.refresh();
        } else {
            prenotazioniTableView.getItems().clear();
        }
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
        calendarioGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        LocalDate primoGiorno = mese.atDay(1);
        int primoGiornoSettimana = primoGiorno.getDayOfWeek().getValue() % 7;
        int giorniNelMese = mese.lengthOfMonth();
        LocalDate oggi = LocalDate.now();

        int giornoCorrente = 1;
        for (int riga = 1; riga <= 6; riga++) {
            for (int colonna = 0; colonna < 7; colonna++) {
                if (giornoCorrente > giorniNelMese) {
                    return;
                }
                if (riga == 1 && colonna < primoGiornoSettimana) {
                    continue;
                }
                LocalDate data = mese.atDay(giornoCorrente);
                Label giorno = new Label(String.valueOf(giornoCorrente));
                if (data.isBefore(oggi)) {
                    giorno.setStyle("-fx-font-size: 14; -fx-alignment: center; -fx-border-color: lightgray; -fx-border-width: 0.5;" +
                            "-fx-background-color: lightgray; -fx-text-fill: darkgray; -fx-cursor: default;");
                } else {
                    giorno.setStyle(DAY_LABEL_DEFAULT_STYLE);
                    giorno.setOnMouseClicked(event -> selezionaGiorno(data, giorno));
                }
                giorno.setPrefSize(40, 40);
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
        emailField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
