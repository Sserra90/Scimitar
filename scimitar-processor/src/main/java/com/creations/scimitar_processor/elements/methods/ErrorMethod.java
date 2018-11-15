package com.creations.scimitar_processor.elements.methods;

import com.creations.scimitar_annotations.OnError;

import javax.lang.model.element.Element;

/**
 * @author Sérgio Serra on 23/09/2018.
 * Criations
 * sergioserra99@gmail.com
 */
public class ErrorMethod extends MethodElement {

    ErrorMethod(Element element) {
        super(element);
    }

    @Override
    public Type type() {
        return Type.ERROR;
    }

    @Override
    public String getId() {
        return getElement().getAnnotation(OnError.class).id();
    }
}
