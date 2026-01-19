package com.ugr.microservices.dependencies.core.tddt4iots.util;

import com.ugr.microservices.dependencies.core.tddt4iots.cons.Tddt4iotsCons;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class GenericException extends Exception{
    private static final long serialVersionUID = 1L;
    private Integer codeError;
    private String  statusError;
    private String  classError;
    private String  methodError;
    private Integer lineError;
    private String  userMessageError;
    private String  technicalMessageError;
    private String  messageError;

    private List<Integer> controllError = new ArrayList<>(Arrays.asList(Tddt4iotsCons.MISSING_VALUES,
            Tddt4iotsCons.INFORMATIVE_VALUES,
            Tddt4iotsCons.EXISTING_VALUES,
            Tddt4iotsCons.ERROR_PARSE_VALUES));

    @SuppressWarnings("rawtypes")
    public GenericException(Class classError, String methodError, String codeError, String statusError, String userMessageError) {
        super(userMessageError);
        setCodeError((codeError != null) ? Integer.valueOf(codeError) : Tddt4iotsCons.FAILURE_CODE_DEFAULT);
        setStatusError((statusError !=  null) ? statusError : String.valueOf(Tddt4iotsCons.FAILURE_STATUS_DEFAULT));
        setUserMessageError(userMessageError);
        setClassError(classError.getCanonicalName());
        setMethodError(methodError);
        setTechnicalMessageError(userMessageError);
        log.error("An Error ocurred in the class {}, in the method {} || UserErrorMessage: {}",
                getClassError(), getMethodError(), getUserMessageError());
    }


    @SuppressWarnings("rawtypes")
    public GenericException(Class classError, String methodError, String userMessageError, Throwable throwable) {
        super(throwable);
        List<StackTraceElement> errorList = new ArrayList<>();
        setClassError(classError.getCanonicalName());
        setUserMessageError(userMessageError);
        setCodeError((this.codeError != null) ? this.codeError : Tddt4iotsCons.FAILURE_CODE_DEFAULT);
        setStatusError((this.statusError != null) ? this.statusError : Tddt4iotsCons.FAILURE_STATUS_DEFAULT);
        for (StackTraceElement element : throwable.getStackTrace()) {
            if (classError.getName().equalsIgnoreCase(element.getClassName())
                    && element.getMethodName().equalsIgnoreCase(methodError)) {
                errorList.add(element);
            }
        }
        StackTraceElement element = (!errorList.isEmpty()) ? errorList.get(0) : null;
        setMethodError((element != null) ? element.getMethodName() : "ND");
        setLineError((element != null) ? element.getLineNumber() : 0);
        setTechnicalMessageError((throwable.getLocalizedMessage() != null)
                ? throwable.getLocalizedMessage() :	(throwable.getCause() != null) ? throwable.getCause().toString()
                : (throwable.getMessage() != null) ? throwable.getMessage() : throwable.toString());
        log.error("An Error ocurred in the class {}, in the method {} in the line {} || User error message: {} || Technical error: {}",
                getClassError(), getMethodError(), getLineError(), userMessageError, getTechnicalMessageError());
    }

    public GenericException(String userMessageError) {
        super(userMessageError);
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 3) {
            StackTraceElement callerElement = stackTrace[2];
            setCodeError((codeError != null) ? Integer.valueOf(codeError) : Tddt4iotsCons.FAILURE_CODE_DEFAULT);
            setStatusError((statusError !=  null) ? statusError : String.valueOf(Tddt4iotsCons.FAILURE_STATUS_DEFAULT));
            setUserMessageError(userMessageError);
            setClassError(callerElement.getClassName());
            setMethodError(callerElement.getMethodName());
            setLineError(callerElement.getLineNumber());
            setMessageError(userMessageError);
            log.error("An Error ocurred in the class {}, in the method {} || UserErrorMessage: {}",
                    getClassError(), getMethodError(), getUserMessageError());
        }
    }

    public GenericException(String userMessageError, Throwable throwable) {
        super(throwable);
        List<StackTraceElement> errorList = new ArrayList<>();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 3) {
            setClassError(stackTrace[2].getClassName());
            setMethodError(stackTrace[2].getMethodName());
        }
        setMessageError(userMessageError);
        setUserMessageError(userMessageError);
        setCodeError((this.codeError != null) ? this.codeError : Tddt4iotsCons.FAILURE_CODE_DEFAULT);
        setStatusError((this.statusError != null) ? this.statusError : Tddt4iotsCons.FAILURE_STATUS_DEFAULT);
        for (StackTraceElement element : throwable.getStackTrace()) {
            if (classError.equalsIgnoreCase(element.getClassName())
                    && element.getMethodName().equalsIgnoreCase(methodError)) {
                errorList.add(element);
            }
        }
        StackTraceElement element = (!errorList.isEmpty()) ? errorList.get(0) : null;
        setLineError((element != null) ? element.getLineNumber() : 0);
        setTechnicalMessageError((throwable.getLocalizedMessage() != null)
                ? throwable.getLocalizedMessage() :	(throwable.getCause() != null) ? throwable.getCause().toString()
                : (throwable.getMessage() != null) ? throwable.getMessage() : throwable.toString());
        log.error("An Error ocurred in the class {}, in the method {} in the line {} || User error message: {} || Technical error: {}",
                getClassError(), getMethodError(), getLineError(), userMessageError, getTechnicalMessageError());
    }




    public GenericException(String technicallError, Integer codeError) {
        super(technicallError);
        setTechnicalMessageError(technicallError);
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 3) {
            StackTraceElement callerElement = stackTrace[2];
            setCodeError((codeError != null) ? Integer.valueOf(codeError) : Tddt4iotsCons.FAILURE_CODE_DEFAULT);
            setStatusError((statusError !=  null) ? statusError : String.valueOf(Tddt4iotsCons.FAILURE_STATUS_DEFAULT));
            if(controllError.contains(codeError)) {
                setUserMessageError(technicallError);
            }else {
                setUserMessageError("An error ocurred!!!");
            }
            setMessageError(technicallError);
            setClassError(callerElement.getClassName());
            setMethodError(callerElement.getMethodName());
            setLineError(callerElement.getLineNumber());
            log.error("An Error ocurred in the class {}, in the method {} || technicalErrorMessage: {}",
                    getClassError(), getMethodError(), getUserMessageError());
        }
    }
}
