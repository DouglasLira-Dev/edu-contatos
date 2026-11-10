package br.educontatos.view;

import br.educontatos.dao.SetorDAO;
import br.educontatos.model.Setor;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador da tela principal.
 * Gerencia os eventos da interface (cliques em botões, digitação na busca) e 
 * conecta a View com o Banco de Dados através do SetorDAO.
 *
 * @author Douglas Lira
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
    private List<Setor> todosOsSetores = new ArrayList<>();

    @FXML
    public void initialize() {
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
                    abrirJanelaCadastro(row.getItem());
                }
            });
            return row;
        });

        // Carrega os dados iniciais
        carregarTabela("");

        // Listener de busca instantânea (agora filtra na memória, sem travar o banco)
        txtBusca.textProperty().addListener((observable, oldValue, newValue) -> {
            carregarTabela(newValue);
        });
    }

    private void carregarTabela(String filtro) {
        try {
            // Carrega do banco apenas se a lista estiver vazia
            if (todosOsSetores.isEmpty()) {
                todosOsSetores = dao.listar("");
            }

            if (filtro == null || filtro.trim().isEmpty()) {
                tabelaContatos.setItems(FXCollections.observableArrayList(todosOsSetores));
                return;
            }

            // Filtro em memória ignorando acentos e maiúsculas
            String filtroNormalizado = normalizar(filtro);
            List<Setor> filtrados = todosOsSetores.stream()
                    .filter(s -> normalizar(s.getNome()).contains(filtroNormalizado) ||
                            normalizar(s.getResponsavel()).contains(filtroNormalizado) ||
                            normalizar(s.getRamal()).contains(filtroNormalizado) ||
                            normalizar(s.getEmail()).contains(filtroNormalizado) ||
                            normalizar(s.getDescricao()).contains(filtroNormalizado))
                    .toList();

            tabelaContatos.setItems(FXCollections.observableArrayList(filtrados));

        } catch (SQLException e) {
            mostrarAlerta("Erro no Banco", "Não foi possível carregar os dados: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Função utilitária para remover acentos
    private String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    @FXML
    public void abrirCadastro() {
        abrirJanelaCadastro(null);
    }

    @FXML
    public void editarSelecionado() {
        Setor selecionado = tabelaContatos.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            abrirJanelaCadastro(selecionado);
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
                    todosOsSetores.clear(); // Limpa a memória para recarregar
                    carregarTabela(txtBusca.getText());
                } catch (SQLException e) {
                    mostrarAlerta("Erro no Banco", "Não foi possível excluir: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Aviso", "Selecione um contato na tabela para excluir.", Alert.AlertType.WARNING);
        }
    }

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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            mostrarAlerta("Erro", "Não foi possível abrir a tela de cadastro.", Alert.AlertType.ERROR);
        }
    }

    public void atualizarTabela() {
        todosOsSetores.clear(); // Limpa a memória para buscar os dados novos do banco
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
            stage.setTitle("Busca por Assunto");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Erro", "Não foi possível abrir a tela de busca.", Alert.AlertType.ERROR);
        }
    }

    // MÉTODOS DE EXPORTAÇÃO E IMPORTAÇÃO CSV

        @FXML
    public void exportarCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar CSV");
        fileChooser.setInitialFileName("contatos_educontatos.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(tabelaContatos.getScene().getWindow());

        if (file != null) {
            try {
                br.educontatos.util.CsvSetores.exportar(file, todosOsSetores);
                mostrarAlerta("Sucesso", "Arquivo salvo em:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                mostrarAlerta("Erro", "Erro ao exportar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void importarCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fileChooser.showOpenDialog(tabelaContatos.getScene().getWindow());

        if (file != null) {
            try {
                List<Setor> importadosLista = br.educontatos.util.CsvSetores.importar(file);

                // Filtra duplicados (contra o banco e contra o próprio arquivo)
                List<Setor> paraInserir = new ArrayList<>();
                List<String> nomesExistentes = new ArrayList<>();

                // Carrega nomes do banco
                for (Setor s : dao.listar("")) {
                    nomesExistentes.add(br.educontatos.util.Normalizador.normalizar(s.getNome()));
                }

                int ignoradosPorDuplicidade = 0;
                for (Setor s : importadosLista) {
                    String nomeNorm = br.educontatos.util.Normalizador.normalizar(s.getNome());
                    if (nomesExistentes.contains(nomeNorm)) {
                        ignoradosPorDuplicidade++;
                    } else {
                        nomesExistentes.add(nomeNorm);
                        paraInserir.add(s);
                    }
                }

                int importados = dao.importarLote(paraInserir);
                atualizarTabela();

                mostrarAlerta("Importação Concluída", 
                    importados + " contatos importados.\n" +
                    ignoradosPorDuplicidade + " foram ignorados (já existiam no banco ou no arquivo).\n\n" +
                    "Nota: Contatos existentes não são atualizados.", 
                    Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                mostrarAlerta("Erro", "Erro ao importar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
}