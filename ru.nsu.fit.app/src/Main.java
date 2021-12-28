import entities.*;
import managers.DatabaseManager;
import org.projog.api.Projog;
import org.projog.api.QueryResult;
import org.projog.core.term.Term;

import java.io.*;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.*;

//import static org.graalvm.compiler.debug.DebugOptions.PrintGraphTarget.File;

public class Main {
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void prepare_prolog(File main_temp) throws IOException, SQLException {
        FileWriter fw = new FileWriter(main_temp, true);
        DatabaseManager dm = DatabaseManager.getInstance();

        fw.write("\n");

        // Число попыток по умолчанию
        // Служебный параметр
        fw.write("attempts(1).\n");

        // Текущий семестр
        // Пока что служебный параметр (зависит от внедрения Матвея)
        fw.write("semester(1).\n");

        // Добавление ограничений
        Constraints constraints = dm.getConstraints();
        fw.write("study_days_in_week(".concat(constraints.studyDaysInWeek).concat(").\n"));
        fw.write("study_days_in_week_students(".concat(constraints.studyDaysInWeekForStudents).concat(").\n"));
        fw.write("study_days_in_week_teachers(".concat(constraints.studyDaysInWeekForTeachers).concat(").\n"));
        fw.write("classes_in_day(".concat(constraints.classesPerDay).concat(").\n"));
        fw.write("classes_in_day_students(".concat(constraints.classesPerDayStudents).concat(").\n"));
        fw.write("classes_in_day_teachers(".concat(constraints.classesPerDayTeachers).concat(").\n"));
        // Пока что служебные параметры (зависят от внедрения Матвея)
        fw.write("c_gaps(0, 0).\n");
        fw.write("c_gaps(1, 2).\n");
        fw.write("c_gaps(2, 6).\n");
        fw.write("c_gaps(3, 9).\n");
        fw.write("c_gaps(Amount_of_gaps, 10) :- Amount_of_gaps > 3.\n");
        fw.write("classroom_fillness(0, 10, 10).\n");

        // Добавление типов занятий
        fw.write("type_of_class('pr').\n");
        fw.write("type_of_class('lec').\n");
        fw.write("type_of_class('lab').\n");
        fw.write("type_of_class('pe').\n");
        fw.write("type_of_class('misc').\n");

        // Добавлен типов аудиторий
        // Пока что служебные параметры (зависят от внедрения Матвея)
        fw.write("type_of_classroom('huge for lectures and practices', [type_of_class('lec'), type_of_class('pr')], 200).\n");
        fw.write("type_of_classroom('big for lectures and practices', [type_of_class('lec'), type_of_class('pr')], 80).\n");
        fw.write("type_of_classroom('medium for lectures and practices', [type_of_class('lec'), type_of_class('pr')], 40).\n");
        fw.write("type_of_classroom('small for lectures and practices', [type_of_class('lec'), type_of_class('pr')], 20).\n");
        fw.write("type_of_classroom('terminals', [type_of_class('pr'), type_of_class('lab')], 20).\n");
        fw.write("type_of_classroom('room for pe', [type_of_class('pe')], 500).\n");

        DatabaseManager manager = DatabaseManager.getInstance();
        manager.deleteTeacher(new Teacher("Vaskevich", "", "", "", ""));

        // Добавление аудиторий
        ArrayList<Auditory> auditories = (ArrayList<Auditory>) dm.getAuditories();
        for (Auditory auditory : auditories) {
            if (auditory.number.equals("")) {
                continue;
            }
            fw.write("classroom(".
                    concat("'").concat(auditory.number).concat("'").
                    concat(", ").
                    concat(buildPrologList((ArrayList<String>) dm.getAuditoryTypes(auditory.number), true,
                            "type_of_class(", ")").
                            concat(", ").
                            concat(auditory.capacity).
                            concat(").\n")));
        }

        // Добавление факультетов
        ArrayList<Faculty> faculties = (ArrayList<Faculty>) dm.getAllFaculties();
        for (Faculty faculty : faculties) {
            fw.write("faculty(".
                    concat("'").concat(faculty.name).concat("'").
                    concat(").\n"));
        }

        // Добавление программ образований
        for (Faculty faculty : faculties) {
            ArrayList<EducationalProgram> educationalPrograms = (ArrayList<EducationalProgram>) dm.getEducationalPrograms(faculty.name);
            Set<String> educationalProgramsNames = new HashSet<String>();
            for (EducationalProgram educationalProgram : educationalPrograms) {
                educationalProgramsNames.add(educationalProgram.name);
            }
            for (EducationalProgram educationalProgram : educationalPrograms) {
                if (educationalProgramsNames.contains(educationalProgram.name)) {
                    fw.write("ed_program(".
                            concat("'").concat(educationalProgram.faculty).concat("'").
                            concat(", ").
                            concat("'").concat(educationalProgram.name).concat("'").
                            concat(").\n"));
                    educationalProgramsNames.remove(educationalProgram.name);
                }
            }
        }

        // Добавление специализаций
        for (Faculty faculty : faculties) {
            ArrayList<EducationalProgram> educationalPrograms = (ArrayList<EducationalProgram>) dm.getEducationalPrograms(faculty.name);
            for (EducationalProgram educationalProgram : educationalPrograms) {
                fw.write("specialization(".
                        concat("'").concat(educationalProgram.name).concat("'").
                        concat(", ").
                        concat("'").concat(educationalProgram.specialization).concat("'").
                        concat(").\n"));
            }
        }

        // Добавление учителей
        ArrayList<Teacher> teachers = (ArrayList<Teacher>) dm.getAllTeachers();
        for (Teacher teacher : teachers) {
            fw.write("teacher(".
                    concat("'").concat(teacher.name).concat("'").
                    concat(").\n"));
        }

        // Добавление групп студентов
        for (Faculty faculty : faculties) {
            ArrayList<EducationalProgram> educationalPrograms = (ArrayList<EducationalProgram>) dm.getEducationalPrograms(faculty.name);
            for (EducationalProgram educationalProgram : educationalPrograms) {
                ArrayList<Group> groups = (ArrayList<Group>) dm.getGroups(educationalProgram.specialization);
                for (Group group : groups) {
                    fw.write("group_of_students(".
                            concat("'").concat(group.specialization).concat("'").
                            concat(",").
                            concat("'").concat(group.numberOfGroup).concat("'").
                            concat(",").
                            concat(group.amountOfStudents).
                            concat(",").
                            concat(group.yearOfStudy).
                            concat(").\n"));
                }
            }
        }

        // Добавление списка групп студентов
        for (Faculty faculty : faculties) {
            ArrayList<EducationalProgram> educationalPrograms = (ArrayList<EducationalProgram>)
                    dm.getEducationalPrograms(faculty.name);
            for (EducationalProgram educationalProgram : educationalPrograms) {
                Map<Integer, List<String>> allSpecializationGroups =
                        dm.getAllSpecializationGroups(educationalProgram.specialization);
                for (int year : allSpecializationGroups.keySet()) {
                    fw.write("list_groups_of_students(".
                            concat("'").concat(educationalProgram.specialization).concat("'").
                            concat(",").
                            concat(String.valueOf(year)).
                            concat(",").
                            concat(buildPrologList((ArrayList<String>) allSpecializationGroups.get(year),
                                    true, "", "")).
                            concat(").\n"));
                }
            }
        }

        // Это временная мера, поскольку на данный момент БД не умеет в иерархию дубликатов предметов. Зависит от Матвея
        // Как БД будет исправлена, тотчас будет реализован эффективный алгоритм. Клянёмся!!!!
        // Добавление предмета
        for (Faculty faculty : faculties) {
            ArrayList<EducationalProgram> educationalPrograms = (ArrayList<EducationalProgram>)
                    dm.getEducationalPrograms(faculty.name);

            for (EducationalProgram educationalProgram : educationalPrograms) {
                List<String> subjectNames = dm.getSubjectNames(educationalProgram.specialization);
                for (String subjectName : subjectNames) {
                    List<String> allSemestersOfSubject = dm.getSemestersOfSubject(educationalProgram.specialization,
                            subjectName);
                    for (String semestersOfSubject : allSemestersOfSubject) {
                        List<String> typesOfClass = dm.getTypesOfClasses(educationalProgram.specialization,
                                subjectName, semestersOfSubject);
                        fw.write("subject(".
                                concat("'").concat(educationalProgram.specialization).concat("'").
                                concat(",").
                                concat("'").concat(subjectName).concat("'").
                                concat(",").
                                concat("[").concat(semestersOfSubject).concat("]").
                                concat(",").
                                concat("["));
                        for (int i = 0; i < typesOfClass.size(); i++) {
                            List<Subject> subjects = dm.getSubjectsDuplicates(educationalProgram.specialization,
                                    subjectName, semestersOfSubject, typesOfClass.get(i));

                            fw.write("[type_of_class(".
                                    concat("'").concat(typesOfClass.get(i)).concat("'").concat(")").
                                    concat(",").
                                    concat(subjects.get(0).frequency).
                                    concat(",").
                                    concat("["));

                            for (int j = 0; j < subjects.size(); j++) {
                                fw.write("[".
                                        concat("teacher(").
                                        concat("'").concat(subjects.get(j).teacher).concat("'").
                                        concat("),").
                                        concat(subjects.get(j).amountOfGroups).
                                        concat("]"));
                                if (j < subjects.size() - 1) {
                                    fw.write(",");
                                }
                            }

                            fw.write("]]");

                            if (i < typesOfClass.size() - 1) {
                                fw.write(",");
                            }
                        }

                        fw.write("]).\n");
                    }
                }
            }
        }

        // Добавление дней, когда преподователь МОЖЕТ работать
        for (Teacher teacher : teachers) {
            fw.write("days_teacher_can_work(".
                    concat("teacher('").concat(teacher.name).concat("')").
                    concat(",").
                    concat(teacher.daysTeacherCanWork).
                    concat(").\n"));
        }

        // Добавление дней, когда преподователь ХОЧЕТ работать
        for (Teacher teacher : teachers) {
            fw.write("days_teacher_want_work(".
                    concat("teacher('").concat(teacher.name).concat("')").
                    concat(",").
                    concat(teacher.daysTeacherWantWork).
                    concat(",").
                    concat(teacher.weight).
                    concat(").\n"));
        }

        fw.close();
    }

