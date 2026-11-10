package br.educontatos.view;

import br.educontatos.dao.SetorDAO;
import br.educontatos.model.Setor;
import br.educontatos.util.BuscaSetores;
import br.educontatos.util.Normalizador;
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
 * aplicando a mesma normalização da tela principal.
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

        // Normaliza e separa em palavras-chave
        String perguntaNormalizada = Normalizador.normalizar(pergunta).replaceAll("[?,.!]", "");
        String[] palavrasArray = perguntaNormalizada.split(" ");

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
            // Carrega todos os setores e usa a classe BuscaSetores unificada
            List<Setor> todos = dao.listar("");
            List<Setor> resultados = BuscaSetores.filtrar(todos, String.join(" ", palavrasChave));

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