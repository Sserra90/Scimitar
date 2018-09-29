package com.creations.scimitar_processor.elements;

import com.creations.scimitar_annotations.state.ResourceObserver;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class ResourceAnnotatedElement extends AnnotatedElement {

    private String id;
    private TypeMirror type;

    public ResourceAnnotatedElement(Element element) {
        super(element);
        id = getElement().getAnnotation(ResourceObserver.class).id();
        type = ((DeclaredType) getElement().asType()).getTypeArguments().get(0);
    }

    public String getId() {
        return id;
    }

    public TypeMirror getType() {
        return type;
    }

    @Override
    public String toString() {
        return "AnnotatedElement{" +
                "element=" + getElement() +
                ", id=" + getId() +
                ", type= "+ getType() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ResourceAnnotatedElement that = (ResourceAnnotatedElement) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), id, type);
    }
}
