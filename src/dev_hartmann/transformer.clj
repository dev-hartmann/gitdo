(ns dev-hartmann.transformer
    (:require [clojure.string :as str]
              [clojure.term.colors :refer [green on-grey reverse-color yellow red]]))

(defn- review-count->str [count]
  (if (> count 0)
    (str " reviews: " count)
    " "))

(defn- url->str [url]
  (str "URL: " url))

(defn- get-hours-color [hours]
  (cond
    (and (> hours 0) (<= hours 15)) (on-grey (reverse-color (yellow hours " hours")))
    (> hours 15) (on-grey (reverse-color (red hours " hours")))
    :else (on-grey (reverse-color (green "under one hour.")))))

(defn- review-request->str [state author title since review-count]
  (str (on-grey (reverse-color (green (str state " ")))) (get-hours-color since) " '" title "'" " by " author (review-count->str review-count)))

(defn review-request->todo [entry]
  (let [author (get-in entry [:author :login])
        title (:title entry)
        now (java.time.Instant/now)
        created-at (java.time.Instant/parse (:createdAt entry))
        hours-since (.toHours (java.time.Duration/between created-at now))
        state (:state entry)
        url-str (url->str (:url entry))
        review-count (get-in entry [:reviews :totalCount])]
    (->> [(str/trim (review-request->str state author title hours-since review-count))
          (str/trim url-str)]
         (str/join \newline))))

(defn- change-request->str [state author title comment since]
  (str (on-grey (reverse-color (red state))) (get-hours-color since) " '" title "' " author " reason: '" comment "'"))

(defn- review-comment->str [comment]
  (if-not (nil? comment)
    comment
    "- not commented -"))

(defn change-request->todo [entry]
  (let [title (:title entry)
        now (java.time.Instant/now)
        url-str (url->str (:url entry))
        change-request (first (reverse (sort-by :createdAt (filter #(= "CHANGES_REQUESTED" (get % :state)) (get-in entry [:reviews :nodes])))))
        created-at (java.time.Instant/parse (:createdAt change-request))
        hours-since (.toHours (java.time.Duration/between created-at now))
        author (get-in change-request [:author :login])
        comment (review-comment->str (:body change-request))]
    (->> [(str/trim (change-request->str "Changes requested: " author title comment hours-since))
          (str/trim url-str)]
         (str/join \newline))))

