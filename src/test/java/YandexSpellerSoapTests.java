import constants.Language;
import constants.Option;
import constants.SoapAction;
import constants.Texts;
import core.YandexSpellerSoapObj;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Arrays;

public class YandexSpellerSoapTests {
    @Test
    public void simpleCall(){
        YandexSpellerSoapObj
                .with().texts(Texts.ENG_MISSPELLED)
                .callSOAP()
                .then()
                .body(Matchers.stringContainsInOrder
                        (Arrays.asList(Texts.ENG_MISSPELLED,
                                Texts.ENG_MISSPELLED_CORRECTION)));
    }

    @Test
    public void useRequestBuilderToChangeParams(){
        String secondCorrectWord = "осмысление";
        YandexSpellerSoapObj.with()
                .language(Language.RUSSIAN)
                .texts(Texts.RUS_MISSPELLED, secondCorrectWord)
                .options(Option.IGNORE_DIGITS, Option.IGNORE_CAPITALIZATION)
                .action(SoapAction.CHECK_TEXTS)
                .callSOAP()
                .then()
                .body(Matchers.stringContainsInOrder
                                (Arrays.asList(Texts.RUS_MISSPELLED,
                                        Texts.RUS_CORRECT)),
                        Matchers.not(Matchers.containsString(secondCorrectWord)));

    }
}
