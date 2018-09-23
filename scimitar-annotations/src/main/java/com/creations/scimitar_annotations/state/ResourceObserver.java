package com.creations.scimitar_annotations.state;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bind a field with a StateObserver
 * <pre><code>
 * {@literal @}ResourceObserver() StateObserver stateObs;
 * </code></pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceObserver {
    String id() default "";
}
