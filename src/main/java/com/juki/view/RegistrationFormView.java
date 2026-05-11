package com.juki.view;

import com.juki.controller.RegistrationFormController;
import com.juki.model.User;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

public class RegistrationFormView {
    private VBox view;
    private RegistrationFormController controller;
    private Consumer<User> onSuccess;

    public RegistrationFormView(Consumer<User> onSuccess) {
        this.controller = new RegistrationFormController();
        this.onSuccess = onSuccess;
        
        view = new VBox(20);
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: #FDF3FF;");

        showSignInPage();
    }
    
    public VBox getView() { return view; }

    private void showSignInPage() {
        view.getChildren().clear();

        Label title = new Label("Sign In");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 40));
        title.setStyle("-fx-text-fill: #8D1395;");

        TextField usernameField = createTextField("Username");
        PasswordField passwordField = createPasswordField("Password");
        
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        Button loginButton = createButton("Masuk");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username dan Password tidak boleh kosong!");
                return;
            }
            
            User user = controller.signIn(username, password);
            if (user != null) {
                onSuccess.accept(user);
            } else {
                errorLabel.setText("Username atau Password salah!");
            }
        });

        Label switchLink = new Label("Belum punya akun? Sign Up di sini.");
        switchLink.setStyle("-fx-text-fill: #8D1395; -fx-cursor: hand; -fx-underline: true;");
        switchLink.setOnMouseClicked(e -> showSignUpPage());

        view.getChildren().addAll(title, usernameField, passwordField, errorLabel, loginButton, switchLink);
    }

    private void showSignUpPage() {
        view.getChildren().clear();

        Label title = new Label("Sign Up");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 40));
        title.setStyle("-fx-text-fill: #8D1395;");

        TextField nameField = createTextField("Nama Lengkap");
        TextField usernameField = createTextField("Username");
        PasswordField passwordField = createPasswordField("Password");
        PasswordField confirmPasswordField = createPasswordField("Masukkan Password Lagi");
        
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        Button registerButton = createButton("Daftar");
        registerButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            
            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Semua field wajib diisi!");
                return;
            }
            if (!password.equals(confirmPassword)) {
                errorLabel.setText("Password tidak cocok!");
                return;
            }
            
            User user = controller.signUp(name, username, password);
            if (user != null) {
                onSuccess.accept(user);
            } else {
                errorLabel.setText("Pendaftaran gagal! Username mungkin sudah terpakai.");
            }
        });

        Label switchLink = new Label("Sudah punya akun? Sign In di sini.");
        switchLink.setStyle("-fx-text-fill: #8D1395; -fx-cursor: hand; -fx-underline: true;");
        switchLink.setOnMouseClicked(e -> showSignInPage());

        view.getChildren().addAll(title, nameField, usernameField, passwordField, confirmPasswordField, errorLabel, registerButton, switchLink);
    }
    
    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setMaxWidth(400);
        field.setStyle("-fx-font-size: 18px; -fx-padding: 12px; -fx-background-radius: 10px;");
        field.setPromptText(prompt);
        return field;
    }

    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setMaxWidth(400);
        field.setStyle("-fx-font-size: 18px; -fx-padding: 12px; -fx-background-radius: 10px;");
        field.setPromptText(prompt);
        return field;
    }
    
    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #8D1395; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10px 50px; -fx-background-radius: 10px; -fx-cursor: hand;");
        return btn;
    }
}