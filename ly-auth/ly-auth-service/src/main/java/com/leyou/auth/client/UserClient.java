package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 *
 * @ClassName: UserClient
 * @Author: shentiepeng
 * @Date: 2019/1/8
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
@FeignClient("user-service")
public interface UserClient extends UserApi {

}
