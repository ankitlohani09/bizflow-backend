package com.bizflow.modules.customer.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.customer.dto.CustomerRequest;
import com.bizflow.modules.customer.dto.CustomerResponse;

import java.util.List;

public interface CustomerService {

    ApiResponse<List<CustomerResponse>> getAllCustomers();

    ApiResponse<CustomerResponse> getCustomerById(Long id);

    ApiResponse<CustomerResponse> createCustomer(CustomerRequest request);

    ApiResponse<CustomerResponse> updateCustomer(Long id, CustomerRequest request);

    ApiResponse<Void> deleteCustomer(Long id);
}