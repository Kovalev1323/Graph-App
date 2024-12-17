package com.graphs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Класс Launcher для запуска приложения с настройкой логирования.
 * Логирование перенаправляется как в консоль, так и в файл.
 */
public class Launcher {

    /**
     * Главный метод для запуска приложения и настройки логирования.
     *
     * @param args Аргументы командной строки.
     */
    public static void main(String[] args) {
        setupLoggingToFile(); // Настройка записи логов в файл и консоль
        GraphApp.main(args);  // Запуск основного приложения
    }

    /**
     * Настроить логирование как в консоль, так и в файл.
     * Перенаправляет System.out и System.err.
     */
    private static void setupLoggingToFile() {
        try {
            File logFile = new File("log.txt");
            PrintStream fileOut = new PrintStream(new FileOutputStream(logFile, true));

            // Создаем новый PrintStream, который выводит в оба потока: консоль и файл
            MultiOutputStream multiOut = new MultiOutputStream(System.out, fileOut);
            System.setOut(new PrintStream(multiOut));
            System.setErr(new PrintStream(multiOut));

            System.out.println("Логирование перенаправлено как в консоль, так и в файл: " + logFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Не удалось перенаправить логирование в файл.");
        }
    }

    /**
     * Класс для вывода данных как в консоль, так и в файл.
     */
    private static class MultiOutputStream extends OutputStream {
        private final OutputStream consoleOut;
        private final OutputStream fileOut;

        /**
         * Конструктор, принимающий два потока вывода.
         *
         * @param consoleOut Поток вывода в консоль.
         * @param fileOut    Поток вывода в файл.
         */
        public MultiOutputStream(OutputStream consoleOut, OutputStream fileOut) {
            this.consoleOut = consoleOut;
            this.fileOut = fileOut;
        }

        /**
         * Переопределенный метод для записи данных в оба потока.
         *
         * @param b байт данных, который нужно записать.
         * @throws IOException в случае ошибок записи.
         */
        @Override
        public void write(int b) throws IOException {
            consoleOut.write(b); // Записываем в консоль
            fileOut.write(b);     // Записываем в файл
        }

        /**
         * Переопределенный метод для записи данных в оба потока.
         *
         * @param b массив байтов данных, которые нужно записать.
         * @param off смещение в массиве.
         * @param len количество байтов для записи.
         * @throws IOException в случае ошибок записи.
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            consoleOut.write(b, off, len); // Записываем в консоль
            fileOut.write(b, off, len);     // Записываем в файл
        }

        /**
         * Закрытие потока.
         *
         * @throws IOException в случае ошибок при закрытии.
         */
        @Override
        public void close() throws IOException {
            consoleOut.close();
            fileOut.close();
        }
    }
}
