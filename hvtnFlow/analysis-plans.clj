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

(defn S-Time          [& children] (subset "Time" "S/Time" children))

(defn CD4-CD8-      [& children] (subset "CD4-CD8-" "CD4-CD8-" children))
(defn NKTcells      [& children] (subset "NKT cells" "NKT cells" children))
(defn gd+           [& children] (subset "gd+" "gd+" children))
(defn CD56+         [& children] (subset "CD56+" "CD56+" children))
(defn CXCR5+        [& children] (subset "CXCR5+" "CXCR5+" children))
(defn CXCR5+CD45RA- [& children] (subset "CXCR5+CD45RA-" "CXCR5+CD45RA-" children))
(defn PD1+          [& children] (subset "PD1+" "PD1+" children))
(defn PD1+CCR7-     [& children] (subset "PD1+CCR7-" "PD1+CCR7-" children))
(defn CD8+PD1+      [& children] (subset "PD1+" "8+PD1+" children))
(defn Naive         [& children] (subset "Naive" "Naive" children))
(defn CM            [& children] (subset "CM" "CM" children))
(defn EM            [& children] (subset "EM" "EM" children))
(defn TD            [& children] (subset "TD" "TD" children))
(defn Time          [& children] (subset "Time" "Time" children))
(defn SSH14         [& children] (subset "14+SShi" "14+SShi" children))
(defn Monos         [& children] (subset "Monos total" "Monos total" children))
(defn SSlo          [& children] (subset "14-SSlo" "14-SSlo" children))
(defn Lv*           [& children] (subset "Lv" "Lv" children))
(defn Keeper        [& children] (subset "Keeper" "Keeper" children))
(defn three-        [& children] (subset "3-" "3-" children))
(defn four+         [& children] (subset "4+" "4+" children))
(defn four-eight-   [& children] (subset "4-8-" "4-8-" children))
(defn eight+        [& children] (subset "8+" "8+" children))
(defn DR-           [& children] (subset "DR-" "DR-" children))
(defn NKTotal       [& children] (subset "NK total" "NK total" children))
(defn L*            [& children] (subset "L" "L" children))
(defn three+        [& children] (subset "3+" "3+" children))
(defn IFNg_OR_IL2   [& children] (subset "IFNg_OR_IL2" "IFNg_OR_IL2" children))
(defn sixteen-56-   [& children] (subset "16-56-" "16-56-" children))
(defn gd-           [& children] (subset "gd-" "gd-" children))
(defn twenty6+161+  [& children] (subset "26+161+" "26+161+" children))
(defn Va72+        [& children] (subset "Va7.2+" "Va7.2+" children))
(defn Va72-        [& children] (subset "Va7.2-" "Va7.2-" children))
(defn Not26+161+    [& children] (subset "Not26+161+" "Not26+161+" children))
(defn sixteen+or56+ [& children] (subset "16+or56+" "16+or56+" children))

(defn Blank [& children]
  "Empty subset (used to skip a level to align CD4+ and NKT cells)"
  (subset "" nil children))

(defn make-bools
  [names gates oldstyle]
  (let [bool-names (gen-plans/boolean-strings names false)
        bool-gates (gen-plans/boolean-strings gates oldstyle)]
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

; AnalysisPlan038 booleans and IFNg\IL2
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
        ; added in version 4.0 of analysis plan 039
        (subset  "IFNg\\\\IL2\\\\CD154")
        (subset  "IFNg\\\\IL2\\\\TNFa\\\\CD154")
        ; added in version 3.0 of analysis plan 039
        (subset  "PD1+")
        (PD1+    CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)
        ; memory cells
        (Naive   CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)
        (CM      CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)
        (EM      CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)
        (TD      CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)))

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
; AnalysisPlan042 marginals and IFNg\IL2
(def CD154|GzB|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
  [(Subset. "154+"   "154+"  nil)
   (Subset. "GzB+"   "GzB+"  nil)
   (Subset. "IFNg+"  "IFNg+" nil)
   (Subset. "IL17a+" "IL17a+"  nil)
   (Subset. "IL2+"   "IL2+"  nil)
   (Subset. "IL4+"   "IL4+"  nil)
   (Subset. "TNFa+"  "TNFa+" nil)
   (Subset. "IFNg_OR_IL2" "IFNg\\\\IL2" nil)])

