package org.example;

import org.example.model.Arquivo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.stream.Stream;

public class teste {

    public static void main(String[] args) {

        String caminho = "C:\\Users\\Flavio\\Desktop\\Faculdade";

        System.out.println("=======================================");
        System.out.println("Pesquisa em Java (Recursiva)");
        System.out.println("=======================================");

        long inicio = System.nanoTime();

        ArrayList<Arquivo> arquivos = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(Path.of(caminho))) {

            stream
                    .filter(path -> !path.equals(Path.of(caminho))) // ignora a pasta raiz
                    .forEach(path -> {

                        try {

                            BasicFileAttributes attr =
                                    Files.readAttributes(path, BasicFileAttributes.class);

                            arquivos.add(new Arquivo(
                                    path.getFileName().toString(),
                                    (int) attr.size(),
                                    attr.isDirectory() ? 1 : 0,
                                    LocalDateTime.ofInstant(
                                            attr.lastModifiedTime().toInstant(),
                                            ZoneId.systemDefault()
                                    ),
                                    path.toAbsolutePath().toString()
                            ));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

        long fim = System.nanoTime();

        double tempo = (fim - inicio) / 1_000_000_000.0;

        System.out.println();
        System.out.println("Arquivos encontrados: " + arquivos.size());
        System.out.printf("Tempo: %.6f segundos%n", tempo);

        // Descomente se quiser imprimir tudo
        // arquivos.forEach(System.out::println);
    }
}