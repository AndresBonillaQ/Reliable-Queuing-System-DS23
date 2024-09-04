package it.polimi.ds.broker.raft.persistence;

import it.polimi.ds.broker.raft.persistence.utils.RaftStateDto;
import it.polimi.ds.utils.GsonInstance;

import java.io.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RaftPersistence {

    private final static String filePath = "/rqs-logs";
    private final static Logger log = Logger.getLogger(RaftPersistence.class.getName());

    private RaftPersistence(){}

    /**
     * Load RaftState from file
     * */
    public static Optional<RaftStateDto> loadRaftLogFromJson(String clusterId, String brokerId){
        log.log(Level.INFO, "Reading State from json..");

        String fullPath = new File(getWorkingDir(), getFilePath(clusterId, brokerId)).getAbsolutePath();

        try (Reader reader = new FileReader(fullPath)) {
            RaftStateDto raftStateDto = GsonInstance.getInstance().getGson().fromJson(reader, RaftStateDto.class);
            log.log(Level.INFO, "File found, loading..");
            return Optional.of(raftStateDto);
        } catch (FileNotFoundException e){
            log.log(Level.INFO, "File not found!");
            return Optional.empty();
        } catch (IOException e) {
            log.log(Level.SEVERE, "IO ERROR during reading state from file! {0}", e.getMessage());
            return Optional.empty();
        }
    }

    public static void storeRaftLogOnJson(String clusterId, String brokerId, RaftStateDto raftStateDto){
        log.log(Level.INFO, "Storing State in json..");

        File file = new File(getWorkingDir(), getFilePath(clusterId, brokerId));
        checkParentDirExists(file);

        try (FileWriter writer = new FileWriter(file)) {
            GsonInstance.getInstance().getGson().toJson(raftStateDto, writer);
        } catch (IOException e) {
            log.log(Level.SEVERE, "IO ERROR during store state! {0}", e.getMessage());
            return;
        }

        log.log(Level.SEVERE, "State stored successfully!");
    }

    private static void checkParentDirExists(File file){
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }

    private static String getFilePath(String clusterId, String brokerId){
        return filePath + getFileName(clusterId, brokerId);
    }

    private static String getFileName(String clusterId, String brokerId){
        return "/clusterId" + clusterId + "_brokerId" + brokerId +".json";
    }

    private static String getWorkingDir(){
        return System.getProperty("user.dir");
    }

    // clusterId0_brokerId0
}
