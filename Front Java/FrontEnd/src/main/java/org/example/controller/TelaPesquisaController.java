package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.model.Arquivo;
import org.example.service.ArquivoService;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import java.util.ArrayList;

public class TelaPesquisaController {

    @FXML
    private TextField txtCaminho;

    @FXML
    public void pesquisarPeloCaminho(ActionEvent event) throws  Exception{
        String caminho = txtCaminho.getText();
        ArrayList<Arquivo> arquivos = new ArrayList<>();
        ArquivoService arquivoService = ArquivoService.getInstance();

        arquivos = arquivoService.getArquivos(caminho);

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
}
