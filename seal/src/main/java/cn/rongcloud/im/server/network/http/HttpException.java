/*
    Launch Android Client, HttpException
    Copyright (c) 2014 LAUNCH Tech Company Limited
    http:www.cnlaunch.com
 */
package cn.rongcloud.im.server.network.http;


public class HttpException extends Exception {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4010634120321127684L;

    public HttpException() {
        super();
    }

    public HttpException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public HttpException(String detailMessage) {
        super(detailMessage);
    }

    public HttpException(Throwable throwable) {
        super(throwable);
    }


}
