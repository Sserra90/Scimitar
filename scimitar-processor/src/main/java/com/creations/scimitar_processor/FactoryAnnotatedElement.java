package com.creations.scimitar_processor;

import com.creations.scimitar_annotations.ViewModelFactory;

import javax.lang.model.element.Element;

class FactoryAnnotatedElement extends AnnotatedElement {

    FactoryAnnotatedElement(Element _element) {
        super(_element);
    }

    boolean useAsDefault() {
        return getElement().getAnnotation(ViewModelFactory.class).useAsDefault();
    }
}
