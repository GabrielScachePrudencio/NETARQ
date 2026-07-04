package org.example.controller;

import com.sun.source.tree.Tree;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.example.model.Arquivo;

import java.util.ArrayList;

public class ResultadoController {
    private ArrayList<Arquivo> arquivos;

    @FXML
    private TreeView<Arquivo> treeView;

    @FXML
    public void initialize(){
        TreeItem<Arquivo> raiz = new TreeItem<>();
        treeView.setRoot(raiz);
        treeView.setShowRoot(false);
        // cuida de como cada linha aparece (ícone + nome)

        treeView.setCellFactory(tv -> new TreeCell<Arquivo>() {
            @Override
            protected void updateItem(Arquivo arquivo, boolean empty) {
                super.updateItem(arquivo, empty);
                if (empty || arquivo == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String icone = arquivo.getEh_diretorio() == 1 ? "📁" : "📄";
                    String info = arquivo.getEh_diretorio() == 1
                            ? ""
                            : "  (" + arquivo.getTamanho() + " KB)";
                    setText(icone + " " + arquivo.getNome() + info);
                }
            }
        });

    }

    public void setArquivos(ArrayList<Arquivo> arquivos){
        this.arquivos = arquivos;

        TreeItem<Arquivo> raiz = treeView.getRoot();
        raiz.getChildren().clear();

        for(Arquivo a : arquivos){
            raiz.getChildren().add(new TreeItem<>(a));
        }


    }

}
