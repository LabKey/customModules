(ns gen-plans
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.zip :as zip]))

; Turn on the debug flag by passing "-debug" in as a command line argument
(def ^:dynamic *debug* false)

; Print summary tree of the analysis plan instead of writing to the analysis plan file
(def ^:dynamic *print-tree* false)

; Print summary grid  of the analysis plan instead of writing to the analysis plan file
(def ^:dynamic *print-grid* false)

(defn debug [& args]
  (if *debug*
    (println (apply str args))))

(defn boolean-combinations [bools oldstyle]
  "Given a list of boolean names, generate a list of + and - combinations.
  When oldstyle is true, the combinations will be prefixed with ! when negated."
  (let [len (count bools)
        plus-minus ['- '+]
        combs (fn combs [n]
                (if (zero? n)
                  [[]]
                  (for [smaller (combs (dec n))
                        pm plus-minus]
                    ;(cons (str (nth bools (dec n)) pm) smaller))))]
                    (conj smaller
                         (if oldstyle
                           (str (if (= '- pm) "!" "") (nth bools (dec n)) "+")
                           (str (nth bools (dec n)) pm))))))]
        (combs len)))

(defn boolean-strings [bools oldstyle]
  "Create a set of boolean strings from the boolean combinations.
  Example:
    (boolean-strings [\"a\" \"b\"] false)
      => (\"a-b-\" \"a-b+\" \"a+b-\" \"a+b+\")

  Example:
    (boolean-strings [\"a\" \"b\"] true)
      => (\"(a-b-)\" \"(a-b+)\" \"(a+b-)\" \"(a+b+)\")"

  (for [comb (boolean-combinations bools oldstyle)]
    (if oldstyle
      (str "(" (s/join "&" comb) ")")
      (apply str comb))))

; ---------

(defn create-zipper [specification]
  (zip/zipper #(contains? % :children) :children nil specification))

(defn normalize [s]
  (s/join "/" (.normalize (.toPath (io/file s)))))

(defn stat-string [loc]
  (let [node (zip/node loc)
        gate (:gate node)
        path (rest (zip/path loc))
        gate-path (if (empty? path)
                    gate
                    (normalize
                      (s/join "/"
                              [(s/join "/"
                                       (map :gate (filter #(not (nil? %)) path))) gate])))]
    (if (nil? gate)
      "" ; allow blank subset (see AnalysisPlan40)
      (str gate-path ":Count"))))

(defn path-loc
  "Like zip/path, but returns a seq over zipper locs instead of the nodes and includes the current loc."
  [loc]
  (reverse
    (take-while #(not (nil? (zip/up %)))
                (iterate zip/up loc))))

(defn print-path [sortid id description loc]
  "Prints just the node name and stat-string down the loc's path"
  (when (not (zip/children loc))
    (let [locs (path-loc loc)]
      (print (str sortid "\t" id "\t" description "\t"))
      (println
        (s/join "\t"
                (interleave (map #(:name (zip/node %)) locs) (map stat-string locs)))))))

(defn print-path-debug [loc]
  "Prints just the node names down the loc's path"
  (when (not (zip/children loc))
    (let [locs (path-loc loc)]
      (println
              (s/join "\t"
                      (map #(:name (zip/node %)) locs))))))

(defn print-subsets [summary-grid? sortid id description z]
  "Print the exhaustive list of subset names and stats for the entire plan"
  (loop [loc z]
    (if (not (zip/end? loc))
      (do
        (if summary-grid?
          (print-path-debug loc)
          (print-path sortid id description loc))
        (recur (zip/next loc))))))


(defn depth [loc]
  "Count the nodes up to the root minus one"
  (count (zip/path loc)))

(defn strstr [n s]
  "Create a string by repeatedly appending s, n number of times"
  (apply str (repeat n s)))

(defn print-tree [sortid id description z]
  "Print the tree of subset names and stats for the entire plan"
  (loop [loc z]
    (if (not (zip/end? loc))
      (do
        (let [d (depth loc)
              n (zip/node loc)
              sb (StringBuilder.)]
          (if (> d 0)
            (do
              (.append sb (strstr (dec d) "  "))
              (.append sb (:gate n))
              (if (not (= (:gate n) (:name n)))
                (-> sb
                    (.append (strstr (- 40 (.length sb)) " "))
                    (.append (:name n))))
              (println (.toString sb)))))
        (recur (zip/next loc))))))

(defn print-headers []
  (let [headers ["analysisPlanSort" "analysisPlanId" "description"
                 "NAME1" "STAT1"
                 "NAME2" "STAT2"
                 "NAME3" "STAT3"
                 "NAME4" "STAT4"
                 "NAME5" "STAT5"
                 "NAME6" "STAT6"
                 "NAME7" "STAT7"
                 "NAME8" "STAT8"
                 "NAME9" "STAT9"
                 "NAME10" "STAT10"]]
    (println (s/join "\t" headers))))



(defn print-plan [specification]
  "Print a summary of the plan or write out the plan to the 'plans' directory in the tsv format"
  (let [z (create-zipper specification)
        sortid (:sort-id specification)
        id (:id specification)
        desc (:description specification)]

    (if *debug*
      (do
        (debug "id: " id)
        (debug "sort-id: " sortid)
        (debug "name: " (:name specification))
        (debug "description: " desc)
        (debug "----"))

      (println "plan" id "-" desc))

    (cond
      ;; print tree summary of the plan to the console
      *print-tree*
      (print-tree sortid id desc z)

      ;; print tabular summary of the plan to the console
      *print-grid*
      (print-subsets true sortid id desc z)

      ;; default - write out full tsv to plans/AP-039.txt
      :else
      (let [file (io/file "plans" (str sortid ".tsv"))]
            (println "writing to file: " (str file))
            (with-open [f (io/writer file)]
              (binding [*out* f]
                (print-headers)
              (print-subsets false sortid id desc z)))))))


(defn load-plans []
  (debug "loading analysis plans")
  (let [plans (load-file "analysis-plans.clj")]
    (debug "loaded " (count plans) " plans")
    plans))

(defn print-plans [ids]
  (let [plans (load-plans)]
    (dorun

      (if (empty? ids)
        ; print all plans
        (for [plan plans] (print-plan plan))

        ; look for a plan matching each id
        (for [id ids]
          (let [plan (first (filter #(= (:id %) id) plans))]
            (if (nil? plan)
              (println "no plan found for" id)
              (print-plan plan))))))))

; ---------

(defn -main [args]
  (binding [*debug* false
            *print-tree* false
            *print-grid* false]

    ; separate out the flag arguments from the rest
    (let [args (filter #(not (= "gen-plans.clj" %)) args)
          flags (filter #(.startsWith % "-") args)
          ids (filter #(not (.startsWith % "-")) args)]

      ; loop over arguments to set flags
      (doseq [flag flags]
        (condp = flag
          "-debug" (set! *debug* true)
          "-print-grid" (set! *print-grid* true)
          "-print-tree" (set! *print-tree* true)

          ; default
          (print "unknown flag: " flag)))

      ; now, do the stuff
      (print-plans ids))))


(-main *command-line-args*)

