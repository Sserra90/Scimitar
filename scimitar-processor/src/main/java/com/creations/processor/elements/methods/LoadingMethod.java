package com.creations.processor.elements.methods;

import com.creations.annotations.OnLoading;

import javax.lang.model.element.Element;

/**
 * @author Sérgio Serra on 23/09/2018.
 * Criations
 * sergioserra99@gmail.com
 */
public class LoadingMethod extends MethodElement {

    LoadingMethod(Element element) {
        super(element);
    }

    @Override
    public Type type() {
        return Type.LOADING;
    }

    @Override
    public String getId() {
        return getElement().getAnnotation(OnLoading.class).id();
    }
}
