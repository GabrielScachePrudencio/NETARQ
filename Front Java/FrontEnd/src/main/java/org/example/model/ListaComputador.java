package org.example.model;

import java.util.ArrayList;

public class ListaComputador {
    private Computador usuario;
    private ArrayList<Computador> computadoresCadastrados;

    public ListaComputador(ArrayList<Computador> computadoresCadastrados, Computador usuario) {
        setUsuario(usuario);
        setComputadoresCadastrados(computadoresCadastrados);
    }
    public ListaComputador(){
        setUsuario(null);
        setComputadoresCadastrados(new ArrayList<Computador>());
    }



    public Computador getUsuario() {
        return usuario;
    }

    public void setUsuario(Computador usuario) {
        this.usuario = usuario;
    }

    public ArrayList<Computador> getComputadoresCadastrados() {
        return computadoresCadastrados;
    }

    public void setComputadoresCadastrados(ArrayList<Computador> computadoresCadastrados) {
        this.computadoresCadastrados = computadoresCadastrados;
    }
}
