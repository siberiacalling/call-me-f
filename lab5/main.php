<?php

/*
 * http://fedoruk.comcor.ru/Dev_bach/phplab.html
 *
 * В данной лабораторной работе используется следующее программное обеспечение:
 * web-сервер apache (http://www.apache.org);
 * интепретатор языка PHP (http://www.php.org);
 * СУБД mySQL (http://www.mysql.com);
 * "простая" графическая библиотека GD (http://www.boutell.com/gd).
 *
 * Для выполнения лабороторной работы используется сервер rk6lab.bmstu.ru.
 * Php-файлы загружаются в каталог /pub/htdocs от имени пользователя rk6stud с помощью утилиты ftp.
 * Доступ к БД femdb осуществляется по адресу localhost от имени пользователя rk6stud (пароль - rk6stud).
 *
 * Придумать цветовую шкалу, характеризующую значение площади треугольного КЭ.
 * Разработать web-программу для рисования конечно-элементной сетки по данным таблиц nodes и elements базы данных femdb.
 * Раскрасить все КЭ соответственно предложенной шкале.
 */

$conn = mysqli_connect("localhost", "rk6stud", "rk6stud", "femdb");
if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
}

// Делаем join таблицы elements и nodes: в ответе получаем три координаты треугольников
$sql = "SELECT node1.x x1, node1.y y1, node2.x x2, node2.y y2, node3.x x3, node3.y y3
        FROM elements e, nodes node1, nodes node2, nodes node3
        WHERE e.n1 = node1.id AND e.n2 = node2.id AND e.n3 = node3.id";
$result = mysqli_query($conn, $sql);

// Создаем прозрачный фон картинки
$image = imagecreatetruecolor(1000, 500);
imagesavealpha($image, true);
$trans_colour = imagecolorallocatealpha($image, 0, 0, 0, 127);
imagefill($image, 0, 0, $trans_colour);

// Цвет для границ треугольников и надписей
$purple = imagecolorallocate($image, 76, 0, 53);

// Цвета для треугольников различный площадей (для получения цвета, площадь делится на 50 - и получается позиция в данном массиве)
$colors = array(
    imagecolorallocate($image, 255, 255, 204),
    imagecolorallocate($image, 255, 255, 153),
    imagecolorallocate($image, 255, 255, 102),
    imagecolorallocate($image, 255, 255, 51),
    imagecolorallocate($image, 255, 255, 0),
    imagecolorallocate($image, 204, 204, 0),
    imagecolorallocate($image, 153, 153, 0),
    imagecolorallocate($image, 102, 102, 0),
    imagecolorallocate($image, 51, 51, 0),
);

// Обрабатываем результат селекта по строчке за раз (по трегольнику за раз)
while ($row = mysqli_fetch_assoc($result)) {
    // Площадь треугольника по трем координатам
    $area = abs($row["x1"] * ($row["y2"] - $row["y3"]) + $row["x2"] * ($row["y3"] - $row["y1"]) + $row["x3"] * ($row["y1"] - $row["y2"])) / 2;
    // Здесь и далее для всех координат треугольника:
    // 1. Умножаем координату на 4, чтобы было побольше
    // 2. Прибавляем половину размера фона (т.е. 500 по ширине и 250 по высоте), т.к. в базе есть отрицательные координаты,
    //    а библиотека оперируют только положительными координатами
    $values = array(
        $row["x1"] * 4 + 500, $row["y1"] * 4 + 250,
        $row["x2"] * 4 + 500, $row["y2"] * 4 + 250,
        $row["x3"] * 4 + 500, $row["y3"] * 4 + 250,
    );
    // Рисуем фон, а потом границу (чтобы фон не закрасил границу)
    imagefilledpolygon($image, $values, 3, $colors[$area / 50]);
    imagepolygon($image, $values, 3, $purple);
    // Вычисление центра прямоугольника и помещение туда надписи
    imagestring($image, 1, ($row["x1"] + $row["x2"] + $row["x3"]) / 3 * 4 + 500, ($row["y1"] + $row["y2"] + $row["y3"]) / 3 * 4 + 250, $area, $purple);

}

// Отрисовываем легенду
for ($i = 0; $i < sizeof($colors); $i++) {
    imagefilledrectangle($image, $i * 50, 50, ($i + 1) * 50, 0, $colors[$i]);
    imagestring($image, 2, $i * 50, 25, ($i * 50) . "-" . ($i + 1) * 50, $purple);
}

// Вывод изображения
header('Content-type: image/png');
imagepng($image);

imagedestroy($image);
mysqli_close($conn);


