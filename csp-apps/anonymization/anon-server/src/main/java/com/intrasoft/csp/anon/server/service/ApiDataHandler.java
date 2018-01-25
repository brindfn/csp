package com.intrasoft.csp.anon.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intrasoft.csp.anon.commons.exceptions.AnonException;
import com.intrasoft.csp.anon.commons.exceptions.InvalidDataTypeException;
import com.intrasoft.csp.anon.commons.exceptions.MappingNotFoundForGivenTupleException;
import com.intrasoft.csp.anon.commons.model.IntegrationAnonData;
import com.intrasoft.csp.anon.server.model.Rule;
import com.intrasoft.csp.anon.server.model.Rules;
import com.intrasoft.csp.anon.server.utils.HMAC;
import com.intrasoft.csp.commons.apiHttpStatusResponse.HttpStatusResponseType;
import com.intrasoft.csp.commons.model.IntegrationDataType;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.apache.tomcat.util.codec.binary.Base64;
import org.omg.CORBA.Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;

@Service
public class ApiDataHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApiDataHandler.class);

    @Autowired
    RulesService rulesService;

    @Autowired
    HMAC hmac;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String IPADDRESS_PATTERN =
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    private static ObjectMapper mapper = new ObjectMapper();

    private static final Configuration configuration = Configuration.builder()
            .options(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS)
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();

    /**
     *
     * @param integrationAnonData
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     */

    public IntegrationAnonData handleAnonIntegrationData(IntegrationAnonData integrationAnonData) throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        if(integrationAnonData == null){
            throw new AnonException(HttpStatusResponseType.MALFORMED_INTEGRATION_DATA_STRUCTURE.getReasonPhrase()+"[integrationAnonData is null]");
        }

        LOG.info("Handle integrationData for cspId: " + integrationAnonData.getCspId() + " and " + " dataType " + integrationAnonData.getDataType());
        String cspId = integrationAnonData.getCspId();
        IntegrationDataType dataType = integrationAnonData.getDataType();
        if (dataType == null){
            throw new InvalidDataTypeException(HttpStatusResponseType.UNSUPPORTED_DATA_TYPE.getReasonPhrase()+"[dataType is null]");
        }

        if (cspId == null || cspId.equals("")){
            throw new AnonException(HttpStatusResponseType.MALFORMED_INTEGRATION_DATA_STRUCTURE.getReasonPhrase()+"[cspId is empty]");
        }

        Rules rules = rulesService.getRule(dataType, cspId);

        if (rules == null){
            LOG.debug("Ruleset mapping not found, using default.");
            throw new MappingNotFoundForGivenTupleException(HttpStatusResponseType.MAPPING_NOT_FOUND_FOR_GIVEN_TUPLE.getReasonPhrase()
                    +"[dataType: "+dataType+",cspId: "+cspId+"]");
        }

        if (integrationAnonData.getDataObject() == null){
            throw new AnonException(HttpStatusResponseType.MALFORMED_INTEGRATION_DATA_STRUCTURE.getReasonPhrase());
        }

        JsonNode out = mapper.valueToTree(integrationAnonData.getDataObject());
        ReadContext ctx = JsonPath.using(configuration).parse(out);

        for (Rule rule : rules.getRules()){
            List<LinkedHashMap> tmp = ctx.read(rule.getCondition(), List.class);
            for (LinkedHashMap jn : tmp){
                JsonNode jjn = new ObjectMapper().valueToTree(jn);
                out = JsonPath.using(configuration).parse(out).set(rule.getCondition(), ((ObjectNode)jjn).put(rule.getField(),updateField(rule.getAction(), rule.getField(), jjn.get(rule.getField()).textValue()))).json();
            }
        }

        integrationAnonData.setDataObject(out);
        return integrationAnonData;
    }

    /**
     *
     * @param action
     * @param fieldValue
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    private String updateField(String action, String fieldType, String fieldValue) throws InvalidKeyException, NoSuchAlgorithmException {
        String newVal = fieldValue;
        if (action.toLowerCase().equals("pseudo")){
            newVal = pseudoField(fieldValue);
        }
        else if (action.toLowerCase().equals("anon")){
            newVal = anonField(fieldType, fieldValue);
        }
        return newVal;
    }

    /**
     *
     * @param fieldVal
     * @return
     */
    private String anonField(String fieldtype, String fieldVal){

        Pattern rEmail = Pattern.compile(EMAIL_PATTERN);
        Pattern rIp = Pattern.compile(IPADDRESS_PATTERN);
        if (rEmail.matcher(fieldVal).matches()) fieldtype = "email";
        else if (rIp.matcher(fieldVal).matches()) fieldtype = "ip";

        switch (fieldtype) {
            case "string":
                return "*******";
            case "ip":
                return "***.***.***.***";
            case "email":
                return "***@******.**";
            case "alphanumeric":
                return "00000000";
            case "numeric":
                return "00000000";
            default:
                return "$$$$$$$$$";
        }
//        return "*********";
    }

    /**
     *
     * @param val
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private String pseudoField(String val) throws NoSuchAlgorithmException, InvalidKeyException {

        String secret = hmac.getKey().getKey();
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(val.getBytes()));
        return hash;
    }
}
