package com.creations.scimitar_processor;

import com.creations.scimitar_annotations.BindViewModel;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

public class AnnotatedElement {

    private Element element;
    private TypeMirror value;

    public AnnotatedElement(Element _element) {
        element = _element;
        value = getValue(element.getAnnotation(BindViewModel.class));
    }

    public Element getElement() {
        return element;
    }

    public TypeMirror getValue() {
        return value;
    }

    public Name getName() {
        return element.getSimpleName();
    }

    public TypeElement getEnclosingElement() {
        return (TypeElement) element.getEnclosingElement();
    }

    // Just using an hack to get the Class type
    private static TypeMirror getValue(BindViewModel annotation) {
        try {
            annotation.value();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
        return null;
    }

    @Override
    public String toString() {
        return "AnnotatedElement{" +
                "element=" + element +
                ", value=" + value +
                '}';
    }
}
