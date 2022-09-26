package com.smartalerts;

import java.io.Serializable;

public interface AsyncResponse extends Serializable {
    static void onResponse(AsyncResponse asyncResponse, Object responseObj) {
        if (asyncResponse != null) {
            asyncResponse.processFinish(responseObj);
        }
    }

    void processFinish(Object responseObj);
}
