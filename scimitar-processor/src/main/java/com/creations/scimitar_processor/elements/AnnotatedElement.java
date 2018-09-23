package com.creations.scimitar_processor.elements;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public abstract class AnnotatedElement {

    private Element element;

    AnnotatedElement(Element _element) {
        element = _element;
    }

    public Element getElement() {
        return element;
    }

    public Name getName() {
        return element.getSimpleName();
    }

    public TypeElement getEnclosingElement() {
        return (TypeElement) element.getEnclosingElement();
    }

    @Override
    public String toString() {
        return "AnnotatedElement{" +
                "element=" + element +
                '}';
    }
}
