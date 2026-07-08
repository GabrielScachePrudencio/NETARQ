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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.model.Computador;
import org.example.model.ListaComputador;
import org.example.service.ComputadorService;

public class HomeController {

    @FXML private TextField txtCodigo;

    @FXML private Label lblMeuNome;
    @FXML private Label lblMeuCodigo;

    @FXML private TableView<Computador> tabelaRecentes;
    @FXML private TableColumn<Computador, String> colComputador;
    @FXML private TableColumn<Computador, String> colCodigo;
    @FXML private TableColumn<Computador, String> colStatus;

    private final ComputadorService computadorService = new ComputadorService();
    private ListaComputador listaComputador;

    @FXML
    public void initialize() {
        configurarColunas();

        listaComputador = computadorService.carregar();

        atualizarIdentidade();
        atualizarTabela();

        tabelaRecentes.setRowFactory(tv -> {
            TableRow<Computador> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    conectar(row.getItem());
                }
            });
            return row;
        });

        if (listaComputador.getUsuario().getNome() == null
                || listaComputador.getUsuario().getNome().isBlank()) {
            pedirNomeUsuario();
        }
    }

    // ===== Identidade (nome/código deste computador) =====

    private void atualizarIdentidade() {
        Computador usuario = listaComputador.getUsuario();
        lblMeuNome.setText(
                usuario.getNome() != null && !usuario.getNome().isBlank()
                        ? usuario.getNome()
                        : "Sem nome definido"
        );
        lblMeuCodigo.setText(usuario.getCodigo());
    }

    private void pedirNomeUsuario() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Bem-vindo ao NET ARQ");
        dialog.setHeaderText("Como podemos chamar este computador?");
        dialog.setContentText("Nome:");

        dialog.showAndWait().ifPresent(nome -> {
            if (!nome.isBlank()) {
                computadorService.atualizarNomeUsuario(listaComputador, nome.trim());
                atualizarIdentidade();
            }
        });
    }

    @FXML
    public void editarNome(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog(listaComputador.getUsuario().getNome());
        dialog.setTitle("Editar nome");
        dialog.setHeaderText("Alterar o nome deste computador");
        dialog.setContentText("Nome:");

        dialog.showAndWait().ifPresent(nome -> {
            if (!nome.isBlank()) {
                computadorService.atualizarNomeUsuario(listaComputador, nome.trim());
                atualizarIdentidade();
            }
        });
    }

    @FXML
    public void copiarCodigo(ActionEvent event) {
        String codigo = listaComputador.getUsuario().getCodigo();

        ClipboardContent content = new ClipboardContent();
        content.putString(codigo);
        Clipboard.getSystemClipboard().setContent(content);

        // feedback rápido e não intrusivo
        Tooltip tooltip = new Tooltip("Código copiado!");
        tooltip.show(lblMeuCodigo.getScene().getWindow());

        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(
                javafx.util.Duration.seconds(1.2));
        delay.setOnFinished(e -> tooltip.hide());
        delay.play();
    }

    // ===== Resto (sem mudanças) =====

    private void configurarColunas() {
        colComputador.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colComputador.setCellFactory(col -> new TableCell<Computador, String>() {
            @Override
            protected void updateItem(String nome, boolean empty) {
                super.updateItem(nome, empty);
                if (empty || nome == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Computador c = getTableView().getItems().get(getIndex());

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

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<Computador, String>() {
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
    }

    private void atualizarTabela() {
        ObservableList<Computador> computadores = FXCollections.observableArrayList(
                listaComputador.getComputadoresCadastrados()
        );
        tabelaRecentes.setItems(computadores);
    }

    private void conectar(Computador computador) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/pages/Resultado.fxml"));
            Parent root = loader.load();

            ResultadoController controller = loader.getController();
            controller.iniciarComputador(computador);

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

        Computador computador = computadorService.conectarPorCodigo(codigo, listaComputador);

        if (computador == null) {
            new Alert(Alert.AlertType.ERROR, "Computador não encontrado. Verifique o código.").showAndWait();
            return;
        }

        atualizarTabela();
        txtCodigo.clear();
        conectar(computador);
    }
}