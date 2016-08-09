package dao;

import model.Word;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by dirty on 06.08.16.
 */
public class WordMapper implements RowMapper<Word> {
    public Word mapRow(ResultSet resultSet, int i) throws SQLException {

        Word word=new Word();
        word.setData(resultSet.getString("word_data"));
        word.setLanguage(resultSet.getString("word_language"));
        word.setLearned(resultSet.getBoolean("islearned"));
        word.setRightAnswersCount(resultSet.getInt("right_answer_count"));
        return word;
    }
}
