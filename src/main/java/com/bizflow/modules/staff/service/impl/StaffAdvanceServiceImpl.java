package com.bizflow.modules.staff.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.modules.staff.dto.StaffAdvanceDto;
import com.bizflow.modules.staff.entity.Staff;
import com.bizflow.modules.staff.entity.StaffAdvance;
import com.bizflow.modules.staff.repository.StaffAdvanceRepository;
import com.bizflow.modules.staff.repository.StaffRepository;
import com.bizflow.modules.staff.service.StaffAdvanceService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffAdvanceServiceImpl implements StaffAdvanceService {

    private final StaffAdvanceRepository advanceRepository;
    private final StaffRepository staffRepository;

    @Override
    public ApiResponse<List<StaffAdvanceDto>> getByStaff(Long staffId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                advanceRepository.findAllByStaffIdAndTenantId(staffId, tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<StaffAdvanceDto> create(StaffAdvanceDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Staff staff = staffRepository.findByIdAndTenantId(dto.getStaffId(), tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.STAFF_NOT_FOUND));

        StaffAdvance advance = StaffAdvance.builder().tenantId(tenantId).staff(staff).amount(dto.getAmount())
                .advanceDate(dto.getAdvanceDate()).notes(dto.getNotes()).build();

        return ApiResponse.success(MessageConstant.CREATED, toDto(advanceRepository.save(advance)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        StaffAdvance advance = advanceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.NOT_FOUND));
        advanceRepository.delete(advance);
        return ApiResponse.success(MessageConstant.DELETED, null);
    }

    private StaffAdvanceDto toDto(StaffAdvance a) {
        StaffAdvanceDto dto = new StaffAdvanceDto();
        dto.setId(a.getId());
        dto.setStaffId(a.getStaff().getId());
        dto.setStaffName(a.getStaff().getName());
        dto.setAmount(a.getAmount());
        dto.setAdvanceDate(a.getAdvanceDate());
        dto.setNotes(a.getNotes());
        dto.setCreatedBy(a.getCreatedBy());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}