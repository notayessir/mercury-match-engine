package com.notayessir;


import cn.hutool.core.collection.CollectionUtil;
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

    private Publisher publisher;

    public void checkParam() {
        if (StringUtils.isBlank(dirname)){
            throw new RuntimeException("dirname isn't provided");
        }
        if (Objects.isNull(index)){
            throw new RuntimeException("index isn't provided");
        }
        if (Objects.isNull(publisher)){
            throw new RuntimeException("publisher isn't provided");
        }
        if (CollectionUtil.isEmpty(addresses)){
            throw new RuntimeException("addresses isn't provided");
        }
        if (StringUtils.isBlank(groupId)){
            throw new RuntimeException("groupId isn't provided");
        }
    }
}
