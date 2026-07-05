package org.example.model;

import org.example.service.ArquivoService;

import java.time.LocalDateTime;

public class Arquivo {
    private String nome;
    private Integer tamanho;
    private String caminho;
    private int eh_diretorio;
    LocalDateTime date;

    public Arquivo( String nome, Integer tamanho, int eh_diretorio, LocalDateTime date, String caminho){
        setNome(nome);
        setTamanho(tamanho);
        setEh_diretorio(eh_diretorio);
        setDate(date);
        setCaminho(caminho);
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getEh_diretorio() {
        return eh_diretorio;
    }

    public void setEh_diretorio(int eh_diretorio) {
        this.eh_diretorio = eh_diretorio;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getTamanho() {
        return tamanho;
    }

    public void setTamanho(Integer tamanho) {
        this.tamanho = tamanho;
    }

    public String getCaminho(){
        return caminho;
    }
    public void setCaminho(String caminho){
        this.caminho = caminho;
    }

    @Override
    public String toString() {
        return "\nArquivo{" +
                "caminho='" + caminho + '\'' +
                ", nome='" + nome + '\'' +
                ", tamanho=" + tamanho +
                ", eh_diretorio=" + eh_diretorio +
                ", date=" + date +
                '}';
    }
}
