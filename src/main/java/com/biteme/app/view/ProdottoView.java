package com.biteme.app.view;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.LoginController;
import com.biteme.app.controller.ProdottoController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;

public class ProdottoView {

    private static final String ALERT_ERROR_TITLE = "Errore";
    private static final String ALERT_SUCCESS_TITLE = "Successo";

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
        categoriaComboBox.getItems().add(null); // Placeholder
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
        ProdottoBean prodottoBean = new ProdottoBean();
        prodottoBean.setNome(nomeProdottoField.getText());
        prodottoBean.setCategoria(categoriaComboBox.getValue());
        try {
            prodottoBean.setPrezzo(new BigDecimal(prezzoField.getText()));
        } catch (NumberFormatException e) {
            // If invalid, set to zero; the controller will handle the error.
            prodottoBean.setPrezzo(BigDecimal.ZERO);
        }
        prodottoBean.setDisponibile(true);
        // Controller now handles validation and error messages.
        prodottoController.aggiungiProdotto(prodottoBean);
        clearFields();
        refreshTable();
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

    @FXML
    private void modificaProdotto() {
        ProdottoBean prodottoSelezionato = prodottiTableView.getSelectionModel().getSelectedItem();
        if (prodottoSelezionato != null) {
            mostraDialogModifica(prodottoSelezionato);
        }
    }

    private void mostraDialogModifica(ProdottoBean bean) {
        Dialog<ProdottoBean> dialog = new Dialog<>();
        dialog.setTitle("Modifica Prodotto");

        TextField nomeField = new TextField(bean.getNome());
        ComboBox<String> categoriaField = new ComboBox<>(FXCollections.observableArrayList("PIZZE", "PRIMI", "ANTIPASTI", "BEVANDE", "CONTORNI", "DOLCI"));
        categoriaField.setValue(bean.getCategoria());
        TextField priceField = new TextField(bean.getPrezzo().toString());

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

        ButtonType salvaButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvaButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == salvaButtonType) {
                ProdottoBean beanAggiornato = new ProdottoBean();
                beanAggiornato.setId(bean.getId());
                beanAggiornato.setNome(nomeField.getText());
                beanAggiornato.setCategoria(categoriaField.getValue());
                try {
                    beanAggiornato.setPrezzo(new BigDecimal(priceField.getText()));
                } catch (NumberFormatException e) {
                    beanAggiornato.setPrezzo(BigDecimal.ZERO);
                }
                beanAggiornato.setDisponibile(bean.getDisponibile());
                return beanAggiornato;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(beanAggiornato -> {
            prodottoController.modificaProdotto(beanAggiornato);
            refreshTable();
        });
    }

    @FXML
    private void eliminaProdotto() {
        ProdottoBean prodottoSelezionato = prodottiTableView.getSelectionModel().getSelectedItem();
        if (prodottoSelezionato != null) {
            boolean confermato = mostraDialogConferma("Sei sicuro di voler eliminare il prodotto " + prodottoSelezionato.getNome() + "?");
            if (confermato) {
                prodottoController.eliminaProdotto(prodottoSelezionato.getId());
                refreshTable();
            }
        }
    }

    private boolean mostraDialogConferma(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, messaggio, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Conferma Eliminazione");
        alert.showAndWait();
        return alert.getResult() == ButtonType.YES;
    }
}
