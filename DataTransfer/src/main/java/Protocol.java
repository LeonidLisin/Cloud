/*
to do:
1. работа с базой данных, регистрация и авторизация клиентов +
2. интерфейс загрузки файлов на сервер
    через drag and drop файлов или каталогов на окно клиента +
3. переименование файлов и каталогов на сервере +
4. лог событий в клиенте +
5. обработка исключений
6. отключение соединения после любой операции +
7. скачивание и загрузка всего каталога с подкаталогами и файлами
8. статусбар для загрузки/скачивания
9. многопоточная загрузка/скачивание файлов и каталогов +
10. докачка файла при обрыве соединения
*/


// Протокол обмена:
// client request to download file: $dnld[filename]#
// transfer file from server to client: $dnld[file size: 5 bytes, little endian][file bytes][checksum: 1 byte]#
// file upload from client to server: $upld[file name length by bytes][file name by chars][file size: 5 bytes, little endian][file bytes][checksum: 1 byte]#
// client request to change directory: $chdr[dirname] #
// sending dir content from server to client: $chdr/[[dir1]/[dir2]/...file1/file2...]/#
// sending file or dir name to server (for delete, changing dir etc...): $[name]#
// client request for deleting file or dir: $dlte[name length by bytes][name by chars]#
// client request for rename file or dir: $renm[old name]#[new name]#

public enum Protocol {
    START_BYTE("$"),
    END_BYTE("#"),
    UPLOAD("upld"),
    DOWNLOAD("dnld"),
    DELETE("dlte"),
    CHANGE_DIR("chdr"),
    RENAME("renm"),
    MAKE_DIR("mkdr"),
    QUIT("quit"),
    COMMAND_LENGHT("");
    private String command;
    Protocol(String command){
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
    public int getCommandLength(){
        return 4;
    }
}
