#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <string.h>
#include <sys/stat.h> // <- NOVA: Para pegar os metadados (tamanho e data)
#include <time.h>     // <-- NOVA: Para converter o tempo
#include "../include/Arquivo.h"

void inicializar(ListaArquivos *lista){
    printf("Inicializando lista...\n");
    lista->total = 0;
}

void findByFolder(const char *caminho, ListaArquivos *lista){
    DIR *diretorio;
    struct dirent *item;
    struct stat atributos;    // Guarda os metadados do arquivo
    char caminhoCompleto[512]; // Armazena caminho + nome do arquivo

    inicializar(lista);
    diretorio = opendir(caminho);

    if (diretorio == NULL) {
        perror("Erro ao abrir o diretorio");
        return;
    }

    while ((item = readdir(diretorio)) != NULL){
        // Ignora "." e ".."
        if (strcmp(item->d_name, ".") == 0 || strcmp(item->d_name, "..") == 0)
            continue;

        // Limita a capacidade máxima do array estático da struct (100 itens)
        if (lista->total >= 100) {
            printf("Aviso: Limite de 100 arquivos atingido.\n");
            break;
        }

        // Copia o nome do arquivo
        strcpy(lista->dados[lista->total].nome, item->d_name);

        // Define se é pasta ou arquivo
       
        // --- NOVO: BUSCANDO TAMANHO E DATA ---
        
        // 1. Monta o caminho completo do arquivo. Ex: "C:\\Caminho\\arquivo.txt"
        snprintf(caminhoCompleto, sizeof(caminhoCompleto), "%s\\%s", caminho, item->d_name);

        // 2. Chama a função stat para ler os metadados do arquivo
        // 2. Chama a função stat para ler os metadados do arquivo
        if (stat(caminhoCompleto, &atributos) == 0) {
            // Define se é diretório usando o resultado do stat (mais portável que d_type)
            lista->dados[lista->total].eh_diretorio = S_ISDIR(atributos.st_mode) ? 1 : 0;

            // Guarda o tamanho real em bytes
            lista->dados[lista->total].tamanho = atributos.st_size;

            // Converte o timestamp de modificação (st_mtime) para formato local
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