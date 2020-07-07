import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection {
    private final static int PORT = 8484;
    private final static String ADDRESS = "localhost";//"192.168.1.7";

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    Connection() throws IOException {
        this.socket = new Socket(ADDRESS, PORT);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    void close() throws IOException {
        this.in.close();
        this.out.close();
        this.socket.close();
    }

    DataInputStream getIn(){
        return in;
    }

    DataOutputStream getOut(){
        return out;
    }
}

