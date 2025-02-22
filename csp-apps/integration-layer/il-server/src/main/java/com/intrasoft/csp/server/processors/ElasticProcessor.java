package com.intrasoft.csp.server.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intrasoft.csp.commons.model.IntegrationData;
import com.intrasoft.csp.commons.model.IntegrationDataType;
import com.intrasoft.csp.commons.model.elastic.ElasticData;
import com.intrasoft.csp.commons.model.elastic.ElasticSearchRequest;
import com.intrasoft.csp.commons.model.elastic.ElasticSearchResponse;
import com.intrasoft.csp.commons.model.elastic.query.Bool;
import com.intrasoft.csp.commons.model.elastic.query.Filter;
import com.intrasoft.csp.commons.model.elastic.query.Query;
import com.intrasoft.csp.commons.model.elastic.query.Term;
import com.intrasoft.csp.commons.model.elastic.search.Hit;
import com.intrasoft.csp.libraries.restclient.exceptions.CspBusinessException;
import com.intrasoft.csp.server.service.CamelRestService;
import com.intrasoft.csp.server.service.CspUtils;
import org.apache.camel.*;
import org.apache.camel.component.http.HttpMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by iskitsas on 4/11/17.
 */
@Component
public class ElasticProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticProcessor.class);

    @Value("${elastic.protocol}")
    String elasticProtocol;
    @Value("${elastic.host}")
    String elasticHost;
    @Value("${elastic.port}")
    String elasticPort;
    @Value("${elastic.path}")
    String elasticPath;


    @Produce
    ProducerTemplate producerTemplate;

    @Autowired
    CamelRestService camelRestService;

    @Autowired
    CspUtils cspUtils;

    @Override
    public void process(Exchange exchange) throws Exception {

        IntegrationData integrationData = cspUtils.getExchangeData(exchange, IntegrationData.class);
        String httpMethod = (String) exchange.getIn().getHeader(Exchange.HTTP_METHOD);
        IntegrationDataType dataType = integrationData.getDataType();


        /*
        DDL indexes data (DDL -> ELASTIC API)
         */
        if (httpMethod.equals(HttpMethod.POST.name())) {
            //create transaction object
            ElasticData elasticData = new ElasticData(integrationData.getDataParams(), integrationData.getDataObject());

            //query ES for insertion
            LOG.debug(this.getElasticURI() + "/" + dataType.toString().toLowerCase() + "?pretty");
            String response = camelRestService.send(this.getElasticURI() + "/" + dataType.toString().toLowerCase() + "?pretty", elasticData, HttpMethods.POST.name());
            LOG.info("Elastic - ES Insert response: " + response);

        }
        else if (httpMethod.equals(HttpMethod.PUT.name())) {
            //create search transaction object
            ElasticSearchRequest elasticSearchRequest = this.getElasticSearchRequest(integrationData);

            //query ES to get IDs
            String response = camelRestService.send(this.getElasticURI() + "/" + dataType.toString().toLowerCase() + "/_search?pretty&_source=false", elasticSearchRequest, HttpMethods.POST.name());
            LOG.info("Elastic - ES Search response: " + response);

            if(response == null){
                //TODO: this will activate GDelivery. Do we want this?
                throw new CspBusinessException("No response from Elastic (null). Processor will fail and should send message to DeadLetterQ");
            }

            //create update transaction object
            ElasticData elasticData = new ElasticData(integrationData.getDataParams(), integrationData.getDataObject());

            ElasticSearchResponse elasticSearchResponse = new ObjectMapper().readValue(response, ElasticSearchResponse.class);
            for(Hit hit : elasticSearchResponse.getHits().getHits()) {
                LOG.debug(hit.getId());
                //query ES to perform update
                String updateResponse = camelRestService.send(this.getElasticURI() + "/" + dataType.toString().toLowerCase() + "/" + hit.getId() + "", elasticData, HttpMethods.POST.name());
                LOG.info("Elastic - ES Update index "+hit.getId()+" response: " + updateResponse);
            }

        }
        else if (httpMethod.equals(HttpMethod.DELETE.name())) {
            /**
             * Method 1. Camel does not transmits body in DELETE verbs
             */
            /*
            //create delete transaction object
            ElasticDelete elasticDelete = new ElasticDelete();
            elasticDelete.setQuery(this.getElasticQuery(integrationData));

            //query ES to perform deletion
            String response = camelRestService.send(this.getElasticURI() + "/" + dataType.toString().toLowerCase() + "/_query", elasticDelete, httpMethod);
            LOG.info("ES Delete response: " + response);
            */

            /**
             * Method 2
             */
            //create search transaction object
            ElasticSearchRequest elasticSearchRequest = this.getElasticSearchRequest(integrationData);

            //query ES to get IDs
            String response = camelRestService.send(this.getElasticURI() + "/" + dataType.toString().toLowerCase() + "/_search?pretty&_source=false", elasticSearchRequest, HttpMethods.POST.name());
            LOG.info("ES Search response: " + response);

            if(response == null){
                //TODO: this will activate GDelivery. Do we want this?
                throw new CspBusinessException("No response from Elastic (null). Processor will fail and should send message to DeadLetterQ");
            }

            ElasticSearchResponse elasticSearchResponse = new ObjectMapper().readValue(response, ElasticSearchResponse.class);
            for(Hit hit : elasticSearchResponse.getHits().getHits()) {
                LOG.debug(hit.getId());
                //query ES to perform deletion
                String deleteResponse = camelRestService.send(this.getElasticURI() + "/" + dataType.toString().toLowerCase() + "/" + hit.getId(), null, HttpMethod.DELETE.name());
                LOG.info("ES Delete index "+hit.getId()+"response: " + deleteResponse);
            }

        }


    }

    private String getElasticURI() {
        return elasticProtocol + "://" + elasticHost + ":" + elasticPort + elasticPath;
    }

    private Query getElasticQuery(IntegrationData integrationData) {

        Term t1 = new Term();
        t1.setRecordId(integrationData.getDataParams().getRecordId());

        Term t2 = new Term();
        t2.setCspId(integrationData.getDataParams().getCspId());

        Term t3 = new Term();
        t3.setApplicationId(integrationData.getDataParams().getApplicationId());

        Filter m1 = new Filter();
        m1.setTerm(t1);
        Filter m2 = new Filter();
        m2.setTerm(t2);
        Filter m3 = new Filter();
        m3.setTerm(t3);

        ArrayList<Filter> filter = new ArrayList<>();
        filter.add(m1);
        filter.add(m2);
        filter.add(m3);

        Bool bool = new Bool();
        bool.setFilter(filter);

        Query query = new Query();
        query.setBool(bool);

        return query;
    }

    private ElasticSearchRequest getElasticSearchRequest(IntegrationData integrationData) {
        //create search transaction object
        List<String> fields = new ArrayList<>();
        fields.add("_id");

        ElasticSearchRequest elasticSearchRequest = new ElasticSearchRequest();
        elasticSearchRequest.setQuery(this.getElasticQuery(integrationData));
        //elasticSearchRequest.setFields(fields);

        return elasticSearchRequest;
    }
}
