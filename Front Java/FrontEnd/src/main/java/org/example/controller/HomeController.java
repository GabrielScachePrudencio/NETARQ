package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.model.ComputadorRecente;

public class HomeController {

    @FXML private TextField txtCodigo;

    @FXML private TableView<ComputadorRecente> tabelaRecentes;
    @FXML private TableColumn<ComputadorRecente, String> colComputador;
    @FXML private TableColumn<ComputadorRecente, String> colCodigo;
    @FXML private TableColumn<ComputadorRecente, String> colStatus;
    @FXML private TableColumn<ComputadorRecente, String> colAcesso;

    @FXML
    public void initialize() {
        configurarColunas();
        tabelaRecentes.setItems(carregarDadosFake());

        // Clique duplo numa linha "conecta" e abre o explorador de arquivos
        tabelaRecentes.setRowFactory(tv -> {
            TableRow<ComputadorRecente> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    conectar(row.getItem());
                }
            });
            return row;
        });
    }

    private void configurarColunas() {
        // Coluna "Computador" com avatar + nome
        colComputador.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colComputador.setCellFactory(col -> new TableCell<ComputadorRecente, String>() {
            @Override
            protected void updateItem(String nome, boolean empty) {
                super.updateItem(nome, empty);
                if (empty || nome == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                ComputadorRecente c = getTableView().getItems().get(getIndex());

                Label avatar = new Label(c.getIniciais());
                avatar.getStyleClass().add("avatar-circulo");

                Label nomeLabel = new Label(nome);
                nomeLabel.getStyleClass().add("cell-nome");

                HBox linha = new HBox(avatar, nomeLabel);
                linha.setSpacing(12);
                linha.setAlignment(Pos.CENTER_LEFT);

                setText(null);
                setGraphic(linha);
            }
        });

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        // Coluna "Status" com bolinha colorida
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<ComputadorRecente, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label bolinha = new Label();
                bolinha.getStyleClass().add(status.equals("online") ? "status-online" : "status-offline");

                Label texto = new Label(status.equals("online") ? "Online" : "Offline");
                texto.getStyleClass().add("cell-detalhes");

                HBox linha = new HBox(bolinha, texto);
                linha.setSpacing(6);
                linha.setAlignment(Pos.CENTER_LEFT);

                setText(null);
                setGraphic(linha);
            }
        });

        colAcesso.setCellValueFactory(new PropertyValueFactory<>("ultimoAcesso"));
    }

    // Dados fake só pra dar a cara de "computadores acessados"
    private ObservableList<ComputadorRecente> carregarDadosFake() {
        return FXCollections.observableArrayList(
                new ComputadorRecente("PC-Flavio-Trabalho", "482 719 305", "online", "Agora", System.getProperty("user.home")),
                new ComputadorRecente("Notebook-Casa", "119 204 887", "offline", "Ontem, 22:14", System.getProperty("user.home")),
                new ComputadorRecente("Servidor-Dev", "603 558 122", "online", "27/06/2026", System.getProperty("user.home")),
                new ComputadorRecente("PC-Sala", "775 331 940", "offline", "20/06/2026", System.getProperty("user.home"))
        );
    }

    private void conectar(ComputadorRecente computador) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/pages/Resultado.fxml"));
            Parent root = loader.load();

            ResultadoController controller = loader.getController();
            controller.carregarPastaInicial(computador.getCaminhoDemo());

            Stage stage = (Stage) tabelaRecentes.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Não foi possível conectar: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void conectarPorCodigo(ActionEvent event) {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Digite um código para conectar.").showAndWait();
            return;
        }
        // Aqui é só simulação: qualquer código "conecta" no mesmo diretório demo
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/pages/Resultado.fxml"));
            Parent root = loader.load();

            ResultadoController controller = loader.getController();
            controller.carregarPastaInicial(System.getProperty("user.home"));

            Stage stage = (Stage) txtCodigo.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Não foi possível conectar: " + e.getMessage()).showAndWait();
        }
    }
}