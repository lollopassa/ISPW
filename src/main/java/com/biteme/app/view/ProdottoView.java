package com.biteme.app.view;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.LoginController;
import com.biteme.app.controller.ProdottoController;
import com.biteme.app.model.Categoria;
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
    private ComboBox<Categoria> categoriaComboBox;

    @FXML
    private TextField prezzoField;

    // La TableView ora utilizza ProdottoBean
    @FXML
    private TableView<ProdottoBean> prodottiTableView;

    @FXML
    private TableColumn<ProdottoBean, Integer> idColumn;

    @FXML
    private TableColumn<ProdottoBean, String> nomeColumn;

    @FXML
    private TableColumn<ProdottoBean, Categoria> categoriaColumn;

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

        // Verifica il ruolo dell'utente tramite LoginController
        boolean isAdmin = loginController.isUserAdmin();

        if (!isAdmin) {
            aggiungiProdottoVBox.setVisible(false);
            aggiungiProdottoVBox.setManaged(false);
            adminButtonsHBox.setVisible(false);
            adminButtonsHBox.setManaged(false);
            // Disabilita l'editing della tabella per utenti non admin
            prodottiTableView.setEditable(false);
        } else {
            aggiungiProdottoVBox.setVisible(true);
            aggiungiProdottoVBox.setManaged(true);
            adminButtonsHBox.setVisible(true);
            adminButtonsHBox.setManaged(true);

            // Gestione della selezione per abilitare/disabilitare i pulsanti
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
        categoriaComboBox.setValue(null); // Imposta il placeholder
        categoriaComboBox.setCellFactory(lv -> createCategoryCell());
        categoriaComboBox.setButtonCell(createCategoryCell());
    }

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
        // Configurazione delle colonne della tabella tramite PropertyValueFactory sui bean
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        prezzoColumn.setCellValueFactory(new PropertyValueFactory<>("prezzo"));
        disponibileColumn.setCellValueFactory(new PropertyValueFactory<>("disponibile"));

        // Se vuoi permettere l'editing inline, puoi configurare la colonna del nome in questo modo:
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
            // Verifica che il nome non sia vuoto
            if (nomeProdottoField.getText().isBlank()) {
                showAlert(ALERT_ERROR_TITLE, "Il nome del prodotto non può essere vuoto!", Alert.AlertType.ERROR);
                return;
            }
            // Verifica che sia selezionata una categoria valida
            if (categoriaComboBox.getValue() == null) {
                showAlert(ALERT_ERROR_TITLE, "Seleziona una categoria!", Alert.AlertType.ERROR);
                return;
            }
            // Verifica che il prezzo sia numerico e valido
            if (prezzoField.getText().isBlank() || !isNumeric(prezzoField.getText())) {
                showAlert(ALERT_ERROR_TITLE, "Inserisci un valore numerico valido per il prezzo!", Alert.AlertType.ERROR);
                return;
            }

            // Creazione del bean Prodotto
            ProdottoBean prodottoBean = new ProdottoBean();
            prodottoBean.setNome(nomeProdottoField.getText());
            prodottoBean.setCategoria(categoriaComboBox.getValue());
            prodottoBean.setPrezzo(new BigDecimal(prezzoField.getText()));
            prodottoBean.setDisponibile(true); // Di default, il prodotto è disponibile

            // Salva il prodotto tramite il controller
            prodottoController.aggiungiProdotto(prodottoBean);
            showAlert(ALERT_SUCCESS_TITLE, "Prodotto aggiunto correttamente!", Alert.AlertType.INFORMATION);

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

    /**
     * Mostra la dialog per modificare un prodotto. La dialog lavora con il bean.
     */
    private void mostraDialogModifica(ProdottoBean bean) {
        Dialog<ProdottoBean> dialog = new Dialog<>();
        dialog.setTitle("Modifica Prodotto");

        // Configura i campi di input per la modifica
        TextField nomeField = new TextField(bean.getNome());
        ComboBox<Categoria> categoriaField = new ComboBox<>(FXCollections.observableArrayList(Categoria.values()));
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
                try {
                    ProdottoBean beanAggiornato = new ProdottoBean();
                    beanAggiornato.setId(bean.getId());
                    beanAggiornato.setNome(nomeField.getText());
                    beanAggiornato.setCategoria(categoriaField.getValue());
                    beanAggiornato.setPrezzo(new BigDecimal(priceField.getText()));
                    beanAggiornato.setDisponibile(bean.getDisponibile());
                    return beanAggiornato;
                } catch (NumberFormatException e) {
                    showAlert(ALERT_ERROR_TITLE, "Inserisci un valore valido per il prezzo!", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(beanAggiornato -> {
            // Chiedi al controller di aggiornare il prodotto (il controller si occuperà della conversione)
            prodottoController.modificaProdotto(beanAggiornato);
            refreshTable();
            showAlert(ALERT_SUCCESS_TITLE, "Prodotto aggiornato con successo!", Alert.AlertType.INFORMATION);
        });
    }

    @FXML
    private void modificaProdotto() {
        ProdottoBean prodottoSelezionato = prodottiTableView.getSelectionModel().getSelectedItem();
        if (prodottoSelezionato == null) {
            showAlert(ALERT_ERROR_TITLE, "Seleziona un prodotto da modificare.", Alert.AlertType.ERROR);
            return;
        }
        mostraDialogModifica(prodottoSelezionato);
    }

    @FXML
    private void eliminaProdotto() {
        ProdottoBean prodottoSelezionato = prodottiTableView.getSelectionModel().getSelectedItem();
        if (prodottoSelezionato == null) {
            showAlert(ALERT_ERROR_TITLE, "Seleziona un prodotto da eliminare.", Alert.AlertType.ERROR);
            return;
        }
        boolean confermato = mostraDialogConferma("Sei sicuro di voler eliminare il prodotto "
                + prodottoSelezionato.getNome() + "?");
        if (confermato) {
            prodottoController.eliminaProdotto(prodottoSelezionato.getId());
            refreshTable();
            showAlert(ALERT_SUCCESS_TITLE, "Prodotto eliminato con successo!", Alert.AlertType.INFORMATION);
        }
    }
}
