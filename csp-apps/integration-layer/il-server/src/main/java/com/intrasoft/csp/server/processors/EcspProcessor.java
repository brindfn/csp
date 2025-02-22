package com.intrasoft.csp.server.processors;

import com.intrasoft.csp.commons.model.EnhancedTeamDTO;
import com.intrasoft.csp.commons.routes.ContextUrl;
import com.intrasoft.csp.server.config.CspSslConfiguration;
import com.intrasoft.csp.server.service.CamelRestService;
import org.apache.camel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by iskitsas on 4/10/17.
 */
@Component
public class EcspProcessor implements Processor{
    private static final Logger LOG = LoggerFactory.getLogger(EcspProcessor.class);

    @Value("${server.subdomain.prefix}")
    String serverSubdomainPrefix;

    @Autowired
    CamelRestService camelRestService;

    @Autowired
    CspSslConfiguration cspSslConfiguration;

    @Override
    public void process(Exchange exchange) throws Exception {

        String httpMethod = (String) exchange.getIn().getHeader(Exchange.HTTP_METHOD);
        EnhancedTeamDTO enhancedTeamDTO = exchange.getIn().getBody(EnhancedTeamDTO.class);
        LOG.info("DCL - Sending to external CSP: " + enhancedTeamDTO.getTeam().getName() + " -- " + enhancedTeamDTO.getTeam().getUrl());
        //TODO: versioning in context path below should be extracted dynamically from the header

        String externalSslPort = StringUtils.isEmpty(cspSslConfiguration.getExternalSslPort())?"":
                ":"+cspSslConfiguration.getExternalSslPort();

        String uri = enhancedTeamDTO.getTeam().getUrl()+externalSslPort + "/v"+ContextUrl.REST_API_V1+ContextUrl.DCL_INTEGRATION_DATA;
        //external certificate
        if(cspSslConfiguration.getExternalUseSSL()){
            if(uri.contains("http")) {
                uri = uri.replaceAll("http", cspSslConfiguration.getExternalSslEndpointProtocol());
            }else{
                uri = cspSslConfiguration.getExternalSslEndpointProtocol()+"://"
                        +(!StringUtils.isEmpty(serverSubdomainPrefix)?serverSubdomainPrefix+".":"")
                        +uri;
            }
        }
        LOG.info("URI resolved: "+uri);
        String response = camelRestService.send(uri, enhancedTeamDTO.getIntegrationData(), httpMethod, true);

    }
}
