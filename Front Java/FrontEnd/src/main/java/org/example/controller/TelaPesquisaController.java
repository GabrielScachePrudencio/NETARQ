package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.model.Arquivo;
import org.example.service.ArquivoService;

import javafx.scene.control.TextField;
import java.io.File;
import java.util.ArrayList;

public class TelaPesquisaController {

    @FXML
    private TextField txtCaminho;

    @FXML
    public void abrirExplorador(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecione uma pasta");

        File inicial = new File("C:\\");
        if (!txtCaminho.getText().isEmpty()) {
            File atual = new File(txtCaminho.getText());
            if (atual.exists() && atual.isDirectory()) {
                inicial = atual;
            }
        }
        chooser.setInitialDirectory(inicial);

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File selecionado = chooser.showDialog(stage);

        if (selecionado != null) {
            txtCaminho.setText(selecionado.getAbsolutePath());
        }
    }

    @FXML
    public void pesquisarPeloCaminho(ActionEvent event) throws Exception {
        String caminho = txtCaminho.getText();

        if(caminho == null || caminho == ""){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Caminho inválido");
            alert.setContentText("Nenhum caminho foi selecionado ou a pasta não existe.");
            alert.showAndWait();
            return; // Impede que continue para a próxima tela

        }

        ArquivoService arquivoService = ArquivoService.getInstance();

        ArrayList<Arquivo> arquivos = arquivoService.getArquivos(caminho);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/pages/Resultado.fxml"));

        Parent root = loader.load();

        ResultadoController controller = loader.getController();
        controller.setArquivos(arquivos);

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                .getScene()
                .getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void voltar(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/pages/Home.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}