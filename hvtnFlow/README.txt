This is the HVTN flow module.

For more information, see the document in google drive:
https://docs.google.com/document/d/1OlwVYUpES0uxT8aIT1D8nI7NGDpXQTehbdE0OdoeTVA/edit#

### Analysis Plans

Creating a new analysis plan query typically goes something like:

1. Admins on the flow server will:
    1. Create a new flow folder, create a new SampleType, import some FlowJo data.
    2. Notify LabKey via the support ticket portal providing details on the plan shape and where data can be found.

2. LabKey developer will:
    1. Create a new *temp* AnalysisPlanXXX custom query in the "HVTN projects" or "CHIL" top-level project.
      1. Copy/paste the query from the most recent AnalysisPlan query and update the analysis plan id in the WHERE filter.
      2. From the schema browser, select the query, click "Edit properties" and set "Available in child folders?" to "Yes"
      3. If changes are needed in the AnalysisPlanTemplate or AnalysisPlanBase, you may need to create a *temp* custom query named AnalysisPlanTempalte_AP054.  If possible, get these changes incorporated into AnalysisPlanTemplate and AnalysisPlanBase queries without breaking the existing analysis plans.
    2. Upload the analysis plan tsv data into the AnalysisPlans list in the "HVTN projects" or "CHIL" top-level project.
    3. Resolve the support ticket back to the flow admins for verification.
    4. Once verified, commit the plan to git, create a pull request, merge.
    5. After the hvtnFlow module is deployed some time later, the temporary queries can be removed.

#### Things that could be improved:

- Improve ability to test new analysis plan queries without having to create temp queries. Perhaps with a parameterized query?
- Deploy the moduleEditor module, so the hvtnFlow module can be modified directly on the server without needing the temporary queries.
- Build a tool to let the flow users pick the populations to add to an analysis plan.

### Old Analysis Plans

Analysis plans prior to AP-048 used the gen-plans.clj clojure script to generate the plans/AP-xxx.tsv files.  The instructions here are only needed if one of these older plans needs to be updated.

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


