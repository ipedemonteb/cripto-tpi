# visualSSS - Secreto Compartido en Imágenes

El presente trabajo implementa un esquema de secreto compartido en imágenes (k, n) basado en el método de Shamir combinado con esteganografía LSB, siguiendo las especificaciones del artículo *"An Efficient Secret Image Sharing Scheme"* de Kuang-Shyr Wu y Tsung-Ming Lo. El objetivo es permitir la distribución segura e imperceptible de una imagen secreta BMP en $n$ imágenes portadoras (sombras), garantizando que se requieran al menos $k$ sombras para reconstruir el secreto original y que con menos de $k$ no se revele información alguna.

Las funcionalidades incluidas son las siguientes:

- <b>Esquema de Secreto Compartido (k, n)</b>: Implementa la lógica de Shamir en aritmética de cuerpo finito $\mathbb{F}_{257}$ (módulo 257) usando resolución de sistemas mediante el método de Gauss-Jordan para recuperar los coeficientes.
- <b>Esteganografía LSB Adaptativa</b>: Oculta la información de forma visualmente imperceptible en los bits de menor peso de las portadoras, aplicando LSB-1 (1 bit por píxel) para $k = 8$ y LSB-4 (4 bits por píxel) para esquemas con $k \ne 8$.
- <b>Permutación Criptográfica y Semilla</b>: Mezcla los píxeles de la imagen secreta antes de procesarlos para romper correlaciones espaciales. La semilla pseudoaleatoria de 2 bytes se guarda en los bytes de cabecera reservada 6 y 7 de cada portadora.
- <b>Identificación de Sombras</b>: Guarda el ID numérico unívoco de cada sombra (2 bytes) en los bytes de cabecera reservada 8 y 9 del archivo BMP de salida para su correcta identificación al recuperar.
- <b>Soporte Robusto de Formato BMP</b>: Lector y escritor de archivos BMP de 8 bits por píxel (tonos de gris) sin compresión. Soporta y reconstruye correctamente el padding de alineación de fila a múltiplos de 4 bytes para evitar distorsiones.
- <b>Validaciones Rigurosas</b>: Controla de manera preventiva la divisibilidad de la cantidad de píxeles por `k`, la consistencia de las semillas al recuperar, la no inclusión del archivo de salida en los candidatos a sombra y la prevención de bucles infinitos en el ajuste del desbordamiento de módulo 257.

<details>
  <summary>Contenidos</summary>
  <ol>
    <li><a href="#instalación">Instalación</a></li>
    <li><a href="#instrucciones">Instrucciones</a></li>
    <li><a href="#manual-de-usuario">Manual de Usuario</a></li>
    <li><a href="#integrantes">Integrantes</a></li>
  </ol>
</details>

## Instalación

Clonar el repositorio:

- HTTPS:
  ```sh
  git clone https://github.com/ipedemonteb/cripto-tpi.git
  ```
- SSH:
  ```sh
  git clone git@github.com:ipedemonteb/cripto-tpi.git
  ```

Compilación del motor (Java + Maven):

```sh
mvn clean package
```

> **Requisitos**: JDK 21 (compatible con Maven) y un shell compatible con Unix (o Git Bash/WSL en Windows) para ejecutar el script ejecutable `./visualSSS`.

<p align="right">(<a href="#visualsss---secreto-compartido-en-imágenes">Volver</a>)</p>

## Instrucciones

Todos los comandos deben ejecutarse desde la raíz del repositorio con el entorno correspondiente activo.

- Compilación del motor:
  ```sh
  mvn clean package
  ```
- Distribución de un secreto en esquema (2, 4) usando imágenes en un directorio:
  ```sh
  ./visualSSS -d -secret secret.bmp -k 2 -n 4 -dir varias
  ```
- Distribución clásica de un secreto con k=8 usando todas las portadoras del directorio actual:
  ```sh
  ./visualSSS -d -secret secret.bmp -k 8
  ```
- Recuperación del secreto con k=2 a partir de las sombras del directorio "varias":
  ```sh
  ./visualSSS -r -secret recovered.bmp -k 2 -dir varias
  ```
