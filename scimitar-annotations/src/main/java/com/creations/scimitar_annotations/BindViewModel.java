package com.creations.scimitar_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bind a field to the specified ViewModel class
 * <pre><code>
 * {@literal @}BindViewModel(SomeViewModel.class) BindViewModel vm;
 * {@literal @}BindViewModel() BindViewModel vm;
 * </code></pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindViewModel {
    Class value();
}
