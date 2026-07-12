package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.example.model.Arquivo;
import org.example.model.Computador;
import org.example.service.ArquivoService;
import org.example.service.ComputadorService;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class ResultadoController {

    private final ArquivoService service = ArquivoService.getInstance();

    // ===== Painel esquerdo: meu computador =====
    @FXML private Label lblNomeLocal;
    @FXML private TextField txtCaminhoLocal;
    @FXML private Label lblStatusLocal;
    @FXML private TableView<Arquivo> tabelaLocal;
    @FXML private TableColumn<Arquivo, String> colNomeLocal;
    @FXML private TableColumn<Arquivo, String> colTipoLocal;
    @FXML private TableColumn<Arquivo, String> colTamanhoLocal;
    @FXML private TableColumn<Arquivo, String> colDataLocal;

    // ===== Painel direito: computador conectado =====
    @FXML private Label lblNomeRemoto;
    @FXML private TextField txtCaminhoRemoto;
    @FXML private Label lblStatusRemoto;
    @FXML private TableView<Arquivo> tabelaRemoto;
    @FXML private TableColumn<Arquivo, String> colNomeRemoto;
    @FXML private TableColumn<Arquivo, String> colTipoRemoto;
    @FXML private TableColumn<Arquivo, String> colTamanhoRemoto;
    @FXML private TableColumn<Arquivo, String> colDataRemoto;

    private PainelArquivos painelLocal;
    private PainelArquivos painelRemoto;

    private Computador computadorLocal;
    private Computador computadorRemoto;

    @FXML
    public void initialize() {
        painelLocal = new PainelArquivos(tabelaLocal, colNomeLocal, colTipoLocal,
                colTamanhoLocal, colDataLocal, txtCaminhoLocal, lblStatusLocal);

        painelRemoto = new PainelArquivos(tabelaRemoto, colNomeRemoto, colTipoRemoto,
                colTamanhoRemoto, colDataRemoto, txtCaminhoRemoto, lblStatusRemoto);
    }

    /**
     * Chamado pela Home após conectar em outro computador.
     * Preenche o painel esquerdo com a identidade local (config.json)
     * e o painel direito com o computador que acabou de ser conectado.
     */
    public void iniciarComputador(Computador remoto) {
        this.computadorRemoto = remoto;
        this.computadorLocal = new ComputadorService().carregar().getUsuario();

        lblNomeLocal.setText(
                computadorLocal.getNome() != null ? computadorLocal.getNome() : "Meu computador");
        lblNomeRemoto.setText(
                computadorRemoto.getNome() != null ? computadorRemoto.getNome() : "Computador conectado");

        // Painel esquerdo sempre começa na pasta do usuário local
        painelLocal.navegarPara(System.getProperty("user.home"));

        // Painel direito começa no compartilhamento de rede do computador remoto
        String caminhoRemoto = "\\\\" + remoto.getIp() + "\\";
        painelRemoto.navegarPara(caminhoRemoto);
    }

    // ===== Ações do painel esquerdo (delegam pro PainelArquivos) =====

    @FXML public void voltarHistoricoLocal(ActionEvent event) { painelLocal.voltarHistorico(); }
    @FXML public void subirPastaLocal(ActionEvent event) { painelLocal.subirPasta(); }
    @FXML public void irParaHomeLocal(ActionEvent event) { painelLocal.navegarPara(System.getProperty("user.home")); }
    @FXML public void atualizarLocal(ActionEvent event) { painelLocal.atualizar(); }
    @FXML public void navegarPorTextoLocal(ActionEvent event) { painelLocal.navegarPorTexto(); }

    // ===== Ações do painel direito =====

    @FXML public void voltarHistoricoRemoto(ActionEvent event) { painelRemoto.voltarHistorico(); }
    @FXML public void subirPastaRemoto(ActionEvent event) { painelRemoto.subirPasta(); }
    @FXML public void irParaHomeRemoto(ActionEvent event) {
        if (computadorRemoto != null) {
            painelRemoto.navegarPara("\\\\" + computadorRemoto.getIp() + "\\");
        }
    }
    @FXML public void atualizarRemoto(ActionEvent event) { painelRemoto.atualizar(); }
    @FXML public void navegarPorTextoRemoto(ActionEvent event) { painelRemoto.navegarPorTexto(); }

    // ===================================================================
    // Classe interna: encapsula navegação/histórico/tabela de UM painel
    // (evita duplicar toda a lógica entre local e remoto)
    // ===================================================================

    private class PainelArquivos {

        private final TableView<Arquivo> tabela;
        private final TextField txtCaminho;
        private final Label lblStatus;

        private final Deque<String> historico = new ArrayDeque<>();
        private String caminhoAtual;

        PainelArquivos(TableView<Arquivo> tabela,
                       TableColumn<Arquivo, String> colNome,
                       TableColumn<Arquivo, String> colTipo,
                       TableColumn<Arquivo, String> colTamanho,
                       TableColumn<Arquivo, String> colData,
                       TextField txtCaminho,
                       Label lblStatus) {

            this.tabela = tabela;
            this.txtCaminho = txtCaminho;
            this.lblStatus = lblStatus;

            configurarColunas(colNome, colTipo, colTamanho, colData);
            configurarLinhas();
        }

        private void configurarColunas(TableColumn<Arquivo, String> colNome,
                                       TableColumn<Arquivo, String> colTipo,
                                       TableColumn<Arquivo, String> colTamanho,
                                       TableColumn<Arquivo, String> colData) {

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

        private void configurarLinhas() {
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

        // ===== Navegação =====

        void navegarPara(String caminho) {
            if (caminhoAtual != null) {
                historico.push(caminhoAtual);
            }
            caminhoAtual = caminho;
            carregarConteudo(caminho);
        }

        private void carregarConteudo(String caminho) {
            ArrayList<Arquivo> filhos = service.getArquivos(caminho);

            File pastaAtual = new File(caminho);
            if (pastaAtual.getParent() != null) {
                Arquivo pai = new Arquivo();
                pai.setNome("..");
                pai.setCaminho(pastaAtual.getParent());
                pai.setEh_diretorio(1);
                filhos.add(0, pai);
            }

            tabela.setItems(FXCollections.observableArrayList(filhos));
            txtCaminho.setText(caminho);
            atualizarStatus(filhos);
        }

        private void atualizarStatus(ArrayList<Arquivo> itens) {
            long pastas = itens.stream().filter(a -> a.getEh_diretorio() == 1 && !a.getNome().equals("..")).count();
            long arquivosCount = itens.size() - pastas - (itens.isEmpty() || !itens.get(0).getNome().equals("..") ? 0 : 1);
            lblStatus.setText(pastas + " pasta(s), " + arquivosCount + " arquivo(s)");
        }

        void subirPasta() {
            if (caminhoAtual == null) return;
            File atual = new File(caminhoAtual);
            String pai = atual.getParent();
            if (pai != null) {
                navegarPara(pai);
            }
        }

        void voltarHistorico() {
            if (!historico.isEmpty()) {
                caminhoAtual = historico.pop();
                carregarConteudo(caminhoAtual);
            }
        }

        void atualizar() {
            if (caminhoAtual != null) {
                carregarConteudo(caminhoAtual);
            }
        }

        void navegarPorTexto() {
            String caminho = txtCaminho.getText().trim();
            File f = new File(caminho);
            if (f.exists() && f.isDirectory()) {
                navegarPara(caminho);
            } else {
                new Alert(Alert.AlertType.WARNING, "Caminho inválido: " + caminho).showAndWait();
            }
        }

        private void tratarDuploClique(Arquivo item) {
            if (item.getNome().equals("..")) {
                subirPasta();
            } else if (item.getEh_diretorio() == 1) {
                navegarPara(item.getCaminho());
            } else {
                abrirArquivo(item);
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
    }

    // ===================================================================
    // Utilitários compartilhados (não dependem de qual painel é)
    // ===================================================================

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

    private String getDataFormatada(Arquivo arquivo) {
        if (arquivo.getNome().equals("..") || arquivo.getDate() == null) return "";
        try {
            java.time.LocalDateTime data = (java.time.LocalDateTime) arquivo.getDate();
            return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (ClassCastException e) {
            return arquivo.getDate().toString();
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

    @FXML
    public void selecionarPastaLocal(ActionEvent event) {
        abrirSeletorDePasta(painelLocal, txtCaminhoLocal);
    }

    @FXML
    public void selecionarPastaRemoto(ActionEvent event) {
        abrirSeletorDePasta(painelRemoto, txtCaminhoRemoto);
    }

    private void abrirSeletorDePasta(PainelArquivos painel, TextField campoOrigem) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecionar pasta");

        // tenta abrir já na pasta atual, se ela for um caminho local válido
        String caminhoAtual = campoOrigem.getText();
        if (caminhoAtual != null && !caminhoAtual.isBlank()) {
            File pastaAtual = new File(caminhoAtual);
            if (pastaAtual.exists() && pastaAtual.isDirectory()) {
                chooser.setInitialDirectory(pastaAtual);
            }
        }

        Window janela = campoOrigem.getScene().getWindow();
        File selecionada = chooser.showDialog(janela);

        if (selecionada != null) {
            painel.navegarPara(selecionada.getAbsolutePath());
        }
    }
}