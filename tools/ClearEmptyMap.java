import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class ClearEmptyMap {

    @SuppressWarnings("unchecked")
    private static boolean checkMapEmpty(File file) {

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            Object obj = ois.readObject();
            if (obj instanceof ConcurrentHashMap) {

                ConcurrentHashMap<Object, Object> map = (ConcurrentHashMap<Object, Object>) obj;
                return map.isEmpty();
            }
            else {

                System.err.println(file.getName() + " deserializes to " + obj.getClass());
            }
        }
        catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    public static void main(String[] args) throws IOException {

        File[] directories = new File(args[0]).listFiles(File::isDirectory);
        for (File dir : directories) {

            System.out.println(dir.getName());
            File[] subFiles = dir.listFiles((f) -> !f.isDirectory());
            for (File file : subFiles) {

                boolean empty = checkMapEmpty(file);
                if (empty) {

                    file.delete();
                }
            }
        }
    }
}