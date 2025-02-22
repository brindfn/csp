package com.intrasoft.csp.integration.sandbox.server.internal;

import com.intrasoft.csp.anon.client.AnonClient;
import com.intrasoft.csp.anon.commons.model.IntegrationAnonData;
import com.intrasoft.csp.commons.model.*;
import com.intrasoft.csp.commons.routes.CamelRoutes;
import com.intrasoft.csp.server.CspApp;
import com.intrasoft.csp.server.policy.domain.model.EvaluatedPolicyDTO;
import com.intrasoft.csp.server.policy.domain.model.PolicyDTO;
import com.intrasoft.csp.server.policy.domain.model.SharingPolicyAction;
import com.intrasoft.csp.server.policy.service.SharingPolicyService;
import com.intrasoft.csp.server.processors.TcProcessor;
import com.intrasoft.csp.server.routes.RouteUtils;
import com.intrasoft.csp.server.service.CamelRestService;
import com.intrasoft.csp.server.utils.MockUtils;
import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created by iskitsas on 4/7/17.
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = {CspApp.class, MockUtils.class},
        properties = {
                "spring.datasource.url:jdbc:h2:mem:csp_policy",
                "flyway.enabled:false",
                "csp.retry.backOffPeriod:10",
                "csp.retry.maxAttempts:1",
                "embedded.activemq.start:false",
                "apache.camel.use.activemq:false",
                "server.camel.rest.service.is.async:false" //make it sync for better handling in tests (gracefull shutdown etc.)
        })
@MockEndpointsAndSkip("http:*")
public class CspServerInternalSandboxTest implements CamelRoutes{
    private static final Logger LOG = LoggerFactory.getLogger(CspServerInternalSandboxTest.class);

    private MockMvc mvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @EndpointInject(uri = CamelRoutes.MOCK_PREFIX+":"+DIRECT+":"+DSL)
    private MockEndpoint mockedDsl;

    @EndpointInject(uri = CamelRoutes.MOCK_PREFIX+":"+DIRECT+":"+DDL)
    private MockEndpoint mockedDdl;

    @EndpointInject(uri = CamelRoutes.MOCK_PREFIX+":"+DIRECT+":"+TC)
    private MockEndpoint mockedTC;

    //deprecated
    /*@EndpointInject(uri = CamelRoutes.MOCK_PREFIX+":"+DIRECT+":"+TCT)
    private MockEndpoint mockedTCT;*/

    @EndpointInject(uri = CamelRoutes.MOCK_PREFIX+":"+DIRECT+":"+ECSP)
    private MockEndpoint mockedEcsp;

    @MockBean
    CamelRestService camelRestService;

    @MockBean
    AnonClient anonClient;

    @Autowired
    MockUtils mockUtils;

    @Autowired
    RouteUtils routes;

    @Autowired
    SpringCamelContext springCamelContext;

    @MockBean
    SharingPolicyService sharingPolicyService;

    @Autowired
    TcProcessor tcProcessor;

    @Autowired
    Environment env;

    String tcShortNameToTest = IntegrationDataType.CTC_CSP_SHARING;//default

    DataParams anonObject;

    private Integer numOfCsps = 3;
    private Integer currentCspId = 0;

