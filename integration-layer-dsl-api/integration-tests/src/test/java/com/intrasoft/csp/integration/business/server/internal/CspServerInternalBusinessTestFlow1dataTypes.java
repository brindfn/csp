package com.intrasoft.csp.integration.business.server.internal;


import com.intrasoft.csp.commons.exceptions.CspBusinessException;
import com.intrasoft.csp.commons.model.*;
import com.intrasoft.csp.commons.routes.CamelRoutes;
import com.intrasoft.csp.integration.MockUtils;
import com.intrasoft.csp.server.CspApp;
import com.intrasoft.csp.server.routes.RouteUtils;
import com.intrasoft.csp.server.service.CamelRestService;
import com.intrasoft.csp.server.service.ErrorMessageHandler;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = {CspApp.class, MockUtils.class},
        properties = {
        /*
        //added in application-dangerduck.properties
                "consume.errorq.on.interval:false",
                "csp.retry.backOffPeriod:10",
                "csp.retry.maxAttempts:1",
                "embedded.activemq.start:false",
                "apache.camel.use.activemq:false",
                "internal.use.ssl: false",
                "internal.ssl.keystore.resource: sslcert/csp-internal.jks",
                "internal.ssl.keystore.passphrase: 123456",
                "external.use.ssl: false",
                "external.ssl.keystore.resource: sslcert/csp-internal.jks",
                "external.ssl.keystore.passphrase: 123456",

                "misp.protocol: http",
                "misp.host: csp2.dangerduck.gr",
                "misp.port: 8082",
                "misp.path: /adapter/misp",

                "viper.protocol: http",
                "viper.host: csp2.dangerduck.gr",
                "viper.port: 8082",
                "viper.path: /adapter/viper",

                "tc.protocol: http",
                "tc.host: tc.csp2.dangerduck.gr",
                "tc.port: 8000",
                "tc.path.circles: /api/v1/circles",
                "tc.path.teams: /api/v1/teams",

                "trustcircle.protocol: http",
                "trustcircle.host: csp2.dangerduck.gr",
                "trustcircle.port: 8082",
                "trustcircle.path: /adapter/tc",

                "elastic.protocol: http",
                "elastic.host: csp2.dangerduck.gr",
                "elastic.port: 9200",
                "elastic.path: /cspdata"
                */
        })
//@MockEndpointsAndSkip("^https4-in://localhost.*adapter.*|https4-in://csp.*|https4-ex://ex.*") // by removing this any http requests will be sent as expected.
//@MockEndpointsAndSkip("http://external.csp*") // by removing this any http requests will be sent as expected.
@MockEndpointsAndSkip("^http://*.dangerduck.gr:8081/v1/dcl/integrationData")

// In this test we mock all other http requests except for tc. TC dummy server is expected on 3001 port.
// To start the TC dummy server:
// $ APP_NAME=tc SSL=true PORT=8081 node server.js
public class CspServerInternalBusinessTestFlow1dataTypes implements CamelRoutes {

    private static final Logger LOG = LoggerFactory.getLogger(CspServerInternalBusinessTestFlow1verbs.class);

    private MockMvc mvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @EndpointInject(uri = CamelRoutes.MOCK_PREFIX + ":" + DIRECT + ":" + DSL)
    private MockEndpoint mockedDsl;

    @EndpointInject(uri = CamelRoutes.MOCK_PREFIX + ":" + DIRECT + ":" + APP)
    private MockEndpoint mockedApp;

    @EndpointInject(uri = CamelRoutes.MOCK_PREFIX + ":" + DIRECT + ":" + DDL)
    private MockEndpoint mockedDdl;

    @EndpointInject(uri = CamelRoutes.MOCK_PREFIX + ":" + DIRECT + ":" + DCL)
    private MockEndpoint mockedDcl;

    @EndpointInject(uri = CamelRoutes.MOCK_PREFIX + ":" + DIRECT + ":" + TC)
    private MockEndpoint mockedTC;

    @EndpointInject(uri = CamelRoutes.MOCK_PREFIX + ":" + DIRECT + ":" + ECSP)
    private MockEndpoint mockedEcsp;

