package com.zhi.cluster.failmode;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-20 21:03
 */
public enum FailMode {
    FAIL_FAST("failFast"),
    FAIL_RETRY("failRetry"),
    FAIL_OVER("failOver");
    FailMode(String code) {
        this.code = code;
    }
    private String code;
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
}