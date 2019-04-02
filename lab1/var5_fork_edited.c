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
int findDigit(char * line, size_t len) {
  if (line[0] == '1') {
   return 1;
  }
  if (line[0] == '2') {
   return 2;
  }
  if (line[0] == '3') {
   return 3;
  }
  if (line[0] == '4') {
   return 4;
  }
  if (line[0] == '6') {
   return 6;
  }
  if (line[0] == '7') {
   return 7;
  }
  if (line[0] == '8') {
   return 8;
  }
  if (line[0] == '9') {
   return 9;
  }
  return -1;
}

int main() {
  const char *filename = "success.txt";
  const char *filename_timeout = "new_timeout.txt";
  int timeout = 3;
  remove(filename);
  remove(filename_timeout);
  for (int i = 0; i < TRIES; i++) {
    int pid = fork();
    // if child
    if (pid == 0) {
      char *line = NULL;
      size_t len = 0;
      printf("10 / 2 = ?\n");
      getline(&line, &len, stdin);
      if (line[0] != '5') {
        int result = findDigit(line, len);
	if (result != -1) {
          timeout = result;
          FILE *fp = fopen(filename_timeout, "w+");
          fprintf(fp, "%d", timeout);
          fclose(fp);
        }
      } else {
        FILE *fp = fopen(filename, "w+");
        fclose(fp);
      }
      free(line);
      exit(0);
    }
    // if parent
    else {
      if (access(filename_timeout, F_OK) != -1) {
          int i = 0;
          FILE *fp = fopen(filename_timeout, "r");
	  fscanf(fp, "%d", &i);
          timeout = i;
      }
      printf("TIMEOUT %d\n", timeout);
      for (int j = 0; j < timeout * 10; j++) {
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
