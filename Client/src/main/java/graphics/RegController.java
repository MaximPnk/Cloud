package graphics;

import commands.Commands;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import service.ServerConnection;

import java.net.URL;
import java.util.ResourceBundle;

public class RegController implements Initializable {

    @FXML
    public TextField login;

    @FXML
    public TextField password;

    @FXML
    public TextField repeatPass;

    @FXML
    public Label info;

    @FXML
    public Button reg;

    @FXML
    public Button auth;

    private static final ServerConnection connection = ServerConnection.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setClickActions();
    }

    private void setClickActions() {
        auth.setOnAction(click -> {
            Window.loginPage();
        });

        reg.setOnAction(click -> {
            if (!password.getText().equals(repeatPass.getText())) {
                info.setText("Passwords are not equals");
            } else if (!login.getText().matches("^[\\w\\p{Po}]+$") || !password.getText().matches("^[\\w\\p{Po}]+$")) {
                info.setText("Bad input values for login or password");
            } else {
                connection.sendMsg(((char) Commands.REG.getBt() + login.getText() + " " + password.getText()).getBytes());
            }
        });
    }
}
