package com.warnes.erp.modules.catalog.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.dto.request.KitItemRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.KitItemResponseDTO;
import com.warnes.erp.modules.catalog.entity.KitItem;
import com.warnes.erp.modules.catalog.entity.Product;
import com.warnes.erp.modules.catalog.mapper.KitItemMapper;
import com.warnes.erp.modules.catalog.repository.ProductRepository;
import com.warnes.erp.modules.catalog.service.KitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KitServiceImpl implements KitService {

    private final ProductRepository productRepository;
    private final KitItemMapper kitItemMapper;

    @Override
    @Transactional
    public List<KitItemResponseDTO> addOrUpdateKitItem(UUID parentUuid, KitItemRequestDTO request) {
        if (parentUuid.equals(request.childProductUuid())) {
            throw new BusinessRuleException("Un kit no puede contenerse a sí mismo.");
        }

        Product parentProduct = getValidKitParent(parentUuid);
        Product childProduct = productRepository.findByUuid(request.childProductUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Producto componente no encontrado."));

        if (childProduct.isKit()) {
            throw new BusinessRuleException("No se permite anidar kits. El componente no puede ser otro kit.");
        }

        // Buscar si el ítem ya existe en el kit para actualizar cantidad, o crear uno nuevo
        KitItem kitItem = parentProduct.getKitItems().stream()
                .filter(item -> item.getChildProduct().getId().equals(childProduct.getId()))
                .findFirst()
                .orElseGet(() -> {
                    KitItem newItem = new KitItem();
                    newItem.setParentProduct(parentProduct);
                    newItem.setChildProduct(childProduct);
                    parentProduct.getKitItems().add(newItem);
                    return newItem;
                });

        kitItem.setQuantity(request.quantity());
        productRepository.save(parentProduct);

        return parentProduct.getKitItems().stream()
                .map(kitItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeKitItem(UUID parentUuid, UUID childUuid) {
        Product parentProduct = getValidKitParent(parentUuid);

        boolean removed = parentProduct.getKitItems().removeIf(item ->
                item.getChildProduct().getUuid().equals(childUuid));

        if (!removed) {
            throw new ResourceNotFoundException("El componente no pertenece a este kit.");
        }

        productRepository.save(parentProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitItemResponseDTO> getKitItems(UUID parentUuid) {
        Product parentProduct = getValidKitParent(parentUuid);
        return parentProduct.getKitItems().stream()
                .map(kitItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private Product getValidKitParent(UUID parentUuid) {
        Product parentProduct = productRepository.findByUuid(parentUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Producto padre no encontrado."));

        if (!parentProduct.isKit()) {
            throw new BusinessRuleException("El producto seleccionado no está configurado como KIT (is_kit = false).");
        }
        return parentProduct;
    }
}