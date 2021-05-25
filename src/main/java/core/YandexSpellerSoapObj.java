package core;

import constants.*;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Arrays;
import java.util.HashMap;

import static constants.ParameterName.OPTIONS;

public class YandexSpellerSoapObj {

        static RequestSpecification spellerSOAPreqSpec = new RequestSpecBuilder()
                .addHeader("Accept-Encoding", "gzip,deflate")
                .setContentType("text/xml;charset=UTF-8")
                .addHeader("Host", "speller.yandex.net")
                .setBaseUri("http://speller.yandex.net/services/spellservice")
                .build();

        //builder pattern
        private YandexSpellerSoapObj(){}

        private HashMap<String, String> params = new HashMap<>();
        private SoapAction action = SoapAction.CHECK_TEXT;
        private static final String QUOTES = "\"";

        public static class SOAPBuilder {
            YandexSpellerSoapObj soapReq;

            private SOAPBuilder(YandexSpellerSoapObj soap) {
                this.soapReq = soap;
            }

            public YandexSpellerSoapObj.SOAPBuilder action(SoapAction action){
                soapReq.action = action;
                return this;
            }

            public YandexSpellerSoapObj.SOAPBuilder texts(String... text) {
                soapReq.params.put(ParameterName.TEXT, Arrays.asList(text).toString());
                return this;
            }

            public YandexSpellerSoapObj.SOAPBuilder options(Option... options) {
                int resultParameter = 0;
                for (Option o : options) resultParameter += o.value;
                soapReq.params.put(OPTIONS, String.valueOf(resultParameter));
                return this;
            }

            public YandexSpellerSoapObj.SOAPBuilder language(Language language) {
                soapReq.params.put(ParameterName.LANGUAGE, language.value);
                return this;
            }

            public Response callSOAP() {
                String soapBody="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:spel=\"http://speller.yandex.net/services/spellservice\">\n" +
                        "   <soapenv:Header/>\n" +
                        "   <soapenv:Body>\n" +
                        "      <spel:" + soapReq.action.getReqName() + " lang=" + QUOTES +
                        (soapReq.params.getOrDefault(ParameterName.LANGUAGE, "en")) + QUOTES
                        +  " options=" + QUOTES + (soapReq.params.getOrDefault(
                        ParameterName.OPTIONS, "0"))+ QUOTES
                        + " format=\"\">\n" +
                        "         <spel:text>"+ (soapReq.params.getOrDefault(
                        ParameterName.TEXT, Texts.ENG_MISSPELLED)) + "</spel:text>\n" +
                        "      </spel:"+ soapReq.action.getReqName() + ">\n" +
                        "   </soapenv:Body>\n" +
                        "</soapenv:Envelope>";


                return RestAssured.with()
                        .spec(spellerSOAPreqSpec)
                        .header("SOAPAction", "http://speller.yandex.net/services/spellservice/" + soapReq.action.getMethod())
                        .body(soapBody)
                        .log().all().with()
                        .post().prettyPeek();
            }
        }


        public static SOAPBuilder with() {
            core.YandexSpellerSoapObj soap = new YandexSpellerSoapObj();
            return new SOAPBuilder(soap);
        }
}
