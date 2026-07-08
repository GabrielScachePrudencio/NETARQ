package org.example.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.Computador;
import org.example.model.Conexao;
import org.example.model.ListaComputador;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Responsável por:
 *  - Gerenciar o config.json local (identidade deste computador + recentes)
 *  - Sincronizar essa identidade com o banco central (upsert)
 *  - Buscar/cadastrar computadores por código no banco central
 *
 * Schema esperado da tabela `computadores`:
 *   id INT AUTO_INCREMENT PRIMARY KEY,
 *   codigo VARCHAR(20) NOT NULL UNIQUE,
 *   nome VARCHAR(100) NOT NULL,
 *   ip VARCHAR(45) NOT NULL,
 *   porta INT DEFAULT 5050,
 *   ultimo_acesso TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 */
public class ComputadorService {

    private static final String NOME_PASTA = "NETARQ";
    private static final String NOME_ARQUIVO = "config.json";
    private static final int PORTA_PADRAO = 5050;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ===================================================================
    // Arquivo local (config.json)
    // ===================================================================

    public static Path getArquivo() throws IOException {
        String appData = System.getenv("APPDATA");
        if (appData == null) {
            throw new IOException("Não foi possível encontrar o diretório APPDATA");
        }

        Path pasta = Paths.get(appData, NOME_PASTA);
        if (!Files.exists(pasta)) {
            Files.createDirectories(pasta);
        }

        Path arq = pasta.resolve(NOME_ARQUIVO);
        if (!Files.exists(arq)) {
            Files.createFile(arq);
            Files.writeString(arq, "{}");
        }

        return arq;
    }

    /**
     * Carrega o config.json. Se estiver vazio/sem "usuario", cria uma
     * identidade nova (código, ip, porta) e sincroniza com o banco central.
     * Chame isso uma vez ao iniciar a Home.
     */
    public ListaComputador carregar() {
        ListaComputador lista;
        try {
            Path arquivo = getArquivo();
            String conteudo = Files.readString(arquivo).trim();

            if (conteudo.isEmpty() || conteudo.equals("{}")) {
                lista = new ListaComputador();
            } else {
                lista = gson.fromJson(conteudo, ListaComputador.class);
                if (lista == null) lista = new ListaComputador();
            }
        } catch (IOException e) {
            lista = new ListaComputador();
        }

        if (lista.getUsuario() == null) {
            Computador novoUsuario = new Computador();
            novoUsuario.setCodigo(gerarCodigo());
            novoUsuario.setNome(null); // preenchido depois pelo usuário na Home
            novoUsuario.setIp(obterIpLocal());
            novoUsuario.setPorta(PORTA_PADRAO);
            lista.setUsuario(novoUsuario);
            salvar(lista);
        }

        // Toda abertura do app garante presença/atualização no banco central
        try {
            registrarOuAtualizarNoBanco(lista.getUsuario());
        } catch (SQLException e) {
            System.err.println("Não foi possível sincronizar com o banco: " + e.getMessage());
        }

        return lista;
    }

    public void salvar(ListaComputador lista) {
        try (FileWriter writer = new FileWriter(getArquivo().toFile())) {
            gson.toJson(lista, writer);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar config.json", e);
        }
    }

    /**
     * Chamado quando o usuário define/edita o próprio nome na Home.
     */
    public void atualizarNomeUsuario(ListaComputador lista, String novoNome) {
        lista.getUsuario().setNome(novoNome);
        salvar(lista);
        try {
            registrarOuAtualizarNoBanco(lista.getUsuario());
        } catch (SQLException e) {
            System.err.println("Não foi possível atualizar nome no banco: " + e.getMessage());
        }
    }

    // ===================================================================
    // Geração de identidade
    // ===================================================================

    private String gerarCodigo() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String obterIpLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> enderecos = iface.getInetAddresses();
                while (enderecos.hasMoreElements()) {
                    InetAddress addr = enderecos.nextElement();
                    if (addr.isSiteLocalAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        return addr.getHostAddress(); // ex: 192.168.0.15
                    }
                }
            }
        } catch (SocketException ignored) {}
        return "127.0.0.1";
    }

    // ===================================================================
    // Banco central (registro/descoberta)
    // ===================================================================

    /**
     * Upsert: se o código já existe no banco, atualiza nome/ip/porta;
     * se não existe, insere. É isso que faz este computador "existir"
     * pros outros da casa.
     */
    private void registrarOuAtualizarNoBanco(Computador usuario) throws SQLException {
        String sql = """
            INSERT INTO computadores (codigo, nome, ip, porta)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                nome = VALUES(nome),
                ip = VALUES(ip),
                porta = VALUES(porta),
                ultimo_acesso = CURRENT_TIMESTAMP
            """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getCodigo());
            stmt.setString(2, usuario.getNome() == null ? "" : usuario.getNome());
            stmt.setString(3, usuario.getIp());
            stmt.setInt(4, usuario.getPorta());
            stmt.executeUpdate();
        }
    }

    /**
     * Busca um computador pelo código diretamente no banco central.
     */
    public Computador buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM computadores WHERE codigo = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Computador c = new Computador();
                    c.setId(rs.getLong("id"));
                    c.setCodigo(rs.getString("codigo"));
                    c.setNome(rs.getString("nome"));
                    c.setIp(rs.getString("ip"));
                    c.setPorta(rs.getInt("porta"));
                    c.setOnline(false); // status é calculado em runtime, não vem do banco
                    c.setUltimoHeartbeat(rs.getTimestamp("ultimo_acesso"));
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Fluxo usado pelo botão "Conectar" da Home:
     * busca no banco central e, se achar, adiciona/atualiza a lista
     * de recentes no config.json local.
     */
    public Computador conectarPorCodigo(String codigo, ListaComputador listaLocal) {
        try {
            Computador encontrado = buscarPorCodigo(codigo);
            if (encontrado == null) return null;

            listaLocal.getComputadoresCadastrados().removeIf(
                    c -> c.getCodigo().equals(encontrado.getCodigo())
            );
            listaLocal.getComputadoresCadastrados().add(0, encontrado);
            salvar(listaLocal);

            atualizarUltimoAcesso(codigo);

            return encontrado;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar: " + e.getMessage(), e);
        }
    }

    public void atualizarUltimoAcesso(String codigo) {
        String sql = "UPDATE computadores SET ultimo_acesso = CURRENT_TIMESTAMP WHERE codigo = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Não foi possível atualizar último acesso: " + e.getMessage());
        }
    }
}