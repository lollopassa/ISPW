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
import javafx.scene.layout.Pane;
import java.util.logging.Logger;

public class AdminHomeBoundary {

    private static final String GUADAGNI_LABEL = "Guadagni";
    private static final String GUADAGNI_GIORNALIERO = "Guadagni Giornalieri";
    private static final String GUADAGNI_AGGREGATI = "Guadagni Aggregati";
    private static final String GIORNO_DELLA_SETTIMANA = "Giorno della settimana";

    @FXML
    private ComboBox<String> periodoComboBox;

    
    @FXML
    private Button switchButton;

    
    @FXML
    private Button switchAggregationButton;

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

    
    private boolean mostraGuadagni;
    
    private boolean usaGuadagniAggregati;

    private static final Logger logger = Logger.getLogger(AdminHomeBoundary.class.getName());

    public AdminHomeBoundary() {
        this.archivioController = new ArchivioController();
        this.mostraGuadagni = false;     
        this.usaGuadagniAggregati = false; 
    }

    @FXML
    public void initialize() {
        colonnaPiatto.setCellValueFactory(new PropertyValueFactory<>("piatto"));
        colonnaOrdini.setCellValueFactory(new PropertyValueFactory<>("totale"));

        
        if (mostraGuadagni) {
            xAxis.setLabel(GIORNO_DELLA_SETTIMANA);
            yAxis.setLabel(GUADAGNI_LABEL);
        } else {
            xAxis.setLabel("Piatto");
            yAxis.setLabel("Totale");
        }

        if (periodoComboBox != null) {
            periodoComboBox.setValue("Settimana");
        }

        barChart.setCategoryGap(10);

        
        if (switchAggregationButton != null) {
            switchAggregationButton.setText(usaGuadagniAggregati ? GUADAGNI_AGGREGATI : GUADAGNI_GIORNALIERO);
        }
    }

    @FXML
    public void aggiornaDati() {
        String periodoSelezionato = periodoComboBox.getValue();
        Map<String, Number> statistiche;
        if (mostraGuadagni) {
            if (usaGuadagniAggregati) {
                statistiche = archivioController.guadagniPerPeriodo(periodoSelezionato.toLowerCase());
            } else {
                statistiche = archivioController.guadagniPerGiorno(periodoSelezionato.toLowerCase());
            }
        } else {
            statistiche = archivioController.piattiPiuOrdinatiPerPeriodo(periodoSelezionato.toLowerCase());
        }

        if (statistiche.isEmpty()) {
            logger.severe("Nessun dato disponibile per il periodo selezionato o periodo non valido.");
        }

        
        ObservableList<PiattoStatistiche> data = FXCollections.observableArrayList();
        statistiche.forEach((key, value) -> data.add(new PiattoStatistiche(key, value)));
        statisticheTable.setItems(data);

        
        BarChart<String, Number> newChart = createNewBarChart(mostraGuadagni);

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        String serieName;
        if (mostraGuadagni) {
            serieName = usaGuadagniAggregati ? GUADAGNI_LABEL + " Aggregati" : GUADAGNI_LABEL;
        } else {
            serieName = "Totale Ordini";
        }
        serie.setName(serieName);
        statistiche.forEach((key, value) -> serie.getData().add(new XYChart.Data<>(key, value)));
        newChart.getData().add(serie);

        
        Pane parent = (Pane) barChart.getParent();
        int index = parent.getChildren().indexOf(barChart);
        parent.getChildren().remove(barChart);
        parent.getChildren().add(index, newChart);
        barChart = newChart;
    }

    @FXML
    public void switchView() {
        mostraGuadagni = !mostraGuadagni;
        
        if (switchButton != null) {
            switchButton.setText(mostraGuadagni ? "Prodotti" : GUADAGNI_LABEL);
        } else {
            logger.warning("switchButton è null. Verifica l'fx:id nel file FXML.");
        }
        aggiornaDati();
    }

    @FXML
    public void switchAggregation() {
        
        usaGuadagniAggregati = !usaGuadagniAggregati;
        if (switchAggregationButton != null) {
            switchAggregationButton.setText(usaGuadagniAggregati ? GUADAGNI_AGGREGATI : GUADAGNI_GIORNALIERO);
        } else {
            
            logger.info("switchAggregationButton non presente nell'FXML.");
        }
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

    private BarChart<String, Number> createNewBarChart(boolean isGuadagni) {
        CategoryAxis newXAxis = new CategoryAxis();
        NumberAxis newYAxis = new NumberAxis();
        if (isGuadagni) {
            if (usaGuadagniAggregati) {
                newXAxis.setCategories(FXCollections.observableArrayList("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"));
                newXAxis.setLabel(GIORNO_DELLA_SETTIMANA);
                newYAxis.setLabel(GUADAGNI_LABEL);
            } else {
                newXAxis.setLabel(GIORNO_DELLA_SETTIMANA);
                newYAxis.setLabel(GUADAGNI_LABEL);
            }
        } else {
            newXAxis.setLabel("Piatto");
            newYAxis.setLabel("Totale Ordini");
        }
        BarChart<String, Number> newChart = new BarChart<>(newXAxis, newYAxis);
        newChart.setCategoryGap(10);
        newChart.setAnimated(false);
        newChart.setPrefHeight(barChart.getPrefHeight());
        newChart.setPrefWidth(barChart.getPrefWidth());
        newChart.setLayoutX(barChart.getLayoutX());
        newChart.setLayoutY(barChart.getLayoutY());
        return newChart;
    }
}
