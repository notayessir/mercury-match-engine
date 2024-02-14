package com.notayessir.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration

public class AppConfiguration {

    @Value("${kafka.enable:false}")
    private boolean kafkaEnable;

    @Value("${log.enable:true}")
    private boolean logEnable;

    @Value("${kafka.topic:match_result_topic}")
    private String kafkaTopic;

    @Value("${raft.server.mode:group}")
    private String raftServerMode;

    @Value("${raft.group.list:}")
    private String raftGroupList;

    @Value("${raft.storage.dir:}")
    private String raftStorageDir;

    @Value("${raft.server.list:}")
    private String raftServerList;

}
