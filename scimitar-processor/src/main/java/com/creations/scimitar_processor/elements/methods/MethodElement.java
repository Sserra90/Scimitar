package com.creations.scimitar_processor.elements.methods;

import com.creations.scimitar_annotations.state.OnLoading;
import com.creations.scimitar_annotations.state.OnSuccess;
import com.creations.scimitar_processor.elements.AnnotatedElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * @author Sérgio Serra on 23/09/2018.
 */
public abstract class MethodElement extends AnnotatedElement {

    public enum Type {
        ERROR, SUCCESS, LOADING
    }

    private ExecutableElement executableElement;

    MethodElement(Element element) {
        super(element);
        executableElement = (ExecutableElement) element;
    }

    public abstract Type type();

    public abstract String getId();

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
}
