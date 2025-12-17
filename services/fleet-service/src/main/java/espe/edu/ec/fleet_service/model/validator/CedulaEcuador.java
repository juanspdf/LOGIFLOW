package espe.edu.ec.fleet_service.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CedulaEcuadorValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CedulaEcuador {
    String message() default "La cédula ecuatoriana no es válida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}