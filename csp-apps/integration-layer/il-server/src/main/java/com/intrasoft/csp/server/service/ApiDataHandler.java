package com.intrasoft.csp.server.service;

import com.intrasoft.csp.commons.apiHttpStatusResponse.HttpStatusResponseType;
import com.intrasoft.csp.commons.exceptions.InvalidDataTypeException;
import com.intrasoft.csp.commons.model.IntegrationData;
import com.intrasoft.csp.commons.routes.CamelRoutes;
import com.intrasoft.csp.commons.validators.IntegrationDataValidator;
import com.intrasoft.csp.server.routes.RouteUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by iskitsas on 6/10/17.
 */
@Service
public class ApiDataHandler implements CamelRoutes{
    private static final Logger LOG = LoggerFactory.getLogger(ApiDataHandler.class);

    @Autowired
    RouteUtils routes;

    @Autowired
    IntegrationDataValidator integrationDataValidator;

    @Autowired
    CamelRestService camelRestService;

    @Value("${server.camel.rest.service.is.async:true}")
    Boolean camelRestServiceIsAsync;

    @Value("${check.cspid.cert.header}")
    String checkCspIdCertHeader;

    @Produce
    private ProducerTemplate producerTemplate;

    public ResponseEntity<String> handleIntegrationData(String route, IntegrationData integrationData, String requestMethod) {
        BindingResult bindingResult = new BeanPropertyBindingResult(integrationData, "integrationData");
        integrationDataValidator.validate(integrationData, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new InvalidDataTypeException(bindingResult.getAllErrors().toString());
        }

        if(camelRestServiceIsAsync){
            //producerTemplate.asyncRequestBodyAndHeader(route, integrationData, Exchange.HTTP_METHOD, requestMethod);
            Map<String,Object> headers = new ConcurrentHashMap<>();
            headers.put(Exchange.HTTP_METHOD, requestMethod);
            headers.put(Exchange.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            long now = DateTime.now().getMillis();
            camelRestService.asyncSendInOnly(route,integrationData,headers);
            long after = DateTime.now().getMillis();
            LOG.debug("async route: "+route+" ["+integrationData.getDataParams().getCspId()+"]");
            LOG.debug("async duration: "+(after-now));
        }else{
            producerTemplate.sendBodyAndHeader(route, integrationData, Exchange.HTTP_METHOD, requestMethod);
        }

        return new ResponseEntity<>(HttpStatusResponseType.SUCCESSFUL_OPERATION.getReasonPhrase(),
                HttpStatus.OK);
    }

    public  void checkIsValidCspIdAgainstCertificateHeader(HttpHeaders headers, IntegrationData integrationData){
        String headerValue;
        LOG.debug("checkCspIdCertHeader: "+checkCspIdCertHeader);
        if(!StringUtils.isEmpty(checkCspIdCertHeader)){
            headerValue = headers.getFirst(checkCspIdCertHeader);
            LOG.debug("CspId checking...");
            LOG.debug(checkCspIdCertHeader+": "+headerValue);
            if(StringUtils.isEmpty(headerValue)){
                throw new InvalidDataTypeException("Could not detect header value with valid CspId extracted from certificate. IntegrationData.cspId: "+integrationData.getDataParams().getCspId());
            }else{
                LOG.debug("url: "+integrationData.getDataParams().getUrl());
                if(headerValue.toLowerCase().contains(integrationData.getDataParams().getCspId().toLowerCase())){
                    LOG.debug("CspIds Match! "+checkCspIdCertHeader+" value = "+headerValue+", IntegrationData.cspId="+integrationData.getDataParams().getCspId());
                }else{
                    throw new InvalidDataTypeException("CpsIds are not matching! IntegrationData.cspId="+integrationData.getDataParams().getCspId()+" while cspId identified: "+headerValue);
                }
            }
        }
    }

    public String getCheckCspIdCertHeader() {
        return checkCspIdCertHeader;
    }
}
