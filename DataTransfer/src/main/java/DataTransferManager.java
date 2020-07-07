import java.io.*;

class DataTransferManager {
    final byte START_BYTE = Protocol.START_BYTE.getCommand().getBytes()[0];
    final byte END_BYTE = Protocol.END_BYTE.getCommand().getBytes()[0];
    final int BYTES_FOR_FILE_LENGTH = 5; // размер файла передается 5ю байтами
    final int BYTES_FOR_NAME_LENGTH = 2; // длина имени файла или каталога передается 2мя байтами
    final int BYTES_FOR_PATH_LENGTH = 3; // длина пути передается 3мя байтами
    StringBuilder command;
    String[] tokens;


    void sendStartByte(DataOutputStream out) throws IOException {
        out.write(START_BYTE);
    }

    void sendEndByte(DataOutputStream out) throws IOException {
        out.write(END_BYTE);
    }

    private byte[] lenghtToBytes(long length, int bytes){
        long mask = 0xFFL;
        byte[] lengthByBytes = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            lengthByBytes[i] = (byte) (length & mask);
            length >>= 8;
        }
        return lengthByBytes;
    }

    long lenghtFromBytes(byte[] lengthByBytes){
        long length = 0L, currentByte;
        int k;
        for (int i = 0; i < lengthByBytes.length; i++) {
            currentByte = 0;
            k = i;
            if (lengthByBytes[i] > 0)
                currentByte = lengthByBytes[i];
            if (lengthByBytes[i] < 0)
                currentByte = lengthByBytes[i] + 256;
            k <<= 3;
            currentByte <<= k;
                length |= currentByte;
            }
        return length;
    }

    void sendCommand(DataOutputStream out, Protocol command) throws IOException {
        for (int i = 0; i < command.getCommand().getBytes().length; i++) {
            out.write(command.getCommand().getBytes()[i]);
        }
    }

    synchronized String recievedCommand(DataInputStream in) throws IOException {
        command.setLength(0);
        for (int i = 0; i < Protocol.COMMAND_LENGHT.getCommandLength(); i++) {
            command.append((char) in.read());
        }
        return command.toString();
    }

    void sendLengthByBytes(DataOutputStream out, long length, int bytes) throws IOException {
        byte[] lengthByBytes = lenghtToBytes(length, bytes);
        for (byte fileLengthByte : lengthByBytes) {
            out.write(fileLengthByte);
        }
    }

    byte[] recieveLengthByBytes(DataInputStream in, int bytes) throws IOException {
        byte[] lengthByBytes = new byte[bytes];
        for (int i = 0; i < bytes ; i++) {
            lengthByBytes[i] = (byte) in.read();
        }
        return lengthByBytes;

    }

    void sendFileBodyAndCheckSum(DataOutputStream out, File file) throws IOException {
        int byteToSend;
        int checkSum = 255;
        FileInputStream inFile = new FileInputStream(file);
        BufferedInputStream bufIn = new BufferedInputStream(inFile, 512000);
        BufferedOutputStream bufOut = new BufferedOutputStream(out, 512000);
        while ((byteToSend = bufIn.read()) != -1) {
            bufOut.write(byteToSend);
            checkSum ^= byteToSend;
        }
        bufOut.write(checkSum);
        bufIn.close();
        inFile.close();
        bufOut.flush();
    }

    void sendName(DataOutputStream out, String fileName) throws IOException {
        char[] chars = fileName.toCharArray();
        for (char c: chars) {
            out.writeChar(c);
        }
    }

    synchronized String recieveName(DataInputStream in, long nameLength) throws IOException {
        char[] chars = new char[(int)nameLength];
        for (int i =0; i<nameLength; i++){
            chars[i] = in.readChar();
        }
        return String.valueOf(chars);
    }
}
