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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * Scimitar custom annotation processor
 */
@AutoService(Processor.class)
public class ScimitarAnnotationProcessor extends AbstractProcessor {

    private static final String SCIMITAR_SUFFIX = "_Scimitar";
    private static final String CONST_PARAM_TARGET_NAME = "target";

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
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
        mMessager.printMessage(WARNING, "Process");

        final Set<VariableElement> fields = ElementFilter.fieldsIn(env.getElementsAnnotatedWith(BindViewModel.class));
        for (VariableElement field : fields) {
            parseBindViewModel(field);
        }

        return true;
    }

    private void parseBindViewModel(VariableElement field) {

        printWarning("Name: " + field.getSimpleName());
        printWarning("Type: " + getValue(field.getAnnotation(BindViewModel.class)));
        printWarning("Enclosing element: " + field.getEnclosingElement().toString());

        if (!checkFieldAccessible(BindViewModel.class, field)) {
            return;
        }



    }

    private boolean checkFieldAccessible(Class<? extends Annotation> annotationClass, Element element) {
        boolean hasError = false;

        final TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify field modifiers
        final Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            printError(
                    String.format(
                            "@%s %s must not be private or static. (%s.%s)",
                            annotationClass.getSimpleName(),
                            "fields",
                            enclosingElement.getQualifiedName(),
                            element.getSimpleName()
                    )
            );
            hasError = true;
        }

        return hasError;
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
