import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;

public class MakeDirController {
    private ServerHandler serverHandler;
    private MainController mc;
    private String name;

    @FXML
    TextField makeDirField;

    @FXML
    Button makeDirButton, cancelButton;

    void setServerHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    void setMc(MainController mc){
        this.mc = mc;
    }

    void setName(String name){
        this.name = name;
    }

    public void makeDir() throws IOException {
        if (makeDirField.getText().equals("") || makeDirField.getText() == null) {
            disappear();
        }
        else {
            Connection connection = new Connection();
            serverHandler.sendHandShake(connection, name);
            serverHandler.makeDir(connection, makeDirField.getText());
            mc.refreshFileList(serverHandler.recieveDirContent(connection));
            mc.toConsole(serverHandler.recieveResult(connection.getIn()));
            connection.close();
            disappear();
        }
    }

    public void disappear(){
        makeDirField.getScene().getWindow().hide();
    }
}
