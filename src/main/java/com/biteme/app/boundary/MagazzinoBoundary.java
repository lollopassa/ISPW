package com.biteme.app.boundary;

import com.biteme.app.bean.MagazzinoBean;
import com.biteme.app.controller.MagazzinoController;
import com.biteme.app.entity.Prodotto;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MagazzinoBoundary {

    @FXML
    private TextField nomeProdottoField;

    @FXML
    private TextField categoriaField;

    @FXML
    private TextField quantitaField;

    @FXML
    private TextField prezzoField;

    @FXML
    private TextField dataScadenzaField;

    @FXML
    private TableView<Prodotto> prodottiTableView;

    @FXML
    private TableColumn<Prodotto, Integer> idColumn;

    @FXML
    private TableColumn<Prodotto, String> nomeColumn;

    @FXML
    private TableColumn<Prodotto, String> categoriaColumn;

    @FXML
    private TableColumn<Prodotto, Integer> quantitaColumn;

    @FXML
    private TableColumn<Prodotto, BigDecimal> prezzoColumn;

    @FXML
    private TableColumn<Prodotto, LocalDate> dataScadenzaColumn;

    @FXML
    private TableColumn<Prodotto, Boolean> disponibileColumn;

    @FXML
    private TableColumn<Prodotto, Void> azioniColumn;

    private final MagazzinoController magazzinoController;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public MagazzinoBoundary() {
        this.magazzinoController = new MagazzinoController();
    }

    @FXML
    private void initialize() {
        configureTableColumns();
        configureActionColumns();
        refreshTable();
    }

    private void configureTableColumns() {
        // Configurazione delle colonne della tabella
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        quantitaColumn.setCellValueFactory(new PropertyValueFactory<>("quantita"));
        prezzoColumn.setCellValueFactory(new PropertyValueFactory<>("prezzo"));
        disponibileColumn.setCellValueFactory(new PropertyValueFactory<>("disponibile"));

        // Configurazione della colonna "Scadenza"
        dataScadenzaColumn.setCellFactory(_ -> new TextFieldTableCell<>(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return LocalDate.parse(string, formatter);
            }
        }));
        dataScadenzaColumn.setCellValueFactory(new PropertyValueFactory<>("dataScadenza"));
    }

    private void configureActionColumns() {
        // Configurazione della colonna Azioni con inizializzazione tramite classe separata
        azioniColumn.setCellFactory(_ -> new ActionCell());
    }


    @FXML
    private void aggiungiProdotto() {
        try {
            MagazzinoBean magazzinoBean = new MagazzinoBean();
            magazzinoBean.setNomeProdotto(nomeProdottoField.getText());
            magazzinoBean.setCategoria(categoriaField.getText());
            magazzinoBean.setQuantita(Integer.parseInt(quantitaField.getText()));
            magazzinoBean.setPrezzo(new BigDecimal(prezzoField.getText()));

            LocalDate dataScadenza = safeParseDataScadenza(dataScadenzaField.getText());
            if (dataScadenza == null) {
                return; // Errore già mostrato nel metodo safeParseDataScadenza()
            }
            magazzinoBean.setDataScadenza(dataScadenza);

            magazzinoBean.setDisponibile(true); // Di default, disponibilità impostata a true

            magazzinoController.aggiungiProdotto(magazzinoBean);
            showAlert("Successo", "Prodotto aggiunto correttamente!", Alert.AlertType.INFORMATION);
            clearFields();
            refreshTable();
        } catch (NumberFormatException e) {
            showAlert("Errore", "Inserisci valori validi per quantità e prezzo!", Alert.AlertType.ERROR);
        }
    }

    private LocalDate safeParseDataScadenza(String data) {
        try {
            return LocalDate.parse(data, formatter);
        } catch (DateTimeParseException e) {
            showAlert("Errore", "Inserisci una data di scadenza valida (formato: dd-MM-yyyy)", Alert.AlertType.ERROR);
            return null;
        }
    }

    private void refreshTable() {
        prodottiTableView.getItems().setAll(magazzinoController.getProdotti());
    }

    private void clearFields() {
        nomeProdottoField.clear();
        categoriaField.clear();
        quantitaField.clear();
        prezzoField.clear();
        dataScadenzaField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class ActionCell extends TableCell<Prodotto, Void> {
        private final Button deleteButton;
        private void eliminaProdotto(int id) {
            magazzinoController.eliminaProdotto(id);
            refreshTable();
            showAlert("Successo", "Prodotto eliminato con successo!", Alert.AlertType.INFORMATION);
        }

        public ActionCell() {
            this.deleteButton = new Button("Elimina");
            deleteButton.setOnAction(_ -> {
                Prodotto prodotto = getTableView().getItems().get(getIndex());
                eliminaProdotto(prodotto.getId());
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(deleteButton);
            }
        }
    }
}