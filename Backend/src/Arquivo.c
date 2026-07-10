#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <string.h>
#include <sys/stat.h>
#include <time.h>
#include "../include/Arquivo.h"

// Separador de caminho correto por plataforma
#ifdef _WIN32
    #define SEPARADOR "\\"
#else
    #define SEPARADOR "/"
#endif

void inicializar(ListaArquivos *lista){
    printf("Inicializando lista...\n");
    lista->total = 0;
}

void findByFolder(const char *caminho, ListaArquivos *lista){
    DIR *diretorio;
    struct dirent *item;
    struct stat atributos;
    char caminhoCompleto[512];

    inicializar(lista);
    diretorio = opendir(caminho);

    if (diretorio == NULL) {
        perror("Erro ao abrir o diretorio");
        return;
    }

    while ((item = readdir(diretorio)) != NULL){
        if (strcmp(item->d_name, ".") == 0 || strcmp(item->d_name, "..") == 0)
            continue;

        if (lista->total >= 100) {
            printf("Aviso: Limite de 100 arquivos atingido.\n");
            break;
        }

        strcpy(lista->dados[lista->total].nome, item->d_name);

        // Usa o separador correto pra plataforma (era "\\" fixo antes)
        snprintf(caminhoCompleto, sizeof(caminhoCompleto), "%s" SEPARADOR "%s", caminho, item->d_name);

        strcpy(lista->dados[lista->total].caminho, caminhoCompleto);

        if (stat(caminhoCompleto, &atributos) == 0) {
            lista->dados[lista->total].eh_diretorio = S_ISDIR(atributos.st_mode) ? 1 : 0;
            lista->dados[lista->total].tamanho = atributos.st_size;

            struct tm *tempoLocal = localtime(&atributos.st_mtime);

            lista->dados[lista->total].date.dia     = tempoLocal->tm_mday;
            lista->dados[lista->total].date.mes     = tempoLocal->tm_mon + 1;
            lista->dados[lista->total].date.ano     = tempoLocal->tm_year + 1900;
            lista->dados[lista->total].date.hora    = tempoLocal->tm_hour;
            lista->dados[lista->total].date.minuto  = tempoLocal->tm_min;
            lista->dados[lista->total].date.segundo = tempoLocal->tm_sec;
        } else {
            lista->dados[lista->total].tamanho = 0;
            lista->dados[lista->total].eh_diretorio = 0;
            memset(&lista->dados[lista->total].date, 0, sizeof(Datatime));
        }

        lista->total++;
    }

    closedir(diretorio);
}

void exibirLista(ListaArquivos *lista) {
    printf("Itens contados de fato: %d\n", lista->total);

    printf("==============================================================================================================\n");
    printf("%-40s %-12s %-12s %-20s\n", "Nome", "Tamanho (B)", "Tipo", "Data de Modificacao");
    printf("==============================================================================================================\n");

    for (int i = 0; i < lista->total; i++) {
        printf("%-40s %-12ld %-12s %02d/%02d/%04d %02d:%02d:%02d\n",
               lista->dados[i].nome,
               lista->dados[i].tamanho,
               lista->dados[i].eh_diretorio ? "Pasta" : "Arquivo",
               lista->dados[i].date.dia,
               lista->dados[i].date.mes,
               lista->dados[i].date.ano,
               lista->dados[i].date.hora,
               lista->dados[i].date.minuto,
               lista->dados[i].date.segundo);
    }

    printf("==============================================================================================================\n");
    printf("Total de itens exibidos: %d\n", lista->total);
}