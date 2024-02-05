package com.notayessir;

import com.notayessir.publisher.LogPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcConfigKeys;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.RaftServerConfigKeys;
import org.apache.ratis.server.storage.RaftStorage;
import org.apache.ratis.util.NetUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;


@Slf4j
public class MatchServer {

    private final RaftServer server;


    public MatchServer(MatchServerConfig config) throws IOException {
        if (Objects.isNull(config)){
            throw new RuntimeException("nullify config");
        }
        config.checkParam();

        List<String> addresses = config.getAddresses();
        Integer index = config.getIndex();
        String dirname = config.getDirname();
        String groupId = config.getGroupId();

        List<RaftPeer> peers = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i++) {
            RaftPeer raftPeer = RaftPeer.newBuilder()
                    .setId("node" + i).setAddress(addresses.get(i)).build();
            peers.add(raftPeer);
        }
        RaftPeer raftPeer = peers.get(index);
        int port = NetUtils.createSocketAddr(raftPeer.getAddress()).getPort();
        RaftProperties properties = new RaftProperties();
        GrpcConfigKeys.Server.setPort(properties, port);

        File storageDir = new File(dirname + File.separator + raftPeer.getId());
        RaftServerConfigKeys.setStorageDir(properties, Collections.singletonList(storageDir));
        RaftServerConfigKeys.Snapshot.setAutoTriggerEnabled(properties, true);
        RaftServerConfigKeys.Snapshot.setAutoTriggerThreshold(properties, 25000);

        MatchStateMachine counterStateMachine = new MatchStateMachine(config.getPublisher());

        RaftGroup raftGroup = RaftGroup.valueOf(RaftGroupId.valueOf(UUID.fromString(groupId)), peers);
        this.server = RaftServer.newBuilder()
                .setGroup(raftGroup)
                .setProperties(properties)
                .setServerId(raftPeer.getId())
                .setStateMachine(counterStateMachine)
                .setOption(RaftStorage.StartupOption.RECOVER)
                .build();

    }


    public void start() throws IOException {
        this.server.start();
    }

    public void close() throws IOException {
        this.server.close();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        String groupId = "02511d47-d67c-49a3-9011-abb3109a44c1";
        List<String> addresses = Arrays.asList("127.0.0.1:18080","127.0.0.1:18081","127.0.0.1:18082");
        String dirname = "/Users/geek/IdeaProjects/mercury-match-engine/dir";
        for (int i = 0; i < 3; i++) {
            MatchServerConfig config = MatchServerConfig.builder()
                    .addresses(addresses).dirname(dirname).groupId(groupId).index(i)
                    .publisher(new LogPublisher())
                    .build();
            MatchServer matchServer = new MatchServer(config);
            matchServer.start();
        }
        Thread.sleep(1000 * 60 * 24);

    }

}
