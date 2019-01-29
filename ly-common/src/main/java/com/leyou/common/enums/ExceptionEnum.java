package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum  ExceptionEnum {

    PRICE_CANNOT_BE_NULL(400,"价格不能为空"),
    CATEGORY_NOT_FOUND(404,"商品分类没有找到"),
    SPEC_NOT_FOUND(404,"商品规格组没有找到"),
    BRAND_NOT_FOUND(404,"品牌不存在"),
    SPEC_PARAM_NOT_FOUND(404,"品牌不存在"),
    GOODS_NOT_FOUND(404,"商品不存在"),
    SPU_DETAIL_NOT_FOUND(404,"商品详情不存在"),
    SKU_NOT_FOUND(404,"商品SKU不存在"),
    SKU_STOCK_NOT_FOUND(404,"商品库存不存在"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    CATEGORY_BRAND_SAVE_ERROR(500,"新增品牌分类失败"),
    UPLOAD_FILE_ERROR(500,"文件保存失败"),
    INVALID_FILE_TYPE(500,"无效的文件类型"),
    UPDATE_BRAND_FAIL(500,"更新品牌失败"),
    DELETE_BRAND_FAIL(500,"删除品牌失败"),
    GOODS_SAVE_ERROR(500,"商品保存失败"),
    GOODS_UPDATE_ERROR(500,"商品更新失败"),
    GOODS_ID_CANNOT_BE_NULL(400,"商品id不能为空"),
    SPU_NOT_FOUND(404,"商品SPU不存在"),
    INVALID_USER_DATA_TYPE(400,"用户初始化类型错误"),
    INVALID_VERIFY_CODE(400,"无效的验证码"),
    INVALID_USERNAME_PASSWORD(400,"用户名或者密码错误"),
    CREATE_TOKEN_ERROR(500,"用户生成token失败"),
    UN_AUTHORIZED(403,"未授权")
    ;

    private int code;
    private String message;
}
