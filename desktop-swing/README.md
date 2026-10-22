# Ötöslottó Swing alkalmazás

Java Swing asztali szimulátor Windowsra és más Java platformokra. A program 90 számot jelenít meg, legfeljebb öt tippet enged kijelölni, majd öt különböző számot sorsol 1 és 90 között, és megmutatja a találatok számát.

## Indítás a JAR fájllal

```sh
java -jar target/lotto-swing-1.0.0.jar
```

## Fordítás forrásból

Java 17 vagy újabb JDK szükséges. A projekt gyökeréből:

```sh
mkdir -p target/classes
javac --release 17 -d target/classes src/main/java/hu/vandi/lotto/DesktopLottoApp.java
jar --create --file target/lotto-swing-1.0.0.jar \
  --main-class hu.vandi.lotto.DesktopLottoApp -C target/classes .
```
