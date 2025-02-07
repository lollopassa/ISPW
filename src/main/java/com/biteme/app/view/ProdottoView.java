package com.biteme.app.view;

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

public class ProdottoView {

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

    public ProdottoView() {
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
        categoriaComboBox.getItems().addAll("PIZZE", "PRIMI", "ANTIPASTI", "BEVANDE", "CONTORNI", "DOLCI");
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

    // Resto del codice rimane invariato...
}