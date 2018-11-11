package com.creations.scimitar_runtime.state;

import java.util.Arrays;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.creations.scimitar_runtime.state.Resource.Status.ERROR;
import static com.creations.scimitar_runtime.state.Resource.Status.LOADING;
import static com.creations.scimitar_runtime.state.Resource.Status.SUCCESS;

public final class Resource<T> {

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    @NonNull
    private final Status status;

    @Nullable
    private final Throwable error;

    @Nullable
    public final T data;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable Throwable error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(SUCCESS, data, null);
    }

    public static <T> Resource<T> error(Throwable error, @Nullable T data) {
        return new Resource<>(ERROR, data, error);
    }

    public static <T> Resource<T> error(Throwable error) {
        return new Resource<>(ERROR, null, error);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(LOADING, data, null);
    }

    public boolean success() {
        return status.equals(SUCCESS);
    }

    public boolean isLoading() {
        return status.equals(LOADING);
    }

    public boolean error() {
        return status.equals(ERROR);
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource<?> resource = (Resource<?>) o;
        return status == resource.status &&
                equals(error, resource.error) &&
                equals(data, resource.data);
    }

    @Override
    public int hashCode() {
        return hash(status, error, data);
    }

    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}