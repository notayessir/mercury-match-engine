package com.notayessir;

import com.alibaba.fastjson2.JSONObject;
import com.notayessir.bo.MatchResultBO;
import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.Parameters;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcFactory;
import org.apache.ratis.protocol.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MatchClient {


    private final RaftClient client;

    public MatchClient(MatchClientConfig config) {
        if (Objects.isNull(config)){
            throw new RuntimeException("nullify config");
        }
        config.checkParam();

        List<String> addresses = config.getAddresses();
        String groupId = config.getGroupId();

        List<RaftPeer> peers = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i++) {
            RaftPeer raftPeer = RaftPeer.newBuilder()
                    .setId("node" + i).setAddress(addresses.get(i)).build();
            peers.add(raftPeer);
        }

        RaftGroup raftGroup = RaftGroup
                .valueOf(RaftGroupId.valueOf(UUID.fromString(groupId)), peers);
        RaftProperties raftProperties = new RaftProperties();

        client = RaftClient.newBuilder()
                .setProperties(raftProperties)
                .setClientRpc(new GrpcFactory(new Parameters())
                        .newRaftClientRpc(ClientId.randomId(), raftProperties))
                .setRaftGroup(raftGroup)
                .build();

    }


    public CompletableFuture<RaftClientReply> sendAsync(MatchResultBO command) {
        Message message = Message.valueOf(JSONObject.toJSONString(command));
        return client.async().send(message);

    }

    public RaftClientReply sendSync(MatchResultBO command) throws Exception{
        Message message = Message.valueOf(JSONObject.toJSONString(command));
        return client.io().send(message);
    }

    public void close() throws IOException {
        client.close();
    }


}
