#include <stdio.h>
#include <stdlib.h>
#include "mpi.h"

// Длина стержня
#define L 20

// Параметр теплопроводности
#define Lamb 0.5

// Временные узлы
#define TIME 10

// Шаг по времени
#define dT 1

// Граничные условия первого рода
#define LEFT_CONDITION 20

#define RIGHT_CONDITION 50

/*
  Вариант 1.
  Разработать средствами MPI параллельную программу решения (численного интегрирования) одномерной
  нестационарной краевой задачи методом конечных разностей с использованием явной вычислительной схемы.

  Дан цилиндрический стержень длиной L и площадью поперечного сечения S. Цилиндричекая поверхность стержня
  теплоизолирована. На торцевых поверхностях стержня слева и справа могут иметь место граничный условия первого
  и второго родов. Распределение поля температур по длине стержня описывается уравнением теплопроводности.
  Требуется разработать параллельную программу, в которой каждый процесс ответственнен за расчеты для "полосы" расчетной
  сетки шириной в m/N пространственных узлов, где N - число процессов.
  Подробности:
  http://fedoruk.comcor.ru/Dev_bach/mpi_lab.html
*/

int main(int argc, char **argv) {

  remove("test.dat");
  remove("my_graph");

  // myrank - номер процесса
  // total - число процессов
  int myrank, total;

  // Инициализация
  MPI_Init(&argc, &argv);
  MPI_Comm_size(MPI_COMM_WORLD, &total);
  MPI_Comm_rank(MPI_COMM_WORLD, &myrank);

  // Число шагов по времени
  int time_steps_amount = TIME / dT;

  // Длины на процесс
  int length_per_process = L / total;

  // Текущая полоса
  double *T = NULL;

  // В нулевом процессе
  if (!myrank) {
    // Выделение памяти под исходные данные
    T = (double *) calloc(L, sizeof(double));

    // Граничные условие первого рода
    T[0] = LEFT_CONDITION;
    T[L - 1] = RIGHT_CONDITION;

    FILE *file = fopen("test.dat", "a");
    for (int j = 0; j < L; j++) {
      fprintf(file, "%d %f\n", j, T[j]);
    }
    fprintf(file, "\n\n");
    fclose(file);
  }

  for (int i = 0; i < time_steps_amount - 1; i++) {
    double *t = (double *) calloc(length_per_process, sizeof(double));

    // Разделение начальных условий по процессам
    MPI_Scatter((void *) T,
                length_per_process,
                MPI_DOUBLE,
                (void *) t,
                length_per_process,
                MPI_DOUBLE,
                0,
                MPI_COMM_WORLD);

    // Номера процессов соседних полос (слева от текущей полосой и справа)
    int left = myrank - 1;
    int right = myrank + 1;

    // Значение числа из соседней полосы (справа и слева), необходимое для подсчета температуры в текущенй полосе
    double left_number = 0, right_number = 0;

    MPI_Status s1, s2;

    // У первой полосы нет соседней левой, есть только правая
    if (myrank != 0) {
      MPI_Sendrecv(&t[0],
                   1,
                   MPI_DOUBLE,
                   left,
                   0,
                   &left_number,
                   1,
                   MPI_DOUBLE,
                   left,
                   MPI_ANY_TAG,
                   MPI_COMM_WORLD,
                   &s2);
    }

    // У последней полосы нет соседней правой, есть только левая
    if (myrank != total - 1) {
      MPI_Sendrecv(&t[length_per_process - 1],            // Адрес начала расположения посылаемого сообщения
                   1,                                     // Число посылаемых элементов
                   MPI_DOUBLE,                            // Тип посылаемых элементов
                   right,                                 // Номер процесса-получателя в группе
                   1,                                     // Идентификатор сообщения
                   &right_number,                         // Адрес начала расположения принимаемого сообщения
                   1,                                     // Максимальное число принимаемых элементов
                   MPI_DOUBLE,                            // тип элементов принимаемого сообщения;
                   right,                                 // номер процесса-отправителя
                   MPI_ANY_TAG,                           // идентификатор принимаемого сообщения
                   MPI_COMM_WORLD,                        // коммуникатор области связи
                   &s1);                                  // атрибуты принятого сообщения.
    }
    double *t_result = (double *) calloc(length_per_process, sizeof(double));

    for (int j = 0; j < length_per_process; j++) {
      // Левая граница
      if (j == 0 && !myrank) {
        t_result[j] = LEFT_CONDITION;
      }
        // Правая граница
      else if (j == length_per_process - 1 && myrank == total - 1) {
        t_result[j] = RIGHT_CONDITION;
      }
        // Остальные случаи
      else {
        double current_left = t[j - 1];
        double current_right = t[j + 1];
        t_result[j] = Lamb * (current_right - 2 * t[j] + current_left) * dT + t[j];
      }
    }

    MPI_Gather((void *) (t_result),
               length_per_process,
               MPI_DOUBLE,
               (void *) (T),
               length_per_process,
               MPI_DOUBLE,
               0,
               MPI_COMM_WORLD);

    if (!myrank) {
      FILE *file = fopen("test.dat", "a");
      for (int j = 0; j < L; j++) {
        fprintf(file, "%d %f\n", j, T[j]);
      }
      fprintf(file, "\n\n");
      fclose(file);
    }
    free(t);
    free(t_result);
  }

  if (!myrank) {
    FILE *file2 = fopen("my_grapth", "w");
    fprintf(file2, "set yrange [0:100]\n");
    fprintf(file2, "do for [i=0:%d]{\n", length_per_process - 1);
    fprintf(file2, "plot 'test.dat' index i using 1:2 with lines\n");
    fprintf(file2, "pause 0.5}\n pause -1");
    fclose(file2);
  }
  MPI_Finalize();
}