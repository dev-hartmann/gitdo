(ns dev-hartmann.formatters
  (:require [clojure.term.colors :refer :all]))

(def so-review-count (underline "Not reviewd yet (higher priority):"))
(def st-review-count (underline "Already reviewed by someone else:"))
(def so-no-review (underline "Not reviewd yet:"))
(def st-change-requested (underline "Changes requested:"))

(defn- review-count->str [count]
  (if (> count 0)
    (str " reviews: " count)
    " "))

(defn url->str [url]
  (str "URL: " url))

(defn- hours->string [hours]
  (cond
    (and (> hours 0) (<= hours 15)) (on-grey (reverse-color (yellow hours " hours")))
    (> hours 15) (on-grey (reverse-color (red hours " hours")))
    :else (on-grey (reverse-color (green "under one hour.")))))

(defn review-request->str [state author title since review-count]
  (str (on-grey (reverse-color (green (str state " ")))) (hours->string since) " '" title "'" " by " author (review-count->str review-count)))

(defn change-request->str [state author title comment since]
  (str (on-grey (reverse-color (red state))) (hours->string since) " '" title "' " author " reason: '" comment "'"))

(defn review-comment->str [comment]
  (if-not (nil? comment)
    comment
    "- not commented -"))

(defn print-todos! [heading todos]
  (println (underline (bold heading)))
  (println "")
  (if (> (count todos) 0)
    (doseq [todo todos]
      (println todo))
    (println "- Nothing to do")))
