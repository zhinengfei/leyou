package com.leyou.common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * HTTP返回的最外层对象
 */
@Data
public class ResultVO<T> implements Serializable {

    private static final long serialVersionUID = -12542672706271828L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 信息
     */
    private String msg;

    /**
     * 数量
     */
    private Long count;

    private T data;
}
