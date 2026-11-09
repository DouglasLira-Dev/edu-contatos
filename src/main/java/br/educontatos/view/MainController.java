package br.educontatos.view;

import br.educontatos.dao.SetorDAO;
import br.educontatos.model.Setor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.io.IOException;
import java.util.Optional;

/**
 * Controlador da tela principal.
 * Gerencia os eventos da interface (cliques em botões, digitação na busca) e 
 * conecta a View com o Banco de Dados através do SetorDAO.
 *
 * @autor Douglas Lira
 * @version 1.0
 */
public class MainController {

    @FXML private TextField txtBusca;
    @FXML private TableView<Setor> tabelaContatos;
    @FXML private TableColumn<Setor, String> colNome;
    @FXML private TableColumn<Setor, String> colResponsavel;
    @FXML private TableColumn<Setor, String> colRamal;
    @FXML private TableColumn<Setor, String> colEmail;
    @FXML private TableColumn<Setor, String> colDescricao;

    private final SetorDAO dao = new SetorDAO();

    /**
     * Método chamado automaticamente pelo JavaFX quando a tela abre.
     */
    @FXML
    public void initialize() {
        // Mapeia as colunas da tabela para os atributos da classe Setor
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colResponsavel.setCellValueFactory(new PropertyValueFactory<>("responsavel"));
        colRamal.setCellValueFactory(new PropertyValueFactory<>("ramal"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        // Duplo clique para editar
        tabelaContatos.setRowFactory(tv -> {
            TableRow<Setor> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Setor rowData = row.getItem();
                    abrirJanelaCadastro(rowData);
                }
            });
            return row;
        });

        // Carrega todos os contatos ao abrir a tela
        carregarTabela("");

        // Listener em tempo real: toda vez que o usuário digita, a tabela atualiza
        txtBusca.textProperty().addListener((observable, oldValue, newValue) -> {
            carregarTabela(newValue);
        });
    }

    /**
     * Atualiza os dados da tabela com base no filtro digitado.
     * @param filtro Texto digitado pelo usuário.
     */
    private void carregarTabela(String filtro) {
        try {
            ObservableList<Setor> lista = FXCollections.observableArrayList(dao.listar(filtro));
            tabelaContatos.setItems(lista);
        } catch (SQLException e) {
            mostrarAlerta("Erro no Banco", "Não foi possível carregar os dados: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void abrirCadastro() {
        abrirJanelaCadastro(null); // Passa null para indicar que é um novo cadastro
    }

    @FXML
    public void editarSelecionado() {
        Setor selecionado = tabelaContatos.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            abrirJanelaCadastro(selecionado); // Passa o objeto para edição
        } else {
            mostrarAlerta("Aviso", "Selecione um contato na tabela para editar.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void excluirSelecionado() {
        Setor selecionado = tabelaContatos.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar Exclusão");
            confirm.setHeaderText("Excluir Contato");
            confirm.setContentText("Tem certeza que deseja excluir o setor " + selecionado.getNome() + "?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    dao.excluir(selecionado.getId());
                    carregarTabela(txtBusca.getText());
                } catch (SQLException e) {
                    mostrarAlerta("Erro no Banco", "Não foi possível excluir: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Aviso", "Selecione um contato na tabela para excluir.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Abre a janela de cadastro. Se um setor for passado, entra em modo de edição.
     * @param setor O setor a ser editado ou null para novo.
     */
    private void abrirJanelaCadastro(Setor setor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/educontatos/view/Cadastro.fxml"));
            Parent root = loader.load();

            CadastroController controller = loader.getController();
            controller.setSetorParaEdicao(setor);
            controller.setTelaPrincipal(this);

            Stage stage = new Stage();
            stage.setTitle(setor == null ? "Novo Contato" : "Editar Contato");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloqueia a tela de trás até fechar
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir a tela de cadastro.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Método público chamado pelo CadastroController para atualizar a tabela após salvar.
     */
    public void atualizarTabela() {
        carregarTabela(txtBusca.getText());
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    public void abrirIA() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/educontatos/view/IA.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Assistente Virtual");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}