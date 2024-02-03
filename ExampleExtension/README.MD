## Jest to przykładowe rożeszenie które pokazuje jak można tworzyć inne rozszerzenia.

# Zależności (dependencies)

Aktualnie nie posiadamy żadnego repozytorium ani nie używam `Maven Central` dlatego aby dodać zależności musisz zrobić
to lokalnie<br>

## Jak to zrobić?

Dodaj w głównym katalogu projektu folder `libs`. a następnie w `<dependencies>` dodaj zależności tak jak jest to
pokazane poniżej

```xml
<dependencies>
    <!--        Inne zależności     -->
    <dependency>
        <groupId>me.indian.bds</groupId>
        <artifactId>BDS-Auto-Enable</artifactId>
        <version>1.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/libs/BDS-Auto-Enable-1.0.jar</systemPath>
    </dependency>
    <!--        Inne zależności     -->
</dependencies>
```

Jeśli danej nie rozumiesz zobacz te poradniki: <br>
[Baeldung -- Install local jar with-maven](https://www.baeldung.com/install-local-jar-with-maven)<br>
[Cloudhadoop -- Maven install local jarfile](https://www.cloudhadoop.com/maven-install-local-jarfile#how-to-install-the-local-repository-jar-with-systempath-of-dependency)<br>

----
[Przykładowy `pom.xml` (W projekcie Multi-Module)](pom.xml)

