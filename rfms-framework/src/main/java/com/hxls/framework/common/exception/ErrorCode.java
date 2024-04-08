package com.hxls.framework.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aspectj.weaver.ast.Not;

/**
 * 错误编码
 *
 * @author
 *
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNAUTHORIZED(401, "还未授权，不能访问"),
    FORBIDDEN(403, "没有权限，禁止访问"),
    REFRESH_TOKEN_INVALID(400, "refresh_token 已失效"),
    INTERNAL_SERVER_ERROR(500, "服务器异常，请稍后再试"),
    NOT_FOUND(404,"您所查询的数据不存在或已被删除");
    private final int code;
    private final String msg;
}
