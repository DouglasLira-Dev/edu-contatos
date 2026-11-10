package br.educontatos.view;

import br.educontatos.dao.SetorDAO;
import br.educontatos.model.Setor;
import br.educontatos.util.Normalizador; // Importando a classe utilitária
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador da tela de Busca por Assunto.
 * Interpreta a pergunta do usuário e busca setores por palavras-chave,
 * aplicando a mesma normalização (sem acentos) da tela principal.
 *
 * @author Douglas Lira
 * @version 1.0
 */
public class IAController {

    @FXML private TextField txtPergunta;
    @FXML private TextArea txtResposta;

    private final SetorDAO dao = new SetorDAO();

    @FXML
    public void buscarResposta() {
        String pergunta = txtPergunta.getText().trim();

        if (pergunta.isEmpty()) {
            txtResposta.setText("Por favor, digite uma pergunta.");
            return;
        }

        // 1. Normaliza a pergunta (tira acentos, pontuação) e separa em palavras
        String perguntaNormalizada = Normalizador.normalizar(pergunta).replaceAll("[?,.!]", "");
        String[] palavrasArray = perguntaNormalizada.split(" ");

        // 2. Remove as "stop words" (apenas palavras gramaticais)
        List<String> stopWords = List.of("quem", "cuida", "da", "de", "do", "o", "a", "os", "as", "um", "uma", "e", "para", "com", "preciso", "falar", "sobre");
        List<String> palavrasChave = new ArrayList<>();

        for (String p : palavrasArray) {
            if (!stopWords.contains(p) && p.length() >= 2) {
                palavrasChave.add(p);
            }
        }

        if (palavrasChave.isEmpty()) {
            txtResposta.setText("Não consegui entender. Tente digitar apenas a palavra-chave (ex: RH, merenda, transporte).");
            return;
        }

        try {
            // 3. Carrega todos os setores do banco e filtra em memória
            List<Setor> todos = dao.listar("");
            List<Setor> resultados = new ArrayList<>();

            for (Setor s : todos) {
                // Concatena todos os campos e normaliza
                String textoSetor = Normalizador.normalizar(
                        s.getNome() + " " + s.getResponsavel() + " " + 
                        s.getEmail() + " " + s.getRamal() + " " + s.getDescricao());

                // Verifica se TODAS as palavras-chave estão presentes no texto do setor
                boolean todosOsTermosPresentes = true;
                for (String palavra : palavrasChave) {
                    if (!textoSetor.contains(palavra)) {
                        todosOsTermosPresentes = false;
                        break;
                    }
                }

                if (todosOsTermosPresentes) {
                    resultados.add(s);
                }
            }

            if (resultados.isEmpty()) {
                txtResposta.setText("Desculpe, não encontrei nenhum setor relacionado a: " + String.join(", ", palavrasChave));
            } else {
                txtResposta.setText("Encontrei os seguintes setores relacionados:\n\n");
                for (Setor s : resultados) {
                    txtResposta.appendText("🏢 " + s.getNome() + "\n");
                    txtResposta.appendText("👤 Responsável: " + (s.getResponsavel() == null || s.getResponsavel().isBlank() ? "Não informado" : s.getResponsavel()) + "\n");
                    txtResposta.appendText("📞 Ramal: " + (s.getRamal() == null || s.getRamal().isBlank() ? "Não informado" : s.getRamal()) + "\n");
                    txtResposta.appendText("✉️ E-mail: " + (s.getEmail() == null || s.getEmail().isBlank() ? "Não informado" : s.getEmail()) + "\n");
                    txtResposta.appendText("----------------------------------------\n");
                }
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro na busca: " + e.getMessage());
            alert.showAndWait();
        }
    }
}