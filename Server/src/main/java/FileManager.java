import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class FileManager {
    private LinkedList<String> dirTree;
    private StringBuilder currentPath;

    FileManager(String rootFolder) throws IOException {
        this.dirTree = new LinkedList();
        this.currentPath = new StringBuilder();
        dirTree.add(rootFolder);
        currentPath.append(rootFolder);
        Path root = Paths.get(rootFolder);
        if(!Files.exists(root))
            Files.createDirectory(root);
    }

    void deleteFile(String fileName) throws IOException {
        Path current = Paths.get(currentPath.toString());
        Files.deleteIfExists(current.resolve(fileName));
    }

    void deleteDir(String fileName){
        // пока не реализовывал
    }

    void rename(String oldName, String newName) throws IOException {
        Path current = Paths.get(currentPath.toString());
        Path toRename = current.resolve(oldName);
        Path targetName = current.resolve(newName);
        Files.move(toRename, targetName);
    }

    void generateCurrentPath(){
        currentPath.setLength(0);
        for(String s: dirTree){
            currentPath.append(s).append(File.separator);
        }
    }

    void changeDir(String directory){
        Path current = Paths.get(currentPath.toString());

        if(directory.equals("..") && dirTree.size()>1) {
            dirTree.removeLast();
            generateCurrentPath();
        }
        if(!directory.equals("..")) {
            generateCurrentPath();
            if (Files.isDirectory(current.resolve(directory))) {
                dirTree.add(directory);
                currentPath.append(directory);
            }
        }
    }

    void makeDir(String dirName) throws IOException {
        Path current = Paths.get(currentPath.toString());
        Files.createDirectory(current.resolve(dirName));
    }

    List<Path> getDirectoryContent() throws IOException {
        List<Path> folders =  Files.list(Paths.get(currentPath.toString()))
                .filter(p -> Files.isDirectory(p))
                .sorted((Comparator.naturalOrder()))
                .collect(Collectors.toList());

        List<Path> files =  Files.list(Paths.get(currentPath.toString()))
                .filter(p -> !Files.isDirectory(p))
                .sorted((Comparator.naturalOrder()))
                .collect(Collectors.toList());
        List<Path> all = new ArrayList<>(folders);
        all.addAll(files);
        return all;
    }

    StringBuilder getCurrentPath(){
        return currentPath;
    }
}
