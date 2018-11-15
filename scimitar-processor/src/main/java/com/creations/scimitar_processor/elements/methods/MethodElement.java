package com.creations.scimitar_processor.elements.methods;

import com.creations.scimitar_annotations.OnLoading;
import com.creations.scimitar_annotations.OnSuccess;
import com.creations.scimitar_processor.elements.AnnotatedElement;

import java.util.Objects;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * @author Sérgio Serra on 23/09/2018.
 */
public abstract class MethodElement extends AnnotatedElement {

    public enum Type {
        ERROR, SUCCESS, LOADING
    }

    private Type type;
    private String id;

    private ExecutableElement executableElement;

    public abstract Type type();

    public abstract String getId();

    MethodElement(Element element) {
        super(element);
        executableElement = (ExecutableElement) element;
        type = type();
        id = getId();
    }

    public static MethodElement create(ExecutableElement el) {
        if (el.getAnnotation(OnLoading.class) != null) {
            return new LoadingMethod(el);
        } else if (el.getAnnotation(OnSuccess.class) != null) {
            return new SuccessMethod(el);
        } else {
            return new ErrorMethod(el);
        }
    }

    @Override
    public String toString() {
        return "MethodElement{" +
                "executableElement=" + executableElement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MethodElement that = (MethodElement) o;
        return type == that.type &&
                Objects.equals(id, that.id) &&
                Objects.equals(executableElement, that.executableElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, id, executableElement);
    }
}
