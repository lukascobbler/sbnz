## Tehnologije

- Koristio sam Server Side events za brzu komunikaciju izmedju fronta i beka

## Opis sistema

- U domenu se mogu videti cinjenice na osnovu kojih pravila izvode zakljucke

- Simulacija zauzima dosta logike zato sto je potrebno dovesti struktuirane masine i senzore u Drools svet

- Simulacija se deli na sledece komponente:
  - komponente koje rade sa Drools-om: `RuleMatchModel`, `WorkingMemoryOps`
  - komponente koje definisu masine: `MacineProfile`, `MachineRegistry`, `MetricProfile`
  - komponente koje definisu simulirani rad masina: `SimulationEngine`, `SimulatedTelemetry`, `SimulationConstants`
  - komponente koje sakupljaju zakljucke radi slanja na front: `SimulationSnapshotBuilder`

- Svaka masina se sastoji od svog imena, identifikatora i liste metrika koje se definisu za nju. 
  
  Svaka metrika predstavlja vrednost koja se prati tokom simuliranog protoka vremena. Metrike imaju ime i svoju velicinu tj. jedinicu i za njih se pamte razne informacije potrebne za simulaciju poput pocetne vrednosti, da li imaju gornju granicu, ... 
  
  Da bi masina postojala u sistemu, potrebno ju je registrovati u katalogu masina `MachineRegistry` i ovde se nalazi veci deo baze znanja, jer se pravila izvode na osnovu podataka iz metrike.

- Simulacija vremena se sastoji iz koraka generisanja novih vrednosti za svaki senzor (pomocu `SimulatedTelemetry`), prikupljanja cinjenica koje pravila generisu i izgradnje reporta koji se salje na front. 
  
  U engine-u koji podrzava simulaciju dodatno stoji i logika za popravku masine, koja brise sve cinjenice za tu masinu iz radne memorije i resetuje sve senzore na nominalnu tj. podrazumevanu vrednost.
  
  Omoguceno je i podesavanje `workload`-a, sto predstavlja koliko je neki senzor tj. deo masine pod naporom.
  
- Sva logika simulacije ide kroz `SimulationController`.

## Pravila

- Postoje rules.drl gde su L3 pravila, safety (L2) pravila i CEP pravilo koje odredjuje kada je masina crkla

- Postoje templates_thresholds.drt sto su template pravila za gornje granice vrednosti metrike

- Postoje templates_trends.drt (CEP) template pravila koja prate rast i pad vrednosti metrike

- Dosta pravila imaju svog CLEAR suparnika, a neka CLEAR pravila brisu cinjenice na osnovu dodatne logike: za CEP, potrebno je duplo manje merenja da prodje da se pravilo obrise nego sto je trebalo da ono nastane (10 i 5)

- Templejti se kompajliraju i instanciraju u `DroolsConfig` klasi, a baza znanja na osnovu koje se instance rade se nalazi u `MachineRegistry`
  klasi kao sto je vec receno

- Sva CEP pravila koriste "pseudo" clock, jer je vreme simulirano, a on je podesen u `DroolsConfig`

## Dobri primeri

- CEP: penjanje vrednosti metrike RISING_TREND anomaly - CNC vibracija

- CEP: prevelika vrednost metrike ABOVE_THRESHOLD anomely -  PKG reject rate, ali malo duze treba

- Za pokazivanje crkavanja masine - CNC Spindle load, pokazati i safety evaluation uz ovo

- L3: prebrza traka dovodi do loseg pakovanja - LIN vibracija i belt speed + PKG reject rate

- L3: preveliko opterecenje CNC rotorne komponente dovodi do prevelike temperature proizvoda: CNC spindle load + LIN belt speed + PKG seal temperature

## Plan objasnjavanja

1. Prvo pokazati dijagram, a zatim proci kroz objasnjenje simulacije (opis sistema)
2. Pokrenuti aplikaciju na beku i frontu i otvoriti front
3. Pokazati ceo katalog pravila, otvoriti dijalog objasnjena za svaku sekciju i onda objasniti tu sekciju, za vrednosti metrika pokazati sve masine koje postoje i od kojih se sve senzora sadrze
4. Pokazati simulaciju vremena tj. jedan tick, objasniti da moze da prodje vise od jednog ticka i objasniti zasto se sve za sivi (ceka se na SSE zahtev da se ne bi poslalo vise od jednog zahteva)
5. Proci kroz primere koji pokazuju L1 L2 L3 pravila i demonstrirati ta pravila
6. Opisati tekst iz sekcije o pravilima, a zatim otvoriti bek kod i proci kroz rules.drl, templejte i objasniti ukratko sta su generated_.drl fajlovi
7. Otvoriti `MachineRegistry` gde ce se videti baza znanja