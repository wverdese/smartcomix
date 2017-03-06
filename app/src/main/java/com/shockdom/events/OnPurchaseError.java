package com.shockdom.events;

/**
 * Created by Walt on 20/05/2015.
 */
public class OnPurchaseError {

    public int errCode;
    public Throwable exception;

    public OnPurchaseError(int errCode, Throwable exception) {
        this.errCode = errCode;
        this.exception = exception;
    }
}