; AnalysisPlan042 marginals including ICOS+ and IFNg\IL2
(def CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
  [(Subset. "154+"   "154+"    nil)
   (Subset. "GzB+"   "GzB+"    nil)
   (Subset. "ICOS+"  "ICOS+"   nil)
   (Subset. "IFNg+"  "IFNg+"   nil)
   (Subset. "IL17a+" "IL17a+"  nil)
   (Subset. "IL2+"   "IL2+"    nil)
   (Subset. "IL4+"   "IL4+"    nil)
   (Subset. "TNFa+"  "TNFa+"   nil)
   (Subset. "IFNg_OR_IL2" "IFNg\\\\IL2" nil)])

; AnalysisPlan042 marginals with 8+ prefix
(def CD8+CD154|GzB|8+ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
  [(Subset. "154+"   "8+154+"  nil)
   (Subset. "GzB+"   "GzB+"    nil)
   (Subset. "ICOS+"  "8+ICOS+" nil)
   (Subset. "IFNg+"  "IFNg+"   nil)
   (Subset. "IL17a+" "IL17a+"  nil)
   (Subset. "IL2+"   "IL2+"    nil)
   (Subset. "IL4+"   "IL4+"    nil)
   (Subset. "TNFa+"  "TNFa+"   nil)
   (Subset. "IFNg_OR_IL2" "IFNg\\\\IL2" nil)])


; AnalysisPlan042 marginals including PD1+, ICOS+, and IFNg\IL2
(def PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
  [(Subset. "PD1+"   "PD1+"    nil)
   (Subset. "154+"   "154+"    nil)
   (Subset. "GzB+"   "GzB+"    nil)
   (Subset. "ICOS+"  "ICOS+"   nil)
   (Subset. "IFNg+"  "IFNg+"   nil)
   (Subset. "IL17a+" "IL17a+"  nil)
   (Subset. "IL2+"   "IL2+"    nil)
   (Subset. "IL4+"   "IL4+"    nil)
   (Subset. "TNFa+"  "TNFa+"   nil)
   (Subset. "IFNg_OR_IL2" "IFNg\\\\IL2" nil)])

; AnalysisPlan042 marginals including PD1+, ICOS+, and IFNg\IL2
(def PD1|CD154|GzB|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
  [(Subset. "PD1+"   "PD1+"    nil)
   (Subset. "154+"   "154+"    nil)
   (Subset. "GzB+"   "GzB+"    nil)
   (Subset. "IFNg+"  "IFNg+"   nil)
   (Subset. "IL17a+" "IL17a+"  nil)
   (Subset. "IL2+"   "IL2+"    nil)
   (Subset. "IL4+"   "IL4+"    nil)
   (Subset. "TNFa+"  "TNFa+"   nil)
   (Subset. "IFNg_OR_IL2" "IFNg\\\\IL2" nil)])

; AnalysisPlan042 marginals including 8+PD1+, ICOS+, and IFNg\IL2
(def CD8+PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
  [(Subset. "PD1+"   "8+PD1+"  nil)
   (Subset. "154+"   "154+"    nil)
   (Subset. "GzB+"   "GzB+"    nil)
   (Subset. "ICOS+"  "ICOS+"   nil)
   (Subset. "IFNg+"  "IFNg+"   nil)
   (Subset. "IL17a+" "IL17a+"  nil)
   (Subset. "IL2+"   "IL2+"    nil)
   (Subset. "IL4+"   "IL4+"    nil)
   (Subset. "TNFa+"  "TNFa+"   nil)
   (Subset. "IFNg_OR_IL2" "IFNg\\\\IL2" nil)])

