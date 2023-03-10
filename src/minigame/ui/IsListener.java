package minigame.ui;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({FIELD})
public @interface IsListener {
    String id();

    /**
     * @return "enter"||"click"||"exit"||"menu"
     */
    String type() default "click";
}
