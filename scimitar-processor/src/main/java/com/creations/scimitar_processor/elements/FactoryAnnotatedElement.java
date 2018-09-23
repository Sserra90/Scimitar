package com.creations.scimitar_processor.elements;

import com.creations.scimitar_annotations.ViewModelFactory;

import javax.lang.model.element.Element;

public class FactoryAnnotatedElement extends AnnotatedElement {

    public FactoryAnnotatedElement(Element _element) {
        super(_element);
    }

    public boolean useAsDefault() {
        return getElement().getAnnotation(ViewModelFactory.class).useAsDefault();
    }
}
