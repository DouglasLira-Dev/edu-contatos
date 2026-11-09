package br.educontatos;

import br.educontatos.util.ConexaoDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Classe principal que inicializa a aplicação JavaFX.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        ConexaoDB.inicializarBanco();

        // Caminho ajustado para br/educontatos/view/
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(App.class.getResource("/br/educontatos/view/Main.fxml")));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        stage.setTitle("EduContatos - Secretaria de Educação");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}