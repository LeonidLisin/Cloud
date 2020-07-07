import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController implements Initializable {
    @FXML
    ListView<String> fileList;

    @FXML
    Button downloadButton, deleteButton, registerButton, authorizeButton,
            mkDirButton, renameButton;

    @FXML
    TextField regName, regPassword, authName, authPassword;

    @FXML
    TextArea console;

    @FXML
    VBox mainButtonsPanel, authPanel;

    private ServerHandler serverHandler;
    private ExecutorService es;
    private String name;
    private boolean isAuthorized;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeDragAndDrop();
        mainButtonsPanel.setVisible(false);
        mainButtonsPanel.setManaged(false);
        fileList.setVisible(false);
        fileList.setManaged(false);
        this.es = Executors.newFixedThreadPool(3);
        this.serverHandler = new ServerHandler();
    }

    private void initializeDragAndDrop(){
        fileList.setOnDragOver(event -> {
            if (event.getGestureSource() != fileList && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        fileList.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    uploadFile(file);
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    void refreshFileList(ArrayList<String> dirContent){
        Platform.runLater(() -> {
            fileList.getItems().clear();
            fileList.getItems().add("..");

            for (int i = 0; i < dirContent.size(); i++) {
                if (dirContent.get(i).startsWith("[")) {
                    fileList.getItems().add(dirContent.get(i));
                } else {
                    fileList.getItems().add(dirContent.get(i));
                }
            }
        });
    }

    private void hideRegAuthPanels(Connection connection){
        authPanel.setVisible(false);
        authPanel.setManaged(false);
        fileList.setVisible(true);
        fileList.setManaged(true);
        mainButtonsPanel.setVisible(true);
        mainButtonsPanel.setManaged(true);
        try {
            refreshFileList(serverHandler.recieveDirContent(connection));
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadFile() {
        String fileName = fileList.getSelectionModel().getSelectedItem();
        if (fileName != null) {
            es.execute(() -> {
                try {
                    Connection connection = new Connection();
                    serverHandler.sendHandShake(connection, name);
                    toConsole("downloading " + fileName);
                    serverHandler.downloadFile(connection, fileName);
                    toConsole(serverHandler.recieveResult(connection.getIn()));
                    toConsole(fileName + " downloaded");
                    connection.close();
                } catch (IOException e) {
                    System.out.println("server disconnect or file error");
                    e.printStackTrace();
                }
            });
        }
    }

    private void uploadFile(File file){
        es.execute(() -> {
            try {
                Connection connection = new Connection();
                serverHandler.sendHandShake(connection, name);
                serverHandler.uploadFile(connection, file);
                refreshFileList(serverHandler.recieveDirContent(connection));
                toConsole(serverHandler.recieveResult(connection.getIn()));
                connection.close();
            } catch (IOException e) {
                System.out.println("server disconnect or file error");
                e.printStackTrace();
            }
        });
    }

    public void changeDir(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2 && mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            String dir = fileList.getSelectionModel().getSelectedItems().toString();
            Connection connection = new Connection();
            serverHandler.sendHandShake(connection, name);
            if (dir.startsWith("[[")) {
                serverHandler.changeDir(connection, dir.substring(2, dir.length() - 2));
                refreshFileList(serverHandler.recieveDirContent(connection));
            }
            if (dir.equals("[..]")) {
                serverHandler.changeDir(connection,"..");
                refreshFileList(serverHandler.recieveDirContent(connection));
            }
            connection.close();
        }
    }

    public void makeDirManually() throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/makeDirWindow.fxml"));
        Parent root = loader.load();
        MakeDirController mdc = loader.getController();
        mdc.setServerHandler(serverHandler);
        mdc.setName(name);
        mdc.setMc(this);
        stage.setTitle("Enter dir name");
        stage.setScene(new Scene(root, 300, 100));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public void rename() throws IOException {
        String fileOrDirName = fileList.getSelectionModel().getSelectedItems().toString();
        if (fileOrDirName.startsWith("[["))
            fileOrDirName = fileOrDirName.substring(2, fileOrDirName.length()-2);
        else
            fileOrDirName = fileOrDirName.substring(1, fileOrDirName.length()-1);
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/renameWindow.fxml"));
        Parent root = loader.load();
        RenameController rc = loader.getController();
        rc.setMc(this);
        rc.setServerHandler(serverHandler);
        rc.setClientName(name);
        rc.setOldFileOrDirName(fileOrDirName);
        stage.setTitle("Enter new name");
        stage.setScene(new Scene(root, 300, 100));
        stage.initModality(Modality.APPLICATION_MODAL);
        rc.getRenameField().setText(fileOrDirName);
        stage.showAndWait();
    }

    public void delete() throws IOException {
        String fileOrDirName = fileList.getSelectionModel().getSelectedItems().toString();
        Connection connection = new Connection();
        serverHandler.sendHandShake(connection, name);
        serverHandler.deleteFileOrDir(connection, fileOrDirName.substring(1, fileOrDirName.length()-1));
        refreshFileList(serverHandler.recieveDirContent(connection));
        toConsole(serverHandler.recieveResult(connection.getIn()));
        connection.close();
    }

    public void tryToReg() throws IOException {
        if(!regName.getText().equals("") &&
                !regPassword.getText().equals("")){
            Connection connection = new Connection();
            serverHandler.tryToReg(connection, regName.getText(), regPassword.getText());
            if (serverHandler.recieveAuthData(connection.getIn()))
                setClientAuthorized(regName.getText());
            regName.clear();
            regPassword.clear();
            toConsole(serverHandler.recieveResult(connection.getIn()));
            if (isAuthorized){
                hideRegAuthPanels(connection);
            }
        }
    }

    public void tryToAuth() throws IOException {
        if(!authName.getText().equals("") &&
                !authPassword.getText().equals("")){
            Connection connection = new Connection();
            serverHandler.tryToAuth(connection, authName.getText(), authPassword.getText());
            if (serverHandler.recieveAuthData(connection.getIn()))
                setClientAuthorized(authName.getText());
            authName.clear();
            authPassword.clear();
            toConsole(serverHandler.recieveResult(connection.getIn()));
            if (isAuthorized) {
                System.out.println("you're authorized!");
                hideRegAuthPanels(connection);
            }
        }
    }

    void quit(){
        if (isAuthorized) {
            try {
                Connection connection = new Connection();
                serverHandler.sendHandShake(connection, name);
                serverHandler.quit(connection);
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setClientAuthorized(String name){
        this.isAuthorized = true;
        this.name = name;
    }

    void toConsole(String text){
        Platform.runLater(() -> console.appendText("\n" + text));
    }
}