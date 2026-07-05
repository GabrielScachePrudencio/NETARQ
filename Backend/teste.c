#include <windows.h>
#include <stdio.h>

int total = 0;

void listar(const char *diretorio)
{
    char pesquisa[MAX_PATH];
    WIN32_FIND_DATA dados;

    sprintf(pesquisa, "%s\\*", diretorio);

    HANDLE hFind = FindFirstFile(pesquisa, &dados);

    if (hFind == INVALID_HANDLE_VALUE)
        return;

    do {

        if (!strcmp(dados.cFileName, ".") ||
            !strcmp(dados.cFileName, ".."))
            continue;

        char caminho[MAX_PATH];
        sprintf(caminho, "%s\\%s", diretorio, dados.cFileName);

        //printf("%s\n", caminho);

        total++;

        if (dados.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
            listar(caminho);

    } while (FindNextFile(hFind, &dados));

    FindClose(hFind);
}

int main()
{
    LARGE_INTEGER inicio, fim, freq;

    QueryPerformanceFrequency(&freq);
    QueryPerformanceCounter(&inicio);

    listar("C:\\Users\\Flavio\\Desktop\\\Faculdade");

    QueryPerformanceCounter(&fim);

    printf("\nTotal encontrados: %d\n", total);
    printf("Tempo: %.9f segundos\n",
           (double)(fim.QuadPart - inicio.QuadPart) / freq.QuadPart);

    return 0;
}