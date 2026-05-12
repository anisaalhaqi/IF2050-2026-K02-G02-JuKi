package com.juki.view;

import com.juki.controller.RegistrationFormController;
import com.juki.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

public class RegistrationFormView {
    private StackPane view;
    private VBox card;
    private RegistrationFormController controller;
    private Consumer<User> onSuccess;

    // Tambahkan field untuk menyimpan reference input
    private TextField siUsernameField;
    private TextField siPasswordField; // Bisa PasswordField atau TextField biasa untuk visibility

    private TextField suFullNameField;
    private TextField suUsernameField;
    private TextField suPasswordField;
    private TextField suConfirmPasswordField;

    public RegistrationFormView(Consumer<User> onSuccess) {
        this.controller = new RegistrationFormController();
        this.onSuccess = onSuccess;
        
        view = new StackPane();
        view.setStyle("-fx-background-color: #F3A7FF;");
        view.setAlignment(Pos.CENTER);

        card = new VBox();
        card.setMaxWidth(536); // 480 + 28*2
        card.setMaxHeight(VBox.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 50px; -fx-padding: 72px 28px;");
        
        view.getChildren().add(card);

        showSignInPage();
    }
    
    public StackPane getView() { return view; }

    private void showSignInPage() {
        card.getChildren().clear();
        card.setSpacing(64);

        Label title = new Label("Selamat Datang!");
        title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        title.setTextFill(Color.BLACK);

        VBox formContainer = new VBox(32);
        formContainer.setAlignment(Pos.CENTER);

        VBox fieldsBox = new VBox(16);
        fieldsBox.setPrefWidth(480);

        VBox nameBox = createFieldGroup("Username", "Masukkan namamu", false);
        siUsernameField = (TextField) nameBox.getUserData();

        VBox passwordBox = createFieldGroup("Password", "Masukkan password", true);
        siPasswordField = (TextField) passwordBox.getUserData();

        fieldsBox.getChildren().addAll(nameBox, passwordBox);

        VBox actionsBox = new VBox(16);
        actionsBox.setPrefWidth(480);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font("Outfit", 12));

        Button loginButton = createPrimaryButton("Masuk");
        loginButton.setOnAction(e -> {
            String username = siUsernameField.getText().trim();
            String password = siPasswordField.getText();
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

        HBox switchBox = createSwitchBox("Belum punya akun?", "Daftar");
        switchBox.getChildren().get(1).setOnMouseClicked(e -> showSignUpPage());

        actionsBox.getChildren().addAll(loginButton, switchBox, errorLabel);
        
        formContainer.getChildren().addAll(fieldsBox, actionsBox);
        card.getChildren().addAll(title, formContainer);
    }

    private void showSignUpPage() {
        card.getChildren().clear();
        card.setSpacing(64);

        Label title = new Label("Daftar Akun");
        title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        title.setTextFill(Color.BLACK);

        VBox formContainer = new VBox(32);
        formContainer.setAlignment(Pos.CENTER);

        VBox fieldsBox = new VBox(16);
        fieldsBox.setPrefWidth(480);

        VBox fullNameBox = createFieldGroup("Nama Lengkap", "Masukkan nama lengkapmu", false);
        suFullNameField = (TextField) fullNameBox.getUserData();

        VBox usernameBox = createFieldGroup("Username", "Pilih username", false);
        suUsernameField = (TextField) usernameBox.getUserData();

        VBox passwordBox = createFieldGroup("Password", "Masukkan password", true);
        suPasswordField = (TextField) passwordBox.getUserData();

        Label passwordErrorLabel = new Label("Password minimal 8 karakter, serta mengandung huruf dan angka");
        passwordErrorLabel.setFont(Font.font("Outfit", 15));
        passwordErrorLabel.setTextFill(Color.web("#DC2626"));
        passwordErrorLabel.setWrapText(true);
        passwordErrorLabel.setPrefWidth(480);
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);

        VBox confirmBox = createFieldGroup("Konfirmasi Password", "Ulangi password", true);
        suConfirmPasswordField = (TextField) confirmBox.getUserData();

        Label confirmErrorLabel = new Label("Password tidak cocok!");
        confirmErrorLabel.setFont(Font.font("Outfit", 15));
        confirmErrorLabel.setTextFill(Color.web("#DC2626"));
        confirmErrorLabel.setVisible(false);
        confirmErrorLabel.setManaged(false);

        fieldsBox.getChildren().addAll(fullNameBox, usernameBox, passwordBox, passwordErrorLabel, confirmBox, confirmErrorLabel);

        VBox actionsBox = new VBox(16);
        actionsBox.setPrefWidth(480);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font("Outfit", 12));

        Button registerButton = createPrimaryButton("Daftar");
        registerButton.setOnAction(e -> {
            String name = suFullNameField.getText().trim();
            String username = suUsernameField.getText().trim();
            String password = suPasswordField.getText();
            String confirmPassword = suConfirmPasswordField.getText();
            
            passwordErrorLabel.setVisible(false);
            passwordErrorLabel.setManaged(false);
            confirmErrorLabel.setVisible(false);
            confirmErrorLabel.setManaged(false);
            errorLabel.setText("");

            if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                errorLabel.setText("Semua field wajib diisi!");
                return;
            }

            boolean hasError = false;

            if (!isValidPassword(password)) {
                passwordErrorLabel.setVisible(true);
                passwordErrorLabel.setManaged(true);
                hasError = true;
            }

            if (!password.equals(confirmPassword)) {
                confirmErrorLabel.setVisible(true);
                confirmErrorLabel.setManaged(true);
                hasError = true;
            }

            if (hasError) return;
            
            User user = controller.signUp(name, username, password);
            if (user != null) {
                onSuccess.accept(user);
            } else {
                errorLabel.setText("Pendaftaran gagal! Username mungkin sudah terpakai.");
            }
        });

