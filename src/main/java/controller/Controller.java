package controller;


import dao.WordDao;
import model.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import view.View;

import java.util.List;

/**
 * Provides interaction of all parts of program. Works as main class,
 * provides interaction of view with DAO; saves new words to database
 * returns them on request from user.
 */
@Component
public class Controller {

    // Provides interaction with user
    @Autowired
    private View view;
    // Provides interaction with DB
    @Autowired
    private WordDao wordDao;
    /**
     * Gives a chance to repeat words in respectively: 4 words, which are
     * marked as unlearned and the fifth word, which is marked as already
     * leaned. Words are randomly chosen.
     */
    private int counter = 0;


    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
        Controller controller = (Controller) context.getBean("controller");
        controller.createDb();
    }

    /**
     * @param data                String value of the word, which is going to be saved
     * @param translations        translations of the new word separated by commas
     * @param nativeLanguage      language of the word
     * @param translationLanguage language of translation
     */
    public void saveWord(String data, String translations, String nativeLanguage, String translationLanguage) {
        String[] translationsList = translations.split(",");
        Word word = new Word();
        word.setData(data.toLowerCase());
        word.setLanguage(nativeLanguage);
        for (String translation : translationsList) {
            Word translationWord = new Word();
            translationWord.setData(translation.toLowerCase());
            translationWord.setLanguage(translationLanguage);
            word.addWordToTranslations(translationWord);
        }
        wordDao.saveWord(word);
    }

    /**
     * @param nativeLanguage      language of the word
     * @param translationLanguage language of translation
     * @return word from database with requested language. 4 words, which are
     * marked as unlearned and the fifth word, which is marked as already
     * leaned. Words are randomly chosen.
     */
    public Word repeatWord(String nativeLanguage, String translationLanguage) {
        if (counter++ >= 5) {
            counter = 0;
            return wordDao.getWord(nativeLanguage, true);
        } else return wordDao.getWord(nativeLanguage, false);
    }

    /**
     * deletes word with requested data
     *
     * @param wordData a string value of word
     */
    public void deleteWord(String wordData) {
        wordDao.deleteWord(wordData.toLowerCase());
    }

    /**
     * Checks if word's translation list contains the translation from translationArea
     *
     * @param word        word for verification
     * @param translation string of translations
     * @return true if result is positive and false if result is negative
     */
    public boolean checkWord(Word word, String translation) {

        if (translation.equals("")) {
            word.setRightAnswersCount(word.getRightAnswersCount() - 1);
            if (word.getRightAnswersCount() < 5)
                word.setLearned(false);
            wordDao.updateWord(word);
            return false;
        }
        for (Word translationWord : word.getTranslationList()) {
            if (translationWord.getData().toLowerCase().equals(translation.toLowerCase())) {
                word.setRightAnswersCount(word.getRightAnswersCount() + 1);
                if (word.getRightAnswersCount() >= 5)
                    word.setLearned(true);
                wordDao.updateWord(word);
                return true;
            }
        }
        word.setRightAnswersCount(word.getRightAnswersCount() - 1);
        if (word.getRightAnswersCount() < 5)
            word.setLearned(false);
        wordDao.updateWord(word);
        return false;
    }

    /**
     * Makes a string from the list of translations
     * @param translationsList
     * @return
     */
    public String makeTranslationsString(List<Word> translationsList) {
        StringBuilder translationStringBuilder = new StringBuilder();
        for (int i = 0; i < translationsList.size() - 1; i++) {
            translationStringBuilder.append(translationsList.get(i).getData() + ",");
        }
        translationStringBuilder.append(translationsList.get(translationsList.size() - 1).getData());

        return translationStringBuilder.toString();
    }

    /**
     * creates tables if not exist
     */
    public void createDb() {
        try {
            wordDao.createTable();
        } catch (Exception e) {
            return;
        }
    }

}
