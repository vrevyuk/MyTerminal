package com.revyuk.myterminal.model;

/**
 * Created by Vitaly on 28.05.2015.
 */
public class TerminalsServerResponse {
    boolean success;

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "server_response{" +
                "success=" + success +
                '}';
    }
}


