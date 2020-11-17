package graphics;

import commands.Commands;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import operations.ClientCommands;
import service.ServerConnection;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    public TextField clientPath;

    @FXML
    public ListView<String> clientView;

    @FXML
    public TextField serverPath;

    @FXML
    public ListView<String> serverView;

    @FXML
    public VBox clientSide;

    @FXML
    public VBox serverSide;

    @FXML
    public Button upload;

    @FXML
    public Button download;

    @FXML
    public Button get;

    @FXML
    public TextArea logArea;

    @FXML
    public Button help;

    private static final ServerConnection connection = ServerConnection.getInstance();
    private static final ClientCommands clientCommands = ClientCommands.getInstance();

    private static final Image back = new Image(new File("Client/src/main/resources/icons/back.png").toURI().toString());
    private static final Image doc = new Image(new File("Client/src/main/resources/icons/doc.png").toURI().toString());
    private static final Image folder = new Image(new File("Client/src/main/resources/icons/folder.png").toURI().toString());
    private static final Image img = new Image(new File("Client/src/main/resources/icons/img.png").toURI().toString());
    private static final Image pdf = new Image(new File("Client/src/main/resources/icons/pdf.png").toURI().toString());
    private static final Image txt = new Image(new File("Client/src/main/resources/icons/txt.png").toURI().toString());
    private static final Image unknown = new Image(new File("Client/src/main/resources/icons/unknown.png").toURI().toString());
    private static final Image zip = new Image(new File("Client/src/main/resources/icons/zip.png").toURI().toString());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setClickActions();
        get.fire();
    }

    private void setClickActions() {

        clientCommands.help();

        upload.setOnAction(click -> {
            if (clientView.getSelectionModel().getSelectedItem() != null && clientView.getSelectionModel().getSelectedItem().contains(".")) {
                clientCommands.uploadFile(clientView.getSelectionModel().getSelectedItem()).forEach(connection::sendMsg);
                clientCommands.log("Upload completed");
                get.fire();
            }
        });

        download.setOnAction(click -> {
            if (serverView.getSelectionModel().getSelectedItem() != null && serverView.getSelectionModel().getSelectedItem().contains(".")) {
                connection.sendMsg(((char) Commands.DOWNLOAD.getBt() + serverView.getSelectionModel().getSelectedItem()).getBytes());
            }
        });

        get.setOnAction(click -> connection.sendMsg(Commands.GET.getBt()));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem mkdir = new MenuItem("Create new folder");
        MenuItem remove = new MenuItem("Remove");
        contextMenu.getItems().addAll(mkdir, remove);
        clientView.setContextMenu(contextMenu);
        serverView.setContextMenu(contextMenu);

        clientView.setOnMouseClicked(click -> {
            download.setDisable(true);
            upload.setDisable(false);
            if (click.getButton() == MouseButton.PRIMARY && click.getClickCount() == 2 && clientView.getSelectionModel().getSelectedItem() != null) {
                clientCommands.change(clientView.getSelectionModel().getSelectedItem());
                clientCommands.clientFiles();
            } else if (click.getButton() == MouseButton.SECONDARY) {
                mkdir.setOnAction(e -> clientMkdir());
                remove.setOnAction(e -> clientRemove());
                contextMenu.show(Window.parent, click.getScreenX(), click.getScreenY());
            }
        });

        serverView.setOnMouseClicked(click -> {
            download.setDisable(false);
            upload.setDisable(true);
            if (click.getButton() == MouseButton.PRIMARY && click.getClickCount() == 2 && serverView.getSelectionModel().getSelectedItem() != null) {
                connection.sendMsg(((char) Commands.CD.getBt() + serverView.getSelectionModel().getSelectedItem()).getBytes());
                connection.sendMsg(Commands.GET.getBt());
            } else if (click.getButton() == MouseButton.SECONDARY) {
                mkdir.setOnAction(e -> srvMkdir());
                remove.setOnAction(e -> srvRemove());
                contextMenu.show(Window.parent, click.getScreenX(), click.getScreenY());
            }
        });

        serverView.setCellFactory(param -> new ListCell<String>() {
            private final ImageView imageView = new ImageView();
            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    imageView.setFitHeight(15);
                    imageView.setFitWidth(15);
                    if (name.equals("..")) {
                        imageView.setImage(back);
                    } else if (!name.contains(".")) {
                        imageView.setImage(folder);
                    } else if (name.matches(".*\\.docx$")) {
                        imageView.setImage(doc);
                    } else if (name.matches(".*\\.(png|jpg|jpeg|gif|tiff|psd)$")) {
                        imageView.setImage(img);
                    } else if (name.matches(".*\\.pdf$")) {
                        imageView.setImage(pdf);
                    } else if (name.matches(".*\\.txt$")) {
                        imageView.setImage(txt);
                    } else if (name.matches(".*\\.(zip|rar)$")) {
                        imageView.setImage(zip);
                    } else {
                        imageView.setImage(unknown);
                    }
                    setText(name);
                    setGraphic(imageView);
                }
            }
        });

        clientView.setCellFactory(param -> new ListCell<String>() {
            private final ImageView imageView = new ImageView();
            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    imageView.setFitHeight(15);
                    imageView.setFitWidth(15);
                    if (name.equals("..")) {
                        imageView.setImage(back);
                    } else if (!name.contains(".")) {
                        imageView.setImage(folder);
                    } else if (name.matches(".*\\.docx$")) {
                        imageView.setImage(doc);
                    } else if (name.matches(".*\\.(png|jpg|jpeg|gif|tiff|psd)$")) {
                        imageView.setImage(img);
                    } else if (name.matches(".*\\.pdf$")) {
                        imageView.setImage(pdf);
                    } else if (name.matches(".*\\.txt$")) {
                        imageView.setImage(txt);
                    } else if (name.matches(".*\\.(zip|rar)$")) {
                        imageView.setImage(zip);
                    } else {
                        imageView.setImage(unknown);
                    }
                    setText(name);
                    setGraphic(imageView);
                }
            }
        });
    }

    private void clientMkdir() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create new directory");
        dialog.setHeaderText("Enter directory name");
        dialog.getDialogPane().setHeader(null);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (name.matches("^[\\w\\s]+$") && clientCommands.mkdir(name)) {
                clientCommands.log("Successfully created");
            } else {
                clientCommands.log("Invalid directory name");
            }
        });
        get.fire();
    }

    private void clientRemove() {
        if (clientView.getSelectionModel().getSelectedItem() != null) {
            if (clientCommands.remove(clientView.getSelectionModel().getSelectedItem())) {
                clientCommands.log("Successfully deleted");
            } else {
                clientCommands.log("Can't delete");
            }
        }
        get.fire();
    }

    private void srvRemove() {
        if (serverView.getSelectionModel().getSelectedItem() != null) {
            connection.sendMsg(((char) Commands.REMOVE.getBt() + serverView.getSelectionModel().getSelectedItem()).getBytes());
        }
        get.fire();
    }

    private void srvMkdir() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create new directory");
        dialog.setHeaderText("Enter directory name");
        dialog.getDialogPane().setHeader(null);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (name.matches("^[\\w\\s]+$")) {
                connection.sendMsg(((char) Commands.MKDIR.getBt() + name).getBytes());
            } else {
                clientCommands.log("Invalid directory name");
            }
        });
        get.fire();
    }
}
