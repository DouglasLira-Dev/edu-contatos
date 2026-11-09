package br.educontatos.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe utilitária responsável por gerenciar a conexão com o banco de dados SQLite.
 * Ela cria o arquivo do banco e a tabela na primeira execução.
 *
 * @author Seu Nome
 * @version 1.0
 */
public class ConexaoDB {
    // O banco será salvo na raiz do projeto com o nome educontatos.db
    private static final String URL = "jdbc:sqlite:educontatos.db";

    /**
     * Estabelece e retorna uma conexão com o banco de dados SQLite.
     * @return Connection objeto de conexão.
     * @throws SQLException caso ocorra erro na conexão.
     */
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Verifica se a tabela 'setores' existe. Se não existir, ela é criada.
     * Este método deve ser chamado ao iniciar a aplicação.
     */
    public static void inicializarBanco() {
        String sql = "CREATE TABLE IF NOT EXISTS setores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "responsavel TEXT, " +
                "email TEXT, " +
                "ramal TEXT" +
                ");";
        // O bloco try-with-resources garante que a conexão e o statement sejam fechados automaticamente
        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar banco: " + e.getMessage());
        }
    }
}