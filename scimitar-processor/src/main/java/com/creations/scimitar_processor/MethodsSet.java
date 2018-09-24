package com.creations.scimitar_processor;

import com.creations.scimitar_processor.elements.AnnotatedElement;
import com.creations.scimitar_processor.elements.methods.MethodElement;

/**
 * @author Sérgio Serra on 23/09/2018.
 */
public class MethodsSet {

    private AnnotatedElement onSuccess, onError, onLoading;

    public AnnotatedElement success() {
        return onSuccess;
    }

    public AnnotatedElement error() {
        return onError;
    }

    public AnnotatedElement loading() {
        return onLoading;
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
        }
    }

    @Override
    public String toString() {
        return "MethodsSet{" +
                "onSuccess=" + onSuccess +
                ", onError=" + onError +
                ", onLoading=" + onLoading +
                '}';
    }
}
