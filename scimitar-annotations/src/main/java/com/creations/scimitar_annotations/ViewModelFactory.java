package com.creations.scimitar_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the ViewModelFactory to use when binding view models
 * <pre><code>
 * {@literal @}ViewModelFactory ViewModelFactory vmFactory;
 * </code></pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewModelFactory {
    boolean useAsDefault() default false;
}
