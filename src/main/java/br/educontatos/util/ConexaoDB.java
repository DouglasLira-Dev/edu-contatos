package br.educontatos.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe utilitária responsável por gerenciar a conexão com o banco de dados SQLite.
 * O banco é salvo em uma pasta oculta na pasta do usuário para não se perder.
 */
public class ConexaoDB {

    // Define a pasta do usuário e o nome do arquivo
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String DB_DIR = USER_HOME + File.separator + ".educontatos";
    private static final String DB_PATH = DB_DIR + File.separator + "educontatos.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    public static Connection conectar() throws SQLException {
        // Cria a pasta oculta se ela não existir
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return DriverManager.getConnection(URL);
    }

    public static void inicializarBanco() {
        String sql = "CREATE TABLE IF NOT EXISTS setores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " + // UNIQUE evita setores duplicados
                "responsavel TEXT, " +
                "email TEXT, " +
                "ramal TEXT, " +
                "descricao TEXT" +
                ");";
        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_setores_nome ON setores(nome COLLATE NOCASE);");
        } catch (SQLException e) {
            throw new RuntimeException("Erro crítico ao inicializar o banco: " + e.getMessage(), e);
        }
    }
}