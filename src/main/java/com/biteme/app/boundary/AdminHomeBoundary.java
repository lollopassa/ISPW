package com.biteme.app.boundary;

import com.biteme.app.controller.ArchivioController;
import com.biteme.app.exception.ArchiviazioneException;
import java.util.Map;

public class AdminHomeBoundary {

    private static final String ERR_MSG_PERIOD = "Periodo non valido: ";

    private final ArchivioController archivioController = new ArchivioController();



    public Map<String, Number> piattiPiuOrdinatiPerPeriodo(String periodo) throws ArchiviazioneException {
        try {
            return archivioController.piattiPiuOrdinatiPerPeriodo(periodo);
        } catch (IllegalArgumentException e) {
            throw new ArchiviazioneException(ERR_MSG_PERIOD + periodo, e);
        }
    }

    public Map<String, Number> guadagniPerPeriodo(String periodo) throws ArchiviazioneException {
        try {
            return archivioController.guadagniPerPeriodo(periodo);
        } catch (IllegalArgumentException e) {
            throw new ArchiviazioneException(ERR_MSG_PERIOD + periodo, e);
        }
    }

    public Map<String, Number> guadagniPerGiorno(String periodo) throws ArchiviazioneException {
        try {
            return archivioController.guadagniPerGiorno(periodo);
        } catch (IllegalArgumentException e) {
            throw new ArchiviazioneException(ERR_MSG_PERIOD + periodo, e);
        }
    }
}