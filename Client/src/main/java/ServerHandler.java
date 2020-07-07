import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;


class ServerHandler extends ClientDataTransferManager {

    private ArrayList<String> fileList;

    ServerHandler() {
        this.fileList = new ArrayList<>();
        this.command = new StringBuilder();
    }

    void sendHandShake(Connection connection, String name) throws IOException {
        sendLengthByBytes(connection.getOut(), name.length(), BYTES_FOR_NAME_LENGTH);
        sendName(connection.getOut(), name);
    }

    void deleteFileOrDir(Connection connection, String name) throws IOException {
        sendStartByte(connection.getOut());
        sendCommand(connection.getOut(), Protocol.DELETE);
        sendLengthByBytes(connection.getOut(), name.length(), BYTES_FOR_NAME_LENGTH);
        sendName(connection.getOut(), name);
        sendEndByte(connection.getOut());
    }

    void uploadFile(Connection connection, File file) throws IOException {
        sendStartByte(connection.getOut());
        sendCommand(connection.getOut(),Protocol.UPLOAD);
        sendLengthByBytes(connection.getOut(), file.getName().length(), BYTES_FOR_NAME_LENGTH);
        sendName(connection.getOut(), file.getName());
        sendLengthByBytes(connection.getOut(), file.length(), BYTES_FOR_FILE_LENGTH);
        sendFileBodyAndCheckSum(connection.getOut(), file);
        sendEndByte(connection.getOut());
        System.out.println("file " + file.getName() + " sent to server");
    }

    void makeDir(Connection connection, String dirName) throws IOException {
        sendStartByte(connection.getOut());
        sendCommand(connection.getOut(), Protocol.MAKE_DIR);
        sendLengthByBytes(connection.getOut(), dirName.length(), BYTES_FOR_NAME_LENGTH);
        sendName(connection.getOut(), dirName);
        sendEndByte(connection.getOut());
    }

    void changeDir(Connection connection, String dirName) throws IOException {
        sendStartByte(connection.getOut());
        sendCommand(connection.getOut(), Protocol.CHANGE_DIR);
        sendLengthByBytes(connection.getOut(), dirName.length(), BYTES_FOR_NAME_LENGTH);
        sendName(connection.getOut(), dirName);
        sendEndByte(connection.getOut());
    }

    synchronized ArrayList<String> recieveDirContent(Connection connection) throws IOException {
        long dirContentLenght = lenghtFromBytes(recieveLengthByBytes(connection.getIn(), BYTES_FOR_PATH_LENGTH));
        String dirContent = recieveName(connection.getIn(), dirContentLenght);
        tokens = dirContent.split("/");
        fileList.clear();
        fileList.addAll(Arrays.asList(tokens));
        return fileList;
    }

    void downloadFile(Connection connection, String fileName) throws IOException {
        int byteToRead;
        sendStartByte(connection.getOut());
        sendCommand(connection.getOut(), Protocol.DOWNLOAD);
        sendLengthByBytes(connection.getOut(), fileName.length(), BYTES_FOR_NAME_LENGTH);
        sendName(connection.getOut(), fileName);
        sendEndByte(connection.getOut());

        System.out.println("downloading file " + fileName + "...");
        byteToRead = connection.getIn().read();
        if (byteToRead == START_BYTE){
            if(recievedCommand(connection.getIn()).equals(Protocol.DOWNLOAD.getCommand())){
                int checkSum = 255;
                byte[] fileLength = recieveLengthByBytes(connection.getIn(), BYTES_FOR_FILE_LENGTH);
                long fileLenght = lenghtFromBytes(fileLength);
                System.out.println("recieved file size " + fileLenght);
                File file = new File(fileName);
                try (FileOutputStream outFile = new FileOutputStream(file);
                BufferedOutputStream bufOut = new BufferedOutputStream(outFile, 512000)) {
                    for (long i = 0; i < fileLenght; i++) {
                        byteToRead = connection.getIn().read();
                        checkSum ^= byteToRead;
                        bufOut.write(byteToRead);
                    }
                }
                System.out.println("recieved checksum = " + checkSum);
                if (connection.getIn().read() == checkSum && connection.getIn().read() == END_BYTE) {
                    System.out.println("file " + fileName + " downloaded from server");
                }
                else
                {
                    System.out.println("download error");
                    Files.deleteIfExists(Paths.get(fileName));
                }
            }
        }
    }

    void rename(Connection connection, String oldName, String newName) throws IOException {
        System.out.println("newname = " + newName + " oldname " + oldName);
        sendStartByte(connection.getOut());
        sendCommand(connection.getOut(), Protocol.RENAME);
        sendLengthByBytes(connection.getOut(), oldName.length(), BYTES_FOR_NAME_LENGTH);
        sendName(connection.getOut(), oldName);
        sendLengthByBytes(connection.getOut(), newName.length(), BYTES_FOR_NAME_LENGTH);
        sendName(connection.getOut(), newName);
        sendEndByte(connection.getOut());
    }

    void tryToReg(Connection connection, String name, String password) throws IOException {
        sendHandShake(connection, "/null");
        sendRegData(connection.getOut(), name, password);

    }

    void tryToAuth(Connection connection, String name, String password) throws IOException {
        sendHandShake(connection, "/null");
        sendAuthData(connection.getOut(), name, password);

    }

    void quit(Connection connection) throws IOException {
        sendStartByte(connection.getOut());
        sendCommand(connection.getOut(), Protocol.QUIT);
        sendEndByte(connection.getOut());
    }
}