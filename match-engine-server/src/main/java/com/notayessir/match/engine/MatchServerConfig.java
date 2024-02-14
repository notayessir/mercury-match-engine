package com.notayessir.match.engine;


import cn.hutool.core.collection.CollectionUtil;
import com.notayessir.match.engine.publisher.Publisher;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
public class MatchServerConfig {


    private String dirname;

    private Integer index;

    private List<String> addresses;

    private String groupId;

    private List<Publisher> publishers;

    public void checkParam() {
        if (StringUtils.isBlank(dirname)){
            throw new RuntimeException("dirname not provided");
        }
        if (Objects.isNull(index)){
            throw new RuntimeException("index not provided");
        }
        if (CollectionUtil.isEmpty(publishers)){
            throw new RuntimeException("publishers not provided");
        }
        if (CollectionUtil.isEmpty(addresses)){
            throw new RuntimeException("addresses not provided");
        }
        if (StringUtils.isBlank(groupId)){
            throw new RuntimeException("groupId not provided");
        }
    }
}
