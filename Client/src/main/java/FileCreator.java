import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class FileCreator {
    public static void main(String[] args) throws IOException {
        Random random = new Random();
        for (int i = 0; i < 1000; i++){
            String name = Integer.toString(i);
            File file = new File("files/" + name + "_____");
            FileOutputStream fos = new FileOutputStream(file);
            for (int k = 0; k< random.nextInt(1000000); k++){
                fos.write(random.nextInt(255));
            }
        }
    }
}
