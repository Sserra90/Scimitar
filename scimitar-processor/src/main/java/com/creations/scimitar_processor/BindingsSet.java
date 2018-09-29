package com.creations.scimitar_processor;

import com.creations.scimitar_processor.elements.AnnotatedElement;
import com.creations.scimitar_processor.elements.FactoryAnnotatedElement;
import com.creations.scimitar_processor.elements.ResourceAnnotatedElement;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.PUBLIC;

public class BindingsSet {

    private static final String TARGET_NAME = "target";
    private static final String DOT = ".";
    private static final String CLASS_SUFFIX = ".class";

    private static final ClassName VIEW_MODEL_PROVIDER_CLASS_ANDROID_X
            = ClassName.get("androidx.lifecycle", "ViewModelProviders");
    private static final ClassName VIEW_MODEL_PROVIDER_CLASS
            = ClassName.get("android.arch.lifecycle", "ViewModelProviders");

    private static final String ON_LOADING = "onLoading";
    private static final String ON_SUCCESS = "onSuccess";
    private static final String ON_ERROR = "onError";

    private static final ClassName STATE_OBSERVER_TYPE =
            ClassName.get("com.creations.scimitar_runtime.state", "StateObserver");
    private static final ClassName THROWABLE_TYPE = ClassName.get("java.lang", "Throwable");

    private boolean mUseAndroidX;
    private TypeElement mElement;
    private Messager mMessager;
    private Types mTypeUtils;

    private Map<TypeElement, Set<AnnotatedElement>> factoryBindings = new HashMap<>();
    private Set<AnnotatedElement> viewModelBindings = new HashSet<>();
    private Set<AnnotatedElement> observerBindings = new HashSet<>();
    private Map<String, MethodsSet> methodBindings = new HashMap<>();

    BindingsSet(boolean useAndroidX, Messager messager, Types typeUtils, TypeElement element) {
        mElement = element;
        mMessager = messager;
        mTypeUtils = typeUtils;
        mUseAndroidX = useAndroidX;
    }

    public void putFactoriesMap(Map<TypeElement, Set<AnnotatedElement>> bindings) {
        factoryBindings = bindings;
    }

    public void addViewModelBinding(AnnotatedElement viewModelBinding) {
        viewModelBindings.add(viewModelBinding);
    }

    public Set<AnnotatedElement> getViewModelBindings() {
        return viewModelBindings;
    }

    public void addObserverBinding(AnnotatedElement observerBinding) {
        observerBindings.add(observerBinding);
    }

    public Set<AnnotatedElement> getObserverBindings() {
        return observerBindings;
    }

    public void setMethodBindings(Map<String, MethodsSet> methodBindings) {
        this.methodBindings = methodBindings;
    }

    public Map<String, MethodsSet> getMethodBindings() {
        return methodBindings;
    }

    public MethodSpec makeItHappen() {
        return createConstructor();
    }

    private MethodSpec createConstructor() {

        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(ClassName.bestGuess(mElement.toString()), TARGET_NAME);

        // Generates something like the following:
        // target.vm = ViewModelProviders.of(target,factory).get(com.creations.scimitar.MyViewModel.class);
        for (AnnotatedElement el : viewModelBindings) {
            final AnnotatedElement factory = findViewModelFactory(el);
            builder.addStatement("$L.$L = $T.of($L,$L).get($L)",
                    TARGET_NAME,
                    el.getName(),
                    mUseAndroidX ? VIEW_MODEL_PROVIDER_CLASS_ANDROID_X : VIEW_MODEL_PROVIDER_CLASS,
                    TARGET_NAME,
                    factory != null ? TARGET_NAME + DOT + factory.getName() : "null",
                    el.getElement().asType() + CLASS_SUFFIX
            );
        }

        observerBindings.forEach(observer -> {
            final String id = ((ResourceAnnotatedElement) observer).getId();
            if (methodBindings.containsKey(id)) {
                TypeSpec stateObserverSpec = createResourceObserverType(id, methodBindings.get(id));
                if (stateObserverSpec != null) {
                    builder.addStatement("$L.$L = $L",
                            TARGET_NAME,
                            observer.getElement(),
                            stateObserverSpec
                    );
                }
            }
        });

        return builder.build();
    }

    private ResourceAnnotatedElement findResourceObserverForId(String id) {
        for (AnnotatedElement observer : observerBindings) {
            if (((ResourceAnnotatedElement) observer).getId().equals(id)) {
                return (ResourceAnnotatedElement) observer;
            }
        }
        return null;
    }

