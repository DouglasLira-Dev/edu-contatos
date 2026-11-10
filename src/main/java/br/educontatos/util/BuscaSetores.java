package br.educontatos.util;

import br.educontatos.model.Setor;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário para filtrar setores com base em uma string de busca.
 * Ignora acentos, exige que todas as palavras apareçam e trata
 * palavras curtas (ex: "TI") como termos exatos.
 *
 * @author Douglas Lira
 * @version 1.0
 */
public class BuscaSetores {

    public static List<Setor> filtrar(List<Setor> setores, String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return setores;
        }

        String filtroNormalizado = Normalizador.normalizar(filtro);
        String[] palavras = filtroNormalizado.split("\\s+");

        List<Setor> resultado = new ArrayList<>();

        for (Setor s : setores) {
            // Concatena todos os campos do setor e normaliza
            String textoSetor = Normalizador.normalizar(
                    s.getNome() + " " + s.getResponsavel() + " " + 
                    s.getEmail() + " " + s.getRamal() + " " + s.getDescricao());

            boolean todosOsTermosPresentes = true;

            for (String palavra : palavras) {
                if (palavra.isBlank()) continue;

                if (palavra.length() <= 3) {
                    // Para palavras curtas (ex: TI, RH), exige correspondência exata de palavra
                    if (!textoSetor.matches(".*\\b" + palavra + "\\b.*")) {
                        todosOsTermosPresentes = false;
                        break;
                    }
                } else {
                    // Para palavras longas, basta conter
                    if (!textoSetor.contains(palavra)) {
                        todosOsTermosPresentes = false;
                        break;
                    }
                }
            }

            if (todosOsTermosPresentes) {
                resultado.add(s);
            }
        }
        return resultado;
    }
}