package com.leyou.gateway.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 *
 * @ClassName: FilterProperties
 * @Author: shentiepeng
 * @Date: 2019/1/9
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
@ConfigurationProperties("ly.filter")
public class FilterProperties {

    private List<String> allowPaths;

    public List<String> getAllowPaths() {
        return allowPaths;
    }

    public void setAllowPaths(List<String> allowPaths) {
        this.allowPaths = allowPaths;
    }
}
