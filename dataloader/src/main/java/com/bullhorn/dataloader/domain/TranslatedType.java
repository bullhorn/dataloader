package bullhorn.dataloader.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TranslatedType {
	
	public boolean isDate() default false;
	public boolean isID() default false;

}
