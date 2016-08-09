package view;


import controller.Controller;
import model.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Communicates between user and main program
 */
@Component
public class View extends JFrame {

    private final int HEIGHT = 500;
    private final int WIDTH = 400;
    private final String TITLE = "Foreign languages learner";
    //Serves for input word information from user
    private JPanel wordPanel;
    //Serves for input and output translation information
    private JPanel translationPanel;
    // Contains control elements
    private JPanel southPanel;
    // Displays the information
    private JLabel answerIndicator;
    private Word currentRepeatWord;
    private JTextArea wordArea;
    private JTextArea translationArea;
    private String nativeLanguage;
    private String translationLanguage;
    @Autowired
    private Controller controller;

    public View() throws HeadlessException {
        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);

        // panel which serves for input word information
        wordPanel = new JPanel(new BorderLayout());
        wordPanel.setBorder(emptyBorder);
        JLabel wordLabel = new JLabel("Word: ", SwingConstants.CENTER);
        wordLabel.setBorder(emptyBorder);
        wordArea = new JTextArea("Print your word here");
        wordPanel.add(wordLabel, BorderLayout.NORTH);
        wordPanel.add(wordArea, BorderLayout.CENTER);

        // panel which serves for translation information
        translationPanel = new JPanel(new BorderLayout());
        translationPanel.setBorder(emptyBorder);
        JLabel translationLabel = new JLabel("Translation: ", SwingConstants.CENTER);
        translationLabel.setBorder(emptyBorder);
        translationArea = new JTextArea("Here print the translation");
        translationPanel.add(translationLabel, BorderLayout.NORTH);
        translationPanel.add(translationArea, BorderLayout.CENTER);

        // Contains wordPanel and translationPanel
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        add(centerPanel, BorderLayout.CENTER);
        centerPanel.add(wordPanel);
        centerPanel.add(translationPanel);
        //Contains controls
        southPanel = new JPanel(new GridLayout(6, 1));
        southPanel.setBorder(emptyBorder);
        JLabel translationModeLabel = new JLabel("Choose translation mode", SwingConstants.CENTER);
        southPanel.add(translationModeLabel);

        JComboBox<String> languageComboBox = new JComboBox<>();
        languageComboBox.addItem("russian/english");
        languageComboBox.addItem("english/russian");
        southPanel.add(languageComboBox);

        JLabel actionLabel = new JLabel("Choose the action", SwingConstants.CENTER);
        southPanel.add(actionLabel);
        JComboBox<String> actionComboBox = new JComboBox<>();
        actionComboBox.addItem("Add to vocabulary");
        actionComboBox.addItem("Repeat");
        actionComboBox.addItem("Train skills");
        actionComboBox.addItem("Check the answer");
        actionComboBox.addItem("Delete word");
        southPanel.add(actionLabel);
        southPanel.add(actionComboBox);

        JButton actionButton = new JButton("Perform action");
        southPanel.add(actionButton);

        answerIndicator = new JLabel("", SwingConstants.CENTER);
        southPanel.add(answerIndicator);

        add(southPanel, BorderLayout.SOUTH);
        //defines the application job
        actionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (((String) languageComboBox.getSelectedItem()).equals("english/russian")) {
                    nativeLanguage = "english";
                    translationLanguage = "russian";
                } else {
                    nativeLanguage = "russian";
                    translationLanguage = "english";
                }

                int actionIndex = actionComboBox.getSelectedIndex();
                switch (actionIndex) {
                    case 0:
                        controller.saveWord(wordArea.getText(), translationArea.getText(), nativeLanguage, translationLanguage);
                        clearFields();
                        answerIndicator.setText("Word saved");
                        break;
                    case 1:
                        clearFields();
                        currentRepeatWord = controller.repeatWord(nativeLanguage, translationLanguage);
                        wordArea.setText(currentRepeatWord.getData());
                        translationArea.setText(controller.makeTranslationsString(currentRepeatWord.getTranslationList()));
                        answerIndicator.setText("Repeat word: " + currentRepeatWord.getData());
                        break;
                    case 2:
                        trainSkills();
                        actionComboBox.setSelectedIndex(3);
                        break;
                    case 3:
                        String translationString = translationArea.getText();
                        boolean isRightAnswer = controller.checkWord(currentRepeatWord, translationString);
                        if (isRightAnswer) {
                            answerIndicator.setText("Congratulations! Right answer");
                            actionComboBox.setSelectedIndex(2);
                            trainSkills();

                        } else {
                            answerIndicator.setText("Wrong answer!");
                            translationArea.setText("Right answer is:\n" + controller.makeTranslationsString(currentRepeatWord.getTranslationList()));
                            actionComboBox.setSelectedIndex(2);
                            break;
                        }
                    case 4:
                        controller.deleteWord(wordArea.getText());
                }
            }
        });

        setVisible(true);
    }

    private void clearFields() {
        translationArea.setText("");
        wordArea.setText("");
    }

    private void trainSkills() {
        clearFields();
        currentRepeatWord = controller.repeatWord(nativeLanguage, translationLanguage);
        wordArea.setText(currentRepeatWord.getData());
        answerIndicator.setText("Waiting for translation");
    }


    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
