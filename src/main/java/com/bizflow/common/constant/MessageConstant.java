package com.bizflow.common.constant;

public class MessageConstant {

    // Auth
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String ACCOUNT_DISABLED = "Account is disabled. Contact admin.";

    // Common
    public static final String SUCCESS = "Success";
    public static final String CREATED = "Created successfully";
    public static final String UPDATED = "Updated successfully";
    public static final String DELETED = "Deleted successfully";
    public static final String NOT_FOUND = "Record not found";
    public static final String ALREADY_EXISTS = "Record already exists";

    // User
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_CREATED = "User created successfully";
    public static final String USER_UPDATED = "User updated successfully";
    public static final String USER_DELETED = "User deleted successfully";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";

    // JWT
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String BEARER_NAME = "bearer";
    public static final String JWT = "JWT";
    public static final String BEARER_AUTHENTICATION = "Bearer Authentication";
    public static final String TOKEN_EXPIRED = "{\"message\": \"Token expired or invalid\"}";
    public static final String SESSION_EXPIRED = "Session expired";

    // Tenant
    public static final String TENANT_NOT_FOUND = "Tenant not found";
    public static final String TENANT_CREATED = "Tenant created successfully";
    public static final String TENANT_UPDATED = "Tenant updated successfully";

    // Catalogue
    public static final String ITEM_NOT_FOUND = "Item not found";
    public static final String ITEM_CREATED = "Item created successfully";
    public static final String ITEM_UPDATED = "Item updated successfully";
    public static final String ITEM_DELETED = "Item deleted successfully";
    public static final String CATEGORY_NOT_FOUND = "Category not found";
    public static final String CATEGORY_CREATED = "Category created successfully";
    public static final String CATEGORY_UPDATED = "Category updated successfully";
    public static final String CATEGORY_DELETED = "Category deleted successfully";

    // Inventory
    public static final String INVENTORY_NOT_FOUND = "Inventory record not found";
    public static final String INVENTORY_UPDATED = "Inventory updated successfully";
    public static final String STOCK_ADJUSTED = "Stock adjusted successfully";
    public static final String WAREHOUSE_NOT_FOUND = "Warehouse not found";
    public static final String WAREHOUSE_CREATED = "Warehouse created successfully";
    public static final String WAREHOUSE_UPDATED = "Warehouse updated successfully";
    public static final String WAREHOUSE_DELETED = "Warehouse deleted successfully";
    public static final String STOCK_NOT_FOUND = "Stock record not found";
    public static final String STOCK_UPDATED = "Stock updated successfully";
    public static final String STOCK_DELETED = "Stock deleted successfully";

    // Invoice
    public static final String INVOICE_NOT_FOUND = "Invoice not found";
    public static final String INVOICE_CREATED = "Invoice created successfully";
    public static final String INVOICE_UPDATED = "Invoice updated successfully";
    public static final String INVOICE_DELETED = "Invoice deleted successfully";

    // Purchase
    public static final String PURCHASE_NOT_FOUND = "Purchase not found";
    public static final String PURCHASE_CREATED = "Purchase created successfully";
    public static final String PURCHASE_UPDATED = "Purchase updated successfully";
    public static final String PURCHASE_DELETED = "Purchase deleted successfully";
    public static final String SUPPLIER_NOT_FOUND = "Supplier not found";
    public static final String SUPPLIER_CREATED = "Supplier created successfully";
    public static final String SUPPLIER_UPDATED = "Supplier updated successfully";
    public static final String SUPPLIER_DELETED = "Supplier deleted successfully";

    // Party
    public static final String PARTY_NOT_FOUND = "Party not found";
    public static final String PARTY_CREATED = "Party created successfully";
    public static final String PARTY_UPDATED = "Party updated successfully";
    public static final String PARTY_DELETED = "Party deleted successfully";

    // Expense
    public static final String EXPENSE_NOT_FOUND = "Expense not found";
    public static final String EXPENSE_CREATED = "Expense created successfully";
    public static final String EXPENSE_UPDATED = "Expense updated successfully";
    public static final String EXPENSE_DELETED = "Expense deleted successfully";

    // Staff
    public static final String STAFF_NOT_FOUND = "Staff not found";
    public static final String STAFF_CREATED = "Staff created successfully";
    public static final String STAFF_UPDATED = "Staff updated successfully";
    public static final String STAFF_DELETED = "Staff deleted successfully";
    public static final String ATTENDANCE_MARKED = "Attendance marked successfully";
    public static final String ATTENDANCE_ALREADY_MARKED = "Attendance already marked for this date";
    public static final String ADVANCE_CREATED = "Advance created successfully";

    // Transaction
    public static final String TRANSACTION_NOT_FOUND = "Transaction not found";
    public static final String TRANSACTION_CREATED = "Transaction created successfully";
    public static final String TRANSACTION_UPDATED = "Transaction updated successfully";
    public static final String TRANSACTION_DELETED = "Transaction deleted successfully";

    // Customer
    public static final String CUSTOMER_NOT_FOUND = "Customer not found";
    public static final String CUSTOMER_CREATED = "Customer created successfully";
    public static final String CUSTOMER_UPDATED = "Customer updated successfully";
    public static final String CUSTOMER_DELETED = "Customer deleted successfully";

    // Returns
    public static final String RETURN_NOT_FOUND = "Return not found";
    public static final String RETURN_CREATED = "Return created successfully";
    public static final String RETURN_UPDATED = "Return updated successfully";
    public static final String RETURN_DELETED = "Return deleted successfully";

    // API Docs
    public static final String API_DOCUMENTATION = "BizFlow API";
    public static final String V1 = "1.0.0";

    private MessageConstant() {
    }
}