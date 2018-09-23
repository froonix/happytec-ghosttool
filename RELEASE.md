Schritte für eine neue Veröffentlichung:

* Neuen Branch erstellen und zu diesem wechseln. (z.B. `v0.0.0`)
* `APPLICATION_VERSION` in HTGT.java aktualisieren.
* Änderung mit `git` `commit`'en und `push`'en.
* `make clean && make && make sig` (GPG-Sig!)
* Neuen Tag anlegen. (z.B. `release-0.0.0`)
* Neues Release auf GitHub eintragen.
* Zurück zum `master` Branch wechseln.

----

```bash
# Example commands...
RELEASE="0.0.0-beta10"
git checkout master -b "v${RELEASE}"
sed -i -r "s/(APPLICATION_VERSION) = \"git-master\";\$/\1 = \"${RELEASE}\";/" src/HTGT.java

git commit -a -S -m "Prepare v${RELEASE} release"
git push origin "v${RELEASE}"

git tag "release-${RELEASE}"
git push origin "release-${RELEASE}"

make clean && make sig
# edit new release/tag on github
# upload binary files to github
# protect release branch on github
git checkout master
```

----

* HAPPYTEC-Downloads: ZIP/JAR/SIG-Dateien aktualisieren.
* HAPPYTEC-Forum: Ersten Beitrag des Threads aktualisieren.
* HAPPYTEC-Forum: Neuen Beitrag als Hinweis veröffentlichen.
* HAPPYTEC-eSports-API: Config für `offline/update.check` anpassen.
