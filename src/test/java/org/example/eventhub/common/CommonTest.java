package org.example.eventhub.common;

import org.junit.jupiter.api.Test;

public class CommonTest {
    @Test
    public void testApiResponseSuccess() {
        ApiResponse<String> response = ApiResponse.success("Hello, World!");
        assert response.getCode() == 0;
        assert response.getMessage().equals("success");
        assert response.getData().equals("Hello, World!");
    }

    @Test
    public void testApiResponseFail() {
        ApiResponse<Void> response = ApiResponse.fail(ErrorCode.PARAM_ERROR);
        assert response.getCode() == 400;
        assert response.getMessage().equals("参数校验失败");
        assert response.getData() == null;
    }

    @Test
    public void testBusinessException() {
        BusinessException exception = new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        assert exception.getCode() == 1001;
        assert exception.getMessage().equals("学生不存在");
    }

    @Test
    public void testGlobalExceptionHandler() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BusinessException businessException = new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        ApiResponse<Void> response = handler.handleBusinessException(businessException);
        assert response.getCode() == 1001;
        assert response.getMessage().equals("学生不存在");
    }
}
