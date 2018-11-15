package com.creations.scimitar_processor.elements.methods;

import com.creations.scimitar_annotations.OnSuccess;

import javax.lang.model.element.Element;

/**
 * @author Sérgio Serra on 23/09/2018.
 * Criations
 * sergioserra99@gmail.com
 */
public class SuccessMethod extends MethodElement {

    SuccessMethod(Element element) {
        super(element);
    }

    @Override
    public Type type() {
        return Type.SUCCESS;
    }

    @Override
    public String getId() {
        return getElement().getAnnotation(OnSuccess.class).id();
    }
}
