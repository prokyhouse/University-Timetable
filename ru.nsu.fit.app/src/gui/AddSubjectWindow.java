package gui;

import entities.Subject;
import managers.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class AddSubjectWindow {
    JFrame frame;
    JLabel label;
    JList<String> subjects;
    String specialization;
    JTextField enterNewSubjectName;
    JTextField enterAmountOfSemesters;
    JTextField enterTypeOfClass;
    JTextField enterFrequency;
    JTextField enterAmountOfGroups;
    JTextField enterTeacherName;
    JButton addButton;
    JButton deleteButton;
    JButton selectTeacherButton;

    public AddSubjectWindow(String specialization) {
        this.specialization = specialization;

        this.frame = new JFrame("Edit Subject");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        this.label = new JLabel("List of subjects");
        frame.add(this.label, BorderLayout.NORTH);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        try {
            DatabaseManager manager = DatabaseManager.getInstance();
            List<Subject> subjectList = manager.getSubjects(specialization);
            for (Subject subject: subjectList) {
                listModel.addElement("Subject: \t" + subject.subjectName + "\t Semesters: \t" +
                        subject.semesters + "\t Teacher: \t" + subject.teacher + "\t Frequency: \t" +
                        subject.frequency + "\t Type: \t" + subject.typeOfClass + "\t Groups: \t" +
                        subject.amountOfGroups);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        this.subjects = new JList<>(listModel);
        JScrollPane scrollableList = new JScrollPane(subjects);
        frame.add(scrollableList, BorderLayout.CENTER);

        JPanel addNewSubjectPanel = new JPanel();
        addNewSubjectPanel.setLayout(new GridLayout(8, 3, 20, 50));

        addNewSubjectPanel.add(new JLabel("Enter new Subject: "));
        this.enterNewSubjectName = new JTextField("", 20);
        addNewSubjectPanel.add(enterNewSubjectName);
        addNewSubjectPanel.add(new JLabel("[e.g \"SoftwareDesign\"]"));

        addNewSubjectPanel.add(new JLabel("Enter semesters: "));
        this.enterAmountOfSemesters = new JTextField("", 20);
        addNewSubjectPanel.add(enterAmountOfSemesters);
        addNewSubjectPanel.add(new JLabel("[e.g \"5,6\"]"));


        addNewSubjectPanel.add(new JLabel("Enter frequency: "));
        this.enterFrequency = new JTextField("", 20);
        addNewSubjectPanel.add(enterFrequency);
        addNewSubjectPanel.add(new JLabel("[e.g \"2\"]"));

        addNewSubjectPanel.add(new JLabel("Enter type of class: "));
        this.enterTypeOfClass = new JTextField("", 20);
        addNewSubjectPanel.add(enterTypeOfClass);
        addNewSubjectPanel.add(new JLabel("[e.g \"Lec\"]"));

        addNewSubjectPanel.add(new JLabel("Enter amount of groups: "));
        this.enterAmountOfGroups = new JTextField("", 20);
        addNewSubjectPanel.add(enterAmountOfGroups);
        addNewSubjectPanel.add(new JLabel("[e.g \"2\"]"));

        addNewSubjectPanel.add(new JLabel("Enter teacher: "));
        this.enterTeacherName = new JTextField("", 20);
        addNewSubjectPanel.add(enterTeacherName);
        addNewSubjectPanel.add(new JLabel("[e.g \"Denis Miginskii\"]"));

        this.addButton = new JButton("edit");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = enterNewSubjectName.getText();
                String amountSemesters = enterAmountOfSemesters.getText();
                String teacher = enterTeacherName.getText();
                String freq = enterFrequency.getText();
                String type = enterTypeOfClass.getText();
                String groups = enterAmountOfGroups.getText();
                try {
                    DatabaseManager manager = DatabaseManager.getInstance();
                    manager.addSubject(new Subject(specialization, name, amountSemesters,
                            type, freq, teacher, groups));
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                listModel.addElement("Subject: \t" + name + "\t Semesters: \t" +
                        amountSemesters + "\t Teacher: \t" + teacher + "\t Frequency: \t" +
                        freq + "\t Type: \t" + type + "\t Groups: \t" + groups);
            }
        });
        addNewSubjectPanel.add(new JLabel(""));
        addNewSubjectPanel.add(addButton);
        addNewSubjectPanel.add(new JLabel(""));
        frame.add(addNewSubjectPanel, BorderLayout.WEST);

        this.selectTeacherButton = new JButton("select Teacher");
        selectTeacherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSubject = subjects.getSelectedValue();
                String[] splitted = selectedSubject.split(" ");
                new SelectTeacherWindow(splitted[1], specialization, splitted[3],
                        splitted[9], splitted[7], splitted[11]);
                try {
                    DatabaseManager manager = DatabaseManager.getInstance();
                    //String teacher = manager.getTeacherName(educationalProgram, splitted[1]);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(4, 1, 20, 50));
        buttonsPanel.add(new JLabel(""));
        buttonsPanel.add(new JLabel(""));

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = listModel.remove(subjects.getSelectedIndex());
                String[] words = selected.split("\t");
                Subject subject = new Subject(specialization, words[1], words[3], words[9],
                        words[7], words[5], words[11]);
                try {
                    DatabaseManager manager = DatabaseManager.getInstance();
                    manager.deleteSubject(subject);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(new JLabel(""));
        frame.add(buttonsPanel, BorderLayout.EAST);
    }
}
