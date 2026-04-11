package com.bizflow.modules.catalogue.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.catalogue.dto.UnitDto;
import com.bizflow.modules.catalogue.entity.Unit;
import com.bizflow.modules.catalogue.repository.UnitRepository;
import com.bizflow.modules.catalogue.service.UnitService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;

    @Override
    public ApiResponse<List<UnitDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(unitRepository.findAllByTenantId(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<UnitDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Unit unit = unitRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));
        return ApiResponse.success(toDto(unit));
    }

    @Override
    public ApiResponse<UnitDto> create(UnitDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Unit unit = Unit.builder().tenantId(tenantId).name(dto.getName()).symbol(dto.getSymbol()).build();
        return ApiResponse.success(MessageConstant.CREATED, toDto(unitRepository.save(unit)));
    }

    @Override
    public ApiResponse<UnitDto> update(Long id, UnitDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Unit unit = unitRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));
        unit.setName(dto.getName());
        unit.setSymbol(dto.getSymbol());
        return ApiResponse.success(MessageConstant.UPDATED, toDto(unitRepository.save(unit)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Unit unit = unitRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));
        unitRepository.delete(unit);
        return ApiResponse.success(MessageConstant.DELETED, null);
    }

    private UnitDto toDto(Unit u) {
        UnitDto dto = new UnitDto();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setSymbol(u.getSymbol());
        return dto;
    }
}