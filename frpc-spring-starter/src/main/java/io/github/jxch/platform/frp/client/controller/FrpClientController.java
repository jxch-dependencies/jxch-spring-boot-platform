package io.github.jxch.platform.frp.client.controller;

import io.github.jxch.platform.frp.client.core.model.ProxyMapping;
import io.github.jxch.platform.frp.client.core.model.ReconcileReport;
import io.github.jxch.platform.frp.client.core.service.FrpClientService;
import io.github.jxch.platform.frp.client.dto.CreateMappingRequest;
import io.github.jxch.platform.frp.client.dto.UpdateMappingRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${spring.frp.client.management.base-path:/internal/frp/client}")
public class FrpClientController {

    private final FrpClientService service;

    public FrpClientController(FrpClientService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public Object status() {
        return service.status();
    }

    @GetMapping("/mappings")
    public List<ProxyMapping> listMappings() {
        return service.listMappings();
    }

    @GetMapping("/mappings/{name}")
    public ProxyMapping getMapping(@PathVariable String name) {
        return service.getMapping(name);
    }

    @PostMapping("/mappings")
    public ProxyMapping createMapping(@Valid @RequestBody CreateMappingRequest request) {
        return service.createMapping(request);
    }

    @PutMapping("/mappings/{name}")
    public ProxyMapping updateMapping(@PathVariable String name,
                                      @Valid @RequestBody UpdateMappingRequest request) {
        return service.updateMapping(name, request);
    }

    @DeleteMapping("/mappings/{name}")
    public void deleteMapping(@PathVariable String name) {
        service.deleteMapping(name);
    }

    @PostMapping("/reconcile")
    public ReconcileReport reconcile() {
        return service.reconcile("manual");
    }

    @PostMapping("/restart")
    public void restart() {
        service.restart();
    }
}


