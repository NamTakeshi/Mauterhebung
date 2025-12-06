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
- `closeBooking()`
- `handleAutomatic()` / `handleManual()`

**Resultat:** Alle 6 Tests sind grün ✓

### Phase 2: DAOs erstellen & Service anpassen

Für jeden DAO:
1. Interface schreiben (Methodensignaturen)
2. Implementierung schreiben (SQL-Code)
3. Im Service nutzen statt direkt SQL zu schreiben
4. Alte, unbenutzte Methoden von Service-Klasse entfernen

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
- [ ] DAO-Pattern: Interface + Implementierung
- [ ] Exceptions: UnkownVehicleException, InvalidVehicleDataException, 


