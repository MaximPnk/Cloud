package graphics;

import commands.Commands;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import service.ServerConnection;

import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {

    @FXML
    public TextField login;

    @FXML
    public TextField password;

    @FXML
    public Button authenticate;

    @FXML
    public Button registration;

    @FXML
    public Label info;

    private static final ServerConnection connection = ServerConnection.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setClickActions();
    }

    private void setClickActions() {
        registration.setOnAction(click -> {
            Window.regPage();
        });
    }

    public void auth(ActionEvent actionEvent) {
        if (login.getText().matches("^[\\w\\p{Po}]+$") && password.getText().matches("^[\\w\\p{Po}]+$")) {
            connection.sendMsg(((char) Commands.AUTH.getBt() + login.getText() + " " + password.getText()).getBytes());
        }
    }
}
