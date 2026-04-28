# SBNZ – Project Proposal

## Predictive maintenance

Sistem za rano otkrivanje anomalija, procenu bezbednosti i preporuke intervencija u industrijskom pogonu.

## Članovi tima

- Luka Bursać SV22-2022

---

## 1. Motivacija

Industrijski proizvodni sistemi (transportne trake, CNC mašine, pakovanje, uslovi okoline) rade u režimu gde mala odstupanja u radu mogu brzo da prerastu u ozbiljan kvar, zastoj linije ili pad kvaliteta proizvoda. Klasičan pristup “čekaj da se kvar desi” dovodi do:

- neplaniranih zastoja,
- većih troškova održavanja,
- povećanog škarta,
- rizika po bezbednost rada.

Cilj ovog projekta je razvoj sistema baziranog na znanju koji automatski:

1. prepoznaje rane signale problema,
2. procenjuje da li je stanje bezbedno,
3. predlaže intervencije,
4. vodi istoriju događaja i računa “zdravlje” mašine.

Na ovaj način operater dobija jasne i pravovremene odluke, umesto ručnog praćenja velikog broja metrika.

---

## 2. Pregled problema

### 2.1 Problem koji rešavamo

Posmatra se pogon sa 4 mašine:

- CLM – kontrola uslova okoline,
- LIN – transportna linija,
- CNC – CNC obrada,
- PKG – pakovanje.

Sistem periodično prima merenja (npr. temperatura, vibracije, opterećenje vretena, procenat škarta, brzina trake) i treba da odgovori na pitanja:

- Da li je neko merenje prešlo dozvoljen prag?
- Da li postoji rastući trend koji najavljuje problem?
- Da li kombinacija više signala zahteva intervenciju?
- Da li mašina mora da bude zaustavljena iz bezbednosnih razloga?
- Koliko je mašina “zdrava” na osnovu istorije problema i popravki?

### 2.2 Kratak pregled postojećih pristupa i nedostaci

U praksi se često koriste:

