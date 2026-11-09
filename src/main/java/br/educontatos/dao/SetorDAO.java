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
 * @author Douglas (Coloque seu nome aqui)
 * @version 1.0
 */
public class SetorDAO {

    /**
     * Insere um novo setor no banco de dados.
     * @param setor O objeto Setor a ser salvo.
     * @throws SQLException se houver erro na gravação.
     */
    public void salvar(Setor setor) throws SQLException {
        String sql = "INSERT INTO setores (nome, responsavel, email, ramal, descricao) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, setor.getNome());
            stmt.setString(2, setor.getResponsavel());
            stmt.setString(3, setor.getEmail());
            stmt.setString(4, setor.getRamal());
            stmt.setString(5, setor.getDescricao());
            stmt.executeUpdate();
        }
    }

    /**
     * Atualiza os dados de um setor existente no banco.
     * @param setor O objeto Setor com os dados atualizados (deve conter o ID).
     * @throws SQLException se houver erro na atualização.
     */
    public void atualizar(Setor setor) throws SQLException {
        String sql = "UPDATE setores SET nome=?, responsavel=?, email=?, ramal=?, descricao=? WHERE id=?";
        try (Connection conn = ConexaoDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, setor.getNome());
            stmt.setString(2, setor.getResponsavel());
            stmt.setString(3, setor.getEmail());
            stmt.setString(4, setor.getRamal());
            stmt.setString(5, setor.getDescricao());
            stmt.setInt(6, setor.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Remove um setor do banco de dados com base no seu ID.
     * @param id O ID do setor a ser excluído.
     * @throws SQLException se houver erro na exclusão.
     */
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM setores WHERE id=?";
        try (Connection conn = ConexaoDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Lista os setores cadastrados aplicando filtro e ordenação.
     *
     * @param filtroBusca Texto a ser pesquisado.
     * @return Uma lista contendo os setores encontrados.
     * @throws SQLException se houver erro na leitura.
     */
    public List<Setor> listar(String filtroBusca) throws SQLException {
        List<Setor> lista = new ArrayList<>();
        // Busca em todas as colunas (inclusive email e descricao) e ordena pelo nome
        String sql = "SELECT * FROM setores WHERE nome LIKE ? OR responsavel LIKE ? OR ramal LIKE ? OR email LIKE ? OR descricao LIKE ? ORDER BY nome ASC";

        try (Connection conn = ConexaoDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            String termo = "%" + (filtroBusca == null ? "" : filtroBusca.trim()) + "%";
            stmt.setString(1, termo);
            stmt.setString(2, termo);
            stmt.setString(3, termo);
            stmt.setString(4, termo);
            stmt.setString(5, termo);

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
        }
        return lista;
    }

    /**
     * Busca setores baseando-se em palavras-chave (para o Assistente).
     */
    public List<Setor> buscarPorPalavrasChave(List<String> palavras) throws SQLException {
        List<Setor> resultados = new ArrayList<>();
        if (palavras.isEmpty()) return resultados;

        // Monta a query dinamicamente: ... WHERE (nome LIKE ? OR descricao LIKE ?) OR (nome LIKE ? OR descricao LIKE ?) ...
        StringBuilder sql = new StringBuilder("SELECT * FROM setores WHERE ");
        for (int i = 0; i < palavras.size(); i++) {
            sql.append("(nome LIKE ? OR descricao LIKE ? OR responsavel LIKE ?)");
            if (i < palavras.size() - 1) sql.append(" OR ");
        }
        sql.append(" ORDER BY nome ASC");

        try (Connection conn = ConexaoDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (String palavra : palavras) {
                String termo = "%" + palavra + "%";
                stmt.setString(paramIndex++, termo);
                stmt.setString(paramIndex++, termo);
                stmt.setString(paramIndex++, termo);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                resultados.add(new Setor(
                    rs.getInt("id"), rs.getString("nome"), rs.getString("responsavel"),
                    rs.getString("email"), rs.getString("ramal"), rs.getString("descricao")
                ));
            }
        }
        return resultados;
    }

    public int importarLote(List<Setor> setores) throws SQLException {
        String sql = "INSERT OR IGNORE INTO setores (nome, responsavel, email, ramal, descricao) VALUES (?, ?, ?, ?, ?)";
        int importados = 0;

        try (Connection conn = ConexaoDB.conectar()) {
            conn.setAutoCommit(false); // Inicia transação
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Setor setor : setores) {
                    stmt.setString(1, setor.getNome());
                    stmt.setString(2, setor.getResponsavel());
                    stmt.setString(3, setor.getEmail());
                    stmt.setString(4, setor.getRamal());
                    stmt.setString(5, setor.getDescricao());
                    stmt.addBatch();
                }
                int[] resultados = stmt.executeBatch();
                for (int r : resultados) {
                    if (r > 0) importados++;
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return importados;
    }
}