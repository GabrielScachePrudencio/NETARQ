package org.example.controller;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import org.example.model.Arquivo;
import org.example.service.ArquivoService;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class ResultadoController {

    private final ArquivoService service = ArquivoService.getInstance();

    // Histórico de navegação (pra botão "voltar")
    private final Deque<String> historico = new ArrayDeque<>();
    private String caminhoAtual;

    @FXML private TableView<Arquivo> tabela;
    @FXML private TableColumn<Arquivo, String> colNome;
    @FXML private TableColumn<Arquivo, String> colTipo;
    @FXML private TableColumn<Arquivo, String> colTamanho;
    @FXML private TableColumn<Arquivo, String> colData;

    @FXML private TextField txtCaminho;
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        configurarColunas();

        // Duplo clique: entra na pasta, sobe (..), ou abre o arquivo
        tabela.setRowFactory(tv -> {
            TableRow<Arquivo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    tratarDuploClique(row.getItem());
                }
            });
            row.setContextMenu(criarMenuLinha(row));
            return row;
        });
    }

    private void configurarColunas() {
        // Nome com ícone
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setCellFactory(col -> new TableCell<Arquivo, String>() {
            @Override
            protected void updateItem(String nome, boolean empty) {
                super.updateItem(nome, empty);
                if (empty || nome == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Arquivo a = getTableView().getItems().get(getIndex());
                setText((a.getNome().equals("..") ? "" : getIcone(a) + "  ") + nome);
            }
        });

        colTipo.setCellValueFactory(cell -> new SimpleStringProperty(getTipo(cell.getValue())));
        colTamanho.setCellValueFactory(cell -> new SimpleStringProperty(getTamanhoFormatado(cell.getValue())));
        colData.setCellValueFactory(cell -> new SimpleStringProperty(getDataFormatada(cell.getValue())));
    }

    private String getIcone(Arquivo arquivo) {
        if (arquivo.getEh_diretorio() == 1) return "📁";
        String nome = arquivo.getNome().toLowerCase();
        if (nome.endsWith(".pdf")) return "📕";
        if (nome.endsWith(".doc") || nome.endsWith(".docx")) return "📘";
        if (nome.endsWith(".xls") || nome.endsWith(".xlsx") || nome.endsWith(".csv")) return "📗";
        if (nome.endsWith(".ppt") || nome.endsWith(".pptx")) return "📙";
        if (nome.endsWith(".jpg") || nome.endsWith(".jpeg") || nome.endsWith(".png") || nome.endsWith(".gif")) return "🖼️";
        if (nome.endsWith(".mp3") || nome.endsWith(".wav")) return "🎵";
        if (nome.endsWith(".mp4") || nome.endsWith(".avi") || nome.endsWith(".mkv")) return "🎬";
        if (nome.endsWith(".zip") || nome.endsWith(".rar") || nome.endsWith(".7z")) return "🗜️";
        if (nome.endsWith(".txt") || nome.endsWith(".md")) return "📝";
        if (nome.endsWith(".exe") || nome.endsWith(".msi")) return "⚙️";
        return "📄";
    }

    private String getTipo(Arquivo arquivo) {
        if (arquivo.getNome().equals("..")) return "";
        if (arquivo.getEh_diretorio() == 1) return "Pasta de arquivos";
        String nome = arquivo.getNome();
        int idx = nome.lastIndexOf('.');
        return idx > 0 ? "Arquivo " + nome.substring(idx + 1).toUpperCase() : "Arquivo";
    }

    private String getTamanhoFormatado(Arquivo arquivo) {
        if (arquivo.getEh_diretorio() == 1) return "";
        long bytes = arquivo.getTamanho();
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.0f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // ATENÇÃO: ajuste esse método conforme o tipo real de arquivo.getDate()
    private String getDataFormatada(Arquivo arquivo) {
        if (arquivo.getNome().equals("..") || arquivo.getDate() == null) return "";
        try {
            // Se getDate() for LocalDateTime:
            java.time.LocalDateTime data = (java.time.LocalDateTime) arquivo.getDate();
            return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (ClassCastException e) {
            // Fallback: usa toString() se não for LocalDateTime
            return arquivo.getDate().toString();
        }
    }

    private void tratarDuploClique(Arquivo item) {
        if (item.getNome().equals("..")) {
            subirPasta(null);
        } else if (item.getEh_diretorio() == 1) {
            navegarPara(item.getCaminho());
        } else {
            abrirArquivo(item);
        }
    }

    // ===== Navegação =====

    public void carregarPastaInicial(String caminho) {
        navegarPara(caminho);
    }

    private void navegarPara(String caminho) {
        if (caminhoAtual != null) {
            historico.push(caminhoAtual);
        }
        caminhoAtual = caminho;
        carregarConteudo(caminho);
    }

    private void carregarConteudo(String caminho) {
        ArrayList<Arquivo> filhos = service.getArquivos(caminho);

        ObservableListWrapper listaObservavel = new ObservableListWrapper(filhos);
        // Adiciona a entrada ".." se não estiver na raiz
        File pastaAtual = new File(caminho);
        if (pastaAtual.getParent() != null) {
            Arquivo pai = new Arquivo();
            pai.setNome("..");
            pai.setCaminho(pastaAtual.getParent());
            pai.setEh_diretorio(1);
            filhos.add(0, pai);
        }

        tabela.setItems(javafx.collections.FXCollections.observableArrayList(filhos));
        txtCaminho.setText(caminho);
        atualizarStatus(filhos);
    }

    private void atualizarStatus(ArrayList<Arquivo> itens) {
        long pastas = itens.stream().filter(a -> a.getEh_diretorio() == 1 && !a.getNome().equals("..")).count();
        long arquivosCount = itens.size() - pastas - (itens.isEmpty() || !itens.get(0).getNome().equals("..") ? 0 : 1);
        lblStatus.setText(pastas + " pasta(s), " + arquivosCount + " arquivo(s)");
    }

    @FXML
    public void subirPasta(ActionEvent event) {
        File atual = new File(caminhoAtual);
        String pai = atual.getParent();
        if (pai != null) {
            navegarPara(pai);
        }
    }

    @FXML
    public void voltarHistorico(ActionEvent event) {
        if (!historico.isEmpty()) {
            caminhoAtual = historico.pop();
            carregarConteudo(caminhoAtual);
        }
    }

    @FXML
    public void irParaHome(ActionEvent event) {
        navegarPara(System.getProperty("user.home"));
    }

    @FXML
    public void atualizar(ActionEvent event) {
        carregarConteudo(caminhoAtual);
    }

    @FXML
    public void navegarPorTexto(ActionEvent event) {
        String caminho = txtCaminho.getText().trim();
        File f = new File(caminho);
        if (f.exists() && f.isDirectory()) {
            navegarPara(caminho);
        } else {
            new Alert(Alert.AlertType.WARNING, "Caminho inválido: " + caminho).showAndWait();
        }
    }

    // ===== Menu de contexto e ações de arquivo =====

    private ContextMenu criarMenuLinha(TableRow<Arquivo> row) {
        ContextMenu menu = new ContextMenu();

        MenuItem abrir = new MenuItem("Abrir");
        abrir.setOnAction(e -> tratarDuploClique(row.getItem()));

        MenuItem copiarCaminho = new MenuItem("Copiar caminho");
        copiarCaminho.setOnAction(e -> copiarParaClipboard(row.getItem().getCaminho()));

        MenuItem propriedades = new MenuItem("Propriedades");
        propriedades.setOnAction(e -> mostrarPropriedades(row.getItem()));

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
}