    @EndpointInject(uri = CamelRoutes.MOCK_PREFIX + ":" + DIRECT + ":" + ELASTIC)
    private MockEndpoint mockedElastic;

    @Autowired
    MockUtils mockUtils;

    @Autowired
    RouteUtils routes;

    @Autowired
    CamelRestService camelRestService;

    @Autowired
    SpringCamelContext springCamelContext;

    @Autowired
    ErrorMessageHandler errorMessageHandler;

    @Autowired
    Environment env;

    private Integer numOfCspsToTest = 3;
    private Integer currentCspId = 0;
    private HashMap<IntegrationDataType, Integer> internalApps = new HashMap<>();

    String serverName;
    String tcProtocol;
    String tcHost;
    String tcPort;
    String tcPathCircles;
    String tcPathTeams;

    @Before
    public void init() throws Exception {
        mvc = webAppContextSetup(webApplicationContext).build();
        mockUtils.setSpringCamelContext(springCamelContext);

        mockUtils.mockRoute(CamelRoutes.MOCK_PREFIX, routes.apply(DSL), mockedDsl.getEndpointUri());
        mockUtils.mockRoute(CamelRoutes.MOCK_PREFIX, routes.apply(APP), mockedApp.getEndpointUri());
        mockUtils.mockRoute(CamelRoutes.MOCK_PREFIX, routes.apply(DDL), mockedDdl.getEndpointUri());
        mockUtils.mockRoute(CamelRoutes.MOCK_PREFIX, routes.apply(DCL), mockedDcl.getEndpointUri());
        mockUtils.mockRoute(CamelRoutes.MOCK_PREFIX, routes.apply(TC), mockedTC.getEndpointUri());
        mockUtils.mockRouteSkipSendToOriginalEndpoint(CamelRoutes.MOCK_PREFIX, routes.apply(ECSP), mockedEcsp.getEndpointUri());
        mockUtils.mockRoute(CamelRoutes.MOCK_PREFIX, routes.apply(ELASTIC), mockedElastic.getEndpointUri());

        //Initialize internalApps Hashmap according application.properties (internal section)
        internalApps.put(IntegrationDataType.THREAT, 1);
        internalApps.put(IntegrationDataType.ARTEFACT, 2);
        internalApps.put(IntegrationDataType.TRUSTCIRCLE, 1);

        serverName = env.getProperty("server.name");
        tcProtocol=env.getProperty("tc.protocol");
        tcHost=env.getProperty("tc.host");
        tcPort=env.getProperty("tc.port");
        tcPathCircles=env.getProperty("tc.path.circles");
        tcPathTeams=env.getProperty("tc.path.teams");
    }


    // Use @DirtiesContext on each test method to force Spring Testing to automatically reload the CamelContext after
    // each test method - this ensures that the tests don't clash with each other, e.g., one test method sending to an
    // endpoint that is then reused in another test method.
    @DirtiesContext
    @Test
    public void testDslFlow1PostDataTypeThreat() throws Exception {
        mockUtils.sendFlow1Data(mvc, false, true, IntegrationDataType.THREAT, HttpMethods.POST.name());

        // Expect 1-messages/teams from ESCP according to CERT-GR configuration for THREAT on csp2.dangerduck.gr
        _flowImpl(IntegrationDataType.THREAT, 1);

        //Thread.sleep(10*1000); //to avoid "Rejecting received message because of the listener container having been stopped in the meantime"
        //be careful when debugging, you might miss breakpoints if the time is not enough
    }

    @DirtiesContext
    @Test
    public void testDslFlow1PutDataTypeThreat() throws Exception {
        mockUtils.sendFlow1Data(mvc, false, true, IntegrationDataType.THREAT, HttpMethods.PUT.name());

        // Expect 1-messages/teams from ESCP according to CERT-GR configuration for THREAT on csp2.dangerduck.gr
        _flowImpl(IntegrationDataType.THREAT, getEcspMessagesCount(IntegrationDataType.THREAT));

        //Thread.sleep(10*1000); //to avoid "Rejecting received message because of the listener container having been stopped in the meantime"
        //be careful when debugging, you might miss breakpoints if the time is not enough
    }

