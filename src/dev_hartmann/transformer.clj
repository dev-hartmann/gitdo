(ns dev-hartmann.transformer
    (:require [clojure.string :as str]
              [dev-hartmann.formatters :refer [url->str review-request->str review-comment->str change-request->str]]))



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


