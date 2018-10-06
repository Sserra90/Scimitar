package com.creations.scimitar_processor;

import com.creations.scimitar_annotations.BindViewModel;
import com.creations.scimitar_annotations.ViewModelFactory;
import com.creations.scimitar_annotations.state.OnError;
import com.creations.scimitar_annotations.state.OnLoading;
import com.creations.scimitar_annotations.state.OnSuccess;
import com.creations.scimitar_annotations.state.ResourceObserver;
import com.creations.scimitar_processor.elements.AnnotatedElement;
import com.creations.scimitar_processor.elements.FactoryAnnotatedElement;
import com.creations.scimitar_processor.elements.ResourceAnnotatedElement;
import com.creations.scimitar_processor.elements.ViewModelAnnotatedElement;
import com.creations.scimitar_processor.elements.methods.MethodElement;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
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

/**
 * Scimitar custom annotation processor
 */
@AutoService(Processor.class)
public class ScimitarProcessor extends AbstractProcessor {

    private static final String SCIMITAR_SUFFIX = "$$Scimitar";
    private static final String ACTIVITY_TYPE = "android.support.v4.app.FragmentActivity";
    private static final String ACTIVITY_TYPE_ANDROID_X = "androidx.fragment.app.FragmentActivity";
    private static final String FRAGMENT_TYPE = "android.app.Fragment";
    private static final String FRAGMENT_SUPPORT_TYPE = " android.support.v4.app.Fragment";

    private static final String VIEW_MODEL_FACTORY_ANDROID_X = "androidx.lifecycle.ViewModelProvider.Factory";
    private static final String VIEW_MODEL_FACTORY = "android.arch.lifecycle.ViewModelProvider.Factory";

    private static final String THROWABLE = "java.lang.Throwable";
    private static final ClassName THROWABLE_TYPE = ClassName.get("java.lang", "Throwable");

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
        allowedEnclosingTypes.add(FRAGMENT_SUPPORT_TYPE);
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

        final Map<TypeElement, BindingsSet> bindingsMap = new HashMap<>();

        // Parse @BindViewModel annotated fields
        Set<VariableElement> fields = ElementFilter.fieldsIn(env.getElementsAnnotatedWith(BindViewModel.class));
        for (VariableElement field : fields) {
            parseBindViewModel(field, bindingsMap);
        }

        // Parse @ViewModelFactory annotated fields
        final Map<TypeElement, Set<AnnotatedElement>> factoriesMap = new HashMap<>();
        fields = ElementFilter.fieldsIn(env.getElementsAnnotatedWith(ViewModelFactory.class));
        for (VariableElement field : fields) {
            parseViewModelFactory(field, factoriesMap);
        }
        bindingsMap.values().forEach(bindingsSet -> bindingsSet.putFactoriesMap(factoriesMap));

        // Parse @ResourceObserver annotated fields
        fields = ElementFilter.fieldsIn(env.getElementsAnnotatedWith(ResourceObserver.class));
        for (VariableElement field : fields) {
            parseResourceObserver(field, bindingsMap);
        }

        // Parse @OnSuccess annotated methods
        Set<ExecutableElement> methods = ElementFilter.methodsIn(env.getElementsAnnotatedWith(OnSuccess.class));
        for (ExecutableElement method : methods) {
            parseOnSuccessMethod(method, bindingsMap);
        }

        // Parse @OnError annotated methods
        methods = ElementFilter.methodsIn(env.getElementsAnnotatedWith(OnError.class));
        for (ExecutableElement method : methods) {
            parseOnErrorMethod(method, bindingsMap);
        }

        // Parse @OnLoading annotated methods
        methods = ElementFilter.methodsIn(env.getElementsAnnotatedWith(OnLoading.class));
        for (ExecutableElement method : methods) {
            parseOnLoadingMethod(method, bindingsMap);
        }

        // Parse superclasses recursively
        for (TypeElement el : bindingsMap.keySet()) {
            findParent(el, bindingsMap.get(el).getViewModelBindings(), bindingsMap);
        }

        print(bindingsMap, factoriesMap);

        // Generate classes
        generateClasses(bindingsMap);