    @DirtiesContext
    @Test
    public void testDslFlow1PostDataTypeArtefact() throws Exception {
        mockUtils.sendFlow1Data(mvc, false, true, IntegrationDataType.ARTEFACT, HttpMethods.POST.name());

        // Expect 1-messages/teams from ESCP according to CERT-GR configuration for ARTEFACT on csp2.dangerduck.gr
        _flowImpl(IntegrationDataType.ARTEFACT, getEcspMessagesCount(IntegrationDataType.ARTEFACT));

        //Thread.sleep(10*1000); //to avoid "Rejecting received message because of the listener container having been stopped in the meantime"
        //be careful when debugging, you might miss breakpoints if the time is not enough
    }

    @DirtiesContext
    @Test
    public void testDslFlow1PutDataTypeArtefact() throws Exception {
        mockUtils.sendFlow1Data(mvc, false, true, IntegrationDataType.ARTEFACT, HttpMethods.PUT.name());

        // Expect 1-messages/teams from ESCP according to CERT-GR configuration for ARTEFACT on csp2.dangerduck.gr
        _flowImpl(IntegrationDataType.ARTEFACT, getEcspMessagesCount(IntegrationDataType.ARTEFACT));

        //Thread.sleep(10*1000); //to avoid "Rejecting received message because of the listener container having been stopped in the meantime"
        //be careful when debugging, you might miss breakpoints if the time is not enough
    }

    @DirtiesContext
    @Test
    public void testDslFlow1PostDataTypeTrustcircle() throws Exception {
        mockUtils.sendFlow1Data(mvc, false, true, IntegrationDataType.TRUSTCIRCLE, HttpMethods.POST.name());

        // Expect 3-messages/teams from ESCP according to CERT-GR configuration for TRUSTCIRCLE on csp2.dangerduck.gr
        _flowImpl(IntegrationDataType.TRUSTCIRCLE, getEcspMessagesCount(IntegrationDataType.TRUSTCIRCLE));

        //Thread.sleep(10*1000); //to avoid "Rejecting received message because of the listener container having been stopped in the meantime"
        //be careful when debugging, you might miss breakpoints if the time is not enough
    }

    @DirtiesContext
    @Test
    public void testDslFlow1PutDataTypeTrustcircle() throws Exception {
        mockUtils.sendFlow1Data(mvc, false, true, IntegrationDataType.TRUSTCIRCLE, HttpMethods.PUT.name());

        // Expect 3-messages/teams from ESCP according to CERT-GR configuration for TRUSTCIRCLE on csp2.dangerduck.gr
        _flowImpl(IntegrationDataType.TRUSTCIRCLE, getEcspMessagesCount(IntegrationDataType.TRUSTCIRCLE));

        //Thread.sleep(10*1000); //to avoid "Rejecting received message because of the listener container having been stopped in the meantime"
        //be careful when debugging, you might miss breakpoints if the time is not enough
    }



