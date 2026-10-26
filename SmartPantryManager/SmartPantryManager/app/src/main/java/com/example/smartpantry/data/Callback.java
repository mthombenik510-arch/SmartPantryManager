package com.example.smartpantry.data;

/** Delivered on the main thread when a background database job finishes. */
public interface Callback<T> {
    void onResult(T result);
}
