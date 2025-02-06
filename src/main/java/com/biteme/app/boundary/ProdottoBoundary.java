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
import java.math.BigDecimal;
import com.biteme.app.entity.User;
import com.biteme.app.entity.UserRole;
import com.biteme.app.util.UserSession;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ProdottoBoundary {
    private static final String ALERT_ERROR_TITLE = "Errore";
    private static final String ALERT_SUCCESS_TITLE = "Successo";

    @FXML
    private HBox adminButtonsHBox;

    @FXML
    private VBox aggiungiProdottoVBox;

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

        // Recupera l'utente loggato dalla sessione
        User currentUser = UserSession.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getRuolo() == UserRole.ADMIN;

        // Se l'utente NON è admin, nascondi l'intera sezione "aggiungi prodotto" e i pulsanti admin
        if (!isAdmin) {
            aggiungiProdottoVBox.setVisible(false);
            aggiungiProdottoVBox.setManaged(false);
            adminButtonsHBox.setVisible(false);
            adminButtonsHBox.setManaged(false);

            // Inoltre, se non admin, disabilita l'editing della tabella (se lo desideri)
            prodottiTableView.setEditable(false);
        } else {
            // Se admin, mostra le sezioni e imposta correttamente i pulsanti di modifica/eliminazione
            aggiungiProdottoVBox.setVisible(true);
            aggiungiProdottoVBox.setManaged(true);
            adminButtonsHBox.setVisible(true);
            adminButtonsHBox.setManaged(true);

            // Gestione della selezione della tabella per abilitare/disabilitare i pulsanti
            modificaButton.setDisable(true);
            eliminaButton.setDisable(true);
            prodottiTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                boolean isSelected = (newValue != null);
                modificaButton.setDisable(!isSelected);
                eliminaButton.setDisable(!isSelected);
            });
        }
    }

    private void configureComboBox() {
        categoriaComboBox.setItems(FXCollections.observableArrayList());
        categoriaComboBox.getItems().add(null); // Placeholder
        categoriaComboBox.getItems().addAll(Categoria.values());
        categoriaComboBox.setPromptText("Seleziona una categoria");
        categoriaComboBox.setValue(null); // Imposta il valore iniziale al placeholder
        categoriaComboBox.setCellFactory(lv -> createCategoryCell());
        categoriaComboBox.setButtonCell(createCategoryCell());
    }

    // Metodo per creare una ListCell personalizzata
    private ListCell<Categoria> createCategoryCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "Seleziona una categoria" : item.name());
            }
        };
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
            // Controlla che il nome del prodotto non sia vuoto
            if (nomeProdottoField.getText().isBlank()) {
                showAlert(ALERT_ERROR_TITLE, "Il nome del prodotto non può essere vuoto!", Alert.AlertType.ERROR);
                return;
            }

            // Controlla che sia stata selezionata una categoria valida
            if (categoriaComboBox.getValue() == null) { // 'null' rappresenta il placeholder
                showAlert(ALERT_ERROR_TITLE, "Seleziona una categoria!", Alert.AlertType.ERROR);
                return;
            }

            // Controlla che il prezzo sia numerico e valido
            if (prezzoField.getText().isBlank() || !isNumeric(prezzoField.getText())) {
                showAlert(ALERT_ERROR_TITLE, "Inserisci un valore numerico valido per il prezzo!", Alert.AlertType.ERROR);
                return;
            }

            // Creazione del bean Prodotto
            ProdottoBean prodottoBean = new ProdottoBean();
            prodottoBean.setNome(nomeProdottoField.getText());
            prodottoBean.setCategoria(categoriaComboBox.getValue()); // Categoria valida selezionata
            prodottoBean.setPrezzo(new BigDecimal(prezzoField.getText()));
            prodottoBean.setDisponibile(true); // Di default è disponibile

            // Salva attraverso il controller
            prodottoController.aggiungiProdotto(prodottoBean);
            showAlert(ALERT_SUCCESS_TITLE, "Prodotto aggiunto correttamente!", Alert.AlertType.INFORMATION);

            // Pulisce i campi dopo il salvataggio
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
        prezzoField.clear();
        categoriaComboBox.getSelectionModel().clearSelection();
        categoriaComboBox.setValue(null); // Ripristina il placeholder
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
            showAlert(ALERT_ERROR_TITLE, "Seleziona un prodotto da modificare.", Alert.AlertType.ERROR);
            return;
        }

        mostraDialogModifica(prodottoSelezionato);
    }

    @FXML
    private void eliminaProdotto() {
        // Prendere il prodotto selezionato
        Prodotto prodottoSelezionato = prodottiTableView.getSelectionModel().getSelectedItem();
        if (prodottoSelezionato == null) {
            showAlert(ALERT_ERROR_TITLE, "Seleziona un prodotto da eliminare.", Alert.AlertType.ERROR);
            return;
        }

        boolean confermato = mostraDialogConferma("Sei sicuro di voler eliminare il prodotto " + prodottoSelezionato.getNome() + "?");
        if (confermato) {
            prodottoController.eliminaProdotto(prodottoSelezionato.getId());
            refreshTable();
            showAlert(ALERT_SUCCESS_TITLE, "Prodotto eliminato con successo!", Alert.AlertType.INFORMATION);
        }
    }
}