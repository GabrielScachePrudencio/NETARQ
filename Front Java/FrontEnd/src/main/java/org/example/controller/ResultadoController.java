package org.example.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.Arquivo;
import org.example.service.ArquivoService;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;

public class ResultadoController {
    private ArrayList<Arquivo> arquivos;
    private final ArquivoService service = ArquivoService.getInstance();

    @FXML
    private TreeView<Arquivo> treeView;

    @FXML
    public void initialize(){
        TreeItem<Arquivo> raiz = new TreeItem<>();
        treeView.setRoot(raiz);
        treeView.setShowRoot(false);

        treeView.setCellFactory(tv -> criarCell());
    }

    private TreeCell<Arquivo> criarCell() {
        TreeCell<Arquivo> cell = new TreeCell<Arquivo>() {
            @Override
            protected void updateItem(Arquivo arquivo, boolean empty) {
                super.updateItem(arquivo, empty);
                if (empty || arquivo == null) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                } else {
                    String icone = arquivo.getEh_diretorio() == 1 ? "📁" : "📄";
                    String info = arquivo.getEh_diretorio() == 1
                            ? ""
                            : "  (" + arquivo.getTamanho() + " B)";
                    setText(icone + " " + arquivo.getNome() + info);
                    setContextMenu(criarMenu(arquivo));
                }
            }
        };
        return cell;
    }

    // Monta o menu de contexto (clique direito) pra cada item
    private ContextMenu criarMenu(Arquivo arquivo) {
        ContextMenu menu = new ContextMenu();

        MenuItem abrir = new MenuItem("Abrir");
        abrir.setOnAction(e -> abrirArquivo(arquivo));

        MenuItem copiarCaminho = new MenuItem("Copiar caminho");
        copiarCaminho.setOnAction(e -> copiarParaClipboard(arquivo.getCaminho()));

        MenuItem propriedades = new MenuItem("Propriedades");
        propriedades.setOnAction(e -> mostrarPropriedades(arquivo));

        menu.getItems().addAll(abrir, copiarCaminho, new SeparatorMenuItem(), propriedades);
        return menu;
    }

    private void abrirArquivo(Arquivo arquivo) {
        try {
            File file = new File(arquivo.getCaminho());
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Não foi possível abrir: " + e.getMessage()).showAndWait();
        }
    }

    private void copiarParaClipboard(String texto) {
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(texto);
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
    }

    private void mostrarPropriedades(Arquivo arquivo) {
        String tipo = arquivo.getEh_diretorio() == 1 ? "Pasta" : "Arquivo";
        String mensagem = "Nome: " + arquivo.getNome() + "\n" +
                "Tipo: " + tipo + "\n" +
                "Tamanho: " + arquivo.getTamanho() + " bytes\n" +
                "Caminho: " + arquivo.getCaminho() + "\n" +
                "Modificado em: " + arquivo.getDate();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Propriedades");
        alert.setHeaderText(arquivo.getNome());
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public void setArquivos(ArrayList<Arquivo> arquivos){
        this.arquivos = arquivos;

        TreeItem<Arquivo> raiz = treeView.getRoot();
        raiz.getChildren().clear();

        for (Arquivo a : arquivos) {
            raiz.getChildren().add(criarNode(a));
        }
    }

    private TreeItem<Arquivo> criarNode(Arquivo arquivo) {
        return new TreeItem<Arquivo>(arquivo) {
            private boolean carregado = false;

            @Override
            public boolean isLeaf() {
                return getValue().getEh_diretorio() != 1;
            }

            @Override
            public ObservableList<TreeItem<Arquivo>> getChildren() {
                if (!carregado && getValue().getEh_diretorio() == 1) {
                    carregado = true;
                    ArrayList<Arquivo> filhos = service.getArquivos(getValue().getCaminho());
                    for (Arquivo filho : filhos) {
                        super.getChildren().add(criarNode(filho));
                    }
                }
                return super.getChildren();
            }
        };
    }

    @FXML
    public void voltar(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/pages/TelaPesquisa.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}