        HBox switchBox = createSwitchBox("Sudah punya akun?", "Masuk");
        switchBox.getChildren().get(1).setOnMouseClicked(e -> showSignInPage());

        actionsBox.getChildren().addAll(registerButton, switchBox, errorLabel);

        formContainer.getChildren().addAll(fieldsBox, actionsBox);
        card.getChildren().addAll(title, formContainer);
    }
    
    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        boolean hasLetter = false;
        boolean hasDigitOrSymbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c) || !Character.isLetterOrDigit(c)) hasDigitOrSymbol = true;
            if (hasLetter && hasDigitOrSymbol) return true;
        }
        return false;
    }

    private VBox createFieldGroup(String labelText, String prompt, boolean isPassword) {
        VBox group = new VBox(16);
        
        Label label = new Label(labelText);
        label.setFont(Font.font("Outfit", FontWeight.NORMAL, 20));
        label.setTextFill(Color.BLACK);
        
        StackPane fieldContainer = new StackPane();
        fieldContainer.setPrefWidth(480);
        fieldContainer.setPrefHeight(40);

        TextField textField = new TextField();
        PasswordField passwordField = new PasswordField();
        
        String commonStyle = "-fx-background-color: white; " +
                             "-fx-border-color: rgba(0, 0, 0, 0.2); " +
                             "-fx-border-radius: 10px; " +
                             "-fx-background-radius: 10px; " +
                             "-fx-padding: 0 45px 0 16px; " + // Right padding for icon
                             "-fx-font-family: 'Outfit'; " +
                             "-fx-font-size: 15px; " +
                             "-fx-prompt-text-fill: #A5A5A5;";

        textField.setStyle(commonStyle);
        passwordField.setStyle(commonStyle);
        
        textField.setPrefWidth(480);
        textField.setMinWidth(480);
        textField.setPrefHeight(40);
        textField.setMinHeight(40);

        passwordField.setPrefWidth(480);
        passwordField.setMinWidth(480);
        passwordField.setPrefHeight(40);
        passwordField.setMinHeight(40);
        
        textField.setPromptText(prompt);
        passwordField.setPromptText(prompt);

        if (isPassword) {
            textField.setVisible(false);
            passwordField.setVisible(true);

            // Icon for toggle
            ImageView eyeIcon = new ImageView();
            try {
                eyeIcon.setImage(new Image("file:img/icons/eye_hide.png"));
                eyeIcon.setFitWidth(24);
                eyeIcon.setFitHeight(24);
            } catch (Exception e) {
                System.err.println("Could not load eye icon: " + e.getMessage());
            }

            Button toggleBtn = new Button();
            toggleBtn.setGraphic(eyeIcon);
            toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            StackPane.setAlignment(toggleBtn, Pos.CENTER_RIGHT);
            StackPane.setMargin(toggleBtn, new Insets(0, 10, 0, 0));

            toggleBtn.setOnAction(e -> {
                if (passwordField.isVisible()) {
                    textField.setText(passwordField.getText());
                    passwordField.setVisible(false);
                    textField.setVisible(true);
                    try {
                        eyeIcon.setImage(new Image("file:img/icons/eye_show.png"));
                    } catch (Exception ex) {}
                } else {
                    passwordField.setText(textField.getText());
                    textField.setVisible(false);
                    passwordField.setVisible(true);
                    try {
                        eyeIcon.setImage(new Image("file:img/icons/eye_hide.png"));
                    } catch (Exception ex) {}
                }
            });

            // Sync text back and forth for submission
            textField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (textField.isVisible()) passwordField.setText(newVal);
            });
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (passwordField.isVisible()) textField.setText(newVal);
            });

            fieldContainer.getChildren().addAll(textField, passwordField, toggleBtn);
            group.setUserData(passwordField); // Return the active field reference (both synced)
        } else {
            textField.setStyle(commonStyle.replace("0 45px 0 16px", "0 16px 0 16px"));
            fieldContainer.getChildren().add(textField);
            group.setUserData(textField);
        }
        
        group.getChildren().addAll(label, fieldContainer);
        return group;
    }
    
    private Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(480);
        btn.setPrefHeight(40);
        btn.setStyle("-fx-background-color: #8D1395; " +
                     "-fx-text-fill: white; " +
                     "-fx-font-family: 'Outfit'; " +
                     "-fx-font-size: 20px; " +
                     "-fx-background-radius: 100px; " +
                     "-fx-cursor: hand;");
        return btn;
    }

    private HBox createSwitchBox(String text, String linkText) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label(text);
        label.setFont(Font.font("Outfit", 15));
        label.setTextFill(Color.web("#A5A5A5"));
        
        Label link = new Label(linkText);
        link.setFont(Font.font("Outfit", FontWeight.MEDIUM, 15));
        link.setTextFill(Color.web("#8D1395"));
        link.setStyle("-fx-cursor: hand;");
        
        box.getChildren().addAll(label, link);
        return box;
    }
}