package com.biteme.app.ui;

import com.biteme.app.boundary.AdminHomeBoundary;
import com.biteme.app.exception.ArchiviazioneException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

import java.util.Map;
import java.util.logging.Logger;

public class AdminHomeUI {

    private static final String GUADAGNI_LABEL        = "Guadagni";
    private static final String GIORNO_SETTIMANA     = "Giorno della settimana";

    @FXML private ComboBox<String> periodoComboBox;
    @FXML private Button switchButton;
    @FXML private TableView<PiattoStatistiche> statisticheTable;
    @FXML private TableColumn<PiattoStatistiche, String> colonnaPiatto;
    @FXML private TableColumn<PiattoStatistiche, Number> colonnaOrdini;
    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final AdminHomeBoundary boundary = new AdminHomeBoundary();
    private boolean mostraGuadagni      = false;
    private boolean usaGuadagniAggregati = false;
    private static final Logger logger = Logger.getLogger(AdminHomeUI.class.getName());

    @FXML
    public void initialize() {
        colonnaPiatto.setCellValueFactory(new PropertyValueFactory<>("piatto"));
        colonnaOrdini.setCellValueFactory(new PropertyValueFactory<>("totale"));

        periodoComboBox.setItems(FXCollections.observableArrayList("Settimana", "Mese", "Trimestre"));
        periodoComboBox.setValue("Settimana");

        updateAxesLabels();
        barChart.setCategoryGap(10);

        if (switchButton != null) {
            switchButton.setText(GUADAGNI_LABEL);
        } else {
            logger.warning("switchButton non iniettato correttamente.");
        }

        aggiornaDati();
    }

    @FXML
    private void switchView() {
        mostraGuadagni = !mostraGuadagni;
        if (switchButton != null) {
            switchButton.setText(mostraGuadagni ? "Prodotti" : GUADAGNI_LABEL);
        } else {
            logger.warning("switchButton Ã¨ null. Verifica l'fx:id nel file FXML.");
        }
        updateAxesLabels();
        aggiornaDati();
    }



    @FXML
    private void aggiornaDati() {
        String periodo = periodoComboBox.getValue().toLowerCase();
        Map<String, Number> stats;
        try {
            if (mostraGuadagni) {
                stats = usaGuadagniAggregati
                        ? boundary.guadagniPerPeriodo(periodo)
                        : boundary.guadagniPerGiorno(periodo);
            } else {
                stats = boundary.piattiPiuOrdinatiPerPeriodo(periodo);
            }
        } catch (ArchiviazioneException e) {
            logger.severe("Errore nel recupero dati: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Dati non disponibili: " + e.getMessage());
            return;
        }

        ObservableList<PiattoStatistiche> data = FXCollections.observableArrayList();
        stats.forEach((k, v) -> data.add(new PiattoStatistiche(k, v)));
        statisticheTable.setItems(data);

        BarChart<String, Number> newChart = createBarChart();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        String serieName;
        if (mostraGuadagni) {
            serieName = usaGuadagniAggregati ? GUADAGNI_LABEL + " Aggregati" : GUADAGNI_LABEL;
        } else {
            serieName = "Totale Ordini";
        }
        serie.setName(serieName);
        stats.forEach((k, v) -> serie.getData().add(new XYChart.Data<>(k, v)));
        newChart.getData().setAll(serie);

        Pane parent = (Pane) barChart.getParent();
        int idx = parent.getChildren().indexOf(barChart);
        parent.getChildren().set(idx, newChart);
        barChart = newChart;
    }

    private BarChart<String, Number> createBarChart() {
        CategoryAxis xa = new CategoryAxis();
        NumberAxis ya   = new NumberAxis();
        xa.setLabel(mostraGuadagni ? GIORNO_SETTIMANA : "Piatto");
        ya.setLabel(mostraGuadagni ? GUADAGNI_LABEL : "Totale Ordini");
        BarChart<String, Number> chart = new BarChart<>(xa, ya);
        chart.setCategoryGap(10);
        chart.setAnimated(false);
        chart.setPrefSize(barChart.getPrefWidth(), barChart.getPrefHeight());
        chart.setLayoutX(barChart.getLayoutX());
        chart.setLayoutY(barChart.getLayoutY());
        return chart;
    }

    private void updateAxesLabels() {
        xAxis.setLabel(mostraGuadagni ? GIORNO_SETTIMANA : "Piatto");
        yAxis.setLabel(mostraGuadagni ? GUADAGNI_LABEL : "Totale");
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }

    public static class PiattoStatistiche {
        private final String piatto;
        private final Number totale;
        public PiattoStatistiche(String piatto, Number totale) {
            this.piatto  = piatto;
            this.totale = totale;
        }
        public String getPiatto() { return piatto; }
        public Number getTotale() { return totale; }
    }
}