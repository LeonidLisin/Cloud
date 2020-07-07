import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class ClientHandler extends ServerDataTransferManager {

    private FileManager fm;
    private String ROOT, name, password, result;

    ClientHandler(Connection connection, Server server) {
        this.server = server;
        this.command = new StringBuilder();

        try {
            if (chekAuthData(connection)){
                fm = new FileManager(ROOT);
                server.addClientToList(this);
                sendAuthOk(connection.getOut());
                sendResult(connection.getOut(), result);
                fm.generateCurrentPath();
                sendDirContent(connection.getOut(), fm.getDirectoryContent());
            }
        } catch (IOException e) {
            System.out.println("connect error");
            e.printStackTrace();
        }
    }

    private boolean chekAuthData(Connection connection) throws IOException {
        DataInputStream in = connection.getIn();
        DataOutputStream out = connection.getOut();
        boolean authOk = false;
        setResult(null);

        String s = recieveAuthData(in);
        if (s.startsWith("/reg") || s.startsWith("/auth")) {
            tokens = s.split(" ");
            name = tokens[1];
            password = tokens[2];
        }
        if (s.startsWith("/reg")) {
            authOk = DBHandler.regNewUser(name, password);
            if (authOk){
                System.out.println("new user '" + name + "' registered");
                setResult("you're registered");
                ROOT = DBHandler.checkAccount(name, password);
            }
            else {
                setResult("this name is already registered");
                System.out.println(result);
                sendAuthWrong(out);
                sendResult(out, result);
            }
        }
        if (s.startsWith("/auth")) {
            ROOT = DBHandler.checkAccount(name, password);
            authOk = ROOT != null;
            if (ROOT != null){
                for(ClientHandler client: server.getClients()){
                    if (client.name.equals(name)){
                        authOk = false;
                        setResult("already logged name");
                        System.out.println(result);
                    }
                }
            }
            if (!authOk){
                if (result == null) {
                    setResult("wrong name or password");
                    System.out.println(result);
                }
                sendAuthWrong(out);
                sendResult(out, result);
            }
            else {
                System.out.println("client '" + name + "' authorized");
                setResult ("you're authorized");
            }
        }
        return authOk;
    }

    void clientHandle(Connection connection) {
        try {
            DataInputStream in = connection.getIn();
            DataOutputStream out = connection.getOut();
            int byteToRead;
            String recievedCommand;
            byteToRead = in.read();
            if (byteToRead == START_BYTE) {
                recievedCommand = recievedCommand(in);
                if (recievedCommand.equals(Protocol.UPLOAD.getCommand())) {
                    recieveFileFromClient(in, out);
                    sendResult(out, result);
                } else if (recievedCommand.equals(Protocol.DOWNLOAD.getCommand())) {
                    sendFileToClient(in, out);
                    sendResult(out, result);
                } else if (recievedCommand.equals(Protocol.CHANGE_DIR.getCommand())) {
                    changeDir(in, out);
                } else if (recievedCommand.equals(Protocol.DELETE.getCommand())) {
                    delete(in, out);
                    sendResult(out, result);
                } else if (recievedCommand.equals(Protocol.RENAME.getCommand())) {
                    rename(in, out);
                    sendResult(out, result);
                } else if (recievedCommand.equals(Protocol.MAKE_DIR.getCommand())) {
                    makeDir(in, out);
                    sendResult(out, result);
                } else if (recievedCommand.equals(Protocol.QUIT.getCommand())) {
                    quit();
                    System.out.println("client '" + name + "' logged out");
                } else {
                    System.out.println("command error " + recievedCommand);
                }
            } else {
                System.out.println("client request error");
            }
        }
        catch (IOException ioex){
            System.out.println("error connecting with " + name);
            DBHandler.disconnect();
            ioex.printStackTrace();
        }
    }

    private void changeDir(DataInputStream in, DataOutputStream out) throws IOException {
        long dirNameLenght = lenghtFromBytes(recieveLengthByBytes(in, BYTES_FOR_NAME_LENGTH));
        String dir = recieveName(in, dirNameLenght);
        if (dir != null) {
            fm.changeDir(dir); // меняем каталог
        }
        // пуляем клиенту содержимое каталога, в который мы перешли
        sendDirContent(out, fm.getDirectoryContent());
    }

    private void sendFileToClient(DataInputStream in, DataOutputStream out) throws IOException {
        long dirNameLenght = lenghtFromBytes(recieveLengthByBytes(in, BYTES_FOR_NAME_LENGTH));
        String fileOrDirName = recieveName(in, dirNameLenght);
        sendStartByte(out);
        sendCommand(out, Protocol.DOWNLOAD);
        File file = new File(fm.getCurrentPath().toString() + File.separator + fileOrDirName);
        if (file.isFile()) {
            sendLengthByBytes(out, file.length(), BYTES_FOR_FILE_LENGTH);
            sendFileBodyAndCheckSum(out, file);
            sendEndByte(out);
            System.out.println("file " + fileOrDirName + " sent to client");
            setResult("file " + fileOrDirName + " downloaded");
        }
    }

   private void recieveFileFromClient(DataInputStream in, DataOutputStream out) throws IOException {
        int byteToRead;
        int checkSum = 255;
        long dirNameLenght = lenghtFromBytes(recieveLengthByBytes(in, BYTES_FOR_NAME_LENGTH));
        String fileOrDirName = recieveName(in, dirNameLenght);
        File file = new File(fm.getCurrentPath().toString() + File.separator + fileOrDirName);
        try(FileOutputStream outFile = new FileOutputStream(file);
        BufferedOutputStream bufOut = new BufferedOutputStream(outFile)) {
            long fileLenght = lenghtFromBytes(recieveLengthByBytes(in, BYTES_FOR_FILE_LENGTH));
            for (long i = 0; i < fileLenght; i++) {
                byteToRead = in.read();
                bufOut.write(byteToRead);
                checkSum ^= byteToRead;
            }
            bufOut.close();
            if (in.read() == checkSum && in.read() == END_BYTE) {
                sendDirContent(out, fm.getDirectoryContent());
                System.out.println("file " + fileOrDirName + " recieved from client");
                setResult("file " + fileOrDirName + " uploaded");
            } else {
                setResult("upload error");
                Path current = Paths.get(fm.getCurrentPath().toString());
                Files.deleteIfExists(current.resolve(fileOrDirName));
            }
        }
        System.out.println(result);
    }

    private void delete(DataInputStream in, DataOutputStream out) throws IOException {
        long dirNameLenght = lenghtFromBytes(recieveLengthByBytes(in, BYTES_FOR_NAME_LENGTH));
        String fileOrDirName = recieveName(in, dirNameLenght);
        System.out.println(fileOrDirName);
        if (fileOrDirName != null) {
            if (fileOrDirName.startsWith("[")) {
                fm.deleteDir(fileOrDirName);
            } else {
                fm.deleteFile(fileOrDirName);
                setResult("file " + fileOrDirName + " deleted");
                System.out.println(result);
            }
            sendDirContent(out, fm.getDirectoryContent());
        }
    }

    private void makeDir(DataInputStream in, DataOutputStream out) throws IOException {
        long dirNameLenght = lenghtFromBytes(recieveLengthByBytes(in, BYTES_FOR_NAME_LENGTH));
        String dirName = recieveName(in, dirNameLenght);
        fm.makeDir(dirName);
        setResult("dir " + dirName + " created");
        sendDirContent(out, fm.getDirectoryContent());
    }

    private void rename(DataInputStream in, DataOutputStream out) throws IOException {
        long nameLenght;
        nameLenght = lenghtFromBytes(recieveLengthByBytes(in, BYTES_FOR_NAME_LENGTH));
        String oldName = recieveName(in, nameLenght);
        nameLenght = lenghtFromBytes(recieveLengthByBytes(in, BYTES_FOR_NAME_LENGTH));
        String newdName = recieveName(in, nameLenght);
        fm.rename(oldName, newdName);
        setResult(oldName + " renamed to " + newdName);
        sendDirContent(out, fm.getDirectoryContent());
    }

    private void quit() {
        server.removeClientFromList(this);
    }

    synchronized private void setResult(String result){
        this.result = result;
    }

    String getName(){
        return name;
    }
}