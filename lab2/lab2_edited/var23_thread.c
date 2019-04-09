/* Разработать программу, моделирующую в реальном времени работу поточной линии, состоящей из N станков и обрабатывающей заготовки M типов.
Каждая заготовка последовательно проходит обработку на всех станках линии. Времена обработки заготовок каждого типа (в секундах) на каждом
станке линии задаются в виде прямоугольной матрицы размером NxM в конфигурационном файле line.cnf. Первые 2 строки этого файла содержат числа N и M.
Программа реализуется N+1 потоком управления. Корневой поток порождает N потоков-"станков", передавая им информацию о временах обработки заготовок разного типа.
Далее этот поток читает со стандартного ввода последовательность номеров типов заготовок и передает её на вход первого потока-"станка".
Потоки-"станки" имитируют обработку заготовок с помощью функции sleep() и передают номера типов обработанных заготовок потокам-приёмникам.
Предусмотреть вывод информации о ходе обработки заготовок.*/
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <zconf.h>
#include "sts_queue.h"

typedef struct ThreadContext {
  int thread_number;
  int thread_amounts;
  int unfinished_billets_amount;
} ThreadContext;

int **times = NULL;
StsHeader **queues;

void *billet_machine(void *threadContext) {
  ThreadContext *context = (ThreadContext *) threadContext;
  while (context->unfinished_billets_amount > 0) {
    int *billet_number = StsQueue.pop(queues[context->thread_number]);
    if (billet_number == NULL) {
      usleep(100000);
      continue;
    }
    int time_for_sleeping =  times[context->thread_number][*billet_number];
    printf("Thread %d wait %d for %d seconds\n", context->thread_number, *billet_number, time_for_sleeping);
    sleep((unsigned int) time_for_sleeping);
    printf("Thread %d done %d\n", context->thread_number, *billet_number);
    context->unfinished_billets_amount--;
    if (context->thread_number != context->thread_amounts - 1) {
      StsQueue.push(queues[context->thread_number + 1], billet_number);
    }
  }
  return NULL;
}

int main() {
  FILE *fp = fopen("/home/ws/Desktop/lab2", "r");
  int cols_amount = 0, rows_amount = 0;
  fscanf(fp, "%d%d", &rows_amount, &cols_amount);
  //printf("%d %d\n", m, n);

  // int times[n][m];
  times = (int **) malloc(rows_amount * sizeof(int *));
  for (int i = 0; i < rows_amount; i++)
    times[i] = (int *) malloc(cols_amount * sizeof(int));

  for (int i = 0; i < rows_amount; i++) {
    for (int j = 0; j < cols_amount; j++) {
      fscanf(fp, "%d", &times[i][j]);
    }
  }

  int billets_amount = 0;
  fscanf(stdin, "%d", &billets_amount);

  pthread_t threads[billets_amount];
  ThreadContext threads_contexts[billets_amount];

  queues = (StsHeader **) malloc(billets_amount * sizeof(StsHeader *));
  for (int i = 0; i < rows_amount; i++)
    queues[i] = StsQueue.create();

  int billets[billets_amount];
  for (int i = 0; i < billets_amount; i++) {
    fscanf(stdin, "%d", &billets[i]);
    StsQueue.push(queues[0], &billets[i]);
  }

  for (int i = 0; i < billets_amount; i++) {
    threads_contexts[i].thread_amounts = billets_amount;
    threads_contexts[i].thread_number = i;
    threads_contexts[i].unfinished_billets_amount = billets_amount;
    if (pthread_create(&threads[i], NULL, billet_machine, &threads_contexts[i])) {
      fprintf(stderr, "Error creating thread\n");
      return 1;
    }
  }

  for (int i = 0; i < billets_amount; i++) {
    if (pthread_join(threads[i], NULL)) {
      fprintf(stderr, "Error joining thread\n");
      return 2;
    }
  }

  for (int i = 0; i < rows_amount; i++)
    free(times[i]);
  free(times);

  for (int i = 0; i < billets_amount; i++)
    StsQueue.destroy(queues[i]);
  free(queues);

  fclose(fp);
  return 0;
}