    public static String buildPrologList(ArrayList<String> elements, boolean brackets, String prefix, String postfix) {
        String res = "[";
        for (int i = 0; i < elements.size(); i++) {
            res = res.concat(prefix);
            if (brackets) {
                res = res.concat("'");
            }
            res = res.concat(elements.get(i));
            if (brackets) {
                res = res.concat("'");
            }
            res = res.concat(postfix);
            if (i < elements.size() - 1) {
                res = res.concat(", ");
            }
        }
        res = res.concat("]");

        return res;
    }

    public static String buildPrologList(String element, boolean brackets, String prefix, String postfix) {
        String[] elements = element.split(" ");
        String res = "[";
        for (int i = 0; i < elements.length; i++) {
            res = res.concat(prefix);
            if (brackets) {
                res = res.concat("'");
            }
            res = res.concat(elements[i]);
            if (brackets) {
                res = res.concat("'");
            }
            res = res.concat(postfix);
            if (i < elements.length - 1) {
                res = res.concat(", ");
            }
        }
        res = res.concat("]");

        return res;
    }

    public static void main(String[] args) throws IOException, SQLException {
        Projog projog = new Projog();

        File main = new File("main_empty.pl");
        File main_temp = new File("main_mod.pl");
        copyFile(main, main_temp);

        prepare_prolog(main_temp);

        projog.consultFile(main_temp);

        QueryResult result = projog.executeQuery("main(1, Res, Fine).");
        while (result.next()) {
            Term res = result.getTerm("Res");
            if(Objects.equals(res.toString(), "[]")) {
                System.out.println("error");
            } else {
                System.out.println(res.toString());
            }
        }

    }
}