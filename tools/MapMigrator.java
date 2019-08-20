import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class MapMigrator {

    @SuppressWarnings("unchecked")
    private static ConcurrentHashMap<Object, Object> deserialize(File file) {

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            Object obj = ois.readObject();
            if (obj instanceof HashMap) {

                HashMap<Object, Object> map = (HashMap<Object, Object>) obj;
                return new ConcurrentHashMap<Object, Object>(map);
            }
            else {

                System.err.println(file.getName() + " deserializes to " + obj.getClass());
            }
        }
        catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    private static void serialize(File file, ConcurrentHashMap<Object, Object> map) {

        try (FileOutputStream fos = new FileOutputStream(file, false);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(map);
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        File[] directories = new File(args[0]).listFiles(File::isDirectory);
        for (File dir : directories) {

            System.out.println(dir.getName());
            File[] subFiles = dir.listFiles((f) -> !f.isDirectory());
            for (File file : subFiles) {

                ConcurrentHashMap<Object, Object>  map = deserialize(file);
                if (map != null) {

                    serialize(file, map);
                }
            }
        }
    }
}