    @Before
    public void init() throws Exception {
        mvc = webAppContextSetup(webApplicationContext).build();
        MockitoAnnotations.initMocks(this);
        mockUtils.setSpringCamelContext(springCamelContext);
        mockUtils.mockRoute(CamelRoutes.MOCK_PREFIX,routes.wrap(DSL),mockedDsl.getEndpointUri());
        mockUtils.mockRoute(CamelRoutes.MOCK_PREFIX,routes.wrap(DDL), mockedDdl.getEndpointUri());
        mockUtils.mockRoute(CamelRoutes.MOCK_PREFIX,routes.wrap(ECSP), mockedEcsp.getEndpointUri());

        String tcShortNameToTestArg = env.getProperty("tcShortNameToTest");
        if(!StringUtils.isEmpty(tcShortNameToTestArg)){
            tcShortNameToTest = tcShortNameToTestArg;
        }

        /*
        Mockito.when(camelRestService.sendAndGetList(anyString(), anyObject(), eq("GET"), eq(TrustCircle.class),anyObject()))
                .thenReturn(mockUtils.getAllMockedTrustCircles(3));
        Mockito.when(camelRestService.send(anyString(), anyObject(), eq("GET"), eq(TrustCircle.class)))
                .thenReturn(mockUtils.getMockedTrustCircle(3));
        */



        String urlShouldContain = tcProcessor.getTcCirclesURI();
        if(tcShortNameToTest.equalsIgnoreCase(IntegrationDataType.LTC_CSP_SHARING)){
            urlShouldContain = tcProcessor.getLocalCirclesURI();
        }
        Mockito.when(camelRestService.send(Matchers.contains(urlShouldContain), anyObject(), eq("GET"), eq(TrustCircle.class), anyObject()))
                .thenReturn(mockUtils.getMockedTrustCircle(3, tcShortNameToTest));

        Mockito.when(camelRestService.send(Matchers.contains(urlShouldContain), anyObject(), eq("GET"), eq(TrustCircle.class)))
                .thenReturn(mockUtils.getMockedTrustCircle(3, tcShortNameToTest));





        Mockito.when(camelRestService.send(anyString(), anyObject(), eq("GET"), eq(Team.class)))
                .thenReturn(mockUtils.getMockedTeam(1,"http://external.csp%s.com"))
                .thenReturn(mockUtils.getMockedTeam(2,"http://external.csp%s.com"))
                .thenReturn(mockUtils.getMockedTeam(3,"http://external.csp%s.com"));
        ///*deprecated*/ mockUtils.mockRouteSkipSendToOriginalEndpoint(CamelRoutes.MOCK_PREFIX, routes.wrap(TC),mockedTC.getEndpointUri());
        ///*deprecated*/ mockUtils.mockRouteSkipSendToOriginalEndpoint(CamelRoutes.MOCK_PREFIX,routes.wrap(TCT),mockedTCT.getEndpointUri());

        IntegrationAnonData integrationAnonData = new IntegrationAnonData();

        anonObject = new DataParams();
        anonObject.setCspId("AnonymizedCspId");
        anonObject.setApplicationId("AnonymizedApplicationId");

        integrationAnonData.setDataObject(anonObject);
        Mockito.when(anonClient.postAnonData(anyObject())).thenReturn(integrationAnonData);

        EvaluatedPolicyDTO evaluatedPolicyDTO = new EvaluatedPolicyDTO();
        evaluatedPolicyDTO.setSharingPolicyAction(SharingPolicyAction.NO_ACTION_FOUND);
        PolicyDTO mockedPolicyDTO = new PolicyDTO();
        evaluatedPolicyDTO.setPolicyDTO(mockedPolicyDTO);
        Mockito.when(sharingPolicyService.evaluate(anyObject(),anyObject())).thenReturn(evaluatedPolicyDTO);
    }

    // Use @DirtiesContext on each test method to force Spring Testing to automatically reload the CamelContext after
    // each test method - this ensures that the tests don't clash with each other, e.g., one test method sending to an
    // endpoint that is then reused in another test method.
    @DirtiesContext
    @Test
    public void dslFlow1TestUsingCamelEndpoint() throws Exception {
        //deprecated
        /*mockedTC.returnReplyBody(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                try {
                    return (T) TestUtil.convertObjectToJsonBytes(mockUtils.getMockedTrustCircle(3));
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });

        mockedTC.expectedMessagesMatches(new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                String in = exchange.getIn().getBody(String.class);
                TrustCircle tc = null;
                try {
                    tc = objectMapper.readValue(in, TrustCircle.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assertThat(tc.getCsps().size(),is(3));
                return true;
            }
        });*/

        //deprecated
        /*
        mockedTCT.returnReplyBody(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                try {
                    currentCspId = currentCspId+1;
                    return (T) TestUtil.convertObjectToJsonBytes(mockUtils.getMockedTeam(currentCspId,"http://external.csp%s.com"));
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });*/

        mockUtils.sendFlow1IntegrationData(mvc,false);

        mockedDsl.expectedMessageCount(1);
        mockedDsl.assertIsSatisfied();

        List<Exchange> list = mockedDsl.getReceivedExchanges();
        for (Exchange exchange : list) {
            Message in = exchange.getIn();
            IntegrationData data = in.getBody(IntegrationData.class);
            assertThat(data.getDataType(), is(IntegrationDataType.INCIDENT));
            assertThat(data.getDataParams().getOriginCspId(), is("origin-testCspId"));
            assertThat(data.getDataParams().getOriginApplicationId(), is("origin-test1"));
            assertThat(data.getDataParams().getOriginRecordId(), is("origin-recordId"));
            assertThat(data.getDataParams().getUrl(), is("http://rt.cert-gr.melecertes.eu/Ticket/Display.html?id=23453"));
        }

        mockedEcsp.expectedMessageCount(3);
        mockedEcsp.assertIsSatisfied();
        list = mockedEcsp.getReceivedExchanges();
        int i=0;
        for (Exchange exchange : list) {
            i++;
            Message in = exchange.getIn();
            EnhancedTeamDTO enhancedTeamDTO = in.getBody(EnhancedTeamDTO.class);
            assertThat(enhancedTeamDTO.getTeam().getUrl(), is("http://external.csp"+i+".com"));
        }

        mockedDdl.expectedMessageCount(1);
        mockedDdl.assertIsSatisfied();
        //Thread.sleep(10*1000); //to avoid "Rejecting received message because of the listener container having been stopped in the meantime"
        //be careful when debugging, you might miss breakpoints if the time is not enough
    }

