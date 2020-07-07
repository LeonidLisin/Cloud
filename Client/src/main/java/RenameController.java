import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;


public class RenameController {
    private ServerHandler serverHandler;
    private MainController mc;
    private String clientName, oldFileOrDirName;

    @FXML
    TextField renameField;

    @FXML
    Button renameButton, cancelButton;

    void setServerHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    void setClientName(String name){
        this.clientName = name;
    }

    void setOldFileOrDirName(String name){
        this.oldFileOrDirName = name;
    }

    void setMc(MainController mc){
        this.mc = mc;
    }

    TextField getRenameField(){
        return renameField;
    }

    public void rename() throws IOException {
        if (renameField.getText().equals("") || renameField.getText() == null) {
            disappear();
        }
        else {
            Connection connection = new Connection();
            serverHandler.sendHandShake(connection, clientName);
            serverHandler.rename(connection, oldFileOrDirName, renameField.getText());
            mc.refreshFileList(serverHandler.recieveDirContent(connection));
            mc.toConsole(serverHandler.recieveResult(connection.getIn()));
            connection.close();
            disappear();
        }
    }

    public void disappear(){
        renameField.getScene().getWindow().hide();
    }
}
