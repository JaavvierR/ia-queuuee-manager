package org.acme.ports.test_cases_ports;

import java.awt.image.BufferedImage;

public abstract class ProcessContextUiPort {
    /**
     * Procesa el contexto del usuario y genera una respuesta.
     * @param inputData Datos de entrada del usuario.
     * @return Respuesta generada.
     */
    protected abstract Object processContext(Object inputData);

    /**
     * Genera una descripción de la imagen.
     * @param image Imagen a describir.
     * @return Descripción generada.
     */
    protected abstract String generateImageDescription(BufferedImage image);

    /**
     * Valida el contexto proporcionado por el usuario.
     * @param descripcion Descripción a validar.
     * @return true si el contexto es válido, false en caso contrario.
     */
    protected abstract boolean validateUiContext(String descripcion);
}