package com.notayessir.config;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RaftConfig {


    /**
     * raft group id
     */
    private String groupId;

    /**
     * raft group addresses, at least 3 members
     */
    private List<String> addresses;

    /**
     * when use 'single' mode, leave empty
     */
    private String targetAddress;

    /**
     * raft storage position
     */
    private String storage;

    /**
     * single: all member run in a process, simply use 'for' initiate servers in a spring boot server;
     * group: run a single raft node of a group in a process
     */
    private String mode;



}
