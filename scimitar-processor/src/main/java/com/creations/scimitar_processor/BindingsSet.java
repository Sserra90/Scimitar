package com.creations.scimitar_processor;

import com.creations.scimitar_processor.elements.AnnotatedElement;
import com.creations.scimitar_processor.elements.FactoryAnnotatedElement;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.Modifier.PUBLIC;

public class BindingsSet {

    private static final String PARAM_TARGET_NAME = "target";
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

    private static final String ON_LOADING = "onLoading";
    private static final String ON_SUCCESS = "onSuccess";
    private static final String ON_ERROR = "onError";

    private static final ClassName STATE_OBSERVER_TYPE =
            ClassName.get("com.creations.scimitar_runtime.state", "StateObserver");
    private static final ClassName THROWABLE_TYPE = ClassName.get("java.lang", "Throwable");

    private boolean useAndroidX;
    private TypeElement element;
    private AnnotatedElement factory;
    private Map<TypeElement, AnnotatedElement> factoryBindings;
    private List<AnnotatedElement> viewModelBindings = new ArrayList<>();
    private List<AnnotatedElement> observerBindings = new ArrayList<>();
    private Map<String, MethodsSet> methodBindings = new HashMap<>();

    BindingsSet(Boolean useAndroidX, TypeElement element, Map<TypeElement, AnnotatedElement> factoryBindings) {
        this.element = element;
        this.useAndroidX = useAndroidX;
        this.factoryBindings = factoryBindings;
    }

    public void setFactory(AnnotatedElement factory) {
        this.factory = factory;
    }

    public void setViewModelBindings(List<AnnotatedElement> viewModelBindings) {
        this.viewModelBindings = viewModelBindings;
    }

    public void setObserverBindings(List<AnnotatedElement> observerBindings) {
        this.observerBindings = observerBindings;
    }

    public void setMethodBindings(Map<String, MethodsSet> methodBindings) {
        this.methodBindings = methodBindings;
    }

    public MethodSpec makeItHappen() {
        return createConstructor();
    }

    private MethodSpec createConstructor() {

        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(ClassName.bestGuess(element.toString()), PARAM_TARGET_NAME);

        // Generates something like the following:
        // target.vm = ViewModelProviders.of(target).get(com.creations.scimitar.MyViewModel.class);
        for (AnnotatedElement el : viewModelBindings) {

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

        createResourceObserversType().forEach((typeSpec) ->
                builder.addStatement("$L.$L = $L",
                        PARAM_TARGET_NAME,
                        "usersObserver",
                        typeSpec
                ));

        return builder.build();
    }

    private List<TypeSpec> createResourceObserversType() {
        final List<TypeSpec> stateObservers = new ArrayList<>();
        methodBindings.forEach((id, methodsSet) -> {

            ClassName stateTypeParam = ClassName.get("com.creations.scimitar", "User");
            final TypeSpec.Builder stateObserverBuilder = TypeSpec
                    .anonymousClassBuilder("")
                    .superclass(ParameterizedTypeName.get(STATE_OBSERVER_TYPE, stateTypeParam));

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

            stateObservers.add(stateObserverBuilder.build());
        });
        return stateObservers;
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

    @Override
    public String toString() {
        return "BindingsSet{" +
                "element=" + element +
                ", factory=" + factory +
                ", viewModelBindings=" + viewModelBindings +
                ", observerBindings=" + observerBindings +
                ", methodBindings=" + methodBindings +
                '}';
    }
}
