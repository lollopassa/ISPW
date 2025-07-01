package com.biteme.app.util.mapper;

import java.util.HashMap;
import java.util.Map;

public class BeanEntityMapperFactory {

    private static BeanEntityMapperFactory instance = null;

    private final Map<Class<?>, BeanEntityMapper<?, ?>> beanToEntityMappers = new HashMap<>();
    private final Map<Class<?>, BeanEntityMapper<?, ?>> entityToBeanMappers = new HashMap<>();

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

        registerMapper(com.biteme.app.bean.ArchivioRigaBean.class,
                com.biteme.app.entities.ArchivioRiga.class,
                new ArchivioRigaMapper());
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
            throw new IllegalArgumentException("Nessun mapper registrato per il Bean: " + beanClass);
        }
        return mapper.toEntity(bean);
    }

    @SuppressWarnings("unchecked")
    public <B, E> B toBean(E entity, Class<B> beanClass) {
        BeanEntityMapper<B, E> mapper =
                (BeanEntityMapper<B, E>) entityToBeanMappers.get(entity.getClass());
        if (mapper == null) {
            throw new IllegalArgumentException("Nessun mapper registrato per l'Entity: " + entity.getClass());
        }
        return mapper.toBean(entity);
    }
}
