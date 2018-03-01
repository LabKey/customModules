This is the HVTN flow module.

Updating analysis plans on the server:

- Obtain clojure:
    (via macports)
    > /opt/local/bin/port install clojure

    (via lein)
    http://leiningen.org/#install
    also install the "lein-exec" plugin
        https://github.com/kumarshantanu/lein-exec

- Execute the "gen-plans.clj" script:
    (via macports)
    > clj gen-plans.clj [plan-id]

    (via lein)
    > lein exec gen-plans.clj [plan-id]

    (or directly)
    > java -classpath /path/to/clojure.jar clojure.main gen-plans.clj [plan-id]

    Where [plan-id] is an optional analysis plan id, e.g. "43"

    Command line options:
    -debug       -- turn on debug level logging
    -print-tree  -- print a summary tree of the plan (when set, plan file not written)
    -print-grid  -- print a summary grid of the plan (when set, plan file not written)
    [plan-id]    -- one or more plan ids to print


- The generated plans will be written to plans/AP-<plan-id>.tsv

- check there are no duplicate lines:
    > cat plans/AP-042.tsv | sort | uniq -d

- Upload the analysis plans to the server:
    - go to the AnalysisPlans list
    - filter by <plan-id>
    - **make sure all rows for the <plan-id> are visible**, then delete the rows
    - import the out/<plan-id>.tsv via copy/paste


