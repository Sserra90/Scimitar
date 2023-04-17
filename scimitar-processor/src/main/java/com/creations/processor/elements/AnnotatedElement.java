package com.creations.processor.elements;

import java.util.Objects;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

public abstract class AnnotatedElement {

    private Element element;

    public AnnotatedElement(Element _element) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotatedElement that = (AnnotatedElement) o;
        return Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }
}
