package com.mxixm.fastboot.weixin.module;

/**
 * FastBootWeixin 微信常量类
 *
 * @author Guangshan
 * @summary FastBootWeixin  Wx
 * @Copyright (c) 2017, Guangshan Group All Rights Reserved
 * @since 2017/8/5 21:34
 */
public class Wx {

    /**
     * 个人定义的类目
     */
    public enum Category {
        /**
         * 收到用户消息
         */
        MESSAGE,
        /**
         * 包括按钮事件和用户事件(如关注)
         * 后续可考虑分离按钮事件和用户时间
         */
        EVENT,
        /**
         * 用户按钮事件
         */
        BUTTON,
        /**
         * 系统事件
         */
        SYSTEM
    }


    public static class Environment {

        private static Environment instance = new Environment();

        public static Environment instance() {
            return instance;
        }

        private Environment() {
        }

        /**
         * 默认存储路径，在用户目录下的weixin目录
         */
        private String defaultMediaPath = System.getProperty("java.io.tmpdir");
        // "~/weixin/media/";

        private String wxToken;

        private String wxAppId;

        private String wxAppSecret;

        private String callbackUrl;

        public String getCallbackUrl() {
            return callbackUrl;
        }

        public void setCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
        }

        public String getWxToken() {
            return wxToken;
        }

        public void setWxToken(String wxToken) {
            this.wxToken = wxToken;
        }

        public String getWxAppId() {
            return wxAppId;
        }

        public void setWxAppId(String wxAppId) {
            this.wxAppId = wxAppId;
        }

        public String getWxAppSecret() {
            return wxAppSecret;
        }

        public void setWxAppSecret(String wxAppSecret) {
            this.wxAppSecret = wxAppSecret;
        }

        public String getDefaultMediaPath() {
            return defaultMediaPath;
        }

        public void setDefaultMediaPath(String defaultMediaPath) {
            this.defaultMediaPath = defaultMediaPath;
        }
    }

}
