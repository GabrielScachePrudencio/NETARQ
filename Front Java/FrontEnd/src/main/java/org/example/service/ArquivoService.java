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
            caminho += "C:\\Users\\Flavio\\Desktop";
        }

        ArrayList<Arquivo> arquivos = new ArrayList<>();

        try (NativoScanner scanner = new NativoScanner()) {
            scanner.escanear(caminho);
            int total = scanner.getTotal();

            for (int i = 0; i < total; i++) {
                String nome = scanner.getNome(i);
                long tamanho = scanner.getTamanho(i);
                int ehDiretorio = scanner.getEhDiretorio(i);
                LocalDateTime data = LocalDateTime.parse(scanner.getDataFormatada(i), FORMATO);

                arquivos.add(new Arquivo(nome, (int) tamanho, ehDiretorio, data));
                arquivos.toString();
            }
        }

        arquivos.toString();


        return arquivos;
    }



    public ArrayList<Arquivo> carregarComAleatorio(){
        arquivos.clear();

        arquivos.add(new Arquivo("Documento.pdf", 1250, 0, LocalDateTime.now().minusDays(2)));
        arquivos.add(new Arquivo("Fotos", 0, 1, LocalDateTime.now().minusDays(10)));
        arquivos.add(new Arquivo("Planilha.xlsx", 890, 0, LocalDateTime.now().minusHours(5)));
        arquivos.add(new Arquivo("Músicas", 0, 1, LocalDateTime.now().minusMonths(1)));
        arquivos.add(new Arquivo("Apresentacao.pptx", 5400, 0, LocalDateTime.now().minusDays(1)));
        arquivos.add(new Arquivo("Videos", 0, 1, LocalDateTime.now().minusMonths(3)));
        arquivos.add(new Arquivo("Trabalho.docx", 2100, 0, LocalDateTime.now().minusHours(12)));
        arquivos.add(new Arquivo("Backup.zip", 15800, 0, LocalDateTime.now().minusWeeks(2)));
        arquivos.add(new Arquivo("Downloads", 0, 1, LocalDateTime.now().minusDays(30)));
        arquivos.add(new Arquivo("Imagem.png", 760, 0, LocalDateTime.now()));

        return arquivos;
    }

}
