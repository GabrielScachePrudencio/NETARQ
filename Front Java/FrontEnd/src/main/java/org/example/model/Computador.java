package org.example.model;

import java.sql.Timestamp;

public class Computador {

    private long id;
    private String codigo;
    private String nome;
    private String ip;
    private int porta;
    private boolean online;
    private Timestamp ultimoHeartbeat;

    public Computador() {
    }

    public Computador(long id, String codigo, String nome, String ip,
                      int porta, boolean online, Timestamp ultimoHeartbeat) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.ip = ip;
        this.porta = porta;
        this.online = online;
        this.ultimoHeartbeat = ultimoHeartbeat;
    }

    public Computador(String nome, String codigo, Boolean status) { setNome(nome); setCodigo(codigo); setOnline(status); }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Timestamp getUltimoHeartbeat() {
        return ultimoHeartbeat;
    }

    public void setUltimoHeartbeat(Timestamp ultimoHeartbeat) {
        this.ultimoHeartbeat = ultimoHeartbeat;
    }

    /**
     * Usado pela TableView para exibir "online" ou "offline".
     */
    public String getStatus() {
        return online ? "online" : "offline";
    }

    /**
     * Avatar da TableView.
     */
    public String getIniciais() {

        if (nome == null || nome.isBlank()) {
            return "";
        }

        String[] partes = nome.trim().split("\\s+");

        if (partes.length == 1) {
            return partes[0]
                    .substring(0, Math.min(2, partes[0].length()))
                    .toUpperCase();
        }

        return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
    }
}