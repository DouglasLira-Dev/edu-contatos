package br.educontatos;

/**
 * Classe Launcher criada para resolver o problema de empacotamento (Shade/Jar)
 * do JavaFX. Ela não herda de Application, apenas chama a App principal.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}