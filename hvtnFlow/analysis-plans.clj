(defrecord Subset [name gate children])

(defn subset
  ([name]
   (Subset. name name nil))
  ([name gate]
   (Subset. name gate nil))
  ([name gate & children]
   "Create a subset and flatten any first level collections in the varargs children."
   (Subset. name gate
            (apply (partial mapcat
                           #(if (and (sequential? %)
                                     (not (instance? Subset %)))
                              % [%]))
                   children))))
;
;  ----

(defn S [& children] (Subset. "Singlets" "S" children))

(defn Exclude [& children] (Subset. "Excluded" "Exclude" children))

(defn CD14- [& children] (Subset. "CD14-" "14-" children))

(defn Lv [& children] (Subset. "Live" "Lv" children))

(defn L [& children] (Subset. "Lymphocytes" "L" children))

(defn CD3+ [& children] (subset "CD3+" "3+" children))
(defn CD3- [& children] (subset "CD3-" "3-" children))

(defn CD4+ [& children] (subset "CD4+" "4+" children))
(defn CD8+ [& children] (subset "CD8+" "8+" children))

(defn CD3+long [& children] (subset "CD3+" "CD3+" children))
(defn CD4+long [& children] (subset "CD4+" "CD4+" children))
(defn CD8+long [& children] (subset "CD8+" "CD8+" children))

(defn CD4+excl [& children] (subset "CD4+" "Excl/4+" children))
(defn CD8+excl [& children] (subset "CD8+" "Excl/8+" children))

(defn CD4+|CD8+ [& children]
  "Create two identical CD4+ and CD8+ subset branches"
  [(apply CD4+ children)
   (apply CD8+ children)])

(defn CD4+|CD8+long [& children]
  "Create two identical CD4+ and CD8+ subset branches"
  [(apply CD4+long children)
   (apply CD8+long children)])

(defn CD4+|CD8+excl [& children]
  "Create two identical CD4+ and CD8+ subset branches"
  [(apply CD4+excl children)
   (apply CD8+excl children)])


(defn Blank [& children]
  "Empty subset (used to skip a level to align CD4+ and NKT cells)"
  (subset "" nil children))

