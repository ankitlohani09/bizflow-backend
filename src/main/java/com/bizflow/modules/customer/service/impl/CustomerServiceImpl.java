package com.bizflow.modules.customer.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.modules.customer.dto.CustomerRequest;
import com.bizflow.modules.customer.dto.CustomerResponse;
import com.bizflow.modules.customer.entity.Customer;
import com.bizflow.modules.customer.repository.CustomerRepository;
import com.bizflow.modules.customer.service.CustomerService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public ApiResponse<List<CustomerResponse>> getAllCustomers() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse
                .success(customerRepository.findAllByTenantId(tenantId).stream().map(this::toResponse).toList());
    }

    @Override
    public ApiResponse<CustomerResponse> getCustomerById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.CUSTOMER_NOT_FOUND));
        return ApiResponse.success(toResponse(customer));
    }

    @Override
    public ApiResponse<CustomerResponse> createCustomer(CustomerRequest request) {
        Long tenantId = SecurityUtils.getCurrentTenantId();

        if (request.getPhone() != null && customerRepository.existsByPhoneAndTenantId(request.getPhone(), tenantId)) {
            throw new RuntimeException(MessageConstant.ALREADY_EXISTS);
        }

        Customer customer = Customer.builder().tenantId(tenantId).name(request.getName()).email(request.getEmail())
                .phone(request.getPhone()).address(request.getAddress()).city(request.getCity())
                .state(request.getState()).pincode(request.getPincode()).gstin(request.getGstin())
                .openingBalance(request.getOpeningBalance()).isActive(request.getIsActive()).build();

        return ApiResponse.success(MessageConstant.CUSTOMER_CREATED, toResponse(customerRepository.save(customer)));
    }

    @Override
    public ApiResponse<CustomerResponse> updateCustomer(Long id, CustomerRequest request) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.CUSTOMER_NOT_FOUND));

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPincode(request.getPincode());
        customer.setGstin(request.getGstin());
        customer.setOpeningBalance(request.getOpeningBalance());
        customer.setIsActive(request.getIsActive());

        return ApiResponse.success(MessageConstant.CUSTOMER_UPDATED, toResponse(customerRepository.save(customer)));
    }

    @Override
    public ApiResponse<Void> deleteCustomer(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.CUSTOMER_NOT_FOUND));
        customerRepository.delete(customer);
        return ApiResponse.success(MessageConstant.CUSTOMER_DELETED, null);
    }

    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder().id(c.getId()).tenantId(c.getTenantId()).name(c.getName()).email(c.getEmail())
                .phone(c.getPhone()).address(c.getAddress()).city(c.getCity()).state(c.getState())
                .pincode(c.getPincode()).gstin(c.getGstin()).openingBalance(c.getOpeningBalance())
                .isActive(c.getIsActive()).createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).build();
    }
}