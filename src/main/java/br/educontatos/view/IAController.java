package br.educontatos.view;

import br.educontatos.dao.SetorDAO;
import br.educontatos.model.Setor;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class IAController {

    @FXML private TextField txtPergunta;
    @FXML private TextArea txtResposta;

    private final SetorDAO dao = new SetorDAO();

    @FXML
    public void buscarResposta() {
        String pergunta = txtPergunta.getText().trim().toLowerCase();

        if (pergunta.isEmpty()) {
            txtResposta.setText("Por favor, digite uma pergunta.");
            return;
        }

        // 1. Limpa a frase: tira pontuação e separa em palavras
        pergunta = pergunta.replaceAll("[?,.!]", ""); 
        String[] palavrasArray = pergunta.split(" ");

        // 2. Remove as "stop words" (palavras que não ajudam na busca)
        List<String> stopWords = List.of("quem", "cuida", "da", "de", "do", "o", "a", "os", "as", "um", "uma", "e", "setor", "escolar", "falar", "com", "preciso");
        List<String> palavrasChave = new ArrayList<>();

        for (String p : palavrasArray) {
            if (!stopWords.contains(p) && p.length() > 2) {
                palavrasChave.add(p);
            }
        }

        if (palavrasChave.isEmpty()) {
            txtResposta.setText("Não consegui entender. Tente digitar apenas a palavra-chave (ex: merenda, transporte).");
            return;
        }

        try {
            // 3. Busca no banco
            List<Setor> resultados = dao.buscarPorPalavrasChave(palavrasChave);

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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro na busca: " + e.getMessage());
            alert.show();
        }
    }
}