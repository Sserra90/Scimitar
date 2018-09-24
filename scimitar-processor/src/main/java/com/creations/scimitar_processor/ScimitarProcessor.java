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
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

    private static final String ON_LOADING = "onLoading";
    private static final String ON_SUCCESS = "onSuccess";
    private static final String ON_ERROR = "onError";

    private static final ClassName STATE_OBSERVER_TYPE =
            ClassName.get("com.creations.scimitar_runtime.state", "StateObserver");
    private static final String THROWABLE = "java.lang.Throwable";
    private static final ClassName THROWABLE_TYPE = ClassName.get("java.lang", "Throwable");

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

        final Map<TypeElement, AnnotatedElement> factoryBindings = new HashMap<>();
        final Map<TypeElement, List<AnnotatedElement>> bindingsMap = new HashMap<>();
        final Map<TypeElement, List<AnnotatedElement>> observerBindings = new HashMap<>();
        final Map<TypeElement, Map<String, MethodsSet>> methodBindings = new HashMap<>();

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

        // Parse @ResourceObserver annotated fields
        fields = ElementFilter.fieldsIn(env.getElementsAnnotatedWith(ResourceObserver.class));
        for (VariableElement field : fields) {
            parseResourceObserver(field, observerBindings);
        }

        // Parse @OnSuccess annotated methods
        Set<ExecutableElement> methods = ElementFilter.methodsIn(env.getElementsAnnotatedWith(OnSuccess.class));
        for (ExecutableElement method : methods) {
            parseOnSuccessMethod(method, observerBindings, methodBindings);
        }

        // Parse @OnError annotated methods
        methods = ElementFilter.methodsIn(env.getElementsAnnotatedWith(OnError.class));
        for (ExecutableElement method : methods) {
            parseOnErrorMethod(method, observerBindings, methodBindings);
        }

        // Parse @OnLoading annotated methods
        methods = ElementFilter.methodsIn(env.getElementsAnnotatedWith(OnLoading.class));
        for (ExecutableElement method : methods) {
            parseOnLoadingMethod(method, observerBindings, methodBindings);
        }

        // Parse superclasses recursively
        for (TypeElement el : bindingsMap.keySet()) {
            findParent(el, bindingsMap.get(el), bindingsMap);
        }

        warning("\nFactory bindings: " + prettyPrint(factoryBindings));
        warning("\nObserver bindings: " + prettyPrint(observerBindings));
        warning("\nMethod bindings: " + prettyPrint(methodBindings));
        warning("\nFinal bindings: " + prettyPrint(bindingsMap));

        // Generate classes
        generateClasses(bindingsMap, factoryBindings, observerBindings, methodBindings);

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

    private void parseResourceObserver(VariableElement field, Map<TypeElement, List<AnnotatedElement>> bindingsMap) {
        if (checkFieldAccessible(ResourceObserver.class, field)) {
            final ResourceAnnotatedElement el = new ResourceAnnotatedElement(field);

            if (!bindingsMap.containsKey(el.getEnclosingElement())) {
                bindingsMap.put(el.getEnclosingElement(), new ArrayList<>());
            }
            bindingsMap.get(el.getEnclosingElement()).add(el);
        }
    }

    private void parseOnSuccessMethod(ExecutableElement method,
                                      Map<TypeElement, List<AnnotatedElement>> observers,
                                      Map<TypeElement, Map<String, MethodsSet>> resourceBindings) {

        warning("\nParse onSuccess method: " + method);

        final MethodElement methodEl = MethodElement.create(method);
        final TypeElement enclosing = methodEl.getEnclosingElement();

        // Check is not private or static
        if (isPrivateOrStatic(method)) {
            error(String.format("Method %s cannot be private or static", method.getSimpleName()));
            return;
        }

        // Check class has @ResourceObserver annotated fields
        if (!observers.containsKey(enclosing)) {
            error(String.format("No @ResourceObserver annotated fields were found in class: %s", enclosing));
            return;
        }

        // Find @ResourceObserver annotated field with the same id
        final List<AnnotatedElement> resObservers = findResourceObserversForId(observers.get(enclosing), methodEl.getId());
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
        } else if (paramsNr == 1) {

            final TypeMirror paramType = method.getParameters().get(0).asType();
            final TypeMirror resType = ((ResourceAnnotatedElement) resObservers.get(0)).getType();

            if (!mTypeUtils.isSameType(paramType, resType)) {
                error(String.format(
                        Locale.US,
                        "Type mismatch. " +
                                "@ResourceObserver field %s has type %s, @OnSuccess method %s expects %s",
                        resObservers.get(0).getName(), resType, method, paramType
                ));
                return;
            }
        }

        addMethod(methodEl, resourceBindings);
    }

    private void parseOnErrorMethod(ExecutableElement method,
                                    Map<TypeElement, List<AnnotatedElement>> observers,
                                    Map<TypeElement, Map<String, MethodsSet>> resourceBindings) {

        warning("\nParse onError method: " + method);

        if (initialValidation(method, observers)) {

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

            addMethod(MethodElement.create(method), resourceBindings);
        }

    }

    private void parseOnLoadingMethod(ExecutableElement method,
                                      Map<TypeElement, List<AnnotatedElement>> observers,
                                      Map<TypeElement, Map<String, MethodsSet>> resourceBindings) {

        warning("\nParse onLoading method: " + method);

        if (initialValidation(method, observers)) {

            // Check parameter is valid
            final int paramsNr = method.getParameters().size();
            if (!method.getParameters().isEmpty()) {
                error(String.format(Locale.US,
                        "@OnLoading method %s takes no parameters. Got %d parameters.",
                        method, paramsNr)
                );
                return;
            }

            addMethod(MethodElement.create(method), resourceBindings);
        }
    }

    private boolean initialValidation(ExecutableElement method, Map<TypeElement, List<AnnotatedElement>> observers) {
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
        if (!observers.containsKey(enclosing)) {
            error(String.format("No @ResourceObserver annotated fields were found in class: %s", enclosing));
            return false;
        }

        // Find @ResourceObserver annotated field with the same id
        final List<AnnotatedElement> resObservers = findResourceObserversForId(observers.get(enclosing), methodEl.getId());
        if (resObservers.isEmpty()) {
            error(String.format("No @ResourceObserver annotated fields were found in class: %s " +
                    "for id: %s", enclosing, methodEl.getId())
            );
            return false;
        }

        return true;
    }

    private List<AnnotatedElement> findResourceObserversForId(List<AnnotatedElement> resObservers, String id) {
        if (resObservers == null) {
            return new ArrayList<>();
        }
        return resObservers.stream()
                .filter(e -> ((ResourceAnnotatedElement) e).getId().equals(id))
                .collect(Collectors.toList());
    }

    private void addMethod(MethodElement method, Map<TypeElement, Map<String, MethodsSet>> resourceBindings) {
        final Map<String, MethodsSet> bindings = resourceBindings.containsKey(method.getEnclosingElement())
                ? resourceBindings.get(method.getEnclosingElement())
                : new HashMap<>();

        final MethodsSet methodsSet = bindings.containsKey(method.getId())
                ? bindings.get(method.getId())
                : new MethodsSet();

        methodsSet.addMethod(method);
        bindings.put(method.getId(), methodsSet);
        resourceBindings.put(method.getEnclosingElement(), bindings);
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
                                 final Map<TypeElement, AnnotatedElement> factoryBindings,
                                 final Map<TypeElement, List<AnnotatedElement>> observerBindings,
                                 final Map<TypeElement, Map<String, MethodsSet>> methodBindings) {

        final Map<TypeElement, TypeSpec> stateObservers = new HashMap<>();

        observerBindings.forEach(((typeElement, observers) -> observers.forEach(el -> {

            ResourceAnnotatedElement observer = (ResourceAnnotatedElement) el;

            if (methodBindings.containsKey(typeElement)) {

                ClassName stateTypeParam = ClassName.get("com.creations.scimitar", "User");
                final TypeSpec.Builder stateObserverBuilder = TypeSpec
                        .anonymousClassBuilder("")
                        .superclass(ParameterizedTypeName.get(STATE_OBSERVER_TYPE, stateTypeParam));

                final MethodsSet methodsSet = methodBindings.get(typeElement).get(observer.getId());
                if (methodsSet != null) {
                    if (methodsSet.success() != null) {
                        stateObserverBuilder.addMethod(
                                buildMethod(
                                        ON_SUCCESS, ParameterSpec.builder(stateTypeParam, "data").build(),
                                        "$L.$L($N)",
                                        PARAM_TARGET_NAME,
                                        methodsSet.success().getName(),
                                        "data"
                                ));
                    }

                    if (methodsSet.error() != null) {
                        stateObserverBuilder.addMethod(
                                buildMethod(
                                        ON_ERROR, ParameterSpec.builder(THROWABLE_TYPE, "error").build(),
                                        "$L.$L($N)",
                                        PARAM_TARGET_NAME,
                                        methodsSet.error().getName(),
                                        "error"
                                )
                        );
                    }

                    if (methodsSet.loading() != null) {
                        stateObserverBuilder.addMethod(
                                buildMethod(
                                        ON_LOADING, null, "$L.$L()",
                                        PARAM_TARGET_NAME, methodsSet.loading().getName()
                                )
                        );
                    }
                }

                stateObservers.put(
                        typeElement,
                        stateObserverBuilder.build()
                );
            }
        })));

        bindings.forEach((typeElement, annotatedElements) -> {

            try {
                writeClass(typeElement, annotatedElements, factoryBindings, stateObservers);
            } catch (IOException e) {
                error(e.getMessage());
                e.printStackTrace();
            }

        });
    }

    private void writeClass(TypeElement typeElement,
                            List<AnnotatedElement> annotatedElements,
                            Map<TypeElement, AnnotatedElement> factoryBindings,
                            Map<TypeElement, TypeSpec> stateObservers) throws IOException {

        MethodSpec constructor = createBindingConstructor(
                typeElement,
                annotatedElements,
                factoryBindings,
                stateObservers
        );

        TypeSpec binder = createClass(typeElement.getSimpleName().toString(), constructor);

        JavaFile javaFile = JavaFile.builder(getPackage(typeElement.toString()), binder).build();
        javaFile.writeTo(mFiler);

        warning("Generated java class: " + typeElement.getSimpleName() + SCIMITAR_SUFFIX);
    }

    private MethodSpec createBindingConstructor(TypeElement enclosing,
                                                List<AnnotatedElement> annotatedElements,
                                                Map<TypeElement, AnnotatedElement> factoryBindings,
                                                Map<TypeElement, TypeSpec> stateObservers) {

        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(ClassName.bestGuess(enclosing.toString()), PARAM_TARGET_NAME);

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

        if (stateObservers.get(enclosing) != null) {
            stateObservers.forEach((fieldName, typeSpec) ->
                    builder.addStatement("$L.$L = $L",
                            PARAM_TARGET_NAME,
                            "usersObserver",
                            typeSpec
                    ));
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

    private static boolean isPrivateOrStatic(Element el) {
        final Set<Modifier> modifiers = el.getModifiers();
        return modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC);
    }

    private static MethodSpec buildMethod(String name, ParameterSpec paramSpec, String statement, Object... args) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(name)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        if (paramSpec != null) {
            method.addParameter(paramSpec).build();
        }
        method.addStatement(statement, args);
        return method.build();
    }

    private <K, V> String prettyPrint(Map<K, V> map) {
        return new PrettyPrintingMap<>(map).toString();
    }

    public static class PrettyPrintingMap<K, V> {
        private Map<K, V> map;

        PrettyPrintingMap(Map<K, V> map) {
            this.map = map;
        }

        private <A> String printCol(Collection<A> c) {
            StringBuilder sb = new StringBuilder();
            c.forEach(o -> sb.append("\n").append(o.toString()));
            return sb.toString();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<K, V> entry = iter.next();
                sb.append(entry.getKey());
                sb.append('=');
                if (entry.getValue() instanceof Collection) {
                    sb.append("   ").append(printCol((Collection) entry.getValue()));
                } else {
                    sb.append(entry.getValue());
                }
                if (iter.hasNext()) {
                    sb.append('\n');
                }
            }
            return sb.toString();
        }
    }
}
