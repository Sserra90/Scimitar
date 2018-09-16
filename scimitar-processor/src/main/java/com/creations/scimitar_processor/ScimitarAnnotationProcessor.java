package com.creations.scimitar_processor;

import com.creations.scimitar_annotations.BindViewModel;
import com.google.auto.service.AutoService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * Scimitar custom annotation processor
 */
@AutoService(Processor.class)
public class ScimitarAnnotationProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Filer mFiler;

    public ScimitarAnnotationProcessor() {
        super();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mMessager.printMessage(WARNING,"Process");

        for(Element el : roundEnvironment.getElementsAnnotatedWith(BindViewModel.class)){
            mMessager.printMessage(WARNING,"Found el: "+el);

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

}