(defn make-bools
  [names gates oldstyle]
  (let [bool-names (gen-plan/boolean-strings names false)
        bool-gates (gen-plan/boolean-strings gates oldstyle)]
    (mapv #(Subset. %1 %2 nil) bool-names bool-gates)))

(def IFNg*IL2
  (make-bools ["IFNg" "IL2"]
              ["IFNg" "IL2"]
              true))

(def IFNg*IL2+IFNgOrIL2
  (conj IFNg*IL2
        (Subset. "IFNg_OR_IL2" "(IFNg+|IL2+)" nil)))

(def IFNg*IL2*TNFa
  (make-bools ["IFNg" "IL2" "TNFa"]
              ["IFNg" "IL2" "TNFa"]
              true))

; booleans and IFNg\IL2
(def CD154*GzB*IFNg*IL2*IL4*IL17*TNFa+IFNgOrIL2
  (conj (make-bools ["154" "GzB" "IFNg" "2" "4" "17" "TNFa"]
                    ["154" "GzB" "IFNg" "2" "4" "17" "TNFa"]
                    false)
        (Subset. "IFNg_OR_IL2" "(IFNg+|IL2+)" nil)))

; AnalysisPlan039 marginals and IFNg\IL2
(def CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2
  [(Subset. "154+"  "154+"  nil)
   (Subset. "GzB+"  "GzB+"  nil)
   (Subset. "IFNg+" "IFNg+" nil)
   (Subset. "IL2+"  "IL2+"  nil)
   (Subset. "IL21+" "IL21+" nil)
   (Subset. "IL4+"  "IL4+"  nil)
   (Subset. "TNFa+" "TNFa+" nil)
   (Subset. "IFNg_OR_IL2" "IFNg\\\\IL2" nil)])

; marginals and marginals for each set of memory cells
(def ap39-marginals-memory
  (conj CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2
        ; extra boolean subsets only in CD4+ and CD8+
        (Subset. "IFNg_OR_IL2_OR_TNFa" "IFNg\\\\IL2\\\\TNFa" nil)
        (subset  "IFNg+IL2+")
        (subset  "IFNg+TNFa+")
        (subset  "IL2+TNFa+")
        (subset  "IFNg+IL2+TNFa+")
        ; memory cells
        (Subset. "Naive" "Naive" CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)
        (Subset. "CM"    "CM"    CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)
        (Subset. "EM"    "EM"    CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)
        (Subset. "TD"    "TD"    CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)))

; AnalysisPlan040 marginals and IFNg\IL2
(def CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2
  [(Subset. "107a+"  "107a+"  nil)
   (Subset. "154+"   "154+"  nil)
   (Subset. "GzB+"   "GzB+"  nil)
   (Subset. "IFNg+"  "IFNg+" nil)
   (Subset. "IL10+"  "IL10+"  nil)
   (Subset. "IL13+"  "IL13+"  nil)
   (Subset. "IL17+"  "IL17+"  nil)
   (Subset. "IL2+"   "IL2+"  nil)
   (Subset. "IL4+"   "IL4+"  nil)
   (Subset. "TNFa+"  "TNFa+" nil)
   (Subset. "IFNg_OR_IL2" "IFNg\\\\IL2" nil)])

; AnalysisPlan041 marginals and IFNg\IL2
(def CD154|GzB|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
  [(Subset. "154+"   "154+"  nil)
   (Subset. "GzB+"   "GzB+"  nil)
   (Subset. "IFNg+"  "IFNg+" nil)
   (Subset. "IL17a+"  "IL17a+"  nil)
   (Subset. "IL2+"   "IL2+"  nil)
   (Subset. "IL4+"   "IL4+"  nil)
   (Subset. "TNFa+"  "TNFa+" nil)
   (Subset. "IFNg_OR_IL2" "IFNg\\\\IL2" nil)])

;;
;; The Analysis Plan definitions must be the last value in this file
;;


[{:id "1"
  :sort-id "AP-001"
  :name "Analysis Plan 1"
  :description "ICS two cytokine (IFNg and IL-2) positivity testing"
  :children
  [(S
    (Lv
      (L
       (CD3+
         (CD4+|CD8+ IFNg*IL2+IFNgOrIL2)))))]}

 {:id "2"
  :sort-id "AP-002"
  :description "ICS three cytokine (IFNg, IL-2, TNFa) polyfunctionality (8-color)"
  :children
  [(S
    (Lv
      (L
       (CD3+
         (CD4+|CD8+ IFNg*IL2*TNFa)))))]}

 {:id "3"
  :sort-id "AP-003"
  :description "ICS three cytokine (IFNg, IL-2, TNFa) polyfunctionality (10-color)"
  :children
  [(S
    (Lv
      (L
       (CD3+
         (CD4+|CD8+ IFNg*IL2*TNFa)))))]}

 {:id "4"
  :sort-id "AP-004"
  :description "Activation Markers and CCR5"
  :children
  [(S
    (Lv
      (L
       (CD3+long
         (CD4+|CD8+long
           (Subset. "Ki67+BcL2-" "Ki67+BcL2-"
                    (into
                      (into
                        [(Subset. "CCR5+" "CCR5+" nil)]
                        (make-bools ["CCR7" "CCR5"] ["CCR7" "CCR5"] true))
                      ; NOTE: CD27 and not CCR7! This was a typo in the original AnalysisPlan004 table.
                      (make-bools ["CD27" "CCR5"] ["CD27" "CCR5"] true))))))))]}

 {:id "6"
  :sort-id "AP-006"
  :description "11-color bulk memory phenotyping"
  :children
  [(S
    (Lv
      (L
       (CD3+long
         (CD4+|CD8+long
           (make-bools ["RO" "R5" "R7" "27" "28" "57" "103"]
                       ["45RO" "CCR5" "CCR7" "CD27" "CD28" "CD57" "D103"] true))))))]}

 {:id "7"
  :sort-id "AP-007"
  :description "ICS five cytokine (IFN-g, IL-2, TNF-a, Granzyme B, CD57)"
  :children
  [(S
    (Lv
      (L
       (CD3+
         (CD4+|CD8+
           (make-bools ["IFNg" "IL2" "TNFa" "GzB" "57"]
                       ["IFNg" "IL2" "TNFa" "Granzyme B" "57"] true))))))]}

 {:id "7.cd57"
  :sort-id "AP-007.cd57"
  :description "ICS five cytokine (IFN-g, IL-2, TNF-a, Granzyme B, CD57)"
  :children
  [(S
    (Lv
      (L
       (CD3+
         (CD4+|CD8+
           (make-bools ["IFNg" "IL2" "TNFa" "GzB" "57"]
                       ["IFNg" "IL2" "TNFa" "Granzyme B" "CD57"] true))))))]}

 {:id "7.excl"
  :sort-id "AP-007.excl"
  :description "ICS five cytokine (IFN-g, IL-2, TNF-a, Granzyme B, CD57)"
  :children
  [(S
    (Subset. "Live" "Exclude/Lv"
      [(L
        (CD3+
          (CD4+|CD8+
            (make-bools ["IFNg" "IL2" "TNFa" "GzB" "57"]
                        ["IFNg" "IL2" "TNFa" "Granzyme B" "57"] true))))]))]}

 {:id "8"
  :sort-id "AP-008"
  :description "CFSE"
  :children
  [(S
    (Lv
      (L
       (CD3+
         (CD4+
           (Subset. "Gen0" "Gen0" nil)
           (Subset. "Gen1" "Gen1" nil)
           (Subset. "Gen2" "Gen2" nil)
           (Subset. "Gen3" "Gen3" nil)
           (Subset. "Gen4" "Gen4" nil)
           (Subset. "Gen5" "Gen5" nil)
           (Subset. "Gen6" "Gen6" nil)
           (Subset. "Gen7" "Gen7" nil))
         (CD8+
           (Subset. "Gen0" "Gen0" nil)
           (Subset. "Gen1" "Gen1" nil)
           (Subset. "Gen2" "Gen2" nil)
           (Subset. "Gen3" "Gen3" nil)
           (Subset. "Gen4" "Gen4" nil)
           (Subset. "Gen5" "Gen5" nil)
           (Subset. "Gen6" "Gen6" nil)
           (Subset. "Gen7" "Gen7" nil))))))]}

 {:id "9"
  :sort-id "AP-009"
  :description "ICS five cytokine (IFN-g, IL-2, TNF-a, IL-4, CD154, IL-17a)"
  :children
  [(S
    (Lv
      (L
       (CD3+
         (CD4+|CD8+
           (make-bools ["154" "IFNg" "IL2" "IL4" "IL17a" "TNFa"]
                       ["CD154" "IFNg" "IL2" "IL4" "IL17a" "TNFa"] true))))))]}

 {:id "9.excl"
  :sort-id "AP-009.excl"
  :description "ICS five cytokine, with exclusion (IFN-g, IL-2, TNF-a, IL-4, CD154, IL-17a)"
  :children
  [(S
    (Lv
      (L
       (CD3+
         (CD4+|CD8+excl
           (make-bools ["154" "IFNg" "IL2" "IL4" "IL17a" "TNFa"]
                       ["CD154" "IFNg" "IL2" "IL4" "IL17a" "TNFa"] true))))))]}

 {:id "9.excl.5bool"
  :sort-id "AP-009.excl.5bool"
  :description "ICS five cytokine, with exclusion (IFN-g, IL-2, TNF-a, IL-4, CD154, IL-17a)"
  :children
  [(S
    (Lv
      (L
       (CD3+
         (CD4+|CD8+excl
           (make-bools ["154" "IFNg" "IL2" "IL4" "TNFa"]
                       ["CD154" "IFNg" "IL2" "IL4" "TNFa"] true))))))]}

 {:id "24"
  :sort-id "AP-024"
  :description "CD14- 4 color"
  :children
  [(S
    (Exclude
      (CD14-
        (Lv
          (L
           (CD3+
             (apply
               CD4+|CD8+
               (conj
                 (make-bools ["107a" "154" "IFNg" "IL2" "TNFa"]
                             ["107a" "154" "IFNg" "IL2" "TNFa"] false)
                 (subset "CD107a+" "107a+")
                 (subset "CD154+" "154+")
                 (subset "GzB+")
                 (subset "IFNg+")
                 (subset "IFNg_OR_IL2" "(IFNg+|IL2+)")
                 (subset "IL2+")
                 (subset "IL4+")
                 (subset "MIP1B+")
                 (subset "TNFa+")))))))))]}

 {:id "24.7bool"
  :sort-id "AP-024.7bool"
  :description "CD14- 4 color, 7 cytokine (IFN-g, IL-2, TNF-a, IL-4, CD154, CD107a, MIP)"
  :children
  [(S
    (Exclude
      (CD14-
        (Lv
          (L
           (CD3+
             (CD4+|CD8+
               (make-bools ["107" "154" "IFNg" "IL2" "IL4" "MIP" "TNFa"]
                           ["107" "154" "IFNg" "IL2" "IL4" "MIP" "TNFa"] false))))))))]}

 {:id "35"
  :sort-id "AP-035"
  :description "12-color ICS with 6 Boolean analysis including granzyme B"
  :children
  [(S
    (Exclude
      (CD14-
        (Lv
          (L
           (CD3+
             (CD4+|CD8+
               (make-bools ["107a" "154" "GzB" "IFNg" "IL2" "TNFa"]
                           ["107a" "154" "GzB" "IFNg" "IL2" "TNFa"] false))))))))]}

 {:id "37"
  :sort-id "AP-037"
  :description "12-color ICS with 5 Boolean analysis including granzyme B"
  :children
  [(S
    (Exclude
      (CD14-
        (Lv
          (L
           (CD3+
             (CD4+|CD8+
               (make-bools ["154" "GzB" "IFNg" "IL2" "TNFa"]
                           ["154" "GzB" "IFNg" "IL2" "TNFa"] false))))))))]}

 {:id "38"
  :sort-id "AP-038"
  :name "Analysis Plan 038"
  :description "12-color ICS with up to 7 Boolean analysis including granzyme B, IL-17 and IL-4"
  :children
  [(S
    (Exclude
      (CD14-
        (Lv
          (L
           (CD3+
             (CD4+|CD8+ CD154*GzB*IFNg*IL2*IL4*IL17*TNFa+IFNgOrIL2)))))))]}


 {:id "39"
  :sort-id "AP-039"
  :name "Analysis Plan 039"
  :description "16-color ICS with memory, Tfh, NK markers."
  :children
  [(S
    (Exclude
      (CD14-
        (Lv
          (L
           (CD3+
             (apply CD4+
               (conj ap39-marginals-memory
                     (Subset. "CXCR5+" "CXCR5+"
                              [(Subset. "PD1+" "PD1+" CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)])
                     (Subset. "CXCR5+CD45RA-" "CXCR5+CD45RA-"
                              [(Subset. "PD1+CCR7-" "PD1+CCR7-" CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)])))
             (apply CD8+ ap39-marginals-memory)
             (Subset. "CD4-CD8-"  "CD4-CD8-"     CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)
             (Subset. "NKT cells" "NKT cells"    CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2))
           (CD3-
             (Subset. "CD56+"     "CD56+"     CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2))))))) ]}

  {:id "40"
   :sort-id "AP-040"
   :description "16-Color multifunctional ICS."
   :children
   [(S
     (Exclude
       (CD14-
         (Lv
           (L
            (CD3+
              (CD4+    CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2)
              (CD8+    CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2)
              (Subset. "CD4-CD8-"  "CD4-CD8-"
                       CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2)
              (Subset. "gd+"        "gd+"
                       CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2)
              (Subset. "NKT cells" "NKT cells"
                       CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2))
            (CD3-
              (Subset. "CD56+"     "CD56+"
                       CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2)))))))] }



  {:id "41"
   :sort-id "AP-041"
   :description "12-color ICS (with IL-17) for marginal responses only without Boolean combinations."
   :children
   [(S
     (Exclude
       (CD14-
         (Lv
           (L
            (CD3+
              (CD4+|CD8+
                CD154|GzB|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)))))))] }

]
