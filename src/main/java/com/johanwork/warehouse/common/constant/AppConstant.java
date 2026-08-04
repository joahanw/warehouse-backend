package com.johanwork.warehouse.common.constant;

public class AppConstant {
    private AppConstant() {
    }

    public static class Success {
        public static final String FETCHED = "%s fetched successfully";
        public static final String CREATED = "%s created successfully";
        public static final String UPDATED = "%s updated successfully";
        public static final String DELETED = "%s deleted successfully";
        public static final String DELETED_ALL = "%s deleted All successfully";
        public static final String LOGIN = "Login successful";
        public static final String UPLOAD = "Upload file successfully";
        public static final String CSRF_TOKEN = "CSRF token generated successfully";
        private Success(){}
    }

    public static final class Error{
        public static final String TITLE_NOT_FOUND = "%s NOT FOUND";
        public static final String MESSAGE_NOT_FOUND = "%s not found with id: %s";
        public static final String TITLE_ALREADY_EXISTS = "%s ALREADY EXISTS";
        public static final String MESSAGE_ALREADY_EXISTS = "%s already exists";
        public static final String TITLE_BAD_REQUEST = "VALIDATION ERROR";
        public static final String MESSAGE_BAD_REQUEST = "Bad request make sure data is valid";

        public static final String TITLE_INTERNAL_SERVER_ERROR = "INTERNAL SERVER ERROR";
        public static final String MESSAGE_INTERNAL_SERVER_ERROR = "An error occurred. Please try again or contact Dev Team";

        public static final String TITLE_BAD_CREDENTIALS = "BAD CREDENTIALS";
        public static final String MESSAGE_BAD_CREDENTIALS = "Invalid Password";

        public static final String TITLE_TOKEN_EXPIRED = "TOKEN EXPIRED";
        public static final String MESSAGE_TOKEN_EXPIRED = "Token has expired, please login again";

        public static final String TITLE_FAILED_UPLOAD = "FAILED UPLOAD";
        public static final String MESSAGE_FAILED_UPLOAD = "Failed to upload file in Oracle Object Storage";

        public static final String TITLE_WAREHOUSE_CANNOT_DELETE = "WAREHOUSE CANNOT BE DELETED";
        public static final String MESSAGE_WAREHOUSE_CANNOT_DELETE = "Warehouse still contains products";

        public static final String TITLE_INSUFFICIENT_STOCK = "%s INSUFFICIENT STOCK";
        public static final String MESSAGE_INSUFFICIENT_STOCK = "Insufficient stock for product %s in %s";

        public static final String TITLE_QRIS_CHARGE_FAILED = "QRIS CHARGE FAILED";
        public static final String MESSAGE_QRIS_CHARGE_FAILED = "Failed to charge QRIS";

        public static final String TITLE_QR_IMAGE_UNAVAILABLE = "QR IMAGE UNAVAILABLE";
        public static final String MESSAGE_QR_IMAGE_UNAVAILABLE = "This transaction does not have a self-hosted QR code";

        public static final String TITLE_FORBIDDEN = "FORBIDDEN";
        public static final String MESSAGE_FORBIDDEN = "You are not authorized to access this resource";

        public static final String TITLE_DUPLICATE = "DUPLICATE BARCODE";
        public static final String MESSAGE_DUPLICATE = "%s already exists in barcode %s";

        public static final String TITLE_INFISICAL_UNAVAILABLE = "INFISICAL UNAVAILABLE";
        public static final String MESSAGE_INFISICAL_UNAVAILABLE = "Infisical is unavailable, please try again later";

        private Error() {}
    }

    public static final class Role{
        public static final String USER = "USER";
        public static final String ADMIN = "ADMIN";
        public static final String MANAGER = "MANAGER";
        public static final String KEEPER = "KEEPER";
        private Role() {}
    }

}
