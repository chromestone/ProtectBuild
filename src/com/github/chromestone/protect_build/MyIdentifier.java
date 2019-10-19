package com.github.chromestone.protect_build;

import org.bukkit.Location;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.*;

/**
 * The MyIdentifier class
 * Created by Derek Zhang on 8/10/19.
 */
public class MyIdentifier {

    private final String dataDir;

    private HashMap<Object, Object> identifier;

    public MyIdentifier(String dataDir) {

        this.dataDir = dataDir;

        identifier = null;
    }

    @SuppressWarnings("unchecked")
    public boolean init(Logger logger) {

        Path path = Paths.get(dataDir, "identities.bin");
        File file = path.toFile();

        if (file.exists()) {

            if (file.isDirectory()) {

                logger.log(Level.SEVERE, "unable to read: file identities.bin already exists as directory");
                return false;
            }

            boolean status = false;

            try (FileInputStream fis = new FileInputStream(file);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {

                Object obj = ois.readObject();
                if (obj instanceof HashMap) {

                    identifier = (HashMap<Object, Object>) obj;
                    status = true;
                }
                else {

                    logger.log(Level.SEVERE, "identities.bin does not deserialize to a hash-map");
                }
            }
            catch (Exception e) {

                logger.log(Level.SEVERE, "unable to deserialize identifier from identities.bin", e);
            }

            return status;
        }
        else {

            identifier = new HashMap<>();

            return true;
        }
    }

    public void save(Logger logger) {

        Path path = Paths.get(dataDir, "identities.bin");
        File file = path.toFile();

        if (file.isDirectory()) {

            logger.log(Level.SEVERE, "unable to save: file identities.bin already exists as directory");
            return;
        }

        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {

            boolean success = parentFile.mkdirs();

            if (!success) {

                logger.log(Level.SEVERE, "unable to create directories parent of identities.bin");
                return;
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file, false);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(identifier);
        }
        catch (Exception e) {

            logger.log(Level.SEVERE, "unable to serialize identifier to identities.bin", e);
        }
    }

    public void registerIdentity(UUID id) {

        //TODO this might cause problems if you ever want to remove identities
        int size = identifier.size();
        identifier.put(id, new IntPointPair(size));
    }

    public Optional<Integer> getIdentity(UUID id, Logger logger) {

        Object obj = identifier.get(id);
        if (obj == null) {

            return Optional.empty();
        }
        else if (obj instanceof IntPointPair) {

            return Optional.of(((IntPointPair) obj).integer);
        }

        logger.log(Level.SEVERE, "obtained value of type [{0}] for [{1}] from identifier",
                   new Object[]{obj.getClass(), id});

        return Optional.empty();
    }

    public Optional<My3DPoint> setLocation(UUID id, Location location, Logger logger) {

        Object obj = identifier.get(id);
        if (obj == null) {

            return Optional.empty();
        }
        else if (obj instanceof IntPointPair) {

            IntPointPair pair = ((IntPointPair) obj);
            My3DPoint tmp = pair.point;

            pair.point = new My3DPoint(location.getBlockX(), (int) Math.round(location.getY()), location.getBlockZ());

            return Optional.ofNullable(tmp);
        }

        logger.log(Level.SEVERE, "obtained value of type [{0}] for [{1}] from identifier",
                   new Object[]{obj.getClass(), id});

        return Optional.empty();
    }

    public Optional<My3DPoint> getLocation(UUID id, Logger logger) {

        Object obj = identifier.get(id);
        if (obj == null) {

            return Optional.empty();
        }
        else if (obj instanceof IntPointPair) {

            return Optional.ofNullable(((IntPointPair) obj).point);
        }

        logger.log(Level.SEVERE, "obtained value of type [{0}] for [{1}] from identifier",
                new Object[]{obj.getClass(), id});

        return Optional.empty();
    }

}
