/* Глобальные функции */

//int MPI_Reduce(void *buf, void *res, int count, MPI_Datatype datatype, MPI_op op, int root, MPI_Comm comm)
/* количесвто данных = count
все, в том числе и root в своих buf хранят данные

| |	| |	| |
| |---> | |---->| |
| |	| |	| |
| |	| |	| |
| |	| |	| |
| |	| |	| |
| |	| |	| |		MPI_SUM
| |	| |	| |		MPI_PROD
| |	| |	| |		MPI_MAX
| |	| |	| |		MPI_MIN
| |	| |	| |
| |	| |	| |			MPI_LAND		логические, битовые операции
					MPI_BAND		битовое И
					MPI_LOR		`	логическое ИЛИ
					MPI_BOR
					
			Выполняться будут только когда все процессы в группе вызовут эту функцию */
			
			
			/* Пример		
		A		 B	 C
	+---------------+	+-+	+-+	
	|_______________|	| |	|_|
	|_______________|	| |	|_|
	|_______________|	| |  =  |_|
	|_______________|	| |	|_|
	|_______________|	| |	|_|
	|		|	| |	| |
	+---------------+	+-+	+-+
	
Проблема: как раздать ленты вычислений и вектор B соответствующим процессам?

Хранить нужно одномерными массивами
В С++ невозможно вообще использовать двумерные массивы, т.к. это ономерный массив указателей на строки(т.е. хранятся адреса, а адреса отправлять мы не можем, т.к. у каждого процесса своё адресное пространство)
В С хранится двумерный последовательно в памяти, но лучше использовать одномерный. Для надежности.

Всю матрицу хранить в одномерном массиве:
		a[N*i+j]
*/
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>


#include <mpi.h>
#define _REENTRANT
#define NPP 100 // количество строк на процесс (ширина ленты матрицы)

int main(int argc, char **argv)
{
	int myrank/*собственный идентификатор процесса*/, total/*общее количество процессов в группе*/
	double *A, *B, *C; // заполняться только в root, но присутствуют во всех процессах
	double *a, *b, *c; // во всех процессах
	int n, m;
	int buf[2];
	
	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &total); // кол-во процессов указывается при запуске
	MPI_Comm_rank(MPI_COMM_WORLD, &myrank)

	/* Подготовка исходных данных. Только root процесс(удобно детлять рутовым процесс с myrank = 0)*/
	if(!myrank)
	{
		n = NPP*total;
		A = (double *) malloc (sizeof(double)*n*n);
    		B = (double *) malloc (sizeof(double)*n);
    		C = (double *) malloc (sizeof(double)*n);
    		// Инициализация матрицы A и вектора B (псевдослучайными числами)
		for (i=0; i<n; i++) 
		{
			B[i] = (double)i;
	      		for (j=0; j<n; j++)
				A[i*n+j] = (double)(i+j);
		}
	}
	
	if(!myrank)
	{
		int buf[0] = n;
		int buf[1] = NPP;
	}	
	
	MPI_Bcast((void*)int buf, 2, MPI_INT, 0, MPI_COMM_WORLD);
	n = int buf[0];
	m = int buf[1];
	
	a = (double *) malloc (sizeof(double)*n*m);
	b = (double *) malloc (sizeof(double)*n);
	c = (double *) malloc (sizeof(double)*m);
	
	if(!myrank)
	{
		memcpy(b, B, sizeof(double)*n);
	}
	MPI_Bcast((void*)b, n, MPI_DOUBLE, 0, MPI_COMM_WORLD);
	MPI_Scatter((void*)A, n*m, MPI_DOUBLE, (void*)a, n*m, MPI_DOUBLE, 0, MPI_COMM_WORLD);
	
	for (i=0; i<m; i++) 
	{
		c[i] = 0;
		for (j=0; j<n; j++)
		{
			c[i] += a[n*i+j]*b[j];
		}
	}
	
	MPI_Gather((void*)c, m, MPI_DOUBLE, (void*)C, MPI_DOUBLE, 0, MPI_COMM_WORLD);
	MPI_Finalize();
	exit(0);
}

/*
mpicc
 
mpiexec -n 4 -f ~/.machinefile
*/

/*  ОСНОВНАЯ ИДЕЯ ДЛЯ РЕШЕНИЯ ЗАДАЧ ТЕПЛОПРОВОДНОСТИ ИЛИ RC ЦЕПОЧЕК



---|-----------------------------------------------|---------------> x

 t ^
   |---------------------------------------------------
   |---------------------------------------------------
   |---------------------------------------------------
 i |---------------------------------------------------
   |---------------------------------------------------
   |---------------------------------------------------
   |----_-------_--------------------------------------
   |--_|0|_---_|0|_------------------------------------
---|-|0_0_0|-|0_0_0|-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-|---------------> x
			j
Для расчета границы необходимо знать значения в граничном узле соседнего процесса.
Как получить?*/

MPI_SendRecv()  //одновременно передаёт справа налево и слева направо














