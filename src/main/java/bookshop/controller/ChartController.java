package bookshop.controller;

import bookshop.db.DBConnector;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChartController {

    @FXML
    PieChart chart;
    bookshop.db.DBConnector DBConnector = new DBConnector();

    public void initialize() {
        loadGraph();
        showAmounts();

    }

    private void showAmounts() {
        chart.getData().forEach(data ->
                data.nameProperty().bind(
                        Bindings.concat(
                                data.getName(), ": ", data.pieValueProperty()
                        )
                )
        );
    }

    public void loadGraph() {
        try {
            ResultSet booksByGenre = DBConnector.readBooksByGenre();
            ObservableList<PieChart.Data> tempList = FXCollections.observableArrayList();
            while (booksByGenre.next()) {
                PieChart.Data row = new PieChart.Data(booksByGenre.getString(2), booksByGenre.getInt(1));
                tempList.add(row);
            }
            chart.setData(tempList);
            chart.setLabelLineLength(10);
            chart.setLegendSide(Side.LEFT);
            chart.setTitle("Libros por género");
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "ERROR SQL: Comprueba que el servidor esta funcionando!").showAndWait();
        }
    }
}
