package com.biteme.app.boundary;

import com.biteme.app.controller.ArchivioController;
import com.biteme.app.exception.ArchiviazioneException;

import java.util.Map;

public class AdminHomeBoundary {

    private static final String ERR = "Periodo non valido: ";
    private final ArchivioController ctrl = new ArchivioController();

    public Map<String, Number> piattiPiuOrdinatiPerPeriodo(String periodo)
            throws ArchiviazioneException {
        try {
            return ctrl.piattiPiuOrdinatiPerPeriodo(periodo);
        } catch (IllegalArgumentException e) {
            throw new ArchiviazioneException(ERR + periodo, e);
        }
    }

    public Map<String, Number> guadagniPerPeriodo(String periodo)
            throws ArchiviazioneException {
        try {
            return ctrl.guadagniPerPeriodo(periodo);
        } catch (IllegalArgumentException e) {
            throw new ArchiviazioneException(ERR + periodo, e);
        }
    }

    public Map<String, Number> guadagniPerGiorno(String periodo)
            throws ArchiviazioneException {
        try {
            return ctrl.guadagniPerGiorno(periodo);
        } catch (IllegalArgumentException e) {
            throw new ArchiviazioneException(ERR + periodo, e);
        }
    }
}
