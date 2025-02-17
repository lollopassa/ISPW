package com.biteme.app.boundary;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.LoginController;
import com.biteme.app.controller.ProdottoController;
import com.biteme.app.exception.ProdottoException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

public class ProdottoBoundary {

    private static final String ALERT_INFORMATION = "Informazione";
    private static final String ALERT_ERROR = "Errore";

    @FXML
    private HBox adminButtonsHBox;
    @FXML
    private VBox aggiungiProdottoVBox;
    @FXML
    private TextField nomeProdottoField;
    @FXML
    private ComboBox<String> categoriaComboBox;
    @FXML
    private TextField prezzoField;
    @FXML
    private TableView<ProdottoBean> prodottiTableView;
    @FXML
    private TableColumn<ProdottoBean, Integer> idColumn;
    @FXML
    private TableColumn<ProdottoBean, String> nomeColumn;
    @FXML
    private TableColumn<ProdottoBean, String> categoriaColumn;
    @FXML
    private TableColumn<ProdottoBean, BigDecimal> prezzoColumn;
    @FXML
    private TableColumn<ProdottoBean, Boolean> disponibileColumn;
    @FXML
    private Button modificaButton;
    @FXML
    private Button eliminaButton;

    private final ProdottoController prodottoController;
    private final LoginController loginController;

    public ProdottoBoundary() {
        this.prodottoController = new ProdottoController();
        this.loginController = new LoginController();
    }

    @FXML
    private void initialize() {
        configureComboBox();
        configureTableColumns();
        refreshTable();

        boolean isAdmin = loginController.isUserAdmin();

        if (!isAdmin) {
            aggiungiProdottoVBox.setVisible(false);
            aggiungiProdottoVBox.setManaged(false);
            adminButtonsHBox.setVisible(false);
            adminButtonsHBox.setManaged(false);
            prodottiTableView.setEditable(false);
        } else {
            aggiungiProdottoVBox.setVisible(true);
            aggiungiProdottoVBox.setManaged(true);
            adminButtonsHBox.setVisible(true);
            adminButtonsHBox.setManaged(true);

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
        categoriaComboBox.getItems().add(null);
        categoriaComboBox.getItems().addAll("ANTIPASTI","PIZZE", "PRIMI", "SECONDI", "CONTORNI", "BEVANDE", "DOLCI");
        categoriaComboBox.setPromptText("Seleziona una categoria");
        categoriaComboBox.setValue(null);
        categoriaComboBox.setCellFactory(lv -> createCategoryCell());
        categoriaComboBox.setButtonCell(createCategoryCell());
    }

    private ListCell<String> createCategoryCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "Seleziona una categoria" : item);
            }
        };
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        prezzoColumn.setCellValueFactory(new PropertyValueFactory<>("prezzo"));
        disponibileColumn.setCellValueFactory(new PropertyValueFactory<>("disponibile"));
        nomeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    @FXML
    private void aggiungiProdotto() {
        try {
            ProdottoBean prodottoBean = new ProdottoBean();
            prodottoBean.setNome(nomeProdottoField.getText());
            prodottoBean.setCategoria(categoriaComboBox.getValue());
            prodottoBean.setPrezzo(parsePrezzo(prezzoField.getText())); // Blocchi nidificati eliminati
            prodottoBean.setDisponibile(true);

            prodottoController.aggiungiProdotto(prodottoBean);

            showAlert(Alert.AlertType.INFORMATION, ALERT_INFORMATION, "Prodotto aggiunto correttamente!");
            clearFields();
            refreshTable();
        } catch (ProdottoException e) {
            showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Errore nell'aggiunta del prodotto: " + e.getMessage());
        }
    }
    @FXML
    private void modificaProdotto() {
        ProdottoBean selectedProdotto = prodottiTableView.getSelectionModel().getSelectedItem();
        if (selectedProdotto == null) {
            showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Seleziona un prodotto da modificare.");
            return;
        }

        // Creazione del dialogo per la modifica
        Dialog<ProdottoBean> dialog = new Dialog<>();
        dialog.setTitle("Modifica Prodotto");
        dialog.setHeaderText("Modifica i dati del prodotto:");
        dialog.setResizable(true);

        // Campi di input per la modifica
        TextField nomeField = new TextField(selectedProdotto.getNome());
        ComboBox<String> categoriaComboBoxModifica = new ComboBox<>(FXCollections.observableArrayList(
                "ANTIPASTI","PIZZE", "PRIMI", "SECONDI", "CONTORNI", "BEVANDE", "DOLCI"
        ));
        categoriaComboBoxModifica.setValue(selectedProdotto.getCategoria());
        TextField prezzoFieldModifica = new TextField(selectedProdotto.getPrezzo().toString());

        // Layout per i campi
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Nome Prodotto:"), nomeField,
                new Label("Categoria:"), categoriaComboBoxModifica,
                new Label("Prezzo:"), prezzoFieldModifica
        );
        dialog.getDialogPane().setContent(vbox);

        // Pulsanti OK e Annulla
        ButtonType okButtonType = new ButtonType("Modifica", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Gestione della conferma
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                // Aggiorna il prodotto con i nuovi valori
                ProdottoBean prodottoModificato = new ProdottoBean();
                prodottoModificato.setId(selectedProdotto.getId()); // Manteniamo l'ID invariato
                prodottoModificato.setNome(nomeField.getText());
                prodottoModificato.setCategoria(categoriaComboBoxModifica.getValue());
                prodottoModificato.setPrezzo(parsePrezzo(prezzoFieldModifica.getText()));
                prodottoModificato.setDisponibile(selectedProdotto.getDisponibile()); // Manteniamo lo stato di disponibilità

                return prodottoModificato; // Restituiamo il nuovo prodotto
            }
            return null;
        });

        // Mostra il dialogo e gestisce il risultato
        dialog.showAndWait().ifPresent(prodottoModificato -> {
            try {
                prodottoController.modificaProdotto(prodottoModificato);

                showAlert(Alert.AlertType.INFORMATION, ALERT_INFORMATION, "Prodotto aggiornato correttamente!");
                refreshTable();
            } catch (ProdottoException e) {
                showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Errore nella modifica del prodotto: " + e.getMessage());
            }
        });
    }

    @FXML
    private void eliminaProdotto() {
        ProdottoBean selectedProdotto = prodottiTableView.getSelectionModel().getSelectedItem();
        if (selectedProdotto == null) {
            showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Seleziona un prodotto da eliminare.");
            return;
        }

        try {
            prodottoController.eliminaProdotto(selectedProdotto.getId());
            showAlert(Alert.AlertType.INFORMATION, ALERT_INFORMATION, "Prodotto eliminato correttamente!");
            refreshTable();
        } catch (ProdottoException e) {
            showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Errore nell'eliminazione del prodotto: " + e.getMessage());
        }
    }

    private BigDecimal parsePrezzo(String prezzo) {
        try {
            return new BigDecimal(prezzo);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private void refreshTable() {
        prodottiTableView.getItems().setAll(prodottoController.getProdotti());
    }

    private void clearFields() {
        nomeProdottoField.clear();
        prezzoField.clear();
        categoriaComboBox.getSelectionModel().clearSelection();
        categoriaComboBox.setValue(null);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}