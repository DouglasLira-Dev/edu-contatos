package br.educontatos.view;

import br.educontatos.dao.SetorDAO;
import br.educontatos.model.Setor;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

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

        try {
            // A "IA" aqui busca a pergunta do usuário como filtro no banco de dados
            // Se o usuário digitar "merenda", o DAO vai buscar "merenda" na descrição ou nome.
            List<Setor> resultados = dao.listar(pergunta);

            if (resultados.isEmpty()) {
                txtResposta.setText("Desculpe, não encontrei nenhum setor relacionado a: \"" + pergunta + "\".\nTente usar palavras como 'transporte', 'merenda', 'RH', etc.");
            } else if (resultados.size() == 1) {
                Setor s = resultados.get(0);
                txtResposta.setText("Encontrei! O setor responsável é: \n\n" +
                        "🏢 Setor: " + s.getNome() + "\n" +
                        "👤 Responsável: " + (s.getResponsavel() == null ? "Não informado" : s.getResponsavel()) + "\n" +
                        "📞 Ramal: " + (s.getRamal() == null ? "Não informado" : s.getRamal()) + "\n" +
                        "✉️ E-mail: " + (s.getEmail() == null ? "Não informado" : s.getEmail()) + "\n\n" +
                        "📝 Descrição: " + (s.getDescricao() == null ? "Sem descrição." : s.getDescricao()));
            } else {
                txtResposta.setText("Encontrei vários setores relacionados:\n\n");
                for (Setor s : resultados) {
                    txtResposta.appendText("• " + s.getNome() + " (Ramal: " + s.getRamal() + ")\n");
                }
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao buscar no banco: " + e.getMessage());
            alert.show();
        }
    }
}