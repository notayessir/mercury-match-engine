package com.notayessir;

import com.alibaba.fastjson2.JSONObject;
import com.notayessir.bo.MatchCommandBO;
import org.apache.ratis.client.RaftClient;
import org.apache.ratis.client.RaftClientConfigKeys;
import org.apache.ratis.conf.Parameters;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcFactory;
import org.apache.ratis.protocol.*;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;
import org.apache.ratis.util.TimeDuration;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MatchClient {


    private RaftClient client;

    private final MatchClientConfig config;

    public MatchClient(MatchClientConfig config) {
        if (Objects.isNull(config)){
            throw new RuntimeException("nullify config");
        }
        config.checkParam();
        this.config = config;
    }

    public void connect(){
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
        RaftClientConfigKeys.Rpc.setRequestTimeout(raftProperties,
                TimeDuration.valueOf(5000, TimeUnit.MILLISECONDS));
//        raftProperties.setTimeDuration(RaftClientConfigKeys.Rpc.REQUEST_TIMEOUT_KEY, TimeDuration.valueOf(3000, TimeUnit.MILLISECONDS));
//        raftProperties.setTimeDuration(RaftClientConfigKeys.Rpc.WATCH_REQUEST_TIMEOUT_KEY, TimeDuration.valueOf(3000, TimeUnit.MILLISECONDS));
//        RaftClientConfigKeys.Rpc.setRequestTimeout(raftProperties, TimeDuration.valueOf(3, TimeUnit.SECONDS));
        client = RaftClient.newBuilder()
                .setProperties(raftProperties)
                .setClientRpc(new GrpcFactory(new Parameters())
                        .newRaftClientRpc(ClientId.randomId(), raftProperties))
                .setRaftGroup(raftGroup)
                .build();
    }


    public CompletableFuture<Long> sendAsync(MatchCommandBO command) {
        Message message = Message.valueOf(JSONObject.toJSONString(command));
        CompletableFuture<RaftClientReply> origin = client.async().send(message);
        return origin.thenApply(reply -> {
            String resp = unwrap(reply);
            return Long.parseLong(resp);
        });
    }

    public Long sendSync(MatchCommandBO command) throws Exception{
        Message message = Message.valueOf(JSONObject.toJSONString(command));
        RaftClientReply reply = client.io().send(message);
        return Long.parseLong(unwrap(reply));
    }

    private String unwrap(RaftClientReply reply){
        ByteString resp = reply.getMessage().getContent();
        ByteBuffer buffer = resp.asReadOnlyByteBuffer();
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return new String(byteArray);
    }

    public void close() throws IOException {
        client.close();
    }


}