- Recuperación del secreto con k=8 a partir del directorio actual:
  ```sh
  ./visualSSS -r -secret recovered.bmp -k 8
  ```

<p align="right">(<a href="#visualsss---secreto-compartido-en-imágenes">Volver</a>)</p>

## Manual de Usuario

### Distribución de Secreto (-d)

```sh
./visualSSS -d -secret <imagen> -k <número> [-n <número>] [-dir <directorio>]
```

Parámetros:
- `-d`: indica el modo distribución.
- `-secret`: ruta del archivo BMP que contiene la imagen secreta a ocultar. El archivo debe existir, no estar comprimido y ser de 8 bits por píxel.
- `-k`: cantidad mínima de sombras necesarias para recuperar el secreto en el esquema (k, n). Debe ser un entero entre 2 y 10.
- `-n` (opcional): cantidad total de sombras en las que se distribuirá el secreto. Solo válido con `-d`. Si se omite, se usa la cantidad total de imágenes BMP en el directorio.
- `-dir` (opcional): directorio donde se encuentran las imágenes portadoras. Si se omite, se usa el directorio actual.

Archivos generados:
- Se actualizan las primeras `n` imágenes portadoras válidas en el directorio indicado incrustando la información de cada sombra en sus píxeles (LSB) y escribiendo la semilla de permutación en los bytes de cabecera 6-7 y el número de sombra (ID) en los bytes 8-9.

<p align="right">(<a href="#visualsss---secreto-compartido-en-imágenes">Volver</a>)</p>

### Recuperación de Secreto (-r)

```sh
./visualSSS -r -secret <imagen> -k <número> [-dir <directorio>] [-n <número>]
```

Parámetros:
- `-r`: indica el modo de recuperación de secreto.
- `-secret`: nombre y ruta del archivo BMP donde se guardará el secreto recuperado.
- `-k`: cantidad de sombras requeridas para reconstruir el secreto (debe coincidir con la `k` utilizada al distribuir).
- `-dir` (opcional): directorio donde se encuentran las imágenes que contienen oculto el secreto. Si se omite, se usa el directorio actual.
- `-n` (opcional): parámetro tolerado por compatibilidad pero ignorado (se utiliza exactamente `k` sombras para la reconstrucción).

Archivos generados:
- Genera el archivo BMP de salida con la imagen secreta recuperada.

<p align="right">(<a href="#visualsss---secreto-compartido-en-imágenes">Volver</a>)</p>

### Requisitos de las Imágenes y Esteganografía

#### Imagen Secreta
- Formato BMP, 8 bits por píxel (escala de grises), sin compresión.
- El ancho y el total de píxeles (ancho * alto) deben ser divisibles por `k`.

#### Imágenes Portadoras (Esquema k = 8)
- Formato BMP, 8 bits por píxel (escala de grises), sin compresión.
- Deben tener exactamente el mismo ancho y alto que la imagen secreta.
- El ocultamiento se realiza modificando el **1 bit menos significativo** (LSB-1) de cada píxel portador.

#### Imágenes Portadoras (Esquema k ≠ 8)
- Formato BMP, 8 bits por píxel (escala de grises), sin compresión.
- Deben tener dimensiones exactas: **ancho = (ancho_secreto / k) * 2**, **alto = alto_secreto**.
- El ocultamiento se realiza modificando los **4 bits menos significativos** (LSB-4) de cada píxel portador. Se requieren 2 píxeles portadores por cada valor de sombra.
- Semilla y número de sombra se almacenan en los bytes 6-7 y 8-9 del encabezado BMP de cada portadora en formato Little-Endian de 2 bytes.

<p align="right">(<a href="#visualsss---secreto-compartido-en-imágenes">Volver</a>)</p>

## Integrantes

Nicolas Arancibia Carabajal (64481) - narancibiacarabajal@itba.edu.ar

Martín Alejandro Barnatán (64463) - mbarnatan@itba.edu.ar

Ignacio Pedemonte Berthoud (64908) - ipedemonteberthoud@itba.edu.ar

Pedro Salinas (64388) - psalinas@itba.edu.ar

<p align="right">(<a href="#visualsss---secreto-compartido-en-imágenes">Volver</a>)</p>
