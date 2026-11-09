package br.educontatos.util;

import br.educontatos.model.Setor;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CsvSetores {

    // Exporta com BOM UTF-8 e separador ';' (compatível com Excel PT-BR)
    public static void exportar(File file, List<Setor> setores) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF'); // BOM para o Excel reconhecer UTF-8
            writer.write("nome;responsavel;email;ramal;descricao\n");

            for (Setor s : setores) {
                writer.write(formatarCampo(s.getNome()) + ";");
                writer.write(formatarCampo(s.getResponsavel()) + ";");
                writer.write(formatarCampo(s.getEmail()) + ";");
                writer.write(formatarCampo(s.getRamal()) + ";");
                writer.write(formatarCampo(s.getDescricao()) + "\n");
            }
        }
    }

    // Coloca aspas se o campo tiver separador, aspas ou quebra de linha
    private static String formatarCampo(String valor) {
        if (valor == null) return "";
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    // Importa detectando separador (; ou ,) e encoding (UTF-8 ou Windows-1252)
    public static List<Setor> importar(File file) throws IOException {
        List<Setor> setores = new ArrayList<>();
        Charset encoding = StandardCharsets.UTF_8;

        // Tenta ler a primeira linha para detectar encoding (simplificado)
        try (BufferedReader testReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String primeiraLinha = testReader.readLine();
            if (primeiraLinha != null && primeiraLinha.contains("Ã")) { // Sinal comum de UTF-8 lido errado
                encoding = Charset.forName("Windows-1252");
            }
        } catch (Exception ignored) {}

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            String linha;
            boolean primeiraLinha = true;
            String separador = ";";
            int numColunas = 5; // Padrão

            while ((linha = reader.readLine()) != null) {
                if (linha.startsWith("\uFEFF")) linha = linha.substring(1); // Tira BOM
                if (linha.trim().isEmpty()) continue;

                if (primeiraLinha) {
                    // Detecta separador pela linha de cabeçalho
                    if (linha.contains(";")) separador = ";";
                    else if (linha.contains(",")) separador = ",";

                    // Conta colunas aproximadas
                    String[] header = parseLinha(linha, separador);
                    numColunas = header.length;
                    primeiraLinha = false;
                    continue;
                }

                String[] partes = parseLinha(linha, separador);
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
        }
        return setores;
    }

    // Parser simples que respeita aspas
    private static String[] parseLinha(String linha, String separador) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean dentroDeAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                if (dentroDeAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"'); // Aspas duplas escapadas
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