    @DirtiesContext
    @Test
    public void dslFlow1TcIdTest() throws Exception {
        String tcId = "tcId";
        mockUtils.sendFlow1IntegrationData(mvc,false,tcId,null);

        mockedDsl.expectedMessageCount(1);
        mockedDsl.assertIsSatisfied();

        List<Exchange> list = mockedDsl.getReceivedExchanges();
        for (Exchange exchange : list) {
            Message in = exchange.getIn();
            IntegrationData data = in.getBody(IntegrationData.class);
            assertThat(((List)data.getSharingParams().getTcId()).get(0), is(tcId));
            assertThat(data.getDataType(), is(IntegrationDataType.INCIDENT));
            assertThat(data.getDataParams().getOriginCspId(), is("origin-testCspId"));
            assertThat(data.getDataParams().getOriginApplicationId(), is("origin-test1"));
            assertThat(data.getDataParams().getOriginRecordId(), is("origin-recordId"));
            assertThat(data.getDataParams().getUrl(), is("http://rt.cert-gr.melecertes.eu/Ticket/Display.html?id=23453"));
        }

        mockedEcsp.expectedMessageCount(3);
        mockedEcsp.assertIsSatisfied();
        list = mockedEcsp.getReceivedExchanges();
        int i=0;
        for (Exchange exchange : list) {
            i++;
            Message in = exchange.getIn();
            EnhancedTeamDTO enhancedTeamDTO = in.getBody(EnhancedTeamDTO.class);
            assertThat(enhancedTeamDTO.getTeam().getUrl(), is("http://external.csp"+i+".com"));
        }

        mockedDdl.expectedMessageCount(1);
        mockedDdl.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void dslFlow1TeamIdTest() throws Exception {
        String teamId = "teamId";
        mockUtils.sendFlow1IntegrationData(mvc,false,null,teamId);

        mockedDsl.expectedMessageCount(1);
        mockedDsl.assertIsSatisfied();

        List<Exchange> list = mockedDsl.getReceivedExchanges();
        for (Exchange exchange : list) {
            Message in = exchange.getIn();
            IntegrationData data = in.getBody(IntegrationData.class);
            //assertThat(data.getSharingParams().getTeamId().get(0), is(teamId));
            assertThat(data.getDataType(), is(IntegrationDataType.INCIDENT));
            assertThat(data.getDataParams().getOriginCspId(), is("origin-testCspId"));
            assertThat(data.getDataParams().getOriginApplicationId(), is("origin-test1"));
            assertThat(data.getDataParams().getOriginRecordId(), is("origin-recordId"));
            assertThat(data.getDataParams().getUrl(), is("http://rt.cert-gr.melecertes.eu/Ticket/Display.html?id=23453"));
        }

        mockedEcsp.expectedMessageCount(1);
        mockedEcsp.assertIsSatisfied();
        list = mockedEcsp.getReceivedExchanges();
        int i=0;
        for (Exchange exchange : list) {
            i++;
            Message in = exchange.getIn();
            EnhancedTeamDTO enhancedTeamDTO = in.getBody(EnhancedTeamDTO.class);
            assertThat(enhancedTeamDTO.getTeam().getUrl(), is("http://external.csp"+i+".com"));
        }

        mockedDdl.expectedMessageCount(1);
        mockedDdl.assertIsSatisfied();
    }
}
