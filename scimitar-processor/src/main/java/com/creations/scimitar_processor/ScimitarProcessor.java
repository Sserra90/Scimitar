package com.creations.scimitar_processor;

import com.creations.scimitar_annotations.BindViewModel;
import com.creations.scimitar_annotations.ViewModelFactory;
import com.creations.scimitar_processor.elements.AnnotatedElement;
import com.creations.scimitar_processor.elements.FactoryAnnotatedElement;
import com.creations.scimitar_processor.elements.ViewModelAnnotatedElement;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
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
    private static final String VIEW_MODEL_FACTORY_ANDROID_X = "androidx.lifecycle.ViewModelProvider.Factory";
    private static final String VIEW_MODEL_FACTORY = "android.arch.lifecycle.ViewModelProvider.Factory";

    private static final String DOT = ".";
    private static final String CLASS_SUFFIX = ".class";

    // target.vm = ViewModelProviders.of(target).get(com.creations.scimitar.MyViewModel.class);
    private static final String BIND_STATEMENT = "$L.$L = $T.of($L).get($L)";
    // target.vm = ViewModelProviders.of(target,factory).get(com.creations.scimitar.MyViewModel.class);
    private static final String BIND_STATEMENT_WITH_FACTORY = "$L.$L = $T.of($L,$L).get($L)";

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

        final Map<TypeElement, AnnotatedElement> factoryBindings = new HashMap<>();
        final Map<TypeElement, List<AnnotatedElement>> bindingsMap = new HashMap<>();

        // Parse @BindViewModel annotated fields
        Set<VariableElement> fields = ElementFilter.fieldsIn(env.getElementsAnnotatedWith(BindViewModel.class));
        for (VariableElement field : fields) {
            parseBindViewModel(field, bindingsMap);
        }

        // Parse @ViewModelFactory annotated fields
        fields = ElementFilter.fieldsIn(env.getElementsAnnotatedWith(ViewModelFactory.class));
        for (VariableElement field : fields) {
            parseViewModelFactory(field, factoryBindings);
        }

        warning("\nFactory bindings: " + prettyPrint(factoryBindings));

        // Parse superclasses recursively
        for (TypeElement el : bindingsMap.keySet()) {
            findParent(el, bindingsMap.get(el), bindingsMap);
        }

        warning("\nFinal bindings: " + prettyPrint(bindingsMap));

        // Generate classes
        generateClasses(bindingsMap, factoryBindings);

        return true;
    }

    private void parseBindViewModel(VariableElement field, Map<TypeElement, List<AnnotatedElement>> bindingsMap) {
        warning("Name: " + field.getSimpleName());
        warning("Type: " + getValue(field.getAnnotation(BindViewModel.class)));
        warning("Enclosing element: " + field.getEnclosingElement().toString());

        if (checkFieldAccessible(BindViewModel.class, field)) {

            final AnnotatedElement el = new ViewModelAnnotatedElement(field);
            if (!bindingsMap.containsKey(el.getEnclosingElement())) {
                bindingsMap.put(el.getEnclosingElement(), new ArrayList<>());
            }
            bindingsMap.get(el.getEnclosingElement()).add(el);
        }
    }

    private void parseViewModelFactory(VariableElement field, Map<TypeElement, AnnotatedElement> bindingsMap) {
        final TypeMirror factoryType = mElements.getTypeElement(
                useAndroidX ? VIEW_MODEL_FACTORY_ANDROID_X : VIEW_MODEL_FACTORY
        ).asType();

        if (checkFieldAccessible(ViewModelFactory.class, field) && isAssignableTo(field.asType(), factoryType)) {
            bindingsMap.put((TypeElement) field.getEnclosingElement(), new FactoryAnnotatedElement(field));
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
            if (isAssignableTo(element.getEnclosingElement().asType(), allowedType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAssignableTo(TypeMirror type, TypeMirror type2) {
        return mTypeUtils.isAssignable(type, type2);
    }

    private void findParent(TypeElement type,
                            List<AnnotatedElement> elements,
                            Map<TypeElement, List<AnnotatedElement>> bindings) {

        TypeMirror typeMirror = type.getSuperclass();
        if (typeMirror.getKind() == TypeKind.NONE) {
            return;
        }

        TypeElement parentType = (TypeElement) ((DeclaredType) typeMirror).asElement();
        List<AnnotatedElement> parentElements = bindings.get(parentType);
        if (parentElements != null) {
            elements.addAll(parentElements);
        }

        findParent(parentType, elements, bindings);
    }

    private void generateClasses(final Map<TypeElement, List<AnnotatedElement>> bindings,
                                 final Map<TypeElement, AnnotatedElement> factoryBindings) {

        bindings.forEach((typeElement, annotatedElements) -> {
            try {
                writeClass(typeElement, annotatedElements, factoryBindings);
            } catch (IOException e) {
                error(e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void writeClass(TypeElement typeElement,
                            List<AnnotatedElement> annotatedElements,
                            Map<TypeElement, AnnotatedElement> factoryBindings) throws IOException {

        MethodSpec constructor = createBindingConstructor(typeElement.toString(), annotatedElements, factoryBindings);
        TypeSpec binder = createClass(typeElement.getSimpleName().toString(), constructor);
        JavaFile javaFile = JavaFile.builder(getPackage(typeElement.toString()), binder).build();
        javaFile.writeTo(mFiler);
        warning("Generated java class: " + typeElement.getSimpleName() + SCIMITAR_SUFFIX);
    }

    private MethodSpec createBindingConstructor(String targetTypeName,
                                                List<AnnotatedElement> annotatedElements,
                                                Map<TypeElement, AnnotatedElement> factoryBindings) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(ClassName.bestGuess(targetTypeName), PARAM_TARGET_NAME);

        // Generates something like the following:
        // target.vm = ViewModelProviders.of(target).get(com.creations.scimitar.MyViewModel.class);

        for (AnnotatedElement el : annotatedElements) {

            final AnnotatedElement factory = findViewModelFactory(el, factoryBindings);
            if (factory != null) {
                builder.addStatement(BIND_STATEMENT_WITH_FACTORY,
                        PARAM_TARGET_NAME,
                        el.getName(),
                        useAndroidX ? VIEW_MODEL_PROVIDER_CLASS_ANDROID_X : VIEW_MODEL_PROVIDER_CLASS,
                        PARAM_TARGET_NAME,
                        PARAM_TARGET_NAME + DOT + factory.getName(),
                        el.getElement().asType() + CLASS_SUFFIX
                );
            } else {
                builder.addStatement(BIND_STATEMENT,
                        PARAM_TARGET_NAME,
                        el.getName(),
                        useAndroidX ? VIEW_MODEL_PROVIDER_CLASS_ANDROID_X : VIEW_MODEL_PROVIDER_CLASS,
                        PARAM_TARGET_NAME,
                        el.getElement().asType() + CLASS_SUFFIX
                );
            }

        }

        return builder.build();
    }

    private AnnotatedElement findViewModelFactory(AnnotatedElement el, Map<TypeElement, AnnotatedElement> factoryBindings) {

        // If there's one specified in the enclosing class use it.
        final AnnotatedElement factory = factoryBindings.get(el.getEnclosingElement());
        if (factory != null) {
            return factory;
        }

        // Traverse class hierarchy to look for a @ViewModelFactory annotated field with "useAsDefault"
        return findParentFactory(el.getEnclosingElement(), factoryBindings);
    }

    // Traverse class hierarchy to look for a @ViewModelFactory annotated field with "useAsDefault = true"
    private AnnotatedElement findParentFactory(TypeElement el, Map<TypeElement, AnnotatedElement> factoryBindings) {
        TypeMirror typeMirror = el.getSuperclass();
        if (typeMirror.getKind() == TypeKind.NONE) {
            return null;
        }

        TypeElement parentType = (TypeElement) ((DeclaredType) typeMirror).asElement();
        FactoryAnnotatedElement parentFactory = (FactoryAnnotatedElement) factoryBindings.get(parentType);
        if (parentFactory != null && parentFactory.useAsDefault()) {
            return parentFactory;
        }

        return findParentFactory(parentType, factoryBindings);
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

    private <K, V> String prettyPrint(Map<K, V> map) {
        return new PrettyPrintingMap<>(map).toString();
    }

    public static class PrettyPrintingMap<K, V> {
        private Map<K, V> map;

        public PrettyPrintingMap(Map<K, V> map) {
            this.map = map;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<K, V> entry = iter.next();
                sb.append(entry.getKey());
                sb.append('=').append('"');
                sb.append(entry.getValue());
                sb.append('"');
                if (iter.hasNext()) {
                    sb.append(',').append(' ');
                }
            }
            return sb.toString();

        }
    }
}
