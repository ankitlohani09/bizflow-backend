package com.bizflow.modules.restaurant.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.enums.MovementDirection;
import com.bizflow.common.enums.MovementType;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.catalogue.entity.Item;
import com.bizflow.modules.catalogue.entity.ItemVariant;
import com.bizflow.modules.catalogue.repository.ItemRepository;
import com.bizflow.modules.catalogue.repository.ItemVariantRepository;
import com.bizflow.modules.inventory.dto.StockMovementDto;
import com.bizflow.modules.inventory.service.StockMovementService;
import com.bizflow.modules.restaurant.dto.KitchenOrderItemRequest;
import com.bizflow.modules.restaurant.dto.KitchenOrderItemResponse;
import com.bizflow.modules.restaurant.dto.KitchenOrderRequest;
import com.bizflow.modules.restaurant.dto.KitchenOrderResponse;
import com.bizflow.modules.restaurant.entity.KitchenOrder;
import com.bizflow.modules.restaurant.entity.KitchenOrderItem;
import com.bizflow.modules.restaurant.enums.KitchenOrderStatus;
import com.bizflow.modules.restaurant.repository.KitchenOrderItemRepository;
import com.bizflow.modules.restaurant.repository.KitchenOrderRepository;
import com.bizflow.modules.restaurant.service.KitchenOrderService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KitchenOrderServiceImpl implements KitchenOrderService {

    private final KitchenOrderRepository kitchenOrderRepository;
    private final KitchenOrderItemRepository kitchenOrderItemRepository;
    private final ItemRepository itemRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final StockMovementService stockMovementService;

    @Override
    public ApiResponse<List<KitchenOrderResponse>> getAll(KitchenOrderStatus status) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        List<KitchenOrder> orders = status == null
                ? kitchenOrderRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId)
                : kitchenOrderRepository.findAllByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status);
        return ApiResponse.success(orders.stream().map(this::toResponse).toList());
    }

    @Override
    public ApiResponse<KitchenOrderResponse> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        KitchenOrder order = kitchenOrderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen order", id));
        return ApiResponse.success(toResponse(order));
    }

    @Override
    @Transactional
    public ApiResponse<KitchenOrderResponse> create(KitchenOrderRequest request) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        long seq = kitchenOrderRepository.countByTenantId(tenantId) + 1;
        String orderNumber = "KOT-" + String.format("%05d", seq);

        KitchenOrder order = KitchenOrder.builder().tenantId(tenantId).orderNumber(orderNumber)
                .tableNo(request.getTableNo()).customerName(request.getCustomerName()).status(KitchenOrderStatus.PLACED)
                .totalAmount(BigDecimal.ZERO).notes(request.getNotes()).build();
        order = kitchenOrderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (KitchenOrderItemRequest itemRequest : request.getItems()) {
                Item item = itemRepository.findByIdAndTenantId(itemRequest.getItemId(), tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.ITEM_NOT_FOUND));
                ItemVariant variant = itemRequest.getVariantId() == null ? null
                        : itemVariantRepository.findByIdAndTenantId(itemRequest.getVariantId(), tenantId).orElse(null);

                BigDecimal qty = safe(itemRequest.getQuantity());
                BigDecimal price = safe(itemRequest.getUnitPrice());
                BigDecimal lineTotal = qty.multiply(price);

                KitchenOrderItem orderItem = KitchenOrderItem.builder().tenantId(tenantId).kitchenOrder(order)
                        .item(item).variant(variant).quantity(qty).unitPrice(price).lineTotal(lineTotal)
                        .notes(itemRequest.getNotes()).build();
                kitchenOrderItemRepository.save(orderItem);
                total = total.add(lineTotal);

                StockMovementDto movementDto = new StockMovementDto();
                movementDto.setItemId(item.getId());
                movementDto.setVariantId(variant != null ? variant.getId() : null);
                movementDto.setMovementType(MovementType.SALE);
                movementDto.setDirection(MovementDirection.OUT);
                movementDto.setQuantity(qty);
                movementDto.setReferenceType("KITCHEN_ORDER");
                movementDto.setReferenceId(order.getId());
                movementDto.setNotes("Kitchen dispatch for order " + order.getOrderNumber());
                stockMovementService.create(movementDto);
            }
        }

        order.setTotalAmount(total);
        order = kitchenOrderRepository.save(order);
        return ApiResponse.success(MessageConstant.CREATED, toResponse(order));
    }

    @Override
    public ApiResponse<KitchenOrderResponse> updateStatus(Long id, KitchenOrderStatus status) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        KitchenOrder order = kitchenOrderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen order", id));
        order.setStatus(status);
        order = kitchenOrderRepository.save(order);
        return ApiResponse.success(MessageConstant.UPDATED, toResponse(order));
    }

    private KitchenOrderResponse toResponse(KitchenOrder order) {
        KitchenOrderResponse response = new KitchenOrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setTableNo(order.getTableNo());
        response.setCustomerName(order.getCustomerName());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(kitchenOrderItemRepository.findAllByKitchenOrderId(order.getId()).stream()
                .map(this::toItemResponse).toList());
        return response;
    }

    private KitchenOrderItemResponse toItemResponse(KitchenOrderItem item) {
        KitchenOrderItemResponse response = new KitchenOrderItemResponse();
        response.setId(item.getId());
        response.setItemId(item.getItem().getId());
        response.setItemName(item.getItem().getName());
        response.setVariantId(item.getVariant() != null ? item.getVariant().getId() : null);
        response.setVariantName(item.getVariant() != null ? item.getVariant().getVariantName() : null);
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setLineTotal(item.getLineTotal());
        response.setNotes(item.getNotes());
        return response;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
