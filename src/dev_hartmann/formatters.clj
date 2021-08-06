(ns dev-hartmann.formatters
  (:require [clojure.term.colors :refer [underline bold]]))

(def so-review-count (underline "Not reviewd yet (higher priority):"))
(def st-review-count (underline "Already reviewed by someone else:"))
(def so-no-review (underline "Not reviewd yet:"))
(def st-change-requested (underline "Changes requested:"))

(defn print-todos! [heading todos]
  (println (underline (bold heading)))
  (println "")
  (if (> (count todos) 0)
    (doseq [todo todos]
      (println todo))
    (println "- Nothing to do")))