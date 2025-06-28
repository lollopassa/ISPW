package com.biteme.app.util.mapper;

import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.entities.Prenotazione;

public class PrenotazioneMapper implements BeanEntityMapper<PrenotazioneBean, Prenotazione> {

    @Override
    public Prenotazione toEntity(PrenotazioneBean bean) {
        Prenotazione entity = new Prenotazione();
        entity.setId(bean.getId());
        entity.setNomeCliente(bean.getNomeCliente());
        entity.setData(bean.getData());
        entity.setOrario(bean.getOrario());
        entity.setNote(bean.getNote());
        entity.setEmail(bean.getEmail());
        entity.setCoperti(bean.getCoperti());
        return entity;
    }

    @Override
    public PrenotazioneBean toBean(Prenotazione entity) {
        PrenotazioneBean bean = new PrenotazioneBean();
        bean.setId(entity.getId());
        bean.setNomeCliente(entity.getNomeCliente());
        bean.setData(entity.getData());
        bean.setOrario(entity.getOrario());
        bean.setNote(entity.getNote());
        bean.setEmail(entity.getEmail());
        bean.setCoperti(entity.getCoperti());
        return bean;
    }
}