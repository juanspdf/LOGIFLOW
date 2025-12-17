package espe.edu.ec.fleet_service.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CedulaEcuadorValidator implements ConstraintValidator<CedulaEcuador, String> {

    @Override
    public boolean isValid(String cedula, ConstraintValidatorContext context) {
        if (cedula == null || cedula.length() != 10 || !cedula.matches("\\d+")) {
            return false;
        }

        try {
            int provincia = Integer.parseInt(cedula.substring(0, 2));
            if (provincia < 1 || provincia > 24) {
                return false;
            }

            int tercerDigito = Integer.parseInt(cedula.substring(2, 3));
            if (tercerDigito >= 6) {
                return false;
            }

            int[] coeficientes = {2, 1, 2, 1, 2, 1, 2, 1, 2};
            int suma = 0;
            int digitoVerificador = Integer.parseInt(cedula.substring(9, 10));

            for (int i = 0; i < 9; i++) {
                int valor = Integer.parseInt(cedula.substring(i, i + 1)) * coeficientes[i];
                if (valor >= 10) {
                    valor -= 9;
                }
                suma += valor;
            }

            int residuo = suma % 10;
            int resultado = (residuo == 0) ? 0 : 10 - residuo;

            return resultado == digitoVerificador;

        } catch (NumberFormatException e) {
            return false;
        }
    }
}