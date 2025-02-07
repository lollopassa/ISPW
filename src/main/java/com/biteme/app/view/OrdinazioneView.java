package com.biteme.app.view;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.controller.OrdinazioneController;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.controller.ArchivioController;
import com.biteme.app.bean.OrdineBean;
import com.biteme.app.util.SceneLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrdinazioneView {

    @FXML
    private TextField nomeClienteField;
    @FXML
    private ComboBox<String> tipoOrdineComboBox;
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
    private TableColumn<OrdinazioneBean, String> tipoOrdineColumn;
    @FXML
    private TableColumn<OrdinazioneBean, String> orarioColumn;
    @FXML
    private TableColumn<OrdinazioneBean, String> copertiColumn;
    @FXML
    private TableColumn<OrdinazioneBean, String> infoTavoloColumn;
    @FXML
    private TableColumn<OrdinazioneBean, String> statoOrdineColumn;
    @FXML
    private Button modificaButton;
    @FXML
    private Button eliminaButton;
    @FXML
    private Button archiviaButton;

    private final OrdineController ordineController = new OrdineController();
    private final OrdinazioneController ordinazioneController = new OrdinazioneController();
    // Istanza del nuovo ArchivioController per gestire l'archiviazione
    private final ArchivioController archivioController = new ArchivioController();

    private static OrdinazioneBean ordineSelezionato;

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
        tipoOrdineComboBox.getItems().addAll("Al Tavolo", "Asporto");
        tipoOrdineComboBox.setPromptText("Al Tavolo o Asporto?");
        tipoOrdineComboBox.setValue(null);
        tipoOrdineComboBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return (object == null) ? "Al Tavolo o Asporto?" : object;
            }
            @Override
            public String fromString(String string) {
                return string;
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

    // La view raccoglie i dati e chiama il controller per la creazione dell'ordine.
    @FXML
    public void createOrdine() {
        OrdinazioneBean ordinazioneBean = new OrdinazioneBean();
        ordinazioneBean.setNome(nomeClienteField.getText());
        ordinazioneBean.setTipoOrdine(tipoOrdineComboBox.getValue());
        ordinazioneBean.setOrarioCreazione(orarioField.getText());
        ordinazioneBean.setNumeroClienti(copertiField.getText());
        ordinazioneBean.setInfoTavolo(tavoloField.getText());

        // Il controller gestisce internamente eventuali errori o messaggi.
        ordinazioneController.creaOrdine(ordinazioneBean);

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
        if (ordinazione != null) {
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
        }
    }

    @FXML
    private void eliminaOrdine() {
        OrdinazioneBean ordinazioneSelezionata = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazioneSelezionata != null) {
            // Il controller gestisce la cancellazione e gli eventuali alert d'errore.
            ordinazioneController.eliminaOrdinazione(ordinazioneSelezionata.getId());
            refreshTable();
        }
    }

    @FXML
    public void archiviaOrdine() {
        OrdinazioneBean ordinazioneSelezionata = ordinazioniTableView.getSelectionModel().getSelectedItem();
        if (ordinazioneSelezionata != null) {
            // Recupera i dettagli completi dell'ordine.
            OrdineBean ordineBean = ordineController.getOrdineById(ordinazioneSelezionata.getId());
            BigDecimal totale = calcolaTotaleOrdine(ordineBean);

            // Crea l'ArchivioBean per l'archiviazione
            ArchivioBean archivioBean = new ArchivioBean();
            archivioBean.setIdOrdine(ordineBean.getId());
            archivioBean.setProdotti(ordineBean.getProdotti());
            archivioBean.setQuantita(ordineBean.getQuantita());
            archivioBean.setTotale(totale);
            archivioBean.setDataArchiviazione(LocalDateTime.now());

            // Archivia l'ordine tramite ArchivioController
            archivioController.archiviaOrdine(archivioBean);

            // Rimuove l'ordine archiviato dall'elenco degli ordini attivi
            ordinazioneController.eliminaOrdinazione(ordinazioneSelezionata.getId());
            refreshTable();
        }
    }

    private BigDecimal calcolaTotaleOrdine(OrdineBean ordineBean) {
        BigDecimal totale = BigDecimal.ZERO;
        if (ordineBean != null) {
            List<String> prodotti = ordineBean.getProdotti();
            List<Integer> quantita = ordineBean.getQuantita();
            if (prodotti != null && quantita != null && prodotti.size() == quantita.size()) {
                // Calcolo di esempio: sostituisci con la logica di calcolo reale se necessario.
                for (int i = 0; i < prodotti.size(); i++) {
                    totale = totale.add(BigDecimal.valueOf(10).multiply(BigDecimal.valueOf(quantita.get(i))));
                }
            }
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
}
