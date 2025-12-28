## Aufgabe 4
- (Test ->) MautProzedurImpl -> Datenbank

## Grundlagen

PL/SQL ist eine prozedurale Erweiterung von SQL in Oracle, mit der Logik direkt in der Datenbank gespeichert und ausgeführt wird (z.B. Prozeduren, Funktionen, Packages).
- Package: Container für zusammengehörige Prozeduren/Funktionen plus gemeinsame Typen, Konstanten und Exceptions; dient der Kapselung und Wiederverwendung.
- Package Specification: „Interface“ nach außen, enthält nur die Signaturen öffentlicher Prozeduren/Funktionen und öffentlich sichtbare Typen/Exceptions (das, was Java/Test aufruft).
- Package Body: Implementierung der in der Spec deklarierten Prozeduren/Funktionen sowie optional private Hilfsfunktionen, plus die eigentliche Geschäftslogik und Fehlerbehandlung.

## Vorgehensweise für Übung 4 (Allgemein, kurz)
- Bausteine aus dem Ablauf erkennen & Flussdiagramm durchgehen.
- Im Body die Struktur anlegen: Hauptprozedur + private Hilfsfunktionen
- Die Logik Schritt für Schritt in kleinen Teilen ergänzen und jeweils mit den zugehörigen Tests prüfen.


## Package Specs
CREATE OR REPLACE NONEDITIONABLE PACKAGE maut_service AS

-------------------------------------------------------------------------------
-- 1. EXCEPTION DEFINITIONEN
-------------------------------------------------------------------------------
-- Exception wird ausgelöst, falls das Fahrzeug nicht bekannt ist.
-- D.h. es ist nicht im Automatischen Verfahren unterwegs und es liegt keine
-- offene Buchung vor
UNKOWN_VEHICLE EXCEPTION;
PRAGMA EXCEPTION_INIT(UNKOWN_VEHICLE, -20001);

-- Exception wird ausgelöst, falls das Fahrzeug mit der falschen
-- Achszahl unterwegs ist.
INVALID_VEHICLE_DATA EXCEPTION;
PRAGMA EXCEPTION_INIT(INVALID_VEHICLE_DATA, -20002);

-- Exception wird ausgelöst, falls das Fahrzeug den Mautabschnitt
-- bereits befahren hat und keine offene Buchung vorliegt.
ALREADY_CRUISED EXCEPTION;
PRAGMA EXCEPTION_INIT(ALREADY_CRUISED, -20003);

-------------------------------------------------------------------------------
-- 2. VALIDIERUNGSFUNKTIONEN
-------------------------------------------------------------------------------

/**
* Prüft ob Fahrzeug registriert ist (Automatik ODER offene Buchung).
*
* @param p_kennzeichen Amtliches Kennzeichen des Fahrzeugs
* @return Anzahl registrierter Fahrzeuge (0 = nicht registriert)
  */
  FUNCTION f_is_verhicleregistered(
  p_kennzeichen IN VARCHAR2
  ) RETURN NUMBER;

/**
* Prüft Achszahl für Automatik-Verfahren (DB vs. gemessen).
*
* @param p_kennzeichen Amtliches Kennzeichen
* @param p_achszahl Gemessene Achszahl
* @return TRUE wenn Achszahl korrekt, sonst FALSE
  */
  FUNCTION f_is_correct_axle_automatic(
  p_kennzeichen IN FAHRZEUG.kennzeichen%TYPE,
  p_achszahl IN FAHRZEUG.achsen%TYPE
  ) RETURN BOOLEAN;

/**
* Prüft Achszahl für Manuelles Verfahren (aus Buchung/Mautkategorie).
*
* @param p_mautabschnitt Mautabschnitt-ID
* @param p_kennzeichen Amtliches Kennzeichen
* @param p_achszahl Gemessene Achszahl
* @return TRUE wenn Achszahl korrekt, sonst FALSE
  */
  FUNCTION f_is_correct_axle_manual(
  p_mautabschnitt IN BUCHUNG.abschnitts_id%TYPE,
  p_kennzeichen IN FAHRZEUG.kennzeichen%TYPE,
  p_achszahl IN FAHRZEUG.achsen%TYPE
  ) RETURN BOOLEAN;

-------------------------------------------------------------------------------
-- 3. VERFAHREN-ERKENNUNG
-------------------------------------------------------------------------------

/**
* Prüft ob Fahrzeug im Automatik-Verfahren aktiv ist.
* Voraussetzung: Fahrzeug nicht abgemeldet + aktives FAHRZEUGGERÄT
*
* @param p_kennzeichen Amtliches Kennzeichen
* @return TRUE wenn im Automatik-Verfahren, sonst FALSE
  */
  FUNCTION f_is_fahrzeug_im_automatikverfahren(
  p_kennzeichen IN FAHRZEUG.kennzeichen%TYPE
  ) RETURN BOOLEAN;

