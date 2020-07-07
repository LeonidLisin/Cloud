import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class ClientDataTransferManager extends DataTransferManager {

    void sendRegData(DataOutputStream out, String name, String password) throws IOException {
        out.writeUTF("/reg " + name + " " + password);
    }

    void sendAuthData(DataOutputStream out, String name, String password) throws IOException {
        out.writeUTF("/auth " + name + " " + password);
    }

    boolean recieveAuthData(DataInputStream in) throws IOException {
        return in.readUTF().startsWith("/authOk");
    }

    String recieveResult(DataInputStream in) throws IOException {
        return in.readUTF();
    }
}
