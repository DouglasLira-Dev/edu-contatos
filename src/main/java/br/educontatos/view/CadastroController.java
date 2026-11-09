package br.educontatos.view;

import br.educontatos.dao.SetorDAO;
import br.educontatos.model.Setor;

import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Controlador da tela de Cadastro/Edição de contatos.
 * Responsável por validar os dados, salvar ou atualizar no banco.
 *
 * @author Douglas Lira
 * @version 1.0
 */
public class CadastroController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNome;
    @FXML private TextField txtResponsavel;
    @FXML private TextField txtRamal;
    @FXML private TextField txtEmail;
    @FXML private TextArea txtDescricao;

    private final SetorDAO dao = new SetorDAO();
    private Setor setorEdicao = null;
    private MainController telaPrincipal;

    public void setTelaPrincipal(MainController telaPrincipal) {
        this.telaPrincipal = telaPrincipal;
    }

    /**
     * Preenche os campos da tela caso seja uma edição.
     * @param setor O setor a ser editado (null se for novo).
     */
    public void setSetorParaEdicao(Setor setor) {
        this.setorEdicao = setor;
        if (setor != null) {
            lblTitulo.setText("Editar Contato");
            txtNome.setText(setor.getNome());
            txtResponsavel.setText(setor.getResponsavel());
            txtRamal.setText(setor.getRamal());
            txtEmail.setText(setor.getEmail());
            txtDescricao.setText(setor.getDescricao());
        }
    }

    @FXML
    public void salvar() {
        if (txtNome.getText().trim().isEmpty()) {
            mostrarAlerta("Erro de Validação", "O nome do setor é obrigatório!", Alert.AlertType.ERROR);
            return;
        }

        Setor setor = new Setor();
        setor.setNome(txtNome.getText().trim());
        setor.setResponsavel(txtResponsavel.getText().trim());
        setor.setRamal(txtRamal.getText().trim());
        setor.setEmail(txtEmail.getText().trim());
        setor.setDescricao(txtDescricao.getText().trim());

        try {
            if (setorEdicao == null) {
                dao.salvar(setor);
            } else {
                setor.setId(setorEdicao.getId());
                dao.atualizar(setor);
            }

            if (telaPrincipal != null) {
                telaPrincipal.atualizarTabela();
            }
            fecharJanela();

        } catch (SQLException e) {
            // Se o erro for por causa do UNIQUE (nome duplicado)
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                mostrarAlerta("Erro", "Já existe um setor cadastrado com esse nome!", Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Erro no Banco", "Não foi possível salvar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void cancelar() {
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) txtNome.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}