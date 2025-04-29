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
            aggiungiProdottoVBox.setVisible(true);
            aggiungiProdottoVBox.setManaged(true);
            adminButtonsHBox.setVisible(true);
            adminButtonsHBox.setManaged(true);
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
            @Override protected void updateItem(String item, boolean empty) {
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
            ProdottoBean bean = new ProdottoBean();
            bean.setNome(nomeProdottoField.getText());
            bean.setCategoria(categoriaComboBox.getValue());
            bean.setPrezzo(parsePrezzo(prezzoField.getText()));
            bean.setDisponibile(true);
            boundary.aggiungiProdotto(bean);

            showAlert(Alert.AlertType.INFORMATION, ALERT_INFORMATION, "Prodotto aggiunto correttamente!");
            clearFields();
            refreshTable();
        } catch (ProdottoException e) {
            showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Errore nell'aggiunta del prodotto: " + e.getMessage());
        }
    }

    @FXML
    private void modificaProdotto() {
        ProdottoBean selected = prodottiTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Seleziona un prodotto da modificare.");
            return;
        }
        Dialog<ProdottoBean> dialog = new Dialog<>();
        dialog.setTitle("Modifica Prodotto");
        dialog.setHeaderText("Modifica i dati del prodotto:");
        dialog.setResizable(true);

        TextField nomeField = new TextField(selected.getNome());
        ComboBox<String> catMod = new ComboBox<>(FXCollections.observableArrayList(
                "ANTIPASTI","PIZZE","PRIMI","SECONDI","CONTORNI","BEVANDE","DOLCI"
        )); catMod.setValue(selected.getCategoria());
        TextField prezzoFieldMod = new TextField(selected.getPrezzo().toString());

        VBox vbox = new VBox(10, new Label("Nome Prodotto:"), nomeField,
                new Label("Categoria:"), catMod,
                new Label("Prezzo:"), prezzoFieldMod);
        dialog.getDialogPane().setContent(vbox);
        ButtonType ok = new ButtonType("Modifica", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ok) {
                ProdottoBean bean = new ProdottoBean();
                bean.setId(selected.getId());
                bean.setNome(nomeField.getText());
                bean.setCategoria(catMod.getValue());
                bean.setPrezzo(parsePrezzo(prezzoFieldMod.getText()));
                bean.setDisponibile(selected.getDisponibile());
                return bean;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(bean -> {
            try {
                boundary.modificaProdotto(bean);
                showAlert(Alert.AlertType.INFORMATION, ALERT_INFORMATION, "Prodotto aggiornato correttamente!");
                refreshTable();
            } catch (ProdottoException e) {
                showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Errore nella modifica del prodotto: " + e.getMessage());
            }
        });
    }

    @FXML
    private void eliminaProdotto() {
        ProdottoBean selected = prodottiTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, ALERT_ERROR, "Seleziona un prodotto da eliminare.");
            return;
        }
        try {
            boundary.eliminaProdotto(selected.getId());
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
        prodottiTableView.getItems().setAll(boundary.getProdotti());
    }

    private void clearFields() {
        nomeProdottoField.clear();
        prezzoField.clear();
        categoriaComboBox.getSelectionModel().clearSelection();
        categoriaComboBox.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}