package model;


import java.util.ArrayList;
import java.util.List;

public class Word {
    private String data;
    private String language;
    private int rightAnswersCount;
    private boolean Learned;
    private List<Word> translationList;

    public Word() {

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isLearned() {
        return Learned;
    }

    public void setLearned(boolean learned) {
        Learned = learned;
    }

    public List<Word> getTranslationList() {
        return translationList;
    }

    public void setTranslationList(List<Word> translationList) {
        this.translationList = translationList;
    }

    public int getRightAnswersCount() {
        return rightAnswersCount;
    }

    public void setRightAnswersCount(int rightAnswersCount) {
        this.rightAnswersCount = rightAnswersCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Word word = (Word) o;

        return data != null ? data.equals(word.data) : word.data == null;

    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    public void addWordToTranslations(Word word) {
        if (translationList == null)
            translationList = new ArrayList<Word>();
        if (!translationList.contains(word))
            translationList.add(word);
    }

    public void deleteWordFromTranslations(Word word) {
        if (translationList.contains(word))
            translationList.remove(word);
    }
    public List<String> getListOfTranslations(){

        if (!translationList.isEmpty()){
            List<String> resultList=new ArrayList<>();
            for (Word word:translationList)
                resultList.add(word.getData());
            return resultList;
        }
        return null;
    }
}
