# Mauterhebungs-System – Dokumentation

## Was ist die Aufgabe?

Wir bauen ein System, das Maut (Straßengebühren) für Fahrzeuge berechnet und speichert. Das System muss:
1. Fahrzeugdaten prüfen
2. Unterscheiden: automatisches oder manuelles Verfahren
3. Maut berechnen und in der DB speichern (automatisch) oder Buchung abschließen (manuell)

---

## Der Algorithmus: siehe Entscheidungsbaum auf Moodle

---

## Das DAO-Pattern

**Warum?** Um SQL-Code aus dem Service zu trennen.

**Struktur:**
- **MautServiceImpl:** Geschäftslogik (Welche Prüfungen? Welche Exceptions?)
- **DAOs:** Nur Datenbankzugriffe (SQL-Statements)

**Es gibt 5 DAOs (Jedes Dao spiegelt einer Entität von Datenbank wieder):**
1. **FahrzeugDao** – FAHRZEUG + FAHRZEUGGERAT
2. **BuchungDao** – BUCHUNG + MAUTKATEGORIE
3. **MautkategorieDao** – MAUTKATEGORIE
4. **MautabschnittDao** – MAUTABSCHNITT
5. **MauterhebungDao** – MAUTERHEBUNG (neue Einträge)

---

## Implementierung (Schritt für Schritt)

### Phase 1: Geschäftslogik schreiben (kein DAO)

In `MautServiceImpl` alle Hilfsmethoden direkt schreiben:
- `isVehicleRegistered()`
- `hasActiveDevice()`
- `isCorrectAxleCountAutomatic()` / `isCorrectAxleCountManual()`
- `findOpenBooking()`
- `handleAutomatic()` / `handleManual()`

**Resultat:** Alle 6 Tests sind grün ✓

### Phase 2: DAOs erstellen & Service anpassen

Für jeden DAO:
1. Interface schreiben mit folgenden Methoden: create, get, update, delete
2. Implementierungsklasse schreiben
3. In der Serviceklasse dann diese DAOs nutzen, um auf die DB zuzugreifen und Objekte zu erstellen, holen, updaten oder löschen.

---

## Knifflige Details

### 1. VARCHAR2 mit Operatoren
ACHSZAHL speichert: `"= 4"` oder `">= 5"` (nicht nur Zahlen!)

### 2. Einheiten-Umrechnung
- Länge: in Metern → divide by 1000 für km
- Satz: in Cent/km → divide by 100 für €/km

### 3. NULL-Checks

---

## Für die Klausur

- [ ] JDBC: Connection, PreparedStatement, ResultSet
- [ ] SQL: JOIN, UNION, WHERE, COUNT, MAX
- [ ] DAO-Pattern: Interface + Implementierungsklasse
- [ ] Exceptions: UnkownVehicleException, InvalidVehicleDataException, 

--- 
## Anmerkungen vom Dozent: 
Zu jedem Dao muss unbedingt eine Klasse für dieses Objekt erstellt werden (wie bei Buchung).
Jede Methode (find, delete, update, create) außer unwichtigen Hilfsmethoden muss dann auf dieses Objekt zugreifen, 
statt auf (boolean, int, String, etc.) siehe findOpenBooking.

Daos Klassen sollen diese 4 Grund Methoden beeinhalten:
create, get, update, delete

Die restlichen sind Hilfsmethoden und sollten eigentlich bei der Service Implementierungsklasse stehen.

Kurz zusammengefasst: Wir sollen mit Objekten arbeiten.

---
## Dao:
DAO‑Instanz:
Ein Objekt vom Typ BuchungDaooImpl, das nur die „Werkzeuge“ für DB‑Zugriffe bereitstellt (SQL ausführen, ResultSet → Buchung bauen, UPDATE senden).
Dieses Objekt hat keinen Zustand pro Buchung, sondern nur die Connection.

Buchung‑Objekte:
Jede Buchung ist ein eigenes Objekt mit eigenen Feldern (buchung_id, b_id, …).
Sie werden vom DAO nur geladen oder gespeichert.

Erlärung zum Dao:
Das Data Access Object (DAO) ist ein Übersetzer und Vermittler in einem Computerprogramm. 
Es ist ein spezielles Objekt, das als einzige Komponente direkt mit der Datenbank (dem "Speicherort") sprechen darf. 
Die Hauptanwendung (die "Spiellogik") gibt dem DAO Anweisungen wie "speichere diesen Benutzer" oder "hole mir alle Bestellungen". 
Das DAO führt diese technischen Befehle aus und liefert die Ergebnisse zurück, 
wodurch der Rest des Programms sauber, flexibel und von den Details der Datenhaltung entkoppelt bleibt.