/**
* Prüft ob Fahrzeug im Manuellen Verfahren (offene Buchung existiert).
*
* @param p_kennzeichen Amtliches Kennzeichen
* @return TRUE wenn offene Buchung existiert, sonst FALSE
  */
  FUNCTION f_is_fahrzeug_im_manuellem_verfahren(
  p_kennzeichen IN FAHRZEUG.kennzeichen%TYPE
  ) RETURN BOOLEAN;

/**
* Prüft ob offene Buchung für spezifischen Abschnitt existiert.
*
* @param p_mautabschnitt Mautabschnitt-ID
* @param p_kennzeichen Amtliches Kennzeichen
* @return TRUE wenn offene Buchung (b_id=1) existiert, sonst FALSE
  */
  FUNCTION f_has_open_buchung(
  p_mautabschnitt IN BUCHUNG.abschnitts_id%TYPE,
  p_kennzeichen IN BUCHUNG.kennzeichen%TYPE
  ) RETURN BOOLEAN;

-------------------------------------------------------------------------------
-- 4. BERECHNUNGS- & DATENFUNKTIONEN
-------------------------------------------------------------------------------

/**
* Ermittelt passende Kategorie-ID für Automatik-Verfahren (SSKL + Achszahl).
*
* @param p_kennzeichen Amtliches Kennzeichen
* @param p_achszahl Gemessene Achszahl
* @return Kategorie-ID
  */
  FUNCTION f_get_kategorie_id_automatic(
  p_kennzeichen IN FAHRZEUG.kennzeichen%TYPE,
  p_achszahl IN FAHRZEUG.achsen%TYPE
  ) RETURN MAUTKATEGORIE.kategorie_id%TYPE;

/**
* Holt ID des aktiven FAHRZEUGGERÄTS (status='active').
*
* @param p_kennzeichen Amtliches Kennzeichen
* @return fzg_id
  */
  FUNCTION f_get_active_fzg_id(
  p_kennzeichen IN FAHRZEUG.kennzeichen%TYPE
  ) RETURN FAHRZEUGGERAT.fzg_id%TYPE;

/**
* Holt Länge des Mautabschnitts in Metern.
*
* @param p_abschnitts_id Mautabschnitt-ID
* @return Abschnitttlänge in Metern oder NULL
  */
  FUNCTION f_get_abschnitts_laenge(
  p_abschnitts_id IN MAUTABSCHNITT.abschnitts_id%TYPE
  ) RETURN MAUTABSCHNITT.laenge%TYPE;

/**
* Generiert nächste verfügbare MAUTERHEBUNG-ID (MAX+1).
*
* @return Nächste maut_id (1 bei leerer Tabelle)
  */
  FUNCTION f_get_next_maut_id RETURN MAUTERHEBUNG.maut_id%TYPE;

-------------------------------------------------------------------------------
-- 5. HAUPTPROZEDUR
-------------------------------------------------------------------------------

/**
* Die Methode realisiert einen Algorithmus, der die übermittelten
* Fahrzeugdaten mit der Datenbank auf Richtigkeit überprüft und für einen
* mautpflichtigen Streckenabschnitt die zu zahlende Maut für ein Fahrzeug
* im Automatischen Verfahren berechnet.
*
* Zuvor wird überprüft, ob das Fahrzeug registriert ist und über ein
* eingebautes Fahrzeuggerät verfügt und die übermittelten Daten des
* Kontrollsystems korrekt sind. Bei Fahrzeugen im Manuellen Verfahren wird
* darüber hinaus geprüft, ob es noch offene Buchungen für den Mautabschnitt
* gibt oder eine Doppelbefahrung aufgetreten ist. Besteht noch eine offene
* Buchung für den Mautabschnitt, so wird diese Buchung für das Fahrzeug auf
* abgeschlossen gesetzt.
*
* Sind die Daten des Fahrzeugs im Automatischen Verfahren korrekt, wird
* anhand der Mautkategorie (die sich aus der Achszahl und der
* Schadstoffklasse des Fahrzeugs zusammensetzt) und der Mautabschnittslänge
* die zu zahlende Maut berechnet, in der Mauterhebung gespeichert und
* letztendlich zurückgegeben.
*
* Parameter p_mautabschnitt identifiziert den mautpflichtigen Abschnitt
* Parameter p_achszahl identifiziert die Anzahl der Achsen des Fahrzeugs
* Parameter p_kennzeichen identifiziert das amtliche Kennzeichen des Fahrzeugs
  */
  PROCEDURE berechnemaut(
  p_mautabschnitt MAUTABSCHNITT.abschnitts_id%TYPE,
  p_achszahl FAHRZEUG.achsen%TYPE,
  p_kennzeichen FAHRZEUG.kennzeichen%TYPE
  );

END maut_service;