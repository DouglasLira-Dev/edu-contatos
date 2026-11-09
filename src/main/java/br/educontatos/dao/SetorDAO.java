package br.educontatos.dao;

import br.educontatos.model.Setor;
import br.educontatos.util.ConexaoDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) responsável por executar as operações CRUD (Create, Read, Update, Delete)
 * na tabela 'setores' do banco de dados.
 *
 * @author Seu Nome
 * @version 1.0
 */
public class SetorDAO {

    /**
     * Insere um novo setor no banco de dados.
     * @param setor O objeto Setor a ser salvo.
     */
    public void salvar(Setor setor) {
        String sql = "INSERT INTO setores (nome, responsavel, email, ramal, descricao) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, setor.getNome());
            stmt.setString(2, setor.getResponsavel());
            stmt.setString(3, setor.getEmail());
            stmt.setString(4, setor.getRamal());
            stmt.setString(5, setor.getDescricao());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao salvar: " + e.getMessage());
        }
    }

    /**
     * Atualiza os dados de um setor existente no banco.
     * @param setor O objeto Setor com os dados atualizados (deve conter o ID).
     */
    public void atualizar(Setor setor) {
        String sql = "UPDATE setores SET nome=?, responsavel=?, email=?, ramal=?, descricao=? WHERE id=?";
        try (Connection conn = ConexaoDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, setor.getNome());
            stmt.setString(2, setor.getResponsavel());
            stmt.setString(3, setor.getEmail());
            stmt.setString(4, setor.getRamal());
            stmt.setString(5, setor.getDescricao());
            stmt.setInt(6, setor.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar: " + e.getMessage());
        }
    }

    /**
     * Remove um setor do banco de dados com base no seu ID.
     * @param id O ID do setor a ser excluído.
     */
    public void excluir(int id) {
        String sql = "DELETE FROM setores WHERE id=?";
        try (Connection conn = ConexaoDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir: " + e.getMessage());
        }
    }

    /**
     * Lista os setores cadastrados. Pode ser usado para listar todos ou aplicar um filtro de busca.
     *
     * @param filtroBusca Texto a ser pesquisado no nome, responsável ou ramal. Se for vazio, retorna todos.
     * @return Uma lista contendo os setores encontrados.
     */
    public List<Setor> listar(String filtroBusca) {
        List<Setor> lista = new ArrayList<>();
        String sql = "SELECT * FROM setores WHERE nome LIKE ? OR responsavel LIKE ? OR ramal LIKE ?";

        try (Connection conn = ConexaoDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            String termo = "%" + (filtroBusca == null ? "" : filtroBusca.trim()) + "%";
            stmt.setString(1, termo);
            stmt.setString(2, termo);
            stmt.setString(3, termo);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Setor s = new Setor(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("responsavel"),
                    rs.getString("email"),
                    rs.getString("ramal"),
                    rs.getString("descricao")
                );
                lista.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar: " + e.getMessage());
        }
        return lista;
    }
}