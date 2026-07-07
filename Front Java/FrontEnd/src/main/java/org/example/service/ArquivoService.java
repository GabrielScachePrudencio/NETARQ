package org.example.service;

import org.example.model.Arquivo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ArquivoService {
    private static ArquivoService instancia;

    private ArrayList<Arquivo> arquivos;
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private ArquivoService(){
        arquivos = new ArrayList<Arquivo>();
    }

    public static ArquivoService getInstance(){
        if(instancia == null){
            instancia = new ArquivoService();
        }

        return instancia;
    }



    public ArrayList<Arquivo> getArquivos(String caminho) {
        if(caminho == "" || caminho == null){
            return null;
        }

        ArrayList<Arquivo> arquivos = new ArrayList<>();

        try (NativoScanner scanner = new NativoScanner()) {
            scanner.escanear(caminho);
            int total = scanner.getTotal();

            for (int i = 0; i < total; i++) {
                String nome = scanner.getNome(i);
                String caminhoCompleto = scanner.getCaminho(i);
                long tamanho = scanner.getTamanho(i);
                int ehDiretorio = scanner.getEhDiretorio(i);
                String dataStr = scanner.getDataFormatada(i);
                LocalDateTime data;

                try {
                    if (dataStr == null || dataStr.equals("00/00/0000 00:00:00")) {
                        data = null; // ou LocalDateTime.MIN
                    } else {
                        data = LocalDateTime.parse(dataStr, FORMATO);
                    }
                } catch (Exception e) {
                    data = null;
                }
                arquivos.add(new Arquivo(nome, (int) tamanho, ehDiretorio, data, caminhoCompleto));

            }
        }



        return arquivos;
    }



}
