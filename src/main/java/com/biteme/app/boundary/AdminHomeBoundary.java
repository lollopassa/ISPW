package com.biteme.app.boundary;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Map;
import com.biteme.app.controller.ArchivioController;

public class AdminHomeBoundary {

    @FXML
    private ComboBox<String> periodoComboBox;

    @FXML
    private Button aggiornaButton;

    @FXML
    private Button switchButton;

    @FXML
    private TableView<PiattoStatistiche> statisticheTable;
    @FXML
    private TableColumn<PiattoStatistiche, String> colonnaPiatto;
    @FXML
    private TableColumn<PiattoStatistiche, Number> colonnaOrdini;

    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private ArchivioController archivioController;

    private boolean mostraGuadagni; // Flag per gestione dello switch

    public AdminHomeBoundary() {
        this.archivioController = new ArchivioController();
        this.mostraGuadagni = false; // Default: mostra prodotti più ordinati
    }

    @FXML
    public void initialize() {
        colonnaPiatto.setCellValueFactory(new PropertyValueFactory<>("piatto"));
        colonnaOrdini.setCellValueFactory(new PropertyValueFactory<>("totale"));

        xAxis.setLabel("Piatto");
        yAxis.setLabel("Totale");

        if (periodoComboBox != null) {
            periodoComboBox.setValue("Settimana");
        }
    }

    @FXML
    public void aggiornaDati() {
        String periodoSelezionato = periodoComboBox.getValue();
        if (periodoSelezionato == null || periodoSelezionato.isEmpty()) {
            System.out.println("Nessun periodo selezionato.");
            return;
        }

        // Recupera i dati dal controller
        Map<String, Number> statistiche = mostraGuadagni
                ? archivioController.guadagniPerPeriodo(periodoSelezionato.toLowerCase())
                : archivioController.piattiPiuOrdinatiPerPeriodo(periodoSelezionato.toLowerCase());

        // Aggiorna la tabella
        ObservableList<PiattoStatistiche> data = FXCollections.observableArrayList();
        statistiche.forEach((piatto, totale) -> data.add(new PiattoStatistiche(piatto, totale)));
        statisticheTable.setItems(data);

        // Aggiorna il BarChart
        barChart.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName(mostraGuadagni ? "Guadagni" : "Totale Ordini");
        statistiche.forEach((piatto, totale) -> serie.getData().add(new XYChart.Data<>(piatto, totale)));
        barChart.getData().add(serie);
    }

    @FXML
    public void switchView() {
        mostraGuadagni = !mostraGuadagni; // Cambia lo stato

        // Cambia il testo del pulsante
        switchButton.setText(mostraGuadagni ? "Prodotti Ordinati" : "Guadagni");

        // Aggiorna i dati per riflettere lo stato corrente
        aggiornaDati();
    }

    public static class PiattoStatistiche {
        private final String piatto;
        private final Number totale;

        public PiattoStatistiche(String piatto, Number totale) {
            this.piatto = piatto;
            this.totale = totale;
        }

        public String getPiatto() {
            return piatto;
        }

        public Number getTotale() {
            return totale;
        }
    }
}