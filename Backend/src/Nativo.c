#include <stdlib.h>
#include <stdio.h>
#include "../include/Arquivo.h"

#ifdef _WIN32
    #define EXPORT __declspec(dllexport)
#else
    #define EXPORT
#endif

// Escaneia a pasta e devolve um "handle" opaco (ponteiro alocado no heap)
EXPORT void* escanear(const char* caminho) {
    ListaArquivos* lista = malloc(sizeof(ListaArquivos));
    findByFolder(caminho, lista);
    return lista;
}

EXPORT int obterTotal(void* handle) {
    ListaArquivos* lista = (ListaArquivos*) handle;
    return lista->total;
}

EXPORT const char* obterNome(void* handle, int index) {
    ListaArquivos* lista = (ListaArquivos*) handle;
    return lista->dados[index].nome;
}

EXPORT long obterTamanho(void* handle, int index) {
    ListaArquivos* lista = (ListaArquivos*) handle;
    return lista->dados[index].tamanho;
}

EXPORT int obterEhDiretorio(void* handle, int index) {
    ListaArquivos* lista = (ListaArquivos*) handle;
    return lista->dados[index].eh_diretorio;
}

// Preenche um buffer já alocado pelo Java, no formato dd/mm/aaaa hh:mm:ss
EXPORT void obterDataFormatada(void* handle, int index, char* buffer, int bufferSize) {
    ListaArquivos* lista = (ListaArquivos*) handle;
    Datatime d = lista->dados[index].date;
    snprintf(buffer, bufferSize, "%02d/%02d/%04d %02d:%02d:%02d",
             d.dia, d.mes, d.ano, d.hora, d.minuto, d.segundo);
}

// IMPORTANTÍSSIMO: libera a memória alocada no escanear()
EXPORT void liberar(void* handle) {
    free(handle);
}