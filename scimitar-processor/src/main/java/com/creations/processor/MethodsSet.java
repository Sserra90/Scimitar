package com.creations.processor;

import com.creations.processor.elements.AnnotatedElement;
import com.creations.processor.elements.methods.MethodElement;

/**
 * @author Sérgio Serra on 23/09/2018.
 */
public class MethodsSet {

    private AnnotatedElement onSuccess, onError, onLoading, onNoResults;

    public AnnotatedElement success() {
        return onSuccess;
    }

    public AnnotatedElement error() {
        return onError;
    }

    public AnnotatedElement loading() {
        return onLoading;
    }

    public AnnotatedElement noResults() {
        return onNoResults;
    }

    public void addMethod(MethodElement el) {
        switch (el.type()) {
            case ERROR:
                onError = el;
                break;
            case LOADING:
                onLoading = el;
                break;
            case SUCCESS:
                onSuccess = el;
                break;
            case NO_RESULTS:
                onNoResults = el;
                break;
        }
    }

    @Override
    public String toString() {
        return "MethodsSet{" +
                "onSuccess=" + onSuccess +
                ", onError=" + onError +
                ", onLoading=" + onLoading +
                ", onNoResults=" + onNoResults +
                '}';
    }
}
