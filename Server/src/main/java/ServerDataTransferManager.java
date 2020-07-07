import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class ServerDataTransferManager extends DataTransferManager {

    Server server;

    void sendDirContent(DataOutputStream out, List<Path> pathes) throws IOException {
        // пробовал здесь работать с одним StringBuilder'ом,
        // а метод делать синхронизированным - работает гораздо медленнее,
        // но ощутимой разницы в потребляемой памяти не заметил,  а-то можно было
        // бы сделать пул StringBuilder'ов, количеством, равным пулу потоков
        StringBuilder dirContent = new StringBuilder();
        for (Path p: pathes){
            if(Files.isDirectory(p))
                dirContent.append("[")
                          .append(p.getFileName().toString())
                          .append("]")
                          .append("/");
            else
                dirContent.append(p.getFileName().toString())
                          .append("/");
        }
        sendLengthByBytes(out, dirContent.length(), BYTES_FOR_PATH_LENGTH);
        sendName(out, dirContent.toString());
        dirContent.setLength(0);
    }

    String recieveAuthData(DataInputStream in) throws IOException {
        return in.readUTF();
    }

    void sendAuthOk(DataOutputStream out) throws IOException {
        out.writeUTF("/authOk");
    }

    void sendAuthWrong(DataOutputStream out) throws IOException {
        out.writeUTF("/authWrong");
    }

    void sendResult(DataOutputStream out, String result) throws IOException {
        out.writeUTF(result);
    }
}