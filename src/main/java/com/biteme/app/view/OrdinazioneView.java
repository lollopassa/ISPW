package com.biteme.app.view;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.controller.ArchivioController;
import com.biteme.app.controller.OrdinazioneController;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.controller.ProdottoController;
import com.biteme.app.model.TipoOrdine;
import com.biteme.app.util.SceneLoader;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

public class OrdinazioneView {

    // Campi UI
    @FXML
    private TextField nomeClienteField;

    @FXML
    private ComboBox<TipoOrdine> tipoOrdineComboBox;

    @FXML
    private TextField orarioField;

    @FXML
    private TextField copertiField;

    @FXML
    private TextField tavoloField;

    @FXML
    private TableView<OrdinazioneBean> ordinazioniTableView;

    @FXML
    private TableColumn<OrdinazioneBean, Integer> idColumn;

    @FXML
    private TableColumn<OrdinazioneBean, String> nomeColumn;

    @FXML
    private TableColumn<OrdinazioneBean, TipoOrdine> tipoOrdineColumn;

    @FXML
    private TableColumn<OrdinazioneBean, String> orarioColumn;

    @FXML
    private TableColumn<OrdinazioneBean, String> copertiColumn;

    @FXML
    private TableColumn<OrdinazioneBean, String> infoTavoloColumn;

    @FXML
    private TableColumn<OrdinazioneBean, String> statoOrdineColumn;

    @FXML
    private Button modificaButton; // Per modificare l'ordine

    @FXML
    private Button eliminaButton; // Per eliminare l'ordine

    @FXML
    private Button archiviaButton; // Per archiviare l'ordine

    private final OrdineController ordineController = new OrdineController();
    private final ArchivioController archivioController = new ArchivioController();
    private final ProdottoController prodottoController = new ProdottoController();
    private final OrdinazioneController ordinazioneController = new OrdinazioneController();

    private static OrdinazioneBean ordineSelezionato;
    private static final String ERROR_TITLE = "Errore";

