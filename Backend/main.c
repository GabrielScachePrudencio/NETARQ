#include <stdio.h>
#include "./include/Arquivo.h"

int main()
{
    ListaArquivos lista;
    
    // Zere o contador de arquivos da sua struct para ela não começar com lixo de memória!
    lista.total = 0; // (Se na sua struct o nome for 'tamanho' ou 'quantidade', mude aqui)

    char caminho[256] = "C:\\Users\\Flavio\\Desktop";
    
    printf("Buscando arquivos...\n");
    findByFolder(caminho, &lista);

    printf("\n--- Resultados encontrados ---\n");
    exibirLista(&lista);

    return 0;
}