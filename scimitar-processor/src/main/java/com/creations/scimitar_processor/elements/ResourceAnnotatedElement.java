package com.creations.scimitar_processor.elements;

import com.creations.scimitar_annotations.state.ResourceObserver;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class ResourceAnnotatedElement extends AnnotatedElement {

    public ResourceAnnotatedElement(Element element) {
        super(element);
    }

    public String getId() {
        return getElement().getAnnotation(ResourceObserver.class).id();
    }

    public TypeMirror getType() {
        return ((DeclaredType) getElement().asType()).getTypeArguments().get(0);
    }

    @Override
    public String toString() {
        return "AnnotatedElement{" +
                "element=" + getElement() +
                ", id=" + getId() +
                ", type= "+ getType() +
                '}';
    }
}
