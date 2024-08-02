package it.polimi.ds.network2.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConsensusStatus {

    private final Map<UUID, Integer> consensusMap = new ConcurrentHashMap<>();


}
