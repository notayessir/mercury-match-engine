package com.notayessir;


import cn.hutool.core.collection.CollectionUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Getter
@Setter
@Builder
public class MatchClientConfig {



    private List<String> addresses;

    private String groupId;

    public void checkParam() {
        if (CollectionUtil.isEmpty(addresses)){
            throw new RuntimeException("addresses are not provided");
        }
        if (StringUtils.isBlank(groupId)){
            throw new RuntimeException("groupId is not provided");
        }
    }
}
