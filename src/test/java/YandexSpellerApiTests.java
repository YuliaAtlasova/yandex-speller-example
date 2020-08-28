import beans.YandexSpellerAnswer;
import constants.Language;
import core.DataProvidersForSpeller;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static constants.ErrorCode.ERROR_UNKNOWN_WORD;
import static constants.Format.HTML;
import static constants.Format.INCORRECT_FORMAT;
import static constants.Language.*;
import static constants.Option.IGNORE_DIGITS;
import static constants.Option.IGNORE_URLS;
import static constants.Texts.ENG_CORRECT;
import static constants.Texts.RUS_MISSPELLED;
import static core.YandexSpellerServiceObj.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class YandexSpellerApiTests {

    @Test(dataProvider = "correctTextsProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkCorrectTexts(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendGetRequest());
        assertThat("API reported errors in correct text: " + result, result.isEmpty());
    }

    //BUG WAS FOUND
    @Test(dataProvider = "misspelledTextsProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkMisspelledTexts(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendGetRequest());
        assertThat("API failed to find spelling error in text: " + text,
                result.contains(text));
    }

    @Test
    public void checkErrorCodeForMisspelling() {
        List<YandexSpellerAnswer> answers = getAnswers(
                requestBuilder()
                        .setLanguage(RUSSIAN)
                        .setText(RUS_MISSPELLED)
                        .buildRequest()
                        .sendGetRequest());
            assertThat("API displays wrong error code: " + answers.get(0).getCode() + " instead of: "
                    + ERROR_UNKNOWN_WORD.code, answers.get(0).getCode() == ERROR_UNKNOWN_WORD.code);
    }

    //BUGS WERE FOUND
    @Test(dataProvider = "textsWithDigitsProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkIncorrectTextsWithDigits(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendGetRequest());
        assertThat("API failed to find error in text with digits: " + text,
                result.contains(text));
    }

    //BUGS WERE FOUND
    @Test(dataProvider = "textsWithLinksProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkIncorrectTextsWithLinks(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendGetRequest());
        assertThat("API failed to find error in text with URL: " + text,
                result.contains(text));
    }

    //BUGS WERE FOUND
    @Test(dataProvider = "properNamesWithLowerCaseProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkIncorrectProperNamesWithLowerCase(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendGetRequest());
        assertThat("API failed to find error in proper name with lower case: " + text,
                result.contains(text));
    }

    @Test
    public void checkIncorrectLanguageParameter() {
        requestBuilder()
                .setLanguage(INCORRECT_LANGUAGE)
                .setText(ENG_CORRECT)
                .buildRequest()
                .sendGetRequest()
                .then().assertThat()
                .spec(badResponseSpecification())
                .body(containsString("SpellerService: Invalid parameter 'lang'"));
    }

    @Test(dataProvider = "textsWithDigitsProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkIgnoreDigitsOption(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .setOptions(IGNORE_DIGITS)
                        .buildRequest()
                        .sendGetRequest());
        assertThat("API reported errors in text with digits despite 'ignore digits' option: " + result,
                result.isEmpty());
    }

    @Test(dataProvider = "textsWithLinksProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkIgnoreUrlsOption(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .setOptions(IGNORE_URLS)
                        .buildRequest()
                        .sendGetRequest());
        assertThat("API reported errors in text with URL despite 'ignore URLs' option: "
                + result, result.isEmpty());
    }

    @Test
    public void checkCorrectFormatOption() {
        requestBuilder()
                .setLanguage(ENGLISH)
                .setText(ENG_CORRECT)
                .setFormat(HTML)
                .buildRequest()
                .sendGetRequest()
                .then().assertThat()
                .spec(goodResponseSpecification());
    }

    @Test
    public void checkIncorrectFormatOption() {
        requestBuilder()
                .setLanguage(ENGLISH)
                .setText(ENG_CORRECT)
                .setFormat(INCORRECT_FORMAT)
                .buildRequest()
                .sendGetRequest()
                .then().assertThat()
                .spec(badResponseSpecification())
                .and()
                .body(containsString("SpellerService: Invalid parameter 'format'"));
    }

    //BUG WAS FOUND
    @Test(dataProvider = "mixOfMisspelledTextsAndLanguagesProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkMixOfLanguagesForMisspelledTexts(Language[] languages, String[] texts) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(languages)
                        .setText(texts)
                        .buildRequest()
                        .sendGetRequest());
        assertThat("API failed to find 1 or more error(s) in the mix of texts: "
                + Arrays.toString(texts) + " in following languages: "
                + Arrays.toString(languages), result.size() == texts.length);
    }
}