- SCADA/dashboard alati koji prikazuju podatke, ali ne nude dovoljno duboko rezonovanje i često pate od problema alarm flood/preopterećenja operatera [1](https://www.isa.org/products/ansi-isa-18-2-2016-management-of-alarm-systems-for),
- ML modeli za predikciju kvarova, koji često traže velike istorijske skupove podataka i teže su objašnjivi u kritičnim primenama [2](https://ieeexplore.ieee.org/document/10504833/), [3](https://www.nature.com/articles/s41598-024-59958-9),
- ručno definisani alarmi po jednom signalu, bez povezivanja događaja između mašina i bez hvatanja multivarijantnih korelacija kroz vreme [4](https://openreview.net/forum?id=FiaSGyLIdD), [5](https://www.sciencedirect.com/science/article/pii/S0957582025011498), [6](http://arxiv.org/abs/1803.05636).

Nedostaci ovih pristupa su:

- slabija transparentnost odluka [2](https://ieeexplore.ieee.org/document/10504833/),
- teškoća održavanja pravila/alarm konfiguracije kada se menja proces [1](https://www.isa.org/products/ansi-isa-18-2-2016-management-of-alarm-systems-for),
- neujednačeno povezivanje trendova, praga, bezbednosti i istorije u jednu odluku [4](https://openreview.net/forum?id=FiaSGyLIdD), [5](https://www.sciencedirect.com/science/article/pii/S0957582025011498).

### 2.3 Prednost predloženog rešenja

Predloženo rešenje koristi sistem baziran na znanju sa jasnim pravilima i objašnjivim odlukama:

- pravila su čitljiva i lako proverljiva,
- podržani su i trenutni alarmi i trendovi kroz vreme,
- moguće je ulančavanje pravila kroz više nivoa (forward chaining),
- koristi se i upitno rezonovanje za izračun zdravlja mašine,
- pravila za više metrika se generišu putem templejta, što olakšava održavanje.

### 2.4 Uloga simulacije u projektu

Pošto je za potrebe projekta nemoguće obezbebiti pravi SCADA sistem u realnom vremenu, komponente tj. senzori i vreme su simulirani i njihova kontrola je prepuštena korisniku. Simulacija u ovom projektu nema cilj da zameni realan proizvodni sistem. Njena uloga je:

- demonstracija rada sistema baziranog na znanju u kontrolisanim uslovima,
- testiranje i proveravanje pravila bez rizika po realnu opremu,
- priprema koncepta za kasniju integraciju sa stvarnim izvorima podataka.

Suština projekta je logika odlučivanja koja se kasnije može primeniti nad stvarnim merenjima, a ne logika simuliranja sistema.

---

## 3. Metodologija rada

### 3.1 Ulazi u sistem (input)

Sistem koristi sledeće ulaze:

1. Konfiguracija procesa po mašini i metrici
 - nominalne vrednosti,
 - prag anomalije,
 - prag stresa,
 - da li je metrika uključena u trend analizu.

2. Tok telemetrije kroz vreme
 - periodična merenja za svaki senzor (u simuliranom vremenu),
 - vreme svakog merenja.

3. Status opterećenja po metrici
 - `NORMAL` / `OVERWORKED` / `REST` (simulacija opterećenja mašine).

4. Komande operatera
 - "Fix machine" operacija, nakon crkavanja mašine (simulirani majstor).

### 3.2 Izlazi iz sistema (output)

Sistem emituje:

- listu anomalija (tip, metrika, vreme),
- listu intervencija (prioritet i preporuka),
- rezultate bezbednosti (SAFE/UNSAFE + razlog),
- informaciju o zaustavljenim mašinama,
- istoriju događaja,
- izračunat procenat zdravlja mašine (Machine health).

### 3.3 Baza znanja i način popunjavanja

Baza znanja je organizovana kroz:

- statička pravila (`rules.drl`),
- generisana pravila pragova (`generated_thresholds.drl`),
- generisana CEP trend pravila (`generated_trends.drl`),
- templejte (`templates_thresholds.drt`, `templates_trends.drt`).

Instanciranje templejt pravila:

- Templejt fajl definiše “šablon” pravila sa promenljivim poljima (npr. naziv pravila, machineId, metricKey, prag).
- Prilikom pokretanja aplikacije, sistem prolazi kroz definicije mašina i njihovih metrika i za svaku kombinaciju pravi jedan red ulaznih podataka za templejt.
- Na osnovu tih redova, templejt se kompajlira u konkretan DRL sadržaj, koji se zatim učitava zajedno sa ostalim pravilima. Tako se dobija veliki broj sličnih pravila bez ručnog pisanja svakog pojedinačno.

Popunjavanje znanja:

1. simulacija ubacuje nove telemetrijske događaje,
2. pravila izvode nove činjenice (Anomaly, Intervention, MachineOverworked, SafetyResult),
3. pri ubacivanju novih odluka automatski se formiraju audit činjenice (`RecordedAnomaly`, `RecordedIntervention`, `RecordedMachineOverworked`, `RecordedFix`),
4. upit `MachineHealth` računa zbirni health score iz istorije.

Interakcije zasnovane na znanju:

- kada su uslovi ponovo normalni, “clear” pravila uklanjaju aktivne intervencije/trend anomalije,
- kod ponovljenog stresa aktivira se bezbednosno zaustavljanje,
- operater potvrđuje i resetuje stanje preko “Fix”.

---

## 4. Entiteti domena

Mehanizam Machine registry:

- Machine registry je centralno mesto gde su definisane mašine, njihove metrike i osnovne karakteristike procesa (npr. identifikator mašine, tip, koje metrike postoje).
- Registry se koristi u dva ključna trenutka: (1) pri inicijalizaciji simulacije da bi se ubacile početne činjenice za svaku mašinu i (2) pri instanciranju templejt pravila, gde registry daje spisak mašina i metrika iz kojih se generišu prag i trend pravila.
- Prednost ovakvog pristupa je što je konfiguracija domena na jednom mestu, a pravila se “razmnožavaju” automatski na osnovu te konfiguracije.

### 4.1 Ključni entiteti (fakti)

- `Machine` – mašina u sistemu,
- `TelemetryReading` – pojedinačno merenje kroz vreme (event),
- `CurrentMetric` – poslednja vrednost metrike,
- `MetricTick` – vrednost metrike po tick-u (event),
- `TickStatus` – indikator stresa po mašini i tick-u (event),
- `Anomaly` – detektovana anomalija,
- `Intervention` – preporučena akcija (MEDIUM/HIGH/CRITICAL),
- `MachineOverworked` – indikator da je mašina preopterećena i mora da stane,
- `SafetyCheck` / `SafetyResult` – evaluacija bezbednosti,
- `RecordedAnomaly`, `RecordedIntervention`, `RecordedMachineOverworked`, `RecordedFix` – istorija za audit i health.

### 4.2 Mašine i metrike (primer konfiguracije)

- CLM: ambient temperatura, vlažnost,
- LIN: vibracije, brzina trake,
- CNC: temperatura, vibracije, opterećenje vretena,
- PKG: throughput, reject rate, temperatura zaptivanja.

---

## 5. Struktura pravila i nivoi rezonovanja

### 5.1 L1 pravila po pragu

Zajednička logika za svako L1 pravilo:

- Aktivira se kada poslednje merenje pređe gornji prag za datu metriku.
- Pre dodavanja proverava se da ista otvorena anomalija već ne postoji za tu mašinu i metriku.
- Ishod je nova anomalija tipa ABOVE_THRESHOLD.

| Pravilo                                             | Mašina | Metrika          | Uslov (prag) | Ishod                                                |
| --------------------------------------------------- | ------ | ---------------- | ------------ | ---------------------------------------------------- |
| Plant climate — Ambient temperature above high band | CLM    | AMBIENT_C        | >= 29.0      | Anomaly(ABOVE_THRESHOLD, metricKey=AMBIENT_C)        |
| Plant climate — Humidity above high band            | CLM    | HUMIDITY_PCT     | >= 64.0      | Anomaly(ABOVE_THRESHOLD, metricKey=HUMIDITY_PCT)     |
| Conveyor line — Vibration above high band           | LIN    | VIBRATION_RMS    | >= 3.85      | Anomaly(ABOVE_THRESHOLD, metricKey=VIBRATION_RMS)    |
| CNC mill — Temperature above high band              | CNC    | TEMPERATURE_C    | >= 67.5      | Anomaly(ABOVE_THRESHOLD, metricKey=TEMPERATURE_C)    |
| CNC mill — Vibration above high band                | CNC    | VIBRATION_RMS    | >= 5.35      | Anomaly(ABOVE_THRESHOLD, metricKey=VIBRATION_RMS)    |
| CNC mill — Spindle load above high band             | CNC    | SPINDLE_LOAD_PCT | >= 84.0      | Anomaly(ABOVE_THRESHOLD, metricKey=SPINDLE_LOAD_PCT) |
| Auto packer — Reject rate above high band           | PKG    | REJECT_PCT       | >= 4.5       | Anomaly(ABOVE_THRESHOLD, metricKey=REJECT_PCT)       |
| Auto packer — Seal temperature above high band      | PKG    | SEAL_TEMP_C      | >= 99.0      | Anomaly(ABOVE_THRESHOLD, metricKey=SEAL_TEMP_C)      |

### 5.2 L2 pravila za safety evaluaciju

| Pravilo | Uslov | Ishod |
|---|---|---|
| Safety evaluation: not safe | zatražen je SafetyCheck, postoji MachineOverworked, a SafetyResult još nije upisan | upisuje se SafetyResult(safe=false, reason=...) |
| Safety evaluation: safe | zatražen je SafetyCheck, ne postoji MachineOverworked, a SafetyResult još nije upisan | upisuje se SafetyResult(safe=true, reason="No safety issues detected.") |

### 5.3 L3 pravila za kombinovane intervencije

Ova pravila povezuju više metrika i generišu preporuku za operatera.

1. Pushed conveyor line (belt speed, vibration, pack throughput)

 Uslov:
 - LIN.BELT_SPEED_PCT >= 99.0
 - LIN.VIBRATION_RMS >= 3.5
 - PKG.CASES_PER_MIN >= 125.0
 - ista intervencija još nije aktivna 
 
 Ishod: Intervention(HIGH, sourceRule=X_PUSHED_LINE)

1. Conveyor line instability (belt speed, vibration, reject rate)

 Uslov:
 - LIN.BELT_SPEED_PCT >= 99.0
 - LIN.VIBRATION_RMS >= 3.5
 - PKG.REJECT_PCT >= 2.0
 - ista intervencija još nije aktivna
 
 Ishod: Intervention(MEDIUM, sourceRule=X_LIN_INSTABILITY)

### 5.4 CEP pravilo za zaustavljanje mašina

Stop machine after repeated stress on critical sensors

Uslov:

- za istu mašinu postoji 5 uzastopnih TickStatus događaja sa sustainedStressPresent=true,
- postoji vremensko ulančavanje događaja kroz CEP prozor,
- mašina još nema MachineOverworked,
- ne postoji već CRITICAL intervencija za istu mašinu.

Ishod:

- dodaje se MachineOverworked,
- dodaje se Intervention(priority=CRITICAL),
- mašina prelazi u halt režim dok operater ne potvrdi reset.

### 5.5 CEP pravila trenda po metrici

Za svaku metriku postoje u paru dva pravila:

| Tip pravila          | Kada se aktivira                                                              | Ishod                                           |
| -------------------- | ----------------------------------------------------------------------------- | ----------------------------------------------- |
| rising over 10 steps | poslednjih 10 uzastopnih vrednosti je strogo rastuće                          | dodaje se Anomaly(RISING_TREND) za datu metriku |
| rising trend ended   | trend anomalija je otvorena, a poslednjih 5 uzastopnih vrednosti je opadajuće | briše se postojeća RISING_TREND anomalija       |

### 5.6 Query pravilo

Svrha:

- da vrati trenutni health score za izabranu mašinu.

Račun:

- broji RecordedAnomaly, RecordedIntervention, RecordedMachineOverworked i RecordedFix za tu mašinu,
- računa score formulom:
health = `round(100 - anomalije - 2 * intervencije - 4 * unsafe + fix)`,
- rezultat ograničava na opseg 0–100.

Ishod: vraća finalni procenat zdravlja mašine.

### 5.7 Sažetak ulančavanja kroz nivoe

U tipičnom toku rezonovanja:

1. L1/CEP pravila kreiraju anomalije,
2. L2 safety pravila izdaju konačan SafetyResult,
3. L3 pravila iz kombinacija signala kreiraju intervencije,
4. CEP stress pravilo eskalira na MachineOverworked i CRITICAL intervenciju,
5. query pravilo računa health iz audit istorije.

---

## 6. Konkretan primer rezonovanja (korak po korak)

Scenario: mašina LIN radi pod povećanim opterećenjem.

1. Stižu nova merenja:
 `BELT_SPEED_PCT = 100`, `VIBRATION_RMS = 3.9`, `PKG.CASES_PER_MIN = 126`.

2. Aktivira se kombinovano pravilo “Pushed conveyor line...” i ubacuje se `Intervention(HIGH)`.

3. U narednim tick-ovima indikator `TickStatus.sustainedStressPresent` ostaje `true`.

4. Nakon 5 uzastopnih stres provera aktivira se pravilo “Stop machine after repeated stress...”.

5. Sistem ubacuje:
 - `MachineOverworked`,
 - `Intervention(CRITICAL)`.

6. Aktivira se safety evaluacija i formira `SafetyResult(safe=false, reason=...)`.

7. U audit istoriju se automatski upisuju `RecordedIntervention` i `RecordedMachineOverworked`.

8. Upit `MachineHealth("LIN")` vraća niži health score zbog negativnih događaja.

9. Operater izvrši `Fix`, sistem beleži `RecordedFix`, resetuje halt stanje i health score se delimično oporavlja.

---

## 7. Primeri kompleksnih pravila i korišćenja baze znanja

### Primer 1 – Pravilo sa više metrika i više mašina

Uslov:
`LIN.BELT_SPEED` visoko + `LIN.VIBRATION` visoko + `PKG.REJECT_PCT` povišen.

Zaključak:
Intervencija srednjeg prioriteta za LIN sa konkretnom preporukom za mehaničku proveru.

Kompleksnost:
Korelacija signala između različitih delova linije.

### Primer 2 – CEP pravilo sa vremenskim obrascem

Uslov:
5 uzastopnih tick-ova sa aktivnim stresom na kritičnim senzorima.

Zaključak:
`MachineOverworked` + CRITICAL intervencija.

Kompleksnost:
Vremensko rezonovanje i bezbednosna eskalacija.
