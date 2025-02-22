package com.intrasoft.csp.server.processors;

import com.intrasoft.csp.commons.constants.AppProperties;
import com.intrasoft.csp.commons.model.IntegrationData;
import com.intrasoft.csp.commons.model.IntegrationDataType;
import com.intrasoft.csp.commons.routes.CamelRoutes;
import com.intrasoft.csp.commons.routes.HeaderName;
import com.intrasoft.csp.server.routes.RouteUtils;
import com.intrasoft.csp.server.service.CamelRestService;
import com.intrasoft.csp.server.service.CspUtils;
import org.apache.camel.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DslProcessor implements Processor,CamelRoutes {

    @Produce
    ProducerTemplate producerTemplate;

    @Autowired
    CspUtils cspUtils;

    @Autowired
    Environment env;

    @Autowired
    RouteUtils routes;

    @Autowired
    CamelRestService camelRestService;

    @Value("${server.camel.rest.service.is.async:true}")
    Boolean camelRestServiceIsAsync;

    private static final Logger LOG = LoggerFactory.getLogger(DslProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("DSL message received.");
        IntegrationData integrationData = cspUtils.getExchangeData(exchange, IntegrationData.class);
        List<String> recipients = computeRecipientsApps(exchange, integrationData);
        exchange.getIn().setHeader("recipients", recipients);

    }

    private List<String> computeRecipientsApps(Exchange exchange, IntegrationData integrationData) {
        List<String> recipients = new ArrayList<>();
        IntegrationDataType dataType = integrationData.getDataType();
        Boolean isExternal = integrationData.getSharingParams().getIsExternal();

        List<String> apps = new ArrayList<>();
        String appsStr = env.getProperty((isExternal? AppProperties.EXTERNAL:AppProperties.INTERNAL)+"."+dataType.name().toLowerCase()+".apps");

        if(!StringUtils.isEmpty(appsStr)){
            String[] appsArr =  appsStr.split(",");
            apps = Arrays.asList(appsArr).stream().map(s->s.trim()).collect(Collectors.toList());
            if (!isExternal && integrationData.getDataParams()!=null){
                apps.remove(integrationData.getDataParams().getApplicationId().toLowerCase());
            }
        }

        if(!isExternal){
            recipients.add(routes.wrap(DDL));
            LOG.info("DSL - received integrationData with datatype: " + integrationData.getDataType());
        }
        else {
            LOG.info("DSL - received integrationData with datatype: " + integrationData.getDataType() + ", isExternal = true");
        }

        for (String app : apps) {
//            LOG.info(app.toString());
            //String uri = cspUtils.getAppUri(app);
            //recipients.add(uri);
            //recipients.add(CamelRoutes.APP+"?name="+app);//haven't find a solution for this yet, using producerTemplate instead
            Map<String,Object> headers = new HashMap<>();
            headers.put(HeaderName.APP_NAME,app);
            headers.put(Exchange.HTTP_METHOD,exchange.getIn().getHeader(Exchange.HTTP_METHOD));
            if(!camelRestServiceIsAsync) {
                producerTemplate.sendBodyAndHeaders(routes.wrap(APP), ExchangePattern.InOnly, integrationData, headers);
            } else {
                camelRestService.asyncSendInOnly(routes.wrap(APP),integrationData,headers);
            }
        }

        return recipients;
    }
}
