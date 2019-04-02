#define _GNU_SOURCE
#include <stdio.h>
#include <zconf.h>
#include <stdlib.h>
#include <signal.h>

/* Составить программу, которая заданное число раз (для определенности 5)
 * через определенный временной интервал (5 сек.)
 * повторяет на экране запрос, ожидающий стандартный ввод.
 * Процесс должен завершаться в случае корректного ответа на запрос
 * или после исчерпывания заданного числа запросов.
 */
#define TIMEOUT 5
#define TRIES 5

int main() {
  const char *filename = "success.txt";
  remove(filename);

  for (int i = 0; i < TRIES; i++) {
    int pid = fork();
    // if child
    if (pid == 0) {
      char *line = NULL;
      size_t len = 0;
      printf("Waiting for input %d\n", i);
      getline(&line, &len, stdin);

      FILE *fp = fopen(filename, "w+");
      fclose(fp);

      free(line);
      exit(0);
    }
    // if parent
    else {
      for (int j = 0; j < TIMEOUT * 10; j++) {
        usleep(100000);
        if (access(filename, F_OK) != -1) {
          exit(0);
        }
      }
      kill(pid, SIGKILL);
    }
  }
  return 0;
}