    private TypeName parseType(TypeMirror type) {

        // If it's not parameterized use bestGuess to return the type
        if (!isParameterized(type)){
            return ClassName.bestGuess(type.toString());
        }

        final List<ClassName> classNames = new ArrayList<>();
        final List<TypeMirror> typeArguments = new ArrayList<>();
        typeArguments.add(mTypeUtils.erasure(type)); // Add erased parent type
        getTypeArguments(type, typeArguments);

        for (TypeMirror typeArgument : typeArguments) {
            classNames.add(ClassName.bestGuess(typeArgument.toString()));
        }

        return buildParameterizedType(classNames);
    }

    private void getTypeArguments(TypeMirror type, List<TypeMirror> types) {
        final List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();

        for (TypeMirror typeArgument : typeArguments) {
            if (isParameterized(typeArgument)) {
                types.add(mTypeUtils.erasure(typeArgument));
                getTypeArguments(typeArgument, types);
            } else {
                types.add(typeArgument);
            }
        }
    }

    private ParameterizedTypeName buildParameterizedType(List<ClassName> classNames) {
        mMessager.printMessage(Diagnostic.Kind.WARNING, "Build parameterized type for: " + classNames);
        return ParameterizedTypeName.get(
                classNames.get(0),
                classNames.size() > 2
                        ? buildParameterizedType(classNames.subList(1, classNames.size()))
                        : classNames.get(1)
        );
    }

    private TypeSpec createResourceObserverType(String id, MethodsSet methodsSet) {

        final ResourceAnnotatedElement observer = findResourceObserverForId(id);
        if (observer == null) {
            return null;
        }

        TypeName stateTypeParam = parseType(observer.getType());

        final TypeSpec.Builder stateObserverBuilder = TypeSpec
                .anonymousClassBuilder("")
                .superclass(ParameterizedTypeName.get(STATE_OBSERVER_TYPE, stateTypeParam));

        if (methodsSet.success() != null) {
            stateObserverBuilder.addMethod(
                    buildMethod(
                            ON_SUCCESS, ParameterSpec.builder(stateTypeParam, "data").build(),
                            "$L.$L($N)",
                            TARGET_NAME,
                            methodsSet.success().getName(),
                            "data"
                    ));
        }

        if (methodsSet.error() != null) {
            stateObserverBuilder.addMethod(
                    buildMethod(
                            ON_ERROR, ParameterSpec.builder(THROWABLE_TYPE, "error").build(),
                            "$L.$L($N)",
                            TARGET_NAME,
                            methodsSet.error().getName(),
                            "error"
                    )
            );
        }

        if (methodsSet.loading() != null) {
            stateObserverBuilder.addMethod(
                    buildMethod(
                            ON_LOADING, null, "$L.$L()",
                            TARGET_NAME, methodsSet.loading().getName()
                    )
            );
        }

        return stateObserverBuilder.build();
    }

    private AnnotatedElement findViewModelFactory(AnnotatedElement el) {

        // If there's one specified in the enclosing class use it.
        Set<AnnotatedElement> factories = factoryBindings.get(el.getEnclosingElement());
        if (factories != null && !factories.isEmpty()) {
            return factories.iterator().next(); // Use the first in list
        }

        // Traverse class hierarchy to look for a @ViewModelFactory annotated field with "useAsDefault"
        return findParentFactory(el.getEnclosingElement(), factoryBindings);
    }

    // Traverse class hierarchy to look for a @ViewModelFactory annotated field with "useAsDefault = true"
    private AnnotatedElement findParentFactory(TypeElement el, Map<TypeElement, Set<AnnotatedElement>> factoryBindings) {

        TypeMirror typeMirror = el.getSuperclass();
        if (typeMirror.getKind() == TypeKind.NONE) {
            return null;
        }

        TypeElement parentType = (TypeElement) ((DeclaredType) typeMirror).asElement();
        Set<AnnotatedElement> parentFactories = factoryBindings.get(parentType);
        if (parentFactories != null) {
            for (AnnotatedElement parentFactory : parentFactories) {
                if (((FactoryAnnotatedElement) parentFactory).useAsDefault())
                    return parentFactory;
            }
        }

        return findParentFactory(parentType, factoryBindings);
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

    private static boolean isParameterized(TypeMirror typeMirror) {
        return !((DeclaredType) typeMirror).getTypeArguments().isEmpty();
    }

    @Override
    public String toString() {
        return "BindingsSet{" +
                "mElement=" + mElement +
                ", factories=" + factoryBindings +
                ", viewModelBindings=" + viewModelBindings +
                ", observerBindings=" + observerBindings +
                ", methodBindings=" + methodBindings +
                '}';
    }
}
