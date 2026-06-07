# visualSSS — Secreto Compartido en Imágenes

Implementación en Java del esquema de secreto compartido en imágenes descripto en *"An Efficient Secret Image Sharing Scheme"* de Kuang-Shyr Wu y Tsung-Ming Lo, combinado con esteganografía LSB.

## Compilación

Requiere Java 21 y Maven.

```bash
mvn package
```

Genera el JAR en `target/visualsss-1.0-SNAPSHOT-jar-with-dependencies.jar`. El script `visualSSS` en la raíz del proyecto lo invoca directamente.

## Uso

```
./visualSSS -d -secret <imagen> -k <número> [-n <número>] [-dir <directorio>]
./visualSSS -r -secret <imagen> -k <número> [-dir <directorio>]
```

**Parámetros obligatorios:**

- `-d` / `-r`: modo distribución o recuperación (mutuamente excluyentes).
- `-secret imagen`: imagen secreta a ocultar (en `-d`) o archivo de salida con el secreto recuperado (en `-r`). Debe tener extensión `.bmp`.
- `-k número`: cantidad mínima de sombras necesarias para recuperar el secreto. Valor entre 2 y 10.

**Parámetros opcionales:**

- `-n número`: cantidad total de sombras del esquema (k, n). Solo válido con `-d`. Si se omite, se usa la cantidad de imágenes BMP en el directorio.
- `-dir directorio`: directorio donde se encuentran las imágenes portadoras. Si se omite, se usa el directorio actual.

**Ejemplos:**

```bash
# Distribuir clave.bmp en esquema (2, 4) usando imágenes del directorio "varias"
./visualSSS -d -secret clave.bmp -k 2 -n 4 -dir varias

# Distribuir clave.bmp con k=3 usando todas las imágenes del directorio actual
./visualSSS -d -secret clave.bmp -k 3

# Recuperar secreta.bmp con k=2 del directorio "varias"
./visualSSS -r -secret secreta.bmp -k 2 -dir varias

# Recuperar secreta.bmp con k=3 del directorio actual
./visualSSS -r -secret secreta.bmp -k 3
```

## Requisitos de las imágenes

### Imagen secreta

- Formato BMP, 8 bits por píxel (escala de grises), sin compresión.
- El ancho y el total de píxeles (ancho × alto) deben ser divisibles por `k`.

### Imágenes portadoras — Esquema (8, n)

- Formato BMP, 8 bits por píxel, sin compresión.
- Deben tener exactamente el mismo ancho y alto que la imagen secreta.
- Se necesitan al menos `n` imágenes que cumplan esa condición en el directorio.
- El ocultamiento se realiza modificando el **1 bit menos significativo** de cada píxel portador.

### Imágenes portadoras — Esquema (k, n) con k distinto de 8

- Formato BMP, 8 bits por píxel, sin compresión.
- Deben tener dimensiones: **ancho = (ancho\_secreto / k) × 2**, **alto = alto\_secreto**.
- El ocultamiento se realiza modificando los **4 bits menos significativos** de cada píxel portador, lo que requiere 2 píxeles portadores por cada valor de sombra.
- Esto garantiza que las portadoras sean siempre de tamaño menor o igual a la imagen secreta para cualquier k entre 2 y 10.
- La semilla y el número de sombra se almacenan en los bytes 6–9 del encabezado BMP de cada portadora.
