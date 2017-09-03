package com.ndhunju.dailyjournal;

/**
 * Created by ndhunju on 9/3/17.
 * Generic callback to return a result once the operation is finished.
 */

public interface FinishCallback<T> {
    void onFinish(T result);
}
