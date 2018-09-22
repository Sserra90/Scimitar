package com.creations.scimitar_processor;

import com.creations.scimitar_annotations.BindViewModel;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.tools.Diagnostic.Kind.WARNING;
import static javax.xml.bind.JAXBIntrospector.getValue;

/**
 * Scimitar custom annotation processor
 */
@AutoService(Processor.class)
public class ScimitarProcessor extends AbstractProcessor {

    private static final String SCIMITAR_SUFFIX = "$$Scimitar";
    private static final String PARAM_TARGET_NAME = "target";
    private static final String ACTIVITY_TYPE = "android.support.v4.app.FragmentActivity";
    private static final String ACTIVITY_TYPE_ANDROID_X = "androidx.fragment.app.FragmentActivity";
    private static final String FRAGMENT_TYPE = "android.app.Fragment";
    private static final String CLASS_SUFFIX = ".class";

    // target.vm = ViewModelProviders.of(target).get(com.creations.scimitar.MyViewModel.class);
    private static final String BIND_STATEMENT = "$L.$L = $T.of($L).get($L)";
    private static final ClassName VIEW_MODEL_PROVIDER_CLASS_ANDROID_X
            = ClassName.get("androidx.lifecycle", "ViewModelProviders");
    private static final ClassName VIEW_MODEL_PROVIDER_CLASS
            = ClassName.get("android.arch.lifecycle", "ViewModelProviders");

    private static final Set<String> allowedEnclosingTypes = new HashSet<>();

    private Messager mMessager;
    private Filer mFiler;
    private Types mTypeUtils;
    private Elements mElements;

    private boolean useAndroidX = false;

    public ScimitarProcessor() {
        super();
    }

    private void error(String msg) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private void warning(String msg) {
        mMessager.printMessage(Diagnostic.Kind.WARNING, msg);
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        mMessager = env.getMessager();
        mFiler = env.getFiler();
        mTypeUtils = env.getTypeUtils();
        mElements = env.getElementUtils();
        useAndroidX = hasAndroidX(mElements);

        warning("Using androidX: " + useAndroidX);
        allowedEnclosingTypes.add(useAndroidX ? ACTIVITY_TYPE_ANDROID_X : ACTIVITY_TYPE);
        allowedEnclosingTypes.add(FRAGMENT_TYPE);
    }

    /**
     * Perform two lookups to see if the androidx annotation and core libraries are on the application
     * classpath.
     */
    private static boolean hasAndroidX(Elements elementUtils) {
        boolean annotationsPresent
                = elementUtils.getTypeElement("androidx.annotation.NonNull") != null;
        boolean corePresent
                = elementUtils.getTypeElement("androidx.core.content.ContextCompat") != null;
        return annotationsPresent && corePresent;
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

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
        mMessager.printMessage(WARNING, "Process");

        final List<AnnotatedElement> annotatedElements = new ArrayList<>();

        // Parse @BindViewModel annotated fields
        final Set<VariableElement> fields = ElementFilter.fieldsIn(env.getElementsAnnotatedWith(BindViewModel.class));
        for (VariableElement field : fields) {
            parseBindViewModel(field, annotatedElements);
        }

        // Generate classes
        try {
            generateClasses(annotatedElements);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void parseBindViewModel(VariableElement field, List<AnnotatedElement> annotatedElements) {
        warning("Name: " + field.getSimpleName());
        warning("Type: " + getValue(field.getAnnotation(BindViewModel.class)));
        warning("Enclosing element: " + field.getEnclosingElement().toString());

        if (checkFieldAccessible(BindViewModel.class, field)) {
            annotatedElements.add(new AnnotatedElement(field));
        }
    }

    private boolean checkFieldAccessible(Class<? extends Annotation> annotationClass, Element element) {
        boolean isValid = true;

        final TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify field modifiers
        final Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            error(
                    String.format(
                            "@%s %s must not be private or static. (%s.%s)",
                            annotationClass.getSimpleName(),
                            "fields",
                            enclosingElement.getQualifiedName(),
                            element.getSimpleName()
                    )
            );
            isValid = false;
        }

        if (enclosingElement.getKind() != CLASS) {
            error(
                    String.format("@%s %s may only be contained in classes. (%s.%s)",
                            annotationClass.getSimpleName(),
                            "fields",
                            enclosingElement.getQualifiedName(),
                            element.getSimpleName())
            );
            isValid = false;
        }

        // Check enclosing type
        if (!isEnclosingTypeValid(element)) {
            error("Enclosing element is not valid. Should be one of: " + allowedEnclosingTypes);
            isValid = false;
        }

        return isValid;
    }

    // Verify enclosing type is valid
    private boolean isEnclosingTypeValid(Element element) {
        for (String allowed : allowedEnclosingTypes) {
            final TypeMirror allowedType = mElements.getTypeElement(allowed).asType();
            if (mTypeUtils.isAssignable(element.getEnclosingElement().asType(), allowedType)) {
                return true;
            }
        }
        return false;
    }

    private void generateClasses(List<AnnotatedElement> elements) throws IOException {
        for (AnnotatedElement element : elements) {
            MethodSpec constructor = createBindingConstructor(element.getEnclosingElement().toString(), element);
            TypeSpec binder = createClass(element.getEnclosingElement().getSimpleName().toString(), constructor);
            JavaFile javaFile = JavaFile.builder(getPackage(element.getEnclosingElement().toString()), binder).build();
            javaFile.writeTo(mFiler);
            warning("Generate java class for element: " + element);
        }
    }

    private MethodSpec createBindingConstructor(String targetTypeName, AnnotatedElement el) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(ClassName.bestGuess(targetTypeName), PARAM_TARGET_NAME);

        // Generates something like the following:
        // target.vm = ViewModelProviders.of(target).get(com.creations.scimitar.MyViewModel.class);
        builder.addStatement(BIND_STATEMENT,
                PARAM_TARGET_NAME,
                el.getName(),
                useAndroidX ? VIEW_MODEL_PROVIDER_CLASS_ANDROID_X : VIEW_MODEL_PROVIDER_CLASS,
                PARAM_TARGET_NAME,
                el.getValue().toString() + CLASS_SUFFIX
        );

        return builder.build();
    }

    private TypeSpec createClass(String className, MethodSpec constructor) {
        return TypeSpec.classBuilder(className + SCIMITAR_SUFFIX)
                .addModifiers(PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .build();
    }

    private String getPackage(String qualifier) {
        return qualifier.substring(0, qualifier.lastIndexOf("."));
    }
}
