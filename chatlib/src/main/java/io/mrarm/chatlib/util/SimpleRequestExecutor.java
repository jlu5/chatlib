package io.mrarm.chatlib.util;

import io.mrarm.chatlib.ChatApiException;
import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimpleRequestExecutor {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public <T> Future<T> queue(final Callable<T> task, final ResponseCallback<T> callback,
                                  final ResponseErrorCallback errorCallback) {
        return executor.submit(() -> {
            T ret;
            try {
                ret = task.call();
            } catch (ChatApiException ex) {
                if (errorCallback != null)
                    errorCallback.onError(ex);
                throw ex;
            }
            if (callback != null)
                callback.onResponse(ret);
            return ret;
        });
    }

    public static <T> InstantFuture<T> run(final Callable<T> task, final ResponseCallback<T> callback,
                                           final ResponseErrorCallback errorCallback) {
        T ret;
        try {
            ret = task.call();
        } catch (Throwable ex) {
            if (ex instanceof ChatApiException && errorCallback != null)
                errorCallback.onError((ChatApiException) ex);
            return new InstantFuture<T>(null, ex);
        }
        if (callback != null)
            callback.onResponse(ret);
        return new InstantFuture<>(ret);
    }

}
