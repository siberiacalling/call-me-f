#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <mpi.h>
#include <sys/time.h>

// Длина стержня
#define L 20

// Параметр теплопроводности
#define Lamb 0.5

// Временные узлы
#define TIME 10

// Шаг по времени
#define dT 1

int main(int argc, char **argv) {
  int myrank, total;

  // Используется только в root
  double *T;

  // Лента матрицы [mxn], вектор [n], рез-т [m]
  double *t;
  int i, j;

  // Инициализация
  MPI_Init(&argc, &argv);
  MPI_Comm_size(MPI_COMM_WORLD, &total);
  MPI_Comm_rank(MPI_COMM_WORLD, &myrank);

  // Число шагов по времени
  int n = TIME / dT + 1;

  // Длины на процесс
  int m = (L) / total;

  // В нулевом процессе
  if (!myrank) {
    // Выделение памяти под весь результат
    T = (double *) malloc(sizeof(double) * n * L);

    // Граничные условие первого рода
    T[0] = 20;
    T[L - 1] = 50;
  };

  // Выделение памяти под полосу каждого процесса
  t = malloc(n * m * sizeof(double));

  struct timeval start, end;

  MPI_Scatter((void *) T,
              m,
              MPI_DOUBLE,
              (void *) t,
              m,
              MPI_DOUBLE,
              0,
              MPI_COMM_WORLD); // Разделение начальных условий по процессам

   //  gettimeofday(&start, 0);
  // Расчет цикл по времени
  for (i = 1; i < n; i++) {
    double left, right;
    MPI_Status s1, s2;

    // Обмен данными с соседними полосами
    if (myrank != 0) {
      MPI_Sendrecv(&t[(i - 1) * m],
                   1,
                   MPI_DOUBLE,
                   myrank - 1,
                   0,
                   &left,
                   1,
                   MPI_DOUBLE,
                   myrank - 1,
                   MPI_ANY_TAG,
                   MPI_COMM_WORLD,
                   &s1);
    }
    if (myrank != total - 1) {
      MPI_Sendrecv(&t[(i - 1) * m + (m - 1)],
                   1,
                   MPI_DOUBLE,
                   myrank + 1,
                   1,
                   &right,
                   1,
                   MPI_DOUBLE,
                   myrank + 1,
                   MPI_ANY_TAG,
                   MPI_COMM_WORLD,
                   &s2);
    }

    // Цикл по полосе
    for (j = 0; j < m; j++) {
      if (j == 0) {
        // Левая граница
        if (myrank == 0) {
          t[i * m + j] = t[(i - 1) * m + j];
        } else {
          t[i * m + j] = Lamb * (t[(i - 1) * m + j + 1] - 2 * t[(i - 1) * m + j] + left) * dT + t[(i - 1) * m + j];
        }
      }
        // Правая граница
      else if (j == m - 1) {
        if (myrank == total - 1) {
          t[i * m + j] = t[(i - 1) * m + j];
        } else {
          t[i * m + j] = Lamb * (right - 2 * t[(i - 1) * m + j] + t[(i - 1) * m + j - 1]) * dT + t[(i - 1) * m + j];
        }
      } else //внутренние узлы
      {
        t[i * m + j] =
            Lamb * (t[(i - 1) * m + j + 1] - 2 * t[(i - 1) * m + j] + t[(i - 1) * m + j - 1]) * dT + t[(i - 1) * m + j];
      }
    }
  }
  gettimeofday(&end, 0);
//  if (!myrank)
//    printf("%u.%u\n",
//           (unsigned int) end.tv_sec - (unsigned int) start.tv_sec,
//           (unsigned int) end.tv_usec - (unsigned int) start.tv_usec);

  // Сбор всех результатов нулевой процесс
  for (i = 1; i < n; i++) {
    MPI_Gather((void *) (t + i * m), m, MPI_DOUBLE, (void *) (T + i * L), m, MPI_DOUBLE, 0, MPI_COMM_WORLD);
  }

  // Вывод в файл
  if (!myrank) {
    FILE *file;
    file = fopen("lab6.dat", "a");

    for (i = 0; i < n; i++) {
      for (j = 0; j < L; j++) {
        fprintf(file, "%d %f\n", j, T[i * L + j]);
        printf("%.2f ", T[i * L + j]);
      }
      printf("\n");
      fprintf(file, "\n\n");
    }
    fclose(file);

    // Файл для gnuplot
    file = fopen("grapth", "w");
    fprintf(file, "set yrange [0:100]\n");
    fprintf(file, "do for [i=0:%d]{\n", n - 1);
    fprintf(file, "plot 'lab6.dat' index i using 1:2 with lines\n");
    fprintf(file, "pause 0.5}\n pause -1");
  }
  // Завершение
  MPI_Finalize();
  exit(0);
}
