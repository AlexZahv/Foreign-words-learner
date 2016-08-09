package dao;

import model.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class WordDao {
    private static final String INSERT_QUERY = "insert into WORD (word_data,word_language,islearned,right_answer_count) values (?,?,?,?);";
    private static final String INSERT_JOIN_TABLE = "insert into WORD_JOIN (word_data,translation_data) values (?,?);";
    private static final String SELECT_FROM_WORD_JOIN = "SELECT translation_data FROM WORD_JOIN WHERE word_data=? UNION SELECT word_data from WORD_JOIN WHERE translation_data=?;";
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DataSource dataSource;

    /**
     * Stores the new word defined by user in database
     *
     * @param word a new word which will be stored in database
     */
    @Transactional
    public void saveWord(Word word) {
        if (getWord(word.getData()) == null) {
            jdbcTemplate.update(INSERT_QUERY, word.getData(), word.getLanguage(), word.isLearned(), word.getRightAnswersCount());
            saveTranslationList(word);
        } else
            updateWord(word);
    }

    /**
     * Stores in tne database words, which are defined as translations to input word
     *
     * @param word stores the list of translations
     */
    @Transactional
    public void saveTranslationList(Word word) {
        if (word.getTranslationList() != null)
            for (Word translationWord : word.getTranslationList()) {
                if (getWord(translationWord.getData()) == null) {
                    jdbcTemplate.update(INSERT_QUERY, translationWord.getData(), translationWord.getLanguage(), translationWord.isLearned(), translationWord.getRightAnswersCount());
                    jdbcTemplate.update(INSERT_JOIN_TABLE, word.getData(), translationWord.getData());
                }
            }
    }

    /**
     * @param data string value of the word
     * @return returns word object which string data equals to the param data
     */
    @Transactional
    public Word getWord(String data) {
        String sql = "select * from WORD where word_data=?;";
        List<Word> wordList = jdbcTemplate.query(sql, new Object[]{data}, new WordMapper());

        if (wordList.isEmpty())
            return null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        sql = "SELECT * from WORD where word_data in (:words)";
        List<Word> translationList = namedParameterJdbcTemplate.query(sql, getParameters(data, SELECT_FROM_WORD_JOIN), new WordMapper());

        wordList.get(0).setTranslationList(translationList);
        return wordList.get(0);
    }

    /**
     * Allows to get word from database with specified params language and isLearned
     *
     * @param language
     * @param isLearned shows the status of the required word: already learned or in progress
     * @return word object
     */
    @Transactional
    public Word getWord(String language, boolean isLearned) {
        String sql = "select * from WORD where (word_language=? AND islearned=?);";
        List<Word> wordList = jdbcTemplate.query(sql, new Object[]{language, isLearned}, new WordMapper());

        if (wordList.isEmpty())
            return null;

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        sql = "SELECT * from WORD where word_data in (:words)";
        Random random = new Random();
        int randWordPos = random.nextInt(wordList.size());
        List<Word> translationList = namedParameterJdbcTemplate.query(sql, getParameters(wordList.get(randWordPos).getData(), SELECT_FROM_WORD_JOIN), new WordMapper());

        wordList.get(randWordPos).setTranslationList(translationList);

        return wordList.get(randWordPos);
    }

    /**
     * Deletes word with requested data from database and all it's relations
     *
     * @param data stores the data of the word object, which will be deleted
     */
    @Transactional
    public void deleteWord(String data) {

        String sql = "DELETE from WORD WHERE word_data=? ; )";
        jdbcTemplate.update(sql, new Object[]{data});

        sql = "DELETE FROM WORD_JOIN WHERE (word_data=? OR translation_data=?)";
        jdbcTemplate.update(sql, new Object[]{data, data});
        sql = "SELECT translation_data FROM WORD_JOIN  UNION SELECT word_data from WORD_JOIN ;";

        MapSqlParameterSource parameters = getParameters(data, sql);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        sql = "DELETE from WORD where word_data not in (:words)";
        namedParameterJdbcTemplate.update(sql, parameters);

    }

    /**
     * Updates word object in database
     *
     * @param word
     */
    @Transactional
    public void updateWord(Word word) {
        String sql = "UPDATE WORD SET islearned=? WHERE word_data=?;";
        jdbcTemplate.update(sql, new Object[]{word.isLearned(), word.getData()});

        sql = "UPDATE WORD SET right_answer_count=? WHERE word_data=?;";
        jdbcTemplate.update(sql, new Object[]{word.getRightAnswersCount(), word.getData()});

        saveTranslationList(word);

    }

    @Transactional
    private MapSqlParameterSource getParameters(String data, String sql) {
        List<Word> paramWordList = jdbcTemplate.query(sql, new Object[]{data, data}, new RowMapper<Word>() {
            public Word mapRow(ResultSet resultSet, int i) throws SQLException {
                Word word = new Word();
                word.setData(resultSet.getString("translation_data"));
                return word;
            }
        });

        List<String> parametersList = new ArrayList<String>();
        for (Word word : paramWordList) {
            parametersList.add(word.getData());
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("words", parametersList);
        return parameters;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates tables if not exist
     */
    public void createTable() {
        String sql = "CREATE TABLE WORD (word_data VARCHAR(45) NOT NULL PRIMARY KEY ,word_language VARCHAR (20) NOT NULL ,right_answer_count int DEFAULT 0,islearned BOOLEAN DEFAULT FALSE )";
        jdbcTemplate.execute(sql);
        sql = "CREATE TABLE WORD_JOIN (word_data VARCHAR (45),translation_data VARCHAR (45),CONSTRAINT pk_WordData PRIMARY KEY (word_data,translation_data));";
        jdbcTemplate.execute(sql);
    }

}
