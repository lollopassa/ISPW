package com.biteme.app.view;

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

public class AdminHomeView {

    // Definizione della costante per "Guadagni"
    private static final String GUADAGNI_LABEL = "Guadagni";

    @FXML
    private ComboBox<String> periodoComboBox;

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

    public AdminHomeView() {
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

        // Impostiamo il category gap per rendere le colonne più strette
        barChart.setCategoryGap(10); // Puoi ridurre ulteriormente il valore se necessario
    }

    @FXML
    public void aggiornaDati() {
        String periodoSelezionato = periodoComboBox.getValue();
        if (periodoSelezionato == null || periodoSelezionato.isEmpty()) {
            System.out.println("Nessun periodo selezionato.");
            return;
        }

        Map<String, Number> statistiche;
        if (mostraGuadagni) {
            statistiche = archivioController.guadagniPerGiorno(periodoSelezionato.toLowerCase());
        } else {
            statistiche = archivioController.piattiPiuOrdinatiPerPeriodo(periodoSelezionato.toLowerCase());
        }

        // Aggiorna la tabella
        ObservableList<PiattoStatistiche> data = FXCollections.observableArrayList();
        statistiche.forEach((key, value) -> data.add(new PiattoStatistiche(key, value)));
        statisticheTable.setItems(data);

        // Crea un nuovo BarChart in base allo stato corrente
        BarChart<String, Number> newChart = createNewBarChart(mostraGuadagni);

        // Aggiunge i dati al nuovo grafico
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName(mostraGuadagni ? GUADAGNI_LABEL : "Totale Ordini");
        statistiche.forEach((key, value) -> serie.getData().add(new XYChart.Data<>(key, value)));
        newChart.getData().add(serie);

        // Sostituisci il vecchio grafico con il nuovo
        Pane parent = (Pane) barChart.getParent();
        int index = parent.getChildren().indexOf(barChart);
        parent.getChildren().remove(barChart);
        parent.getChildren().add(index, newChart);
        barChart = newChart;  // aggiorna il riferimento
    }

    @FXML
    public void switchView() {
        mostraGuadagni = !mostraGuadagni; // Cambia lo stato

        // Cambia il testo del pulsante
        switchButton.setText(mostraGuadagni ? "Prodotti Ordinati" : GUADAGNI_LABEL);

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
    private BarChart<String, Number> createNewBarChart(boolean isGuadagni) {
        CategoryAxis newXAxis = new CategoryAxis();
        NumberAxis newYAxis = new NumberAxis();
        if (isGuadagni) {
            newXAxis.setCategories(FXCollections.observableArrayList("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"));
            newXAxis.setLabel("Giorno della settimana");
            newYAxis.setLabel(GUADAGNI_LABEL);
        } else {
            newXAxis.setLabel("Piatto");
            newYAxis.setLabel("Totale Ordini");
        }
        BarChart<String, Number> newChart = new BarChart<>(newXAxis, newYAxis);
        newChart.setCategoryGap(10);
        newChart.setAnimated(false);
        // Imposta dimensioni e posizione uguali al vecchio grafico
        newChart.setPrefHeight(barChart.getPrefHeight());
        newChart.setPrefWidth(barChart.getPrefWidth());
        newChart.setLayoutX(barChart.getLayoutX());
        newChart.setLayoutY(barChart.getLayoutY());
        return newChart;
    }

}