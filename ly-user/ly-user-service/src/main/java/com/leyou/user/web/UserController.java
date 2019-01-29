package com.leyou.user.web;/*
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 * @ClassName: UserController
 * @Author:   Administrator
 * @Date:     2018/12/15 21:07
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
         * @Author shentiepeng
         * 功能描述: <br>
         *  验证用户名或手机
         * @Date 21:14 2018/12/15
         * @param data, type
         * @return org.springframework.http.ResponseEntity<java.lang.Boolean>
         * @see [相关类/方法](可选)
         * @since [产品/模块版本](可选)
         */
    @RequestMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data, @PathVariable("type") Integer type){
        return ResponseEntity.ok(userService.checkData(data,type));
    }

    /**
    * @Author shentiepeng
    * 功能描述: 发送验证码
    * @Date 20:49 2019/1/7
    * @param phone
    * @return org.springframework.http.ResponseEntity<java.lang.Void>
    * @see [相关类/方法](可选)
    * @since [产品/模块版本](可选)
    */
    @PostMapping("/code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
    * @Author shentiepeng
    * 功能描述: 注册
    * @Date 21:26 2019/1/7
    * @param user, code
    * @return org.springframework.http.ResponseEntity<java.lang.Void>
    * @see [相关类/方法](可选)
    * @since [产品/模块版本](可选)
    */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult result, @RequestParam("code") String code){

//        if(result.hasFieldErrors()){
//            throw new RuntimeException(result.getFieldErrors().stream()
//                    .map(e -> e.getDefaultMessage()).collect(Collectors.joining("|")));
//        }

        userService.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
    * @Author shentiepeng
    * 功能描述:
    * @Date 19:35 2019/1/8
    * @param [username, password]
    * @return org.springframework.http.ResponseEntity<com.leyou.user.pojo.User>
    * @see [相关类/方法](可选)
    * @since [产品/模块版本](可选)
    */
    @GetMapping("/query")
    public ResponseEntity<User> queryUserByUsernameAndPassword(@RequestParam("username") String username,
                                                               @RequestParam("password") String password){
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username,password));
    }
}
