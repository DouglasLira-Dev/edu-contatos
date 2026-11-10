package br.educontatos.util;

import br.educontatos.model.Setor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário para importação e exportação de setores em formato CSV.
 * Trata corretamente campos com aspas, quebras de linha e acentos.
 *
 * @author Douglas Lira
 * @version 1.0
 */
public class CsvSetores {

    public static void exportar(File file, List<Setor> setores) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM para o Excel reconhecer UTF-8
        sb.append("nome;responsavel;email;ramal;descricao\n");

        for (Setor s : setores) {
            sb.append(formatar(s.getNome())).append(";")
              .append(formatar(s.getResponsavel())).append(";")
              .append(formatar(s.getEmail())).append(";")
              .append(formatar(s.getRamal())).append(";")
              .append(formatar(s.getDescricao())).append("\n");
        }
        Files.write(file.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String formatar(String valor) {
        if (valor == null) return "";
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    public static List<Setor> importar(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        String conteudo;

        // Detecção correta de encoding (UTF-8 ou Windows-1252)
        try {
            conteudo = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(java.nio.ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            // Se falhou, tenta Windows-1252 (padrão do Excel PT-BR)
            conteudo = new String(bytes, Charset.forName("windows-1252"));
        }

        if (conteudo.startsWith("\uFEFF")) {
            conteudo = conteudo.substring(1); // Remove BOM
        }

        List<Setor> setores = new ArrayList<>();
        List<String> linhas = parseLinhas(conteudo);

        if (linhas.isEmpty()) return setores;

        // Detecta o separador pela primeira linha
        String separador = linhas.get(0).contains(";") ? ";" : ",";

        // Pula o cabeçalho (índice 0)
        for (int i = 1; i < linhas.size(); i++) {
            String[] partes = parseLinha(linhas.get(i), separador);
            if (partes.length >= 1 && !partes[0].trim().isEmpty()) {
                Setor s = new Setor();
                s.setNome(partes[0].trim());
                s.setResponsavel(partes.length > 1 ? partes[1].trim() : "");
                s.setEmail(partes.length > 2 ? partes[2].trim() : "");
                s.setRamal(partes.length > 3 ? partes[3].trim() : "");
                s.setDescricao(partes.length > 4 ? partes[4].trim() : "");
                setores.add(s);
            }
        }
        return setores;
    }

    // Lê o arquivo inteiro e separa em linhas LÓGICAS, respeitando aspas
    private static List<String> parseLinhas(String conteudo) {
        List<String> linhas = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean dentroDeAspas = false;

        for (int i = 0; i < conteudo.length(); i++) {
            char c = conteudo.charAt(i);
            if (c == '"') {
                if (dentroDeAspas && i + 1 < conteudo.length() && conteudo.charAt(i + 1) == '"') {
                    // MUDANÇA: Acrescenta AS DUAS aspas e avança o índice, sem alterar o estado
                    atual.append("\"\"");
                    i++;
                } else {
                    // Alterna o estado e acrescenta a aspa (abertura/fechamento)
                    dentroDeAspas = !dentroDeAspas;
                    atual.append(c);
                }
            } else if ((c == '\n' || c == '\r') && !dentroDeAspas) {
                if (c == '\r' && i + 1 < conteudo.length() && conteudo.charAt(i + 1) == '\n') {
                    i++;
                }
                linhas.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        if (atual.length() > 0) {
            linhas.add(atual.toString());
        }
        return linhas;
    }

    private static String[] parseLinha(String linha, String separador) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean dentroDeAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                if (dentroDeAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"');
                    i++;
                } else {
                    dentroDeAspas = !dentroDeAspas;
                }
            } else if (linha.startsWith(separador, i) && !dentroDeAspas) {
                campos.add(atual.toString());
                atual.setLength(0);
                i += separador.length() - 1;
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos.toArray(new String[0]);
    }
}