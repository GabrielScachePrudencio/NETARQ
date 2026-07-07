package org.example.model;

public class ComputadorRecente {
    private final String nome;
    private final String codigo;
    private final String status; // "online" ou "offline"
    private final String ultimoAcesso;
    private final String caminhoDemo; // pasta que abre ao clicar (é só pra simular)

    public ComputadorRecente(String nome, String codigo, String status, String ultimoAcesso, String caminhoDemo) {
        this.nome = nome;
        this.codigo = codigo;
        this.status = status;
        this.ultimoAcesso = ultimoAcesso;
        this.caminhoDemo = caminhoDemo;
    }

    public String getNome() { return nome; }
    public String getCodigo() { return codigo; }
    public String getStatus() { return status; }
    public String getUltimoAcesso() { return ultimoAcesso; }
    public String getCaminhoDemo() { return caminhoDemo; }

    public String getIniciais() {
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
    }
}