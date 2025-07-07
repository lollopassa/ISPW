package com.biteme.app.util.mapper;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.entities.TipoOrdinazione;
import com.biteme.app.exception.OrdinazioneException;

public class OrdinazioneMapper implements BeanEntityMapper<OrdinazioneBean, Ordinazione> {

    @Override
    public Ordinazione toEntity(OrdinazioneBean bean) {
        try {
            bean.validate();
        } catch (OrdinazioneException ex) {
            throw new IllegalArgumentException("Dati ordinazione non validi: " + ex.getMessage(), ex);
        }

        TipoOrdinazione tipo = "Al Tavolo".equalsIgnoreCase(bean.getTipoOrdine())
                ? TipoOrdinazione.AL_TAVOLO
                : TipoOrdinazione.ASPORTO;

        int nClienti = (bean.getNumeroClienti() == null || bean.getNumeroClienti().isBlank())
                ? 0
                : Integer.parseInt(bean.getNumeroClienti());

        StatoOrdinazione stato = (bean.getStatoOrdine() == null || bean.getStatoOrdine().isBlank())
                ? StatoOrdinazione.NUOVO
                : StatoOrdinazione.valueOf(bean.getStatoOrdine().toUpperCase());

        return new Ordinazione(
                bean.getId(),
                bean.getNome(),
                nClienti == 0 ? "" : String.valueOf(nClienti),
                tipo,
                bean.getInfoTavolo(),
                stato,
                bean.getOrarioCreazione()
        );
    }

    @Override
    public OrdinazioneBean toBean(Ordinazione entity) {
        OrdinazioneBean b = new OrdinazioneBean();
        b.setId(entity.getId());
        b.setNome(entity.getNomeCliente());
        b.setNumeroClienti(entity.getNumeroClienti());
        b.setTipoOrdine(entity.getTipoOrdine() == TipoOrdinazione.AL_TAVOLO ? "Al Tavolo" : "Asporto");
        b.setInfoTavolo(entity.getInfoTavolo());
        b.setStatoOrdine(entity.getStatoOrdine().name());
        b.setOrarioCreazione(entity.getOrarioCreazione());
        return b;
    }
}
