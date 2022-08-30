package com.turulin.java;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {

    private static String file = "students.txt";
    private static Scanner scanner = new Scanner(System.in);
    private static StudentJSONFileEditor studentJSONFile;
    private static FTPSocketClient ftp = null;
    private static String command;

    public static void main(String[] args) throws Exception {
        System.out.println("FTP клиент.");
        while (ftp == null) {
            if (args.length == 3)
                try {
                    ftp = new FTPSocketClient(args[2], args[0], args[1]);
                } catch (NotExpectedResponseStatusException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Повторите ввод: [ЛОГИН] [ПОРОЛЬ] [IP_FTP_СЕРВЕРА]");
                    args = scanner.nextLine().split(" ");
                } catch (UnknownHostException | SocketException e) {
                    System.out.println("Incorrect host address");
                    System.out.println("Повторите ввод: [ЛОГИН] [ПОРОЛЬ] [IP_FTP_СЕРВЕРА]");
                    args = scanner.nextLine().split(" ");
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            else {
                System.out.println("Повторите ввод: [ЛОГИН] [ПОРОЛЬ] [IP_FTP_СЕРВЕРА]");
                args = scanner.nextLine().split(" ");
            }

        }
        System.out.println("FTP сервер подключен.");
        System.out.println("Режим обмена с FTP сервером по умолчанию активный. Желаете изменить на пассивный? д/н");
        if ("д".equalsIgnoreCase(scanner.nextLine())) {
            ftp.activePASVMode();
            System.out.println("Выбран пассивный режим");
        } else {
            System.out.println("Выбран активный режим");
        }
        while (studentJSONFile == null)
            try {
                System.out.println("Укажите файл на FTP сервере: ");
                studentJSONFile = new StudentJSONFileEditor(ftp.downloadFileIntoTempFile(scanner.nextLine()));
            } catch (NotExpectedResponseStatusException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        while (!Commands.quite.toString().equals(command)) {
            printMenu();
            command = scanner.nextLine();
            System.out.println();
            String[] commandWords = command.split(" ");
            Commands mainCmd = null;
            try {
                mainCmd = Commands.valueOf(commandWords[0]);
            } catch (Exception e) {
                System.out.println("Неизвестная команда: " + commandWords[0]);
                continue;
            }
            switch (mainCmd) {
                case stdall: {
                    studentJSONFile.getCacheStudents().forEach((key, value) -> System.out.println(value));
                    break;
                }
                case getstd: {
                    if (commandWords.length < 2) {
                        System.out.println("Missing argument");
                        break;
                    }
                    try {
                        System.out.println(studentJSONFile.getStudentById(Integer.valueOf(commandWords[1])));
                    } catch (NumberFormatException e) {
                        System.out.println("Error id format");
                    } catch (NullPointerException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                }
                case addstd: {
                    if (commandWords.length < 2) {
                        System.out.println("Missing argument");
                        break;
                    }
                    studentJSONFile.addStudent(new Student(0, commandWords[1]));
                    break;
                }
                case rmstd: {
                    if (commandWords.length < 2) {
                        System.out.println("Missing argument");
                        break;
                    }
                    try {
                        studentJSONFile.removeStudentById(Integer.valueOf(commandWords[1]));
                    } catch (NullPointerException e) {
                        System.out.println(e.getMessage());
                    } catch (NumberFormatException e) {
                        System.out.println("Error id format");
                    }
                    break;
                }
                case quite: {
                    studentJSONFile.updateJSONFile();
                    try {
                        ftp.updateRemoteFileByTempFile();
                    } catch (NotExpectedResponseStatusException e) {
                        System.out.println(e.getMessage());
                    }
                    ftp.close();
                    break;
                }
            }
        }
    }

    static void printMenu() {
        System.out.println();
        System.out.println("Введите желаемую команду");
        System.out.println(Commands.stdall + " - вывести список студентов по именам");
        System.out.println(Commands.getstd + " [id] - вывести информацию о студенте по его id");
        System.out.println(Commands.addstd + " [name] - добавление студента с именем name");
        System.out.println(Commands.rmstd + " [id] - удаление студента по id");
        System.out.println(Commands.quite + " - сохранить изменения на FTP сервер и завершить работу");
    }

    enum Commands {
        stdall,
        getstd,
        addstd,
        rmstd,
        quite
    }
}
