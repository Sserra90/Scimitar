package com.creations.scimitar_processor;

import javax.lang.model.element.Element;

class ViewModelAnnotatedElement extends AnnotatedElement {

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
