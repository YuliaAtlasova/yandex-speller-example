package core;

import beans.YandexSpellerAnswer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.Format;
import constants.Language;
import constants.Option;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static constants.ParameterName.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class YandexSpellerServiceObj {

    public static final URI SPELLER_URI = URI.create("https://speller.yandex.net/services/spellservice.json/checkText");
    private static long requestNumber = 0L;
    private Method requestMethod;

    //BEGINNING OF BUILDER PATTERN
    private Map<String, String> parameters;

    private YandexSpellerServiceObj(Map<String, String> parameters, Method method) {
        this.parameters = parameters;
        this.requestMethod = method;
    }

    public static ApiRequestBuilder requestBuilder() {
        return new ApiRequestBuilder();
    }

    public static class ApiRequestBuilder {
        private Map<String, String> parameters = new HashMap<>();
        private Method requestMethod = Method.GET;

        public ApiRequestBuilder setMethod (Method method){
            requestMethod = method;
            return this;
        }

        public ApiRequestBuilder setLanguage(Language... lang) {
            parameters.put(LANGUAGE, Arrays.stream(lang).map(l->l.value).collect(Collectors.joining(", ")));
            return this;
        }

        public ApiRequestBuilder setFormat(Format format) {
            parameters.put(FORMAT, format.format);
            return this;
        }

        public ApiRequestBuilder setOptions(Option... options) {
            int resultParameter = 0;
            for (Option o : options) resultParameter += o.value;
            parameters.put(OPTIONS, String.valueOf(resultParameter));
            return this;
        }

        public ApiRequestBuilder setText(String... text) {
            parameters.put(TEXT, Arrays.stream(text).collect(Collectors.joining(", ")));
            return this;
        }

        public YandexSpellerServiceObj buildRequest() {
            return new YandexSpellerServiceObj(parameters, requestMethod);
        }
    }
    //ENDING OF BUILDER PATTERN

    public Response sendRequest() {
        return RestAssured
                .given(requestSpecification()).log().all()
                .queryParams(parameters)
                .request(requestMethod ,SPELLER_URI)
                .prettyPeek();
    }

    public static YandexSpellerAnswer getTheOnlyAnswer(Response response){
        List<YandexSpellerAnswer> answers = new Gson()
                .fromJson(response.asString().trim(), new TypeToken<List<YandexSpellerAnswer>>() {
                }.getType());
        assertThat ("We expect to get one answer, but got " + answers.size(), answers, hasSize(1));
        return answers.get(0);
    }

    public static List<YandexSpellerAnswer> getAnswers(Response response) {
        List<YandexSpellerAnswer> answers = new Gson()
                .fromJson(response.asString().trim(), new TypeToken<List<YandexSpellerAnswer>>() {
                }.getType());
        return answers;
    }

    public static List<String> getStringResult(Response response) {
        return getAnswers(response).stream().map(yandexSpellerAnswer -> yandexSpellerAnswer.getWord()).collect(Collectors.toList());
    }

    public static RequestSpecification requestSpecification() {
        return new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .addQueryParam("requestNumber", ++requestNumber)
                .setBaseUri(SPELLER_URI)
                .build();
    }

    public static ResponseSpecification goodResponseSpecification() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan(10000L))
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification badResponseSpecification() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.TEXT)
                .expectResponseTime(lessThan(10000L))
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build();
    }
}
