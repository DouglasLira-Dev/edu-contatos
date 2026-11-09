package br.educontatos.view;

import br.educontatos.dao.SetorDAO;
import br.educontatos.model.Setor;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador da tela de Cadastro/Edição de contatos.
 * Responsável por validar os dados, salvar ou atualizar no banco.
 *
 * @author Seu Nome
 * @version 1.0
 */
public class CadastroController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNome;
    @FXML private TextField txtResponsavel;
    @FXML private TextField txtRamal;
    @FXML private TextField txtEmail;

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
        }
    }

    @FXML
    public void salvar() {
        // Validação simples: Nome do setor é obrigatório
        if (txtNome.getText().trim().isEmpty()) {
            mostrarAlerta("Erro de Validação", "O nome do setor é obrigatório!", Alert.AlertType.ERROR);
            return;
        }

        // Cria o objeto com os dados digitados
        Setor setor = new Setor();
        setor.setNome(txtNome.getText().trim());
        setor.setResponsavel(txtResponsavel.getText().trim());
        setor.setRamal(txtRamal.getText().trim());
        setor.setEmail(txtEmail.getText().trim());

        // Decide se é INSERT (novo) ou UPDATE (edição)
        if (setorEdicao == null) {
            dao.salvar(setor);
        } else {
            setor.setId(setorEdicao.getId());
            dao.atualizar(setor);
        }

        // Atualiza a tabela da tela principal
        if (telaPrincipal != null) {
            telaPrincipal.atualizarTabela();
        }

        // Fecha a janela de cadastro
        fecharJanela();
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