This is the HVTN flow module.

Updating AnalysisPlans.txt:

- Obtain clojure:
    (via macports)
    > /opt/local/bin/port install clojure

    (via lien)
    http://leiningen.org/#install
    also install the "lien-exec" plugin
        https://github.com/kumarshantanu/lein-exec

- Execute the "gen-plans.clj" clojure script:
    (via macports)
    > clj gen-plans.clj

    (via lien)
    > lien exec gen-plan.clj

    (or directly)
    > java -classpath /path/to/clojure.jar clojure.main gen-plan.clj

- The generated plans will be written to out/<plan-id>.tsv


----

CLEANUP:

- [x] flow folder type

- [ ] make sure hvtnFlow module is enabled in all /HVTN and /CHIL folders

- [ ] find all custom queries under /HVTN and /CHIL folders overriding hvtnFlow module queries
    - get full container list for /CHIL and /HVTN
    - for each container:
        - get module details to check if hvtnFlow module is enabled
        - get query details for the following queries
            - if the query is defined in the folder, print it out

- [ ] delete queries in /HVTN

- [ ] move AnalysisPlans list to /Shared
- [ ] fix queries to point at /Shared.lists.AnalysisPlans


