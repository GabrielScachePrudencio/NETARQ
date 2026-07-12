package org.example.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class NativoScanner implements AutoCloseable {

    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup lookup;

    private static final MethodHandle hEscanear;
    private static final MethodHandle hObterTotal;
    private static final MethodHandle hObterNome;
    private static final MethodHandle hObterTamanho;
    private static final MethodHandle hObterEhDiretorio;
    private static final MethodHandle hObterDataFormatada;
    private static final MethodHandle hLiberar;
    private static final MethodHandle hObterCaminho;
    static {
        try {
            loadNativeLibrary();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }

        lookup = SymbolLookup.loaderLookup();

        hEscanear = linker.downcallHandle(
                lookup.find("escanear").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );

        hObterTotal = linker.downcallHandle(
                lookup.find("obterTotal").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
        );

        hObterNome = linker.downcallHandle(
                lookup.find("obterNome").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
        );

        hObterTamanho = linker.downcallHandle(
                lookup.find("obterTamanho").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
        );

        hObterEhDiretorio = linker.downcallHandle(
                lookup.find("obterEhDiretorio").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
        );

        hObterDataFormatada = linker.downcallHandle(
                lookup.find("obterDataFormatada").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
        );

        hLiberar = linker.downcallHandle(
                lookup.find("liberar").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        hObterCaminho = linker.downcallHandle(
                lookup.find("obterCaminho").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
        );
    }

    private static void loadNativeLibrary() throws IOException {
        try (InputStream in = NativoScanner.class.getResourceAsStream("/org/example/native/nativo.dll")) {
            if (in == null) {
                throw new FileNotFoundException(
                        "nativo.dll não encontrada no classpath em /org/example/native/nativo.dll");
            }
            File temp = File.createTempFile("nativo", ".dll");
            temp.deleteOnExit();
            Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.load(temp.getAbsolutePath());
        }
    }

    private final Arena arena = Arena.ofConfined();
    private MemorySegment handle;



    public void escanear(String caminho) {
        try {
            MemorySegment caminhoNativo = arena.allocateFrom(caminho);
            handle = (MemorySegment) hEscanear.invoke(caminhoNativo);
        } catch (Throwable e) {
            throw new RuntimeException("Erro ao chamar escanear()", e);
        }
    }

    public int getTotal() {
        try {
            return (int) hObterTotal.invoke(handle);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public String getNome(int index) {
        try {
            MemorySegment ptr = (MemorySegment) hObterNome.invoke(handle, index);
            // reinterpreta o ponteiro pra poder ler a string até o \0
            return ptr.reinterpret(256).getString(0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public long getTamanho(int index) {
        try {
            return (long) hObterTamanho.invoke(handle, index);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public int getEhDiretorio(int index) {
        try {
            return (int) hObterEhDiretorio.invoke(handle, index);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public String getDataFormatada(int index) {
        try {
            MemorySegment buffer = arena.allocate(32);
            hObterDataFormatada.invoke(handle, index, buffer, 32);
            return buffer.getString(0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (handle != null) {
                hLiberar.invoke(handle); // libera a memória do lado C
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            arena.close(); // libera a memória do lado Java (a string do caminho, o buffer)
        }
    }

    public String getCaminho(int index) {
        try {
            MemorySegment ptr = (MemorySegment) hObterCaminho.invoke(handle, index);
            return ptr.reinterpret(512).getString(0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}