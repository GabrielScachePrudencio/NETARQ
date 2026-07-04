#ifndef ARQUIVOS_H
#define ARQUIVOS_H

typedef struct  {
    int dia;
    int mes;
    int ano;
    int hora;
    int minuto;
    int segundo;
} Datatime;


typedef struct 
{
    char nome[256];
    long tamanho;
    int eh_diretorio;
    Datatime date;
} Arquivo;


typedef struct {
    Arquivo dados[100];
    int total;
} ListaArquivos;

void inicializar(ListaArquivos *lista);
void findByFolder(const char *caminho, ListaArquivos *lista);
void exibirLista(ListaArquivos *lista);





#endif

