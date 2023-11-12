package com.floodeer.hybrid.database;

public interface Callback<T> {

    void onCall(T result);
}
