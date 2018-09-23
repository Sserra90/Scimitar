package com.creations.scimitar_processor;

import com.creations.scimitar_annotations.BindViewModel;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

public class ViewModelAnnotatedElement extends AnnotatedElement {

    ViewModelAnnotatedElement(Element element) {
        super(element);
    }

    // Just using an hack to get the Class type
    /*private static TypeMirror getValue(BindViewModel annotation) {
        try {
            annotation.value();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
        return null;
    }*/


}