; AnalysisPlan042 marginals including 8+PD1+, ICOS+, and IFNg\IL2
(def CD8+PD1|8+CD154|GzB|8+ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
  [(Subset. "PD1+"   "8+PD1+"  nil)
   (Subset. "154+"   "8+154+"  nil)
   (Subset. "GzB+"   "GzB+"    nil)
   (Subset. "ICOS+"  "8+ICOS+" nil)
   (Subset. "IFNg+"  "IFNg+"   nil)
   (Subset. "IL17a+" "IL17a+"  nil)
   (Subset. "IL2+"   "IL2+"    nil)
   (Subset. "IL4+"   "IL4+"    nil)
   (Subset. "TNFa+"  "TNFa+"   nil)
   (Subset. "IFNg_OR_IL2" "IFNg\\\\IL2" nil)])


; function to generate marginals and marginals for each set of memory cells with
; the PD1 population parameterized -- CD4+ uses "PD1+" while CD8+ uses "8+PD1+"
(defn ap42-marginals-memory [pd1]
  (conj CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
        ; extra boolean subsets only in CD4+ and CD8+
        (subset  "IFNg+IL2+")
        (subset  "IFNg+TNFa+")
        (subset  "IL2+TNFa+")
        (subset  "IFNg+IL2+TNFa+")
        (Subset. "IFNg_OR_IL2_OR_TNFa" "IFNg\\\\IL2\\\\TNFa" nil)
        ;pd1
        (Subset. (:name pd1) (:gate pd1) CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
        ; memory cells
        (Naive   pd1 CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
        (CM      pd1 CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
        (EM      pd1 CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
        (TD      pd1 CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)))

; AnalysisPlan043 marginals and IFNg\IL2
(def CD154|GzB|IFNg|IL13|IL2|IL21|IL4|TNFa|IFNgOrIL2
  [(Subset. "154+"   "154+"  nil)
   (Subset. "GzB+"   "GzB+"  nil)
   (Subset. "IFNg+"  "IFNg+" nil)
   (Subset. "IL13+"  "IL13+"  nil)
   (Subset. "IL2+"   "IL2+"  nil)
   (Subset. "IL21+"  "IL21+" nil)
   (Subset. "IL4+"   "IL4+"  nil)
   (Subset. "TNFa+"  "TNFa+" nil)
   (Subset. "IFNg_OR_IL2" "IFNg\\\\IL2" nil)])

; AnalysisPlan043 marginals with PD1+ and IFNg\IL2
(def CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2
  (conj CD154|GzB|IFNg|IL13|IL2|IL21|IL4|TNFa|IFNgOrIL2
        (subset "PD1+")))

; AnalysisPlan044 marginals
(def !45RA|CCR7|CM|EM|GZa|GzAPerf|KLRG1|Naive|Perf|TD
  [(subset "45RA+")
    (subset "CCR7+")
    (subset "CM")
    (subset "EM")
    (subset "GzA+")
    (subset "GzA+Perf+")
    (subset "GzA+Perf-")
    (subset "GzA-Perf+")
    (subset "GzA-Perf-")    
    (subset "KLRG1+")
    (subset "Naive")
    (subset "Perf+")
    (subset "TD")
  ])

(def !154|CCR6|CCR6CXCR3|CCR7|CM|CXCR3|DR|EM|GzA|GzAPerf|IFNg
  [(subset "154+")
    (subset "CCR6+")
    (subset "CCR6+CXCR3+")
    (subset "CCR6+CXCR3-")
    (subset "CCR6-CXCR3+")
    (subset "CCR6-CXCR3-")
    (subset "CCR7+")    
    (subset "CM")
    (subset "CXCR3+")
    (subset "DR+")
    (subset "EM")
    (subset "GzA+")
    (subset "GzA+Perf+")
    (subset "GzA+Perf-")
    (subset "GzA-Perf+")
    (subset "GzA-Perf-")
    (subset "IFNg+")
  ])

(def !445RA|CCR6|CCR6CXCR3|CCR7|CXCR3|CM|CXCR3|EM|GzA|GzAPerf|KLRG1|Naive|Perf|TD
  [(subset "4+45RA+")
  (subset "CCR6+")
  (subset "CCR6+CXCR3+")
  (subset "CCR6+CXCR3-")
  (subset "CCR6-CXCR3+")
  (subset "CCR6-CXCR3-")
  (subset "CCR7+")    
  (subset "CM")
  (subset "CXCR3+")
  (subset "EM")
  (subset "GzA+")
  (subset "GzA+Perf+")
  (subset "GzA+Perf-")
  (subset "GzA-Perf+")
  (subset "GzA-Perf-")
  (subset "KLRG1+")
  (subset "Naive")
  (subset "Perf+")
  (subset "TD")
  ])

(def !445RA|4KLRG1|CCR6|CCR6CXCR3|CCR7|CXCR3|CM|CXCR3|EM|GzA|GzAPerf|Naive|Perf|TD
  [(subset "4+45RA+")
  (subset "4+KLRG1+")
  (subset "CCR6+")
  (subset "CCR6+CXCR3+")
  (subset "CCR6+CXCR3-")
  (subset "CCR6-CXCR3+")
  (subset "CCR6-CXCR3-")
  (subset "CCR7+")    
  (subset "CM")
  (subset "CXCR3+")
  (subset "EM")
  (subset "GzA+")
  (subset "GzA+Perf+")
  (subset "GzA+Perf-")
  (subset "GzA-Perf+")
  (subset "GzA-Perf-")
  (subset "Naive")
  (subset "Perf+")
  (subset "TD")
  ])

(def !45RA|CCR6|CCR6CXCR3|CCR7|CXCR3|CM|CXCR3|EM|GzA|GzAPerf|KLRG1|Naive|Perf|TD
  [(subset "45RA+")
  (subset "CCR6+")
  (subset "CCR6+CXCR3+")
  (subset "CCR6+CXCR3-")
  (subset "CCR6-CXCR3+")
  (subset "CCR6-CXCR3-")
  (subset "CCR7+")    
  (subset "CM")
  (subset "CXCR3+")
  (subset "EM")
  (subset "GzA+")
  (subset "GzA+Perf+")
  (subset "GzA+Perf-")
  (subset "GzA-Perf+")
  (subset "GzA-Perf-")
  (subset "KLRG1+")
  (subset "Naive")
  (subset "Perf+")
  (subset "TD")
  ])

(def !154|HLADR|IFNg|IFNgOrIL2|IL2|IL17a|IL22|Th2|TNFa
  [
    (subset "154+")
    (subset "HLA-DR+")
    (subset "IFNg+")
    (subset "IFNg_OR_IL2")
    (subset "IL2+")
    (subset "IL17a+")
    (subset "IL22+")
    (subset "Th2+")
    (subset "TNFa+")
  ])

(def !4|48|8|16|16Or56|26161|45RA|56|154|CCR6|CCR6CXCR3|CCR7|CM|CXCR3|DR|EM|GzA|GzAPerf
    [
    (subset "154+")
    (subset "CCR6+")
    (subset "CCR6+CXCR3+")
    (subset "CCR6+CXCR3-")
    (subset "CCR6-CXCR3+")
    (subset "CCR6-CXCR3-")
    (subset "CCR7+")    
    (subset "CM")
    (subset "CXCR3+")
    (subset "DR+")
    (subset "EM")
    (subset "GzA+")
    (subset "GzA+Perf+")
    (subset "GzA+Perf-")
    (subset "GzA-Perf+")
    (subset "GzA-Perf-")
    (subset "IFNg+")
    ])

(def IL2|IL17a|Il22|LRG1|Naive|Perf|TD|Th2+|TNFa+
  [(subset "IL2+")
  (subset "IL17a+")                  
  (subset "IL22+")
  (subset "KLRG1+")
  (subset "Naive")
  (subset "Perf+")
  (subset "TD")
  (subset "Th2+")
  (subset "TNFa+") 
  ])

(def !4|48|8|16|16or56|45RA|56
  [(subset "4+")
    (subset "4-8-")
    (subset "8+")
    (subset "16+")
    (subset "16+or56+")
    (subset "45RA+")
    (subset "56+")
  ])

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
                     (subset "CXCR5+")
                     (CXCR5+
                       CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2
                       (PD1+ CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2))
                     (CXCR5+CD45RA-
                       (PD1+CCR7- CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2))))
             (apply CD8+ ap39-marginals-memory)
             (CD4-CD8-    CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2)
             (NKTcells    CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2))
           (CD3-
             (CD56+    CD154|GzB|IFNg|IL2|IL21|IL4|TNFa|IFNgOrIL2))))))) ]}

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
              (CD4+     CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2)
              (CD8+     CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2)
              (CD4-CD8- CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2)
              (gd+      CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2)
              (NKTcells CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2))
            (CD3-
              (CD56+    CD107a|154|GzB|IFNg|IL10|IL13|IL17|IL2|IL4|TNFa|IFNgOrIL2)))))))] }



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

 ; Same as AP-042 but in a more compact and less understandable form
 {:id "42.legacy"
  :sort-id "AP-042.legacy"
  :name "Analysis Plan 042"
  :description "17-color ICS with memory, Tfh, NK markers."
  :children
  [(S
    (Exclude
      (CD14-
        (Lv
          (L
           (CD3+
             (apply CD4+
               (conj (ap42-marginals-memory (subset "PD1+" "PD1+"))
                     (CXCR5+ CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
                       (PD1+ CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2))))
             (apply CD8+ (ap42-marginals-memory (subset "PD1+" "8+PD1+")))
             (CD4-CD8- (subset "PD1+") CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
             (NKTcells (subset "PD1+") CD154|GzB|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2))
           (CD3-
             (CD56+   CD154|GzB|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2))))))) ]}

 {:id "42"
  :sort-id "AP-042"
  :name "Analysis Plan 042"
  :description "17-color ICS with memory, Tfh, NK markers."
  :children
  [(S
    (Exclude
      (CD14-
        (Lv
          (L
           (CD3+
             (CD4+
               CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
               (subset  "IFNg+IL2+")
               (subset  "IFNg+TNFa+")
               (subset  "IL2+TNFa+")
               (subset  "IFNg+IL2+TNFa+")
               (Subset. "IFNg_OR_IL2_OR_TNFa" "IFNg\\\\IL2\\\\TNFa" nil)
               (PD1+       CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (Naive  PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (CM     PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (EM     PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (TD     PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (CXCR5+     CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
                 (PD1+     CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)))

            (CD8+
               CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
               (subset  "IFNg+IL2+")
               (subset  "IFNg+TNFa+")
               (subset  "IL2+TNFa+")
               (subset  "IFNg+IL2+TNFa+")
               (Subset. "IFNg_OR_IL2_OR_TNFa" "IFNg\\\\IL2\\\\TNFa" nil)
               (CD8+PD1+         CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (Naive    CD8+PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (CM       CD8+PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (EM       CD8+PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (TD       CD8+PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2))

             (CD4-CD8-   PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
             (NKTcells   PD1|CD154|GzB|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2))

           (CD3-
             (CD56+   CD154|GzB|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2))))))) ]}

 ; Same as 042 but uses 8+ICOS+ and 8+154+
 {:id "42.8+"
  :sort-id "AP-042.8+"
  :name "Analysis Plan 042"
  :description "17-color ICS with memory, Tfh, NK markers. (Same as 042 but with 8+ICOS+ and 8+154+)"
  :children
  [(S
    (Exclude
      (CD14-
        (Lv
          (L
           (CD3+
             (CD4+
               CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
               (subset  "IFNg+IL2+")
               (subset  "IFNg+TNFa+")
               (subset  "IL2+TNFa+")
               (subset  "IFNg+IL2+TNFa+")
               (Subset. "IFNg_OR_IL2_OR_TNFa" "IFNg\\\\IL2\\\\TNFa" nil)
               (PD1+       CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (Naive  PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (CM     PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (EM     PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (TD     PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (CXCR5+     CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
                 (PD1+     CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)))

            (CD8+
               CD8+CD154|GzB|8+ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2
               (subset  "IFNg+IL2+")
               (subset  "IFNg+TNFa+")
               (subset  "IL2+TNFa+")
               (subset  "IFNg+IL2+TNFa+")
               (Subset. "IFNg_OR_IL2_OR_TNFa" "IFNg\\\\IL2\\\\TNFa" nil)
               (CD8+PD1+       CD8+CD154|GzB|8+ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (Naive    CD8+PD1|8+CD154|GzB|8+ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (CM       CD8+PD1|8+CD154|GzB|8+ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (EM       CD8+PD1|8+CD154|GzB|8+ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
               (TD       CD8+PD1|8+CD154|GzB|8+ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2))

             (CD4-CD8-   PD1|CD154|GzB|ICOS|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2)
             (NKTcells   PD1|CD154|GzB|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2))

           (CD3-
             (CD56+   CD154|GzB|IFNg|IL17a|IL2|IL4|TNFa|IFNgOrIL2))))))) ]}

 {:id "43"
  :sort-id "AP-043"
  :name "Analysis Plan 043"
  :description "17-color ICS for malaria (MAL067) case-control"
  :children
  [(S
    (Exclude
      (CD14-
        (Lv
          (L
           (CD3+
             (CD4+
               CD154|GzB|IFNg|IL13|IL2|IL21|IL4|TNFa|IFNgOrIL2
               (PD1+   CD154|GzB|IFNg|IL13|IL2|IL21|IL4|TNFa|IFNgOrIL2)
               (Naive  CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2)
               (CM     CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2)
               (EM     CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2)
               (TD     CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2)
               (CXCR5+ CD154|GzB|IFNg|IL13|IL2|IL21|IL4|TNFa|IFNgOrIL2
                       (PD1+ CD154|GzB|IFNg|IL13|IL2|IL21|IL4|TNFa|IFNgOrIL2)))

             (CD8+
               CD154|GzB|IFNg|IL13|IL2|IL21|IL4|TNFa|IFNgOrIL2
               (PD1+ CD154|GzB|IFNg|IL13|IL2|IL21|IL4|TNFa|IFNgOrIL2)
               (Naive  CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2)
               (CM     CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2)
               (EM     CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2)
               (TD     CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2))

             (CD4-CD8-  CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2)
             (NKTcells  CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2))

           (CD3-
             (CD56+   CD154|GzB|IFNg|IL13|IL2|IL21|IL4|PD1|TNFa|IFNgOrIL2))))))) ]}
  {:id "44"
   :sort-id "AP-044"
   :name "Analysis Plan 044"
   :description "26-color ICS for HVTN 602"
   :children
   [(S-Time
    (SSH14 
      (subset "14+16-")
      (subset "14+16+")
      (subset "14lo16+")
      (Monos !154|HLADR|IFNg|IFNgOrIL2|IL2|IL17a|IL22|Th2|TNFa))
    (Lv*
      (SSlo
        (Keeper
          (three-
            (DR-
              (subset "16+56-")
              (subset "16+56+")
              (subset "16-56hi")
              (subset "16-56lo")
              (NKTotal
                (subset "4+")
                (subset "4-8-")
                (subset "8+")
                (subset "45RA+")
                !154|CCR6|CCR6CXCR3|CCR7|CM|CXCR3|DR|EM|GzA|GzAPerf|IFNg
                (IFNg_OR_IL2 !45RA|CCR7|CM|EM|GZa|GzAPerf|KLRG1|Naive|Perf|TD)
                IL2|IL17a|Il22|LRG1|Naive|Perf|TD|Th2+|TNFa+))
            (subset "HLA-DR+"))
          
          (L*
            (three+
              (gd+
                (subset "4+")
                (subset "4-8-")
                (subset "8+")
                (subset "16+")
                (subset "16+or56+")
                (subset "26+161+")
                (subset "45RA+")
                (subset "56+")
                !154|CCR6|CCR6CXCR3|CCR7|CM|CXCR3|DR|EM|GzA|GzAPerf|IFNg
                (IFNg_OR_IL2 !45RA|CCR7|CM|EM|GZa|GzAPerf|KLRG1|Naive|Perf|TD)
                IL2|IL17a|Il22|LRG1|Naive|Perf|TD|Th2+|TNFa+)
              (subset "gd1")
              (subset "gd2")
              (subset "gd3")
              (subset "gd4")
              (subset "gd5")) ; gd5 in provided spreadsheet but not FloJo workspace

          (subset "3+IFNg")
          (subset "3+IL2")  
          (subset "3+TNFa") 
          (subset "3+154")  
          (sixteen-56- 
            (four+ 
              (subset "4+IL22+")
              (subset "4+KLRG1+")
              (subset "4+Th2+")  
              (subset "4+45RA+")
              !154|CCR6|CCR6CXCR3|CCR7|CM|CXCR3|DR|EM|GzA|GzAPerf|IFNg
              (IFNg_OR_IL2 !445RA|4KLRG1|CCR6|CCR6CXCR3|CCR7|CXCR3|CM|CXCR3|EM|GzA|GzAPerf|Naive|Perf|TD)
              (subset "IL2+")
              (subset "IL17a+")
              (subset "Naive")
              (subset "Perf+")
              (subset "TD")
              (subset "TNFa+"))

            (four-eight-
              (subset "45RA+")
              !154|CCR6|CCR6CXCR3|CCR7|CM|CXCR3|DR|EM|GzA|GzAPerf|IFNg
              (IFNg_OR_IL2 !445RA|CCR6|CCR6CXCR3|CCR7|CXCR3|CM|CXCR3|EM|GzA|GzAPerf|KLRG1|Naive|Perf|TD)
              IL2|IL17a|Il22|LRG1|Naive|Perf|TD|Th2+|TNFa+)

            (eight+
              (subset "45RA+")
              !154|CCR6|CCR6CXCR3|CCR7|CM|CXCR3|DR|EM|GzA|GzAPerf|IFNg
              (IFNg_OR_IL2 !45RA|CCR6|CCR6CXCR3|CCR7|CXCR3|CM|CXCR3|EM|GzA|GzAPerf|KLRG1|Naive|Perf|TD)
              IL2|IL17a|Il22|LRG1|Naive|Perf|TD|Th2+|TNFa+))

          (gd-
            (twenty6+161+ 
              (Va72+
                !4|48|8|16|16or56|45RA|56
                !154|CCR6|CCR6CXCR3|CCR7|CM|CXCR3|DR|EM|GzA|GzAPerf|IFNg
                (IFNg_OR_IL2 !45RA|CCR7|CM|EM|GZa|GzAPerf|KLRG1|Naive|Perf|TD)
                IL2|IL17a|Il22|LRG1|Naive|Perf|TD|Th2+|TNFa+)   

              (Va72-
                !4|48|8|16|16or56|45RA|56
                !154|CCR6|CCR6CXCR3|CCR7|CM|CXCR3|DR|EM|GzA|GzAPerf|IFNg
                (IFNg_OR_IL2 !45RA|CCR7|CM|EM|GZa|GzAPerf|KLRG1|Naive|Perf|TD)
                IL2|IL17a|Il22|LRG1|Naive|Perf|TD|Th2+|TNFa+)))

          (Not26+161+
            (subset "16+")
            (sixteen+or56+
              (subset "4+")
              (subset "4-8-")
              (subset "8+")
              (subset "45RA+")
              !154|CCR6|CCR6CXCR3|CCR7|CM|CXCR3|DR|EM|GzA|GzAPerf|IFNg
              (IFNg_OR_IL2 !45RA|CCR7|CM|EM|GZa|GzAPerf|KLRG1|Naive|Perf|TD)
              IL2|IL17a|Il22|LRG1|Naive|Perf|TD|Th2+|TNFa+)
          (subset "56+"))))

        (subset "K1")
        (subset "K2")
        (subset "K3")
        (subset "K4")
        (subset "K5")
        (subset "K6")
        (subset "K7")
        (subset "K8")
        (subset "K9")
        (subset "K10")))) ]}
]
