package com.biteme.app.util.mapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry of all Bean ↔ Entity mappers.
 */
public class BeanEntityMapperFactory {

    private static BeanEntityMapperFactory instance = null;

    private final Map<Class<?>, BeanEntityMapper<?, ?>> beanToEntityMappers   = new HashMap<>();
    private final Map<Class<?>, BeanEntityMapper<?, ?>> entityToBeanMappers   = new HashMap<>();

    private BeanEntityMapperFactory() {
        registerMapper(com.biteme.app.bean.PrenotazioneBean.class,
                com.biteme.app.entities.Prenotazione.class,
                new PrenotazioneMapper());

        registerMapper(com.biteme.app.bean.ProdottoBean.class,
                com.biteme.app.entities.Prodotto.class,
                new ProdottoMapper());

        registerMapper(com.biteme.app.bean.ArchivioBean.class,
                com.biteme.app.entities.Archivio.class,
                new ArchivioMapper());



        registerMapper(com.biteme.app.bean.OrdinazioneBean.class,
                com.biteme.app.entities.Ordinazione.class,
                new OrdinazioneMapper());

        registerMapper(com.biteme.app.bean.OrdineBean.class,
                com.biteme.app.entities.Ordine.class,
                new OrdineMapper());
    }

    public static synchronized BeanEntityMapperFactory getInstance() {
        if (instance == null) {
            instance = new BeanEntityMapperFactory();
        }
        return instance;
    }


    public <B, E> void registerMapper(Class<B> beanClass,
                                      Class<E> entityClass,
                                      BeanEntityMapper<B, E> mapper) {
        beanToEntityMappers.put(beanClass, mapper);
        entityToBeanMappers.put(entityClass, mapper);
    }


    @SuppressWarnings("unchecked")
    public <B, E> E toEntity(B bean, Class<B> beanClass) {
        BeanEntityMapper<B, E> mapper =
                (BeanEntityMapper<B, E>) beanToEntityMappers.get(beanClass);
        if (mapper == null) {
            throw new IllegalArgumentException(
                    "No mapper registered for Bean class: " + beanClass.getName());
        }
        return mapper.toEntity(bean);
    }


    @SuppressWarnings("unchecked")
    public <B, E> B toBean(E entity, Class<B> beanClass) {
        BeanEntityMapper<B, E> mapper =
                (BeanEntityMapper<B, E>) beanToEntityMappers.get(beanClass);
        if (mapper == null) {
            throw new IllegalArgumentException(
                    "No mapper registered for Bean class: " + beanClass.getName());
        }
        return mapper.toBean(entity);
    }
}