    @FXML
    public void initialize() {
        configureComboBox();
        initTableColumns();
        refreshTable();

        modificaButton.setDisable(true);
        eliminaButton.setDisable(true);
        archiviaButton.setDisable(true);

        ordinazioniTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isSelected = (newValue != null);
            modificaButton.setDisable(!isSelected);
            eliminaButton.setDisable(!isSelected);
            archiviaButton.setDisable(!isSelected);
        });
    }

    private void configureComboBox() {
        tipoOrdineComboBox.setItems(FXCollections.observableArrayList());
        tipoOrdineComboBox.getItems().add(null);
        tipoOrdineComboBox.getItems().addAll(TipoOrdine.values());
        tipoOrdineComboBox.setPromptText("Al Tavolo o Asporto?");
        tipoOrdineComboBox.setValue(null);
        // Imposta una StringConverter per mostrare il testo formattato
        tipoOrdineComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TipoOrdine object) {
                return (object == null) ? "Al Tavolo o Asporto?" : object.name().replace("_", " ");
            }
            @Override
            public TipoOrdine fromString(String string) {
                return TipoOrdine.valueOf(string.replace(" ", "_"));
            }
        });
    }

    private void initTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tipoOrdineColumn.setCellValueFactory(new PropertyValueFactory<>("tipoOrdine"));
        orarioColumn.setCellValueFactory(new PropertyValueFactory<>("orarioCreazione"));
        copertiColumn.setCellValueFactory(new PropertyValueFactory<>("numeroClienti"));
        infoTavoloColumn.setCellValueFactory(new PropertyValueFactory<>("infoTavolo"));
        statoOrdineColumn.setCellValueFactory(new PropertyValueFactory<>("statoOrdine"));
    }

    @FXML
    public void createOrdine() {
        String nomeCliente = nomeClienteField.getText();
        TipoOrdine tipoOrdine = tipoOrdineComboBox.getValue();
        String orario = orarioField.getText();
        String coperti = copertiField.getText();
        String tavolo = tavoloField.getText();

        if (tipoOrdine == null) {
            showAlert("Tipo Ordine Mancante",
                    "Seleziona un tipo di ordine: 'Al Tavolo' o 'Asporto'.",
                    Alert.AlertType.WARNING);
            return;
        }

        if (tipoOrdine == TipoOrdine.AL_TAVOLO) {
            orario = LocalTime.now().toString().substring(0, 5);
        } else if (tipoOrdine == TipoOrdine.ASPORTO) {
            coperti = null;
            tavolo = null;
            if (orario.isEmpty()) {
                showAlert("Campi Mancanti",
                        "Il campo Orario deve essere compilato in caso di Asporto.",
                        Alert.AlertType.WARNING);
                return;
            }
            if (!ordinazioneController.isValidTime(orario)) {
                showAlert("Formato Orario Non Valido",
                        "Il campo 'Orario' deve essere nel formato HH:mm, ad esempio '12:20'.",
                        Alert.AlertType.ERROR);
                return;
            }
        }

        if (nomeCliente.isEmpty()) {
            showAlert("Campi Mancanti", "Il campo Nome Cliente deve essere compilato.", Alert.AlertType.WARNING);
            return;
        }

        OrdinazioneBean ordinazioneBean = new OrdinazioneBean();
        ordinazioneBean.setNome(nomeCliente);
        ordinazioneBean.setTipoOrdine(tipoOrdine);
        ordinazioneBean.setOrarioCreazione(orario);
        ordinazioneBean.setNumeroClienti(coperti);
        ordinazioneBean.setInfoTavolo(tavolo);

        ordinazioneController.creaOrdine(ordinazioneBean);

        showAlert("Ordine Creato",
                "L'ordine è stato creato con successo per il cliente: " + nomeCliente,
                Alert.AlertType.INFORMATION);

        clearFields();
        refreshTable();
    }

    public static OrdinazioneBean getOrdineSelezionato() {
        return ordineSelezionato;
    }

    public static void setOrdineSelezionato(OrdinazioneBean ordine) {
        ordineSelezionato = ordine;
    }

    @FXML
    private void modificaOrdine() {
        OrdinazioneBean ordinazione = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazione == null) {
            showAlert(ERROR_TITLE, "Seleziona un ordine da modificare.", Alert.AlertType.ERROR);
            return;
        }
        try {
            OrdinazioneBean ordine = new OrdinazioneBean();
            ordine.setId(ordinazione.getId());
            ordine.setNome(ordinazione.getNome());
            ordine.setNumeroClienti(ordinazione.getNumeroClienti());
            ordine.setTipoOrdine(ordinazione.getTipoOrdine());
            ordine.setInfoTavolo(ordinazione.getInfoTavolo());
            ordine.setStatoOrdine(ordinazione.getStatoOrdine());
            ordine.setOrarioCreazione(ordinazione.getOrarioCreazione());

            setOrdineSelezionato(ordine);
            SceneLoader.loadScene("/com/biteme/app/ordine.fxml", "Modifica Ordine");
        } catch (Exception e) {
            showAlert(ERROR_TITLE, "Errore durante il caricamento della schermata di modifica.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminaOrdine() {
        OrdinazioneBean ordinazioneSelezionata = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazioneSelezionata == null) {
            showAlert(ERROR_TITLE, "Seleziona un ordine da eliminare.", Alert.AlertType.ERROR);
            return;
        }
        boolean conferma = mostraDialogConferma("Sei sicuro di voler eliminare l'ordine " + ordinazioneSelezionata.getId() + "?");
        if (conferma) {
            try {
                ordinazioneController.eliminaOrdine(ordinazioneSelezionata.getId());
                refreshTable();
                showAlert("Successo", "Ordine eliminato con successo.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert(ERROR_TITLE, "Errore durante l'eliminazione dell'ordine.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void archiviaOrdine(ActionEvent actionEvent) {
        OrdinazioneBean ordinazioneSelezionata = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazioneSelezionata == null) {
            showAlert(ERROR_TITLE, "Seleziona un ordine da archiviare.", Alert.AlertType.ERROR);
            return;
        }
        boolean conferma = mostraDialogConferma("Sei sicuro di voler archiviare l'ordine " + ordinazioneSelezionata.getId() + "?");
        if (conferma) {
            try {
                OrdineBean ordineBean = ordineController.getOrdineById(ordinazioneSelezionata.getId());
                BigDecimal totale = calcolaTotaleOrdine(ordineBean);
                ArchivioBean archivioBean = new ArchivioBean();
                archivioBean.setIdOrdine(ordineBean.getId());
                archivioBean.setProdotti(ordineBean.getProdotti());
                archivioBean.setQuantita(ordineBean.getQuantita());
                archivioBean.setTotale(totale);
                archivioBean.setDataArchiviazione(LocalDateTime.now());
                archivioController.archiviaOrdine(archivioBean);
                ordinazioneController.eliminaOrdine(ordinazioneSelezionata.getId());
                refreshTable();
                showAlert("Successo", "Ordine archiviato con successo.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert(ERROR_TITLE, "Errore durante l'archiviazione: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private BigDecimal calcolaTotaleOrdine(OrdineBean ordineBean) {
        BigDecimal totale = BigDecimal.ZERO;
        if (ordineBean == null) {
            throw new IllegalArgumentException("OrdineBean non valido: null");
        }
        List<String> prodotti = ordineBean.getProdotti();
        List<Integer> quantita = ordineBean.getQuantita();
        if (prodotti == null || quantita == null || prodotti.isEmpty() || quantita.isEmpty()) {
            Logger.getLogger(this.getClass().getName())
                    .warning("Lista prodotti o quantità vuota. Totale ordine: 0");
            return totale;
        }
        if (prodotti.size() != quantita.size()) {
            throw new IllegalStateException("Dimensioni di prodotti e quantità non corrispondenti");
        }
        for (int i = 0; i < prodotti.size(); i++) {
            String nomeProdotto = prodotti.get(i);
            ProdottoBean prodotto = prodottoController.getProdottoByNome(nomeProdotto);
            if (prodotto == null) {
                throw new RuntimeException("Prodotto non trovato: " + nomeProdotto);
            }
            BigDecimal prezzo = prodotto.getPrezzo();
            totale = totale.add(prezzo.multiply(BigDecimal.valueOf(quantita.get(i))));
        }
        return totale;
    }

    private void refreshTable() {
        List<OrdinazioneBean> ordini = ordinazioneController.getOrdini();
        ordinazioniTableView.getItems().setAll(ordini);
    }

    private void clearFields() {
        nomeClienteField.clear();
        orarioField.clear();
        copertiField.clear();
        tavoloField.clear();
        tipoOrdineComboBox.getSelectionModel().clearSelection();
        tipoOrdineComboBox.setValue(null);
    }

    private boolean mostraDialogConferma(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma");
        alert.setContentText(messaggio);
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
