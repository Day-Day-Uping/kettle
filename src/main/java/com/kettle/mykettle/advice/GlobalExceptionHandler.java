package com.kettle.mykettle.advice;

import com.kettle.mykettle.entity.ResponseData;
import com.kettle.mykettle.utils.ResponseUtil;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: mykettle
 * @description:
 * @author: Mr.HuangDaDa
 * @create: 2022-06-28 18:34
 **/
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理自定义的业务异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = BizException.class)
    public ResponseData bizExceptionHandler(HttpServletRequest req, BizException e){
        return new ResponseData(ResponseUtil.RESPONSE_STATUS_NOK,e.getErrorMsg());
    }

    /**
     * 处理空指针的异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =NullPointerException.class)
    public ResponseData exceptionHandler(HttpServletRequest req, NullPointerException e){
        return new ResponseData(ResponseUtil.RESPONSE_STATUS_NOK,e.getMessage());
    }


    /**
     * 处理其他异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =Exception.class)
    public ResponseData exceptionHandler(HttpServletRequest req, Exception e){
        return new ResponseData(ResponseUtil.RESPONSE_STATUS_NOK,ResponseUtil.RESPONSE_MESSAGE_ERROR);
    }

}
