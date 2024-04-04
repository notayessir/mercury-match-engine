package com.notayessir.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * basic raft server config, wrap in a list, make it easier to understand
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "raft")
public class RaftServerConfiguration {

    private List<RaftConfig> configs;


}
