package io.github.jxch.platform.frp.client.core.repository;

import io.github.jxch.platform.frp.client.core.model.ProxyMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProxyMappingRepository implements ProxyMappingRepository {

    private final ConcurrentHashMap<String, ProxyMapping> store = new ConcurrentHashMap<>();

    @Override
    public List<ProxyMapping> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(ProxyMapping::name))
                .toList();
    }

    @Override
    public Optional<ProxyMapping> findByName(String name) {
        return Optional.ofNullable(store.get(name));
    }

    @Override
    public ProxyMapping save(ProxyMapping mapping) {
        store.put(mapping.name(), mapping);
        return mapping;
    }

    @Override
    public void deleteByName(String name) {
        store.remove(name);
    }

    @Override
    public boolean existsByName(String name) {
        return store.containsKey(name);
    }

    @Override
    public void replaceAll(List<ProxyMapping> mappings) {
        store.clear();
        for (ProxyMapping mapping : new ArrayList<>(mappings)) {
            store.put(mapping.name(), mapping);
        }
    }
}
