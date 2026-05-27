package io.github.jxch.platform.frp.client.core.repository;

import io.github.jxch.platform.frp.client.core.model.ProxyMapping;

import java.util.List;
import java.util.Optional;

public interface ProxyMappingRepository {
    List<ProxyMapping> findAll();
    Optional<ProxyMapping> findByName(String name);
    ProxyMapping save(ProxyMapping mapping);
    void deleteByName(String name);
    boolean existsByName(String name);
    void replaceAll(List<ProxyMapping> mappings);
}