        return true;
    }

    private void parseBindViewModel(VariableElement field, Map<TypeElement, BindingsSet> bindingsMap) {
        if (checkFieldAccessible(BindViewModel.class, field)) {

            final AnnotatedElement el = new ViewModelAnnotatedElement(field);

            if (!bindingsMap.containsKey(el.getEnclosingElement())) {
                bindingsMap.put(
                        el.getEnclosingElement(),
                        new BindingsSet(
                                useAndroidX, mMessager,
                                mTypeUtils, el.getEnclosingElement()
                        )
                );
            }
            bindingsMap.get(el.getEnclosingElement()).addViewModelBinding(el);
        }
    }

    private void parseViewModelFactory(VariableElement field, Map<TypeElement, Set<AnnotatedElement>> factoriesMap) {
        final TypeMirror factoryType = mElements.getTypeElement(
                useAndroidX ? VIEW_MODEL_FACTORY_ANDROID_X : VIEW_MODEL_FACTORY
        ).asType();

        if (checkFieldAccessible(ViewModelFactory.class, field) && isAssignableTo(field.asType(), factoryType)) {

            final AnnotatedElement el = new FactoryAnnotatedElement(field);
            if (!factoriesMap.containsKey(el.getEnclosingElement())) {
                factoriesMap.put(el.getEnclosingElement(), new HashSet<>());
            }

            factoriesMap.get(el.getEnclosingElement()).add(el);
        }
    }

    private void parseResourceObserver(VariableElement field, Map<TypeElement, BindingsSet> bindingsMap) {
        if (checkFieldAccessible(ResourceObserver.class, field)) {
            final ResourceAnnotatedElement el = new ResourceAnnotatedElement(field);

            if (!bindingsMap.containsKey(el.getEnclosingElement())) {
                bindingsMap.put(
                        el.getEnclosingElement(),
                        new BindingsSet(
                                useAndroidX, mMessager,
                                mTypeUtils, el.getEnclosingElement()
                        )
                );
            }
            bindingsMap.get(el.getEnclosingElement()).addObserverBinding(el);
        }
    }

    private void parseOnSuccessMethod(ExecutableElement method, Map<TypeElement, BindingsSet> bindingsMap) {

        warning("\nParse onSuccess method: " + method);

        final MethodElement methodEl = MethodElement.create(method);
        final TypeElement enclosing = methodEl.getEnclosingElement();

        // Check is not private or static
        if (isPrivateOrStatic(method)) {
            error(String.format("Method %s cannot be private or static", method.getSimpleName()));
            return;
        }

        // Check class has @ResourceObserver annotated fields
        if (bindingsMap.containsKey(enclosing) && bindingsMap.get(enclosing).getObserverBindings().isEmpty()) {
            error(String.format("No @ResourceObserver annotated fields were found in class: %s", enclosing));
            return;
        }

        // Find @ResourceObserver annotated field with the same id
        final List<AnnotatedElement> resObservers = findResourceObserversForId(
                bindingsMap.get(enclosing).getObserverBindings(),
                methodEl.getId()
        );

        if (resObservers.isEmpty()) {
            error(String.format("No @ResourceObserver annotated fields were found in class: %s " +
                    "for id: %s", enclosing, methodEl.getId())
            );
            return;
        }

        // Check parameter is valid
        final int paramsNr = method.getParameters().size();
        if (paramsNr > 1) {
            error(String.format(Locale.US,
                    "Incorrect number of parameters for method %s, %d parameters found.",
                    method, paramsNr)
            );
            return;

        }

        addMethod(methodEl, bindingsMap);
    }

    private void parseOnErrorMethod(ExecutableElement method, Map<TypeElement, BindingsSet> bindingsMap) {

        warning("\nParse onError method: " + method);

        if (initialValidation(method, bindingsMap)) {

            final int paramsNr = method.getParameters().size();
            if (paramsNr > 1) {
                error(String.format(Locale.US,
                        "Incorrect number of parameters for method %s, %d parameters found.",
                        method, paramsNr)
                );
                return;
            } else if (paramsNr == 1) {

                final TypeMirror paramType = method.getParameters().get(0).asType();
                final TypeMirror throwableType = mElements.getTypeElement(THROWABLE).asType();

                if (!mTypeUtils.isSameType(paramType, throwableType)) {
                    error(String.format(
                            Locale.US,
                            "Type mismatch. @OnError method %s expects %s got %s",
                            method, THROWABLE_TYPE, paramType
                    ));
                    return;
                }
            }

            addMethod(MethodElement.create(method), bindingsMap);
        }

    }

    private void parseOnLoadingMethod(ExecutableElement method, Map<TypeElement, BindingsSet> bindingsMap) {

        warning("\nParse onLoading method: " + method);

        if (initialValidation(method, bindingsMap)) {

            // Check parameter is valid
            final int paramsNr = method.getParameters().size();
            if (!method.getParameters().isEmpty()) {
                error(String.format(Locale.US,
                        "@OnLoading method %s takes no parameters. Got %d parameters.",
                        method, paramsNr)
                );
                return;
            }

            addMethod(MethodElement.create(method), bindingsMap);
        }
    }

    private boolean initialValidation(ExecutableElement method, Map<TypeElement, BindingsSet> bindingsMap) {
        final MethodElement methodEl = MethodElement.create(method);
        final TypeElement enclosing = methodEl.getEnclosingElement();

        // Method cannot be private
        if (isPrivateOrStatic(method)) {
            error(String.format(
                    "Method %s cannot be private or static",
                    method.getSimpleName()
                    )
            );
            return false;
        }

        // Check class has @ResourceObserver annotated fields
        if (bindingsMap.containsKey(enclosing) && bindingsMap.get(enclosing).getObserverBindings().isEmpty()) {
            error(String.format("No @ResourceObserver annotated fields were found in class: %s", enclosing));
            return false;
        }

        // Find @ResourceObserver annotated field with the same id
        final List<AnnotatedElement> resObservers = findResourceObserversForId(
                bindingsMap.get(enclosing).getObserverBindings(),
                methodEl.getId()
        );

        if (resObservers.isEmpty()) {
            error(String.format("No @ResourceObserver annotated fields were found in class: %s " +
                    "for id: %s", enclosing, methodEl.getId())
            );
            return false;
        }

        return true;
    }

    private List<AnnotatedElement> findResourceObserversForId(Set<AnnotatedElement> resObservers, String id) {
        if (resObservers == null) {
            return new ArrayList<>();
        }
        return resObservers.stream()
                .filter(e -> ((ResourceAnnotatedElement) e).getId().equals(id))
                .collect(Collectors.toList());
    }

    private void addMethod(MethodElement method, Map<TypeElement, BindingsSet> bindingsMap) {

        final Map<String, MethodsSet> bindings = bindingsMap.containsKey(method.getEnclosingElement())
                ? bindingsMap.get(method.getEnclosingElement()).getMethodBindings()
                : new HashMap<>();

        final MethodsSet methodsSet = bindings.containsKey(method.getId())
                ? bindings.get(method.getId())
                : new MethodsSet();

        methodsSet.addMethod(method);
        bindings.put(method.getId(), methodsSet);
        bindingsMap.get(method.getEnclosingElement()).setMethodBindings(bindings);
    }

    private boolean checkFieldAccessible(Class<? extends Annotation> annotationClass, Element element) {
        boolean isValid = true;

        final TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify field modifiers
        if (isPrivateOrStatic(element)) {
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
                            Set<AnnotatedElement> elements,
                            Map<TypeElement, BindingsSet> bindingsMap) {

        TypeMirror typeMirror = type.getSuperclass();
        if (typeMirror.getKind() == TypeKind.NONE) {
            return;
        }

        TypeElement parentType = (TypeElement) ((DeclaredType) typeMirror).asElement();
        Set<AnnotatedElement> parentElements = bindingsMap.containsKey(parentType)
                ? bindingsMap.get(parentType).getViewModelBindings()
                : null;

        if (parentElements != null) {
            elements.addAll(parentElements);
        }

        findParent(parentType, elements, bindingsMap);
    }

    private void generateClasses(final Map<TypeElement, BindingsSet> bindingsMap) {
        bindingsMap.forEach((typeElement, bindingsSet) -> {
            try {
                writeClass(typeElement, bindingsSet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void writeClass(final TypeElement typeElement, final BindingsSet bindingsSet) throws IOException {
        MethodSpec constructor = bindingsSet.makeItHappen();
        TypeSpec binder = createClass(typeElement.getSimpleName().toString(), constructor);

        JavaFile javaFile = JavaFile.builder(getPackage(typeElement.toString()), binder).build();
        javaFile.writeTo(mFiler);

        warning("Generated java class: " + typeElement.getSimpleName() + SCIMITAR_SUFFIX);
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

    private static boolean isPrivateOrStatic(Element el) {
        final Set<Modifier> modifiers = el.getModifiers();
        return modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC);
    }

    private void print(Map<TypeElement, BindingsSet> bindingsMap,
                       Map<TypeElement, Set<AnnotatedElement>> factoriesMap) {

        StringBuilder sb = new StringBuilder();
        bindingsMap.forEach((typeElement, bindingsSet) -> {

            sb.append("Type: ").append(typeElement.toString()).append("\n");

            sb.append("--- View Models: \n");
            bindingsSet.getViewModelBindings().forEach(el -> sb.append("----- View Model: ").append(el.toString()).append("\n"));

            sb.append("--- Resource observers: \n");
            bindingsSet.getObserverBindings().forEach(el -> sb.append("----- Resource observer: ").append(el.toString()).append("\n"));

            sb.append("--- Annotated Methods: \n");
            bindingsSet.getMethodBindings().forEach((s, methodsSet) -> {
                sb.append("----- Id: ").append(s).append("\n");
                sb.append("------- Methods: ").append(methodsSet).append("\n");
            });

        });

        sb.append("--- Factories: \n");
        factoriesMap.forEach((typeElement1, el) -> {
            sb.append("Type: ").append(typeElement1.toString()).append("\n");
            el.forEach(f -> sb.append("--- Factory: ").append(f.toString()).append("\n"));
        });

        warning(sb.append("\n").toString());
    }
}
