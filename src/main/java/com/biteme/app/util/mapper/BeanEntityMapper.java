package com.biteme.app.util.mapper;

public interface BeanEntityMapper<B, E> {
    E toEntity(B bean);
    B toBean(E entity);
}