    private void _flowImpl(IntegrationDataType dataType, Integer expectedEscpMessages) throws Exception {
       /*
        DSL
         */
        //Expect 1-message
        mockedDsl.expectedMessageCount(1);
        mockedDsl.assertIsSatisfied();


        List<Exchange> list = mockedDsl.getReceivedExchanges();
        for (Exchange exchange : list) {
            Message in = exchange.getIn();
            IntegrationData data = in.getBody(IntegrationData.class);
            assertThat(data.getDataType(), is(dataType));
        }

        /*
        APP
         */
        //Expect message count according to application.properties
        mockedApp.expectedMessageCount(internalApps.get(dataType));
        mockedApp.assertIsSatisfied();

        list = mockedApp.getReceivedExchanges();
        for (Exchange exchange : list) {
            Message in = exchange.getIn();
            IntegrationData data = in.getBody(IntegrationData.class);
            assertThat(data.getDataType(), is(dataType));
        }

        //DDL
        mockedDdl.expectedMessageCount(1);
        mockedDdl.assertIsSatisfied();

        list = mockedDdl.getReceivedExchanges();
        for (Exchange exchange : list) {
            Message in = exchange.getIn();
            IntegrationData data = in.getBody(IntegrationData.class);
            assertThat(data.getDataType(), is(dataType));
        }

        //DCL
        mockedDcl.expectedMessageCount(1);
        mockedDcl.assertIsSatisfied();

        list = mockedDcl.getReceivedExchanges();
        for (Exchange exchange : list) {
            Message in = exchange.getIn();
            IntegrationData data = in.getBody(IntegrationData.class);
            assertThat(data.getDataType(), is(dataType));
        }

        //TC
        mockedTC.expectedMessageCount(1);
        mockedTC.assertIsSatisfied();

        list = mockedTC.getReceivedExchanges();
        for (Exchange exchange : list) {
            Message in = exchange.getIn();
            IntegrationData data = in.getBody(IntegrationData.class);
            assertThat(data.getDataType(), is(dataType));
            assertThat(exchange.getIn().getHeader(CamelRoutes.ORIGIN_ENDPOINT), is(routes.apply(CamelRoutes.DCL)));
        }

        //ESCP
        mockedEcsp.expectedMessageCount(expectedEscpMessages);
        mockedEcsp.assertIsSatisfied();

        list = mockedEcsp.getReceivedExchanges();
        for (Exchange exchange : list) {
            /**
             * @CHECK
            Assertion on Team's url is meaningless for real teams coming from TC
             */
            //Message in = exchange.getIn();
            //EnhancedTeamDTO enhancedTeamDTO = in.getBody(EnhancedTeamDTO.class);
            //assertThat(enhancedTeamDTO.getTeam().getUrl(), is("http://csp2.dangerduck.gr:8081"));
        }

        //ELASTIC
        if(IntegrationDataType.TRUSTCIRCLE.equals(dataType)){
            mockedElastic.expectedMessageCount(0);
        }else {
            mockedElastic.expectedMessageCount(1);
        }
        mockedElastic.assertIsSatisfied();

        list = mockedElastic.getReceivedExchanges();
        for (Exchange exchange : list) {
            Message in = exchange.getIn();
            IntegrationData data = in.getBody(IntegrationData.class);
            assertThat(data.getDataType(), is(dataType));
        }
    }


    Integer getEcspMessagesCount(IntegrationDataType integrationDataType) throws IOException {
        String uri = null;
        String getAllTcUri = this.getTcCirclesURI();
        List<TrustCircle> tcList = camelRestService.sendAndGetList(getAllTcUri, null,  HttpMethod.GET.name(), TrustCircle.class,null);

        Optional<TrustCircle> optionalTc  = tcList.stream().filter(t->t.getShortName().toLowerCase().contains(IntegrationDataType.tcNamingConventionForShortName.get(integrationDataType).toString().toLowerCase())).findAny();
        if(optionalTc.isPresent()){
            uri = this.getTcCirclesURI() + "/" + optionalTc.get().getId();
        }else{
            throw new CspBusinessException("Integration Test error: Could not find trust circle id for this data. ");
        }

        TrustCircle tc = camelRestService.send(uri, null,  HttpMethod.GET.name(), TrustCircle.class);
        List<Team> teams = new ArrayList<>();
        //first make all calls to get the teams
        for (String teamId : tc.getTeams()){
            //make call to TC-team
            Team team = camelRestService.send(this.getTcTeamsURI() + "/" + teamId, teamId, HttpMethod.GET.name(), Team.class);
            if(team.getShortName()==null){
                throw new CspBusinessException("Team short name received from TC API is null - cannot proceed. \n" +
                        "TrustCircle: "+tc.toString()+"\n" +
                        "Team: "+team.toString());
            }

            if (!team.getShortName().toLowerCase().trim().equals(serverName.toLowerCase().trim())){
                teams.add(team);
            }
        }

        return teams.size();
    }

    private String getTcCirclesURI() {


        return tcProtocol + "://" + tcHost + ":" + tcPort + tcPathCircles;
    }

    private String getTcTeamsURI() {
        return tcProtocol + "://" + tcHost + ":" + tcPort + tcPathTeams;
    }

}

