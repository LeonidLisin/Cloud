import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8484;
    private static DataTransferManager dtm = new DataTransferManager();
    private ArrayList<ClientHandler> clients;

    static void start() {
        new Server();
    }

    private Server() {
        try {
            this.clients = new ArrayList<>();
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("server started");
            DBHandler.connect();
            System.out.println("database connected");
            ExecutorService es = Executors.newFixedThreadPool(30);
            while(true) {
                Socket socket = server.accept();
                Connection connection = new Connection(socket);
                String handShake = recieveHandShake(connection);
                ClientHandler currentClient = getClientByName(handShake);
                if (currentClient == null){
                    es.execute(() -> new ClientHandler(connection, this));
                }
                else{
                    es.execute(() -> currentClient.clientHandle(connection));
                }
            }
        } catch (IOException e) {
            System.out.println("connect error");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("error connecting to database");
            e.printStackTrace();
        }
    }

    void addClientToList(ClientHandler client){
        clients.add(client);
    }

    void removeClientFromList(ClientHandler client){
        clients.remove(client);
    }

    private ClientHandler getClientByName(String name){
        for(ClientHandler client: clients){
            if (client.getName().equals(name)) {
                return client;
            }
        }
        return null;
    }

    private String recieveHandShake(Connection connection) throws IOException {
        long dirNameLenght = dtm.lenghtFromBytes(dtm.recieveLengthByBytes(connection.getIn(), dtm.BYTES_FOR_NAME_LENGTH));
        return dtm.recieveName(connection.getIn(), dirNameLenght);
    }

    ArrayList<ClientHandler> getClients() {
        return clients;
    }
}
