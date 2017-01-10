Schritte für eine neue Veröffentlichung:

* Neuen Branch erstellen und zu diesem wechseln. (z.B. `v0.0.0`)
* `APPLICATION_VERSION` in HTGT.java aktualisieren.
* Änderung mit `git` `commit`'en und `push`'en.
* `make clean && make && make sig` (GPG-Sig!)
* Neuen Tag anlegen. (z.B. `release-0.0.0`)
* Neues Release auf GitHub eintragen.

----

```bash
# Example commands...
git branch v0.0.0-alpha2
git checkout v0.0.0-alpha2
nano src/HTGT.java

git commit -a -S -m 'Prepare v0.0.0-alpha2 release'
git push origin v0.0.0-alpha2

git tag release-0.0.0-alpha2
git push origin release-0.0.0-alpha2

make clean && make && make sig
# edit new release/tag on github
# upload binary files to github
```

----

* HAPPYTEC-Downloads: ZIP/JAR/GPG-Dateien aktualisieren.
* HAPPYTEC-Forum: Ersten Beitrag des Threads aktualisieren.
* HAPPYTEC-Forum: Neuen Beitrag als Hinweis veröffentlichen.
* HAPPYTEC-eSports-API: Config für `offline/update.check` anpassen.
