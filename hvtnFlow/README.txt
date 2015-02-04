This is the HVTN flow module.

Updating analysis plans on the server:

- Obtain clojure:
    (via macports)
    > /opt/local/bin/port install clojure

    (via lien)
    http://leiningen.org/#install
    also install the "lien-exec" plugin
        https://github.com/kumarshantanu/lein-exec

- Execute the "gen-plans.clj" script:
    (via macports)
    > clj gen-plans.clj

    (via lien)
    > lien exec gen-plan.clj

    (or directly)
    > java -classpath /path/to/clojure.jar clojure.main gen-plan.clj

- The generated plans will be written to out/<plan-id>.tsv

- Upload the analysis plans to the server:
    - go to the AnalysisPlans list
    - filter by <plan-id>
    - make sure all rows for the <plan-id> are visible, then delete the rows
    - import the out/<plan-id>.tsv via copy/paste


