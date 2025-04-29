package com.biteme.app.ui;

import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.boundary.PrenotazioneBoundary;
import com.biteme.app.exception.EmailSendingException;
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

public class PrenotazioneUI {

    private static final String SUCCESS_TITLE = "Successo";
    private static final String ERROR_TITLE = "Errore";
    private static final String DAY_LABEL_DEFAULT_STYLE =
            "-fx-font-size: 14; -fx-alignment: center; -fx-border-color: lightgray; -fx-border-width: 0.5;"
                    + "-fx-background-color: transparent; -fx-text-fill: black; -fx-cursor: hand;";
    private static final String DAY_LABEL_SELECTED_STYLE =
            "-fx-font-size: 14; -fx-alignment: center; -fx-border-color: lightgray; -fx-border-width: 0.5;"
                    + "-fx-background-color: #E0218A; -fx-text-fill: white; -fx-cursor: hand;";

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

    private final PrenotazioneBoundary prenotazioneBoundary = new PrenotazioneBoundary();
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

        prenotazioniTableView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> {
                    boolean sel = newV != null;
                    modificaButton.setDisable(!sel);
                    eliminaButton.setDisable(!sel);
                    emailButton.setDisable(!sel);
                });

        frecciaIndietro.setOnMouseClicked(e -> mesePrecedente());
        frecciaAvanti.setOnMouseClicked(e -> meseSuccessivo());
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
            prenotazioneBoundary.creaPrenotazione(bean);

            showAlert(SUCCESS_TITLE, "Prenotazione creata con successo!", Alert.AlertType.INFORMATION);
            resetForm();
            refreshTable(giornoSelezionato);
        } catch (PrenotationValidationException e) {
            String msg = e.getMessage();
            if (msg.toLowerCase().contains("identica")) {
                showAlert(ERROR_TITLE,
                        msg + " Per favore modifica il nome per distinguere le prenotazioni.",
                        Alert.AlertType.ERROR);
            } else {
                showAlert(ERROR_TITLE, msg, Alert.AlertType.ERROR);
                resetForm();
            }
        }
    }

    @FXML
    private void eliminaPrenotazione() {
        PrenotazioneBean sel = prenotazioniTableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(ERROR_TITLE, "Seleziona una prenotazione dalla tabella.", Alert.AlertType.ERROR);
            return;
        }
        prenotazioneBoundary.eliminaPrenotazione(sel.getId());
        showAlert(SUCCESS_TITLE, "Prenotazione eliminata con successo.", Alert.AlertType.INFORMATION);
        refreshTable(giornoSelezionato);
    }

    @FXML
    private void modificaPrenotazione() {
        PrenotazioneBean sel = prenotazioniTableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        mostraDialogModifica(sel);
    }

    private void mostraDialogModifica(PrenotazioneBean pren) {
        Dialog<PrenotazioneBean> dialog = new Dialog<>();
        dialog.setTitle("Modifica Prenotazione");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField nomeField = new TextField(pren.getNomeCliente());
        DatePicker dataPicker = new DatePicker(pren.getData());
        TextField orarioInput = new TextField(pren.getOrario() != null ? pren.getOrario().toString() : "");
        TextField copertiInput = new TextField(pren.getCoperti() != 0 ? String.valueOf(pren.getCoperti()) : "");
        TextField emailInput = new TextField(pren.getEmail());
        TextField noteInput = new TextField(pren.getNote());

        grid.add(new Label("Nome cliente:"), 0, 0); grid.add(nomeField, 1, 0);
        grid.add(new Label("Data:"), 0, 1); grid.add(dataPicker, 1, 1);
        grid.add(new Label("Orario (hh:mm):"), 0, 2); grid.add(orarioInput, 1, 2);
        grid.add(new Label("Coperti:"), 0, 3); grid.add(copertiInput, 1, 3);
        grid.add(new Label("Email:"), 0, 4); grid.add(emailInput, 1, 4);
        grid.add(new Label("Note:"), 0, 5); grid.add(noteInput, 1, 5);

        dialog.getDialogPane().setContent(grid);
        ButtonType salva = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salva, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == salva) {
                try {
                    PrenotazioneBean bean = new PrenotazioneBean();
                    bean.setId(pren.getId());
                    bean.setNomeCliente(nomeField.getText().trim());
                    bean.setData(dataPicker.getValue());
                    bean.setOrarioStr(orarioInput.getText().trim());
                    bean.setCopertiStr(copertiInput.getText().trim());
                    bean.setEmail(emailInput.getText().trim());
                    bean.setNote(noteInput.getText().trim());
                    return prenotazioneBoundary.modificaPrenotazione(bean);
                } catch (PrenotationValidationException e) {
                    String m = e.getMessage();
                    if (m.toLowerCase().contains("identica")) {
                        showAlert(ERROR_TITLE,
                                m + " Per favore modifica il nome.",
                                Alert.AlertType.ERROR);
                    } else {
                        showAlert(ERROR_TITLE, m, Alert.AlertType.ERROR);
                    }
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(res -> {
            if (res != null) {
                showAlert(SUCCESS_TITLE, "Prenotazione modificata con successo!", Alert.AlertType.INFORMATION);
                refreshTable(giornoSelezionato);
            }
        });
    }

    @FXML
    private void inviaEmail() {
        PrenotazioneBean sel = prenotazioniTableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(ERROR_TITLE, "Seleziona una prenotazione dalla tabella.", Alert.AlertType.ERROR);
            return;
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Invia via Email");
        dialog.setHeaderText(null);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField newEmail = new TextField();
        newEmail.setPromptText("Email cliente");
        grid.add(new Label("Email:"), 0, 0);
        grid.add(newEmail, 1, 0);
        dialog.getDialogPane().setContent(grid);
        ButtonType inviaBtn = new ButtonType("Invia", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(inviaBtn, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == inviaBtn ? newEmail.getText().trim() : null);

        dialog.showAndWait().ifPresent(email -> {
            if (email == null || email.isEmpty()) {
                showAlert(ERROR_TITLE, "L'indirizzo email non può essere vuoto.", Alert.AlertType.ERROR);
                return;
            }
            try {
                prenotazioneBoundary.inviaEmail(sel, email);
                showAlert(SUCCESS_TITLE, "Email inviata correttamente a " + email, Alert.AlertType.INFORMATION);
            } catch (EmailSendingException e) {
                String detail = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                showAlert(ERROR_TITLE, "Errore durante l'invio dell'email: " + detail, Alert.AlertType.ERROR);
            }
        });
    }

    private void refreshTable(LocalDate data) {
        if (data != null) {
            List<PrenotazioneBean> list = prenotazioneBoundary.getPrenotazioniByData(data);
            prenotazioniTableView.setItems(FXCollections.observableArrayList(list));
        } else {
            prenotazioniTableView.getItems().clear();
        }
    }

    private void aggiornaCalendario() {
        String nomeMese = meseCorrente.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        meseLabel.setText(nomeMese + " " + meseCorrente.getYear());
        popolaCalendario(meseCorrente);
    }

    private void mesePrecedente() { meseCorrente = meseCorrente.minusMonths(1); aggiornaCalendario(); }
    private void meseSuccessivo() { meseCorrente = meseCorrente.plusMonths(1); aggiornaCalendario(); }

    private void popolaCalendario(YearMonth m) {
        calendarioGrid.getChildren().removeIf(n -> GridPane.getRowIndex(n) != null && GridPane.getRowIndex(n) > 0);
        LocalDate primo = m.atDay(1);
        int offset = primo.getDayOfWeek().getValue() % 7;
        int total = m.lengthOfMonth();
        LocalDate today = LocalDate.now();
        int day = 1;
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (day > total) return;
                if (row == 1 && col < offset) continue;
                LocalDate date = m.atDay(day);
                Label lbl = new Label(String.valueOf(day));
                if (date.isBefore(today)) {
                    lbl.setStyle(DAY_LABEL_DEFAULT_STYLE.replace("transparent","lightgray").replace("black","darkgray").replace("hand","default"));
                } else {
                    lbl.setStyle(DAY_LABEL_DEFAULT_STYLE);
                    lbl.setOnMouseClicked(e -> selezionaGiorno(date, lbl));
                }
                lbl.setPrefSize(40,40);
                calendarioGrid.add(lbl, col, row);
                day++;
            }
        }
    }

    private void selezionaGiorno(LocalDate date, Label lbl) {
        if (casellaSelezionata instanceof Label) {
            casellaSelezionata.setStyle(DAY_LABEL_DEFAULT_STYLE);
        }
        lbl.setStyle(DAY_LABEL_SELECTED_STYLE);
        casellaSelezionata = lbl;
        giornoSelezionato = date;
        refreshTable(date);
    }

    private void resetForm() {
        nomeClienteField.clear();
        orarioField.clear();
        copertiField.clear();
        noteField.clear();
        emailField.clear();
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}