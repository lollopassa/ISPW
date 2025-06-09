package com.biteme.app.ui;

import com.biteme.app.boundary.ProdottoBoundary;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.exception.ProdottoException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

public class ProdottoUI {

    private static final String ALERT_INFORMATION = "Informazione";
    private static final String ALERT_ERROR = "Errore";

    @FXML private HBox adminButtonsHBox;
    @FXML private VBox aggiungiProdottoVBox;
    @FXML private TextField nomeProdottoField;
    @FXML private ComboBox<String> categoriaComboBox;
    @FXML private TextField prezzoField;
    @FXML private TableView<ProdottoBean> prodottiTableView;
    @FXML private TableColumn<ProdottoBean, Integer> idColumn;
    @FXML private TableColumn<ProdottoBean, String> nomeColumn;
    @FXML private TableColumn<ProdottoBean, String> categoriaColumn;
    @FXML private TableColumn<ProdottoBean, BigDecimal> prezzoColumn;
    @FXML private TableColumn<ProdottoBean, Boolean> disponibileColumn;
    @FXML private Button modificaButton;
    @FXML private Button eliminaButton;

    private final ProdottoBoundary boundary = new ProdottoBoundary();

    @FXML
    private void initialize() {
        configureComboBox();
        configureTableColumns();
        refreshTable();

        boolean isAdmin = boundary.isUserAdmin();

        if (!isAdmin) {
            aggiungiProdottoVBox.setVisible(false);
            aggiungiProdottoVBox.setManaged(false);
            adminButtonsHBox.setVisible(false);
            adminButtonsHBox.setManaged(false);
            prodottiTableView.setEditable(false);
        } else {
            modificaButton.setDisable(true);
            eliminaButton.setDisable(true);
            prodottiTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                boolean sel = newV != null;
                modificaButton.setDisable(!sel);
                eliminaButton.setDisable(!sel);
            });
        }
    }

    private void configureComboBox() {
        categoriaComboBox.setItems(FXCollections.observableArrayList(
                "ANTIPASTI","PIZZE", "PRIMI", "SECONDI", "CONTORNI", "BEVANDE", "DOLCI"
        ));
        categoriaComboBox.setPromptText("Seleziona una categoria");
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
            String nome      = nomeProdottoField.getText().trim();
            String categoria = categoriaComboBox.getValue();
            BigDecimal prezzo = parsePrezzo(prezzoField.getText().trim());

            // Chiamata al boundary: UI non conosce ProdottoBean
            boundary.aggiungiProdotto(nome, categoria, prezzo);

            showAlert(Alert.AlertType.INFORMATION, ALERT_INFORMATION, "Prodotto aggiunto correttamente!");
            clearFields();
            refreshTable();
        } catch (ProdottoException e) {
            showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Errore nell'aggiunta: " + e.getMessage());
        }
    }

    @FXML
    private void modificaProdotto() {
        ProdottoBean sel = prodottiTableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Seleziona un prodotto.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifica Prodotto");
        dialog.setHeaderText("Aggiorna i campi e conferma");

        TextField nomeField      = new TextField(sel.getNome());
        ComboBox<String> catMod  = new ComboBox<>(FXCollections.observableArrayList(
                "ANTIPASTI","PIZZE","PRIMI","SECONDI","CONTORNI","BEVANDE","DOLCI"
        ));
        catMod.setValue(sel.getCategoria());
        TextField prezzoFieldMod = new TextField(sel.getPrezzo().toString());
        CheckBox disponibileBox  = new CheckBox("Disponibile");
        disponibileBox.setSelected(sel.getDisponibile());

        VBox vbox = new VBox(10,
                new Label("Nome:"), nomeField,
                new Label("Categoria:"), catMod,
                new Label("Prezzo:"), prezzoFieldMod,
                disponibileBox
        );
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boundary.modificaProdotto(
                            sel.getId(),
                            nomeField.getText().trim(),
                            catMod.getValue(),
                            parsePrezzo(prezzoFieldMod.getText().trim()),
                            disponibileBox.isSelected()
                    );
                    showAlert(Alert.AlertType.INFORMATION, ALERT_INFORMATION, "Prodotto aggiornato!");
                    refreshTable();
                } catch (ProdottoException e) {
                    showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Errore nella modifica: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void eliminaProdotto() {
        ProdottoBean sel = prodottiTableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Seleziona un prodotto.");
            return;
        }
        try {
            boundary.eliminaProdotto(sel.getId());
            showAlert(Alert.AlertType.INFORMATION, ALERT_INFORMATION, "Prodotto eliminato!");
            refreshTable();
        } catch (ProdottoException e) {
            showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Errore nell'eliminazione: " + e.getMessage());
        }
    }

    private BigDecimal parsePrezzo(String testo) {
        try {
            return new BigDecimal(testo);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private void refreshTable() {
        prodottiTableView.getItems().setAll(boundary.getProdotti());
    }

    private void clearFields() {
        nomeProdottoField.clear();
        prezzoField.clear();
        categoriaComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
