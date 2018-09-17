package com.creations.scimitar_processor;

import com.creations.scimitar_annotations.BindViewModel;
import com.google.auto.service.AutoService;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * Scimitar custom annotation processor
 */
@AutoService(Processor.class)
public class ScimitarAnnotationProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Filer mFiler;
    private Types mTypeUtils;

    public ScimitarAnnotationProcessor() {
        super();
    }

    private void printError(String msg) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private void printWarning(String msg) {
        mMessager.printMessage(Diagnostic.Kind.WARNING, msg);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
        mTypeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mMessager.printMessage(WARNING, "Process");

        /*for(Element el : roundEnvironment.getElementsAnnotatedWith(BindViewModel.class)){
            mMessager.printMessage(WARNING,"Found el: "+el);
            mMessager.printMessage(WARNING,"Element type: "+el.getSimpleName());

        }*/

        final Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindViewModel.class);
        final Set<VariableElement> fields = ElementFilter.fieldsIn(elements);
        for (VariableElement field : fields) {
            String fullTypeClassName = field.asType().toString();

            printWarning("Found el: " + field);
            printWarning("Element type: " + fullTypeClassName);

            printWarning("Type: " + getValue(field.getAnnotation(BindViewModel.class)));
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindViewModel.class.getCanonicalName());
        return Collections.unmodifiableSet(types);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
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

}
