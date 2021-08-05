(ns dev-hartmann.gitodo
  (:require [cheshire.core :refer [generate-string parse-string]]
            [clj-http.lite.client :as client]
            [java-time :as java-time]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.term.colors :refer [underline green on-grey reverse-color yellow red]])
  (:gen-class))

(def graphql-endpoint "https://api.github.com/graphql")

(defn- generate-security-header [token]
  {"Authorization" token})

(defn- generate-query-variable [querytype user]
  (let [query-value (str " is:open is:pr " querytype ":" user " archived:false")]
    (generate-string {:searchQuery query-value})))

(def reviews-requested-base-query
  "query($searchQuery: String!) {
   search(first: 100, query: $searchQuery, type: ISSUE) {
    nodes {
      ... on PullRequest {
        reviews(first: 100) {
          totalCount
        }
        url
        title
        state
        createdAt
        updatedAt
        author {
          login
        }
      }
    }
  }
}")

(defn- config-file-present? []
  (.exists (io/file "gtdo.edn")))

(defn- generate-config []
  (println "please enter git user-name and access-token:")
  (let [user-name (read-line) 
        token (read-line)
        config {:username user-name :token token}]
    (spit "gtdo.edn" (.toString config))
    config))

(defn- post-grapqhl-request [endpoint request-options]
  (-> (client/post endpoint request-options)
      :body
      (parse-string true)
      :data
      :search
      :nodes))

(defn- get-config []
  (if (config-file-present?)
    (read-string (slurp "gtdo.edn"))
    (generate-config)))

(defn generate-request-body [query query-variables]
  (generate-string {:query query
                    :variables query-variables}))

(defn generate-request [headers body]
  {:body body
   :headers headers
   :accept :json})

(defn- review-count->str [count]
  (if (> count 0) 
    (str " reviewed by" count) 
    "Not reviewd yet, needs attention!"))

(defn- url->str [url]
  (str "check it here: " url))


(defn- get-hours-color [hours]
  (cond
    (and (> hours 0) (<= hours 15)) (on-grey (reverse-color (yellow hours " hours")))
    (> hours 15) (on-grey (reverse-color (red hours " hours"))
    :else (on-grey (reverse-color (green "under one hour."))))))

(defn- base-info->str [state author title since]
  (str (on-grey (reverse-color (green state))) " - " "'"title"'" " by " author " since: " (get-hours-color since)))

(defn result->todo [entry]
  (let [author (get-in entry [:author :login])
        title (:title entry)
        now (java-time/instant)
        created-at (java-time/instant (:createdAt entry))
        hours-since (.toHours (java-time/duration created-at now))
        state (:state entry)
        url-str (url->str (:url entry))
        review-count-str (review-count->str (get-in entry [:reviews :totalCount]))]
    (->> [(base-info->str state author title hours-since)
          review-count-str
          url-str
          "-------------\n"]
         (str/join \newline))))

(defn -main
  [& args]
  (let [config (get-config)
        security-header (generate-security-header (:token config))
        query-variables (generate-query-variable "author" (:username config))
        request-body (generate-request-body reviews-requested-base-query query-variables)
        request (generate-request security-header request-body)
        results (sort-by :createdAt (post-grapqhl-request graphql-endpoint request))]
    (println (underline "PRs to review: \n"))
    (apply println (map result->todo results))))
