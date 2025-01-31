package com.biteme.app.boundary;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.ProdottoController;
import com.biteme.app.entity.Categoria;
import com.biteme.app.entity.Prodotto;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;

public class ProdottoBoundary {
    private static final String ALERT_ERROR_TITLE = "Errore";
    private static final String ALERT_SUCCESS_TITLE = "Successo";
    // Stile per i pulsanti
    private static final String DELETE_BUTTON_STYLE = "-fx-background-color: #E0218A; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 12px;";
    private static final String EDIT_BUTTON_STYLE = "-fx-background-color: #303d68; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 12px;";

    @FXML
    private TextField nomeProdottoField;

    @FXML
    private ComboBox<Categoria> categoriaComboBox;

    @FXML
    private TextField prezzoField;

    @FXML
    private TableView<Prodotto> prodottiTableView;

    @FXML
    private TableColumn<Prodotto, Integer> idColumn;

    @FXML
    private TableColumn<Prodotto, String> nomeColumn;

    @FXML
    private TableColumn<Prodotto, Categoria> categoriaColumn;

    @FXML
    private TableColumn<Prodotto, BigDecimal> prezzoColumn;

    @FXML
    private TableColumn<Prodotto, Boolean> disponibileColumn;

    @FXML
    private Button modificaButton;

    @FXML
    private Button eliminaButton;

    private final ProdottoController prodottoController;

    public ProdottoBoundary() {
        this.prodottoController = new ProdottoController();
    }

    @FXML
    private void initialize() {
        configureComboBox();
        configureTableColumns();
        refreshTable();

        modificaButton.setDisable(true);
        eliminaButton.setDisable(true);

        prodottiTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isSelected = (newValue != null);
            modificaButton.setDisable(!isSelected);
            eliminaButton.setDisable(!isSelected);
        });
    }

    private void configureComboBox() {
        // Configuriamo il ComboBox per la selezione della categoria
        categoriaComboBox.setItems(FXCollections.observableArrayList(Categoria.values()));
        categoriaComboBox.setPromptText("Seleziona una categoria"); // Mostra l'indicazione iniziale
    }

    private void configureTableColumns() {
        // Configurazione delle colonne della tabella
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        prezzoColumn.setCellValueFactory(new PropertyValueFactory<>("prezzo"));
        disponibileColumn.setCellValueFactory(new PropertyValueFactory<>("disponibile"));

        // Permetti modifiche in linea (se necessario) per la colonna del nome
        nomeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    }


    private boolean isNumeric(String str) {
        try {
            new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @FXML
    private void aggiungiProdotto() {
        try {
            // Controllo che il TextField del nome non sia vuoto
            if (nomeProdottoField.getText().isBlank()) {
                showAlert(ALERT_ERROR_TITLE, "Il nome del prodotto non può essere vuoto!", Alert.AlertType.ERROR);
                return;
            }

            // Controllo che la categoria sia selezionata
            if (categoriaComboBox.getValue() == null) {
                showAlert(ALERT_ERROR_TITLE, "Seleziona una categoria valida!", Alert.AlertType.ERROR);
                return;
            }

            // Controllo che il prezzo sia valido (numerico)
            if (prezzoField.getText().isBlank() || !isNumeric(prezzoField.getText())) {
                showAlert(ALERT_ERROR_TITLE, "Inserisci un valore numerico valido per il prezzo!", Alert.AlertType.ERROR);
                return;
            }

            // Creazione del bean e assegnazione dei valori
            ProdottoBean prodottoBean = new ProdottoBean();
            prodottoBean.setNome(nomeProdottoField.getText());
            prodottoBean.setCategoria(categoriaComboBox.getValue());
            prodottoBean.setPrezzo(new BigDecimal(prezzoField.getText()));
            prodottoBean.setDisponibile(true); // Di default, disponibilità impostata a true

            // Chiamata al controller per salvare il prodotto
            prodottoController.aggiungiProdotto(prodottoBean);
            showAlert(ALERT_SUCCESS_TITLE, "Prodotto aggiunto correttamente!", Alert.AlertType.INFORMATION);

            // Ripulire i campi dopo il salvataggio
            clearFields();
            refreshTable();
        } catch (NumberFormatException e) {
            showAlert(ALERT_ERROR_TITLE, "Inserisci un valore valido per il prezzo!", Alert.AlertType.ERROR);
        }
    }

    private void refreshTable() {
        prodottiTableView.getItems().setAll(prodottoController.getProdotti());
    }

    private void clearFields() {
        nomeProdottoField.clear();
        categoriaComboBox.getSelectionModel().clearSelection(); // Deseleziona la categoria
        prezzoField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean mostraDialogConferma(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, messaggio, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Conferma Eliminazione");
        alert.showAndWait();
        return alert.getResult() == ButtonType.YES;
    }

    private void mostraDialogModifica(Prodotto prodotto) {
        Dialog<Prodotto> dialog = new Dialog<>();
        dialog.setTitle("Modifica Prodotto");

        // Configuriamo i campi di input per la modifica
        TextField nomeField = new TextField(prodotto.getNome());
        ComboBox<Categoria> categoriaField = new ComboBox<>(FXCollections.observableArrayList(Categoria.values()));
        categoriaField.setValue(prodotto.getCategoria());
        TextField priceField = new TextField(prodotto.getPrezzo().toString());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Categoria:"), 0, 1);
        grid.add(categoriaField, 1, 1);
        grid.add(new Label("Prezzo (€):"), 0, 2);
        grid.add(priceField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Pulsanti di azione per il dialogo
        ButtonType salvaButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvaButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == salvaButtonType) {
                try {
                    // Ritorna il prodotto aggiornato
                    return new Prodotto(
                            prodotto.getId(),
                            nomeField.getText(),
                            new BigDecimal(priceField.getText()),
                            categoriaField.getValue(),
                            prodotto.isDisponibile() // Mantieni la disponibilità invariata
                    );
                } catch (NumberFormatException e) {
                    showAlert(ALERT_ERROR_TITLE, "Inserisci un valore valido per il prezzo!", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        // Otteniamo il risultato del dialogo
        dialog.showAndWait().ifPresent(prodottoAggiornato -> {
            prodottoController.modificaProdotto(prodottoAggiornato);
            refreshTable();
            showAlert(ALERT_SUCCESS_TITLE, "Prodotto aggiornato con successo!", Alert.AlertType.INFORMATION);
        });
    }

    @FXML
    private void modificaProdotto() {
        // Prendere il prodotto selezionato
        Prodotto prodottoSelezionato = prodottiTableView.getSelectionModel().getSelectedItem();
        if (prodottoSelezionato == null) {
            showAlert("Errore", "Seleziona un prodotto da modificare.", Alert.AlertType.ERROR);
            return;
        }

        mostraDialogModifica(prodottoSelezionato);
    }

    @FXML
    private void eliminaProdotto() {
        // Prendere il prodotto selezionato
        Prodotto prodottoSelezionato = prodottiTableView.getSelectionModel().getSelectedItem();
        if (prodottoSelezionato == null) {
            showAlert("Errore", "Seleziona un prodotto da eliminare.", Alert.AlertType.ERROR);
            return;
        }

        boolean confermato = mostraDialogConferma("Sei sicuro di voler eliminare il prodotto " + prodottoSelezionato.getNome() + "?");
        if (confermato) {
            prodottoController.eliminaProdotto(prodottoSelezionato.getId());
            refreshTable();
            showAlert("Successo", "Prodotto eliminato con successo!", Alert.AlertType.INFORMATION);
        